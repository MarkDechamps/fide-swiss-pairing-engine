package io.github.markdechamps.fideswiss.lim;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.SimulatedTournaments;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * No Oracle exists for Lim. On simulated tournaments the procedure must give the same round with plain enumeration
 * as with the matching, keep every absolute rule, and never block a round that has a legal pairing.
 */
class LimSimulatedTournamentsTest {

    @ParameterizedTest
    @EnumSource(MaxiTournament.class)
    void agreesWithPlainEnumerationAndKeepsTheRules(MaxiTournament maxi) {
        var system = new LimSystem(maxi);
        var rounds = new int[1];
        for (var seed = 0; seed < 150; seed++) {
            var random = new Random(seed);
            SimulatedTournaments.play(system, 6 + random.nextInt(15), 5 + random.nextInt(6), seed, tournament -> {
                rounds[0]++;
                assertRound(system, tournament);
            });
        }
        assertThat(rounds[0]).isGreaterThan(700);
    }

    @Test
    void pairsLargeFieldsWithinTheBudget() {
        var slowest = new long[1];
        SimulatedTournaments.play(new LimSystem(MaxiTournament.NOT_DECLARED), 250, 9, 3L, tournament -> {
            var started = System.nanoTime();
            assertThat(describe(pairing(tournament, Reachability.MATCHING, new LimSystem(MaxiTournament.NOT_DECLARED))))
                    .isNotEqualTo("blocked");
            slowest[0] = Math.max(slowest[0], System.nanoTime() - started);
        });
        assertThat(slowest[0]).isLessThan(1_000_000_000L);
    }

    private static void assertRound(LimSystem system, Tournament tournament) {
        var players = LimSystem.playersToPair(tournament);
        var round = system.roundOf(tournament);
        var matched = pairing(tournament, Reachability.MATCHING, system);
        var enumerated = pairing(tournament, new LiteralEnumeration(), system);
        assertThat(describe(matched)).as("%s", players).isEqualTo(describe(enumerated));
        matched.ifPresentOrElse(
                outcome -> assertLegal(round, players, outcome),
                () -> assertThat(hasAnyLegalPairing(round, players)).isFalse());
    }

    private static Optional<LimProcedure.Outcome> pairing(
            Tournament tournament, Reachability reachability, LimSystem system) {
        try {
            return Optional.of(new LimProcedure(system.roundOf(tournament), reachability)
                    .pair(LimSystem.playersToPair(tournament)));
        } catch (NoRoundPairingException e) {
            return Optional.empty();
        }
    }

    private static String describe(Optional<LimProcedure.Outcome> outcome) {
        return outcome.map(found -> found.games().stream()
                                .map(game ->
                                        game.white().id() + "-" + game.black().id())
                                .sorted()
                                .toList()
                        + " PAB " + found.pairingAllocatedBye().map(Player::id))
                .orElse("blocked");
    }

    /** No rematch, the colour limits outside the last round, everyone paired once, one eligible PAB at most. */
    private static void assertLegal(RoundToPair round, List<Player> players, LimProcedure.Outcome outcome) {
        var seated = new ArrayList<Player>();
        for (var game : outcome.games()) {
            assertThat(game.white().hasMet(game.black())).isFalse();
            if (!round.isLastRound() && !round.isFirstRound()) {
                assertThat(game.white().mayReceive(Colour.WHITE)).isTrue();
                assertThat(game.black().mayReceive(Colour.BLACK)).isTrue();
            }
            seated.add(game.white());
            seated.add(game.black());
        }
        outcome.pairingAllocatedBye().ifPresent(bye -> {
            assertThat(bye.mayReceivePairingAllocatedBye()).isTrue();
            seated.add(bye);
        });
        assertThat(seated.stream().map(Player::id).toList())
                .containsExactlyInAnyOrderElementsOf(
                        players.stream().map(Player::id).toList());
    }

    /** Whether any eligible bye leaves the others with a compatible pairing, by plain enumeration. */
    private static boolean hasAnyLegalPairing(RoundToPair round, List<Player> players) {
        var enumeration = new LiteralEnumeration();
        if (players.size() % 2 == 0) {
            return enumeration.canPairAll(players, round::compatible);
        }
        return players.stream()
                .filter(Player::mayReceivePairingAllocatedBye)
                .anyMatch(bye -> enumeration.canPairAll(
                        players.stream().filter(player -> player != bye).toList(), round::compatible));
    }
}
