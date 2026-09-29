package io.github.markdechamps.fideswiss.olympiad;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SimulatedTournaments;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * No Oracle exists for the Olympiad Pairing Rules. On simulated team tournaments the procedure must give the same
 * round with plain enumeration as with the matchings, keep every absolute rule, and never block a round that has a
 * legal pairing.
 */
class OlympiadSimulatedTournamentsTest {

    private static final OlympiadSystem SYSTEM = new OlympiadSystem();

    @Test
    void agreesWithPlainEnumerationAndKeepsTheRules() {
        var rounds = new int[1];
        for (var seed = 0; seed < 150; seed++) {
            var random = new Random(seed);
            var settings = Profiles.olympiad(NumberOfRounds.of(5 + random.nextInt(7)));
            SimulatedTournaments.play(settings, 6 + random.nextInt(15), seed, tournament -> {
                rounds[0]++;
                assertRound(tournament);
            });
        }
        assertThat(rounds[0]).isGreaterThan(700);
    }

    @Test
    void pairsAnOlympiadSizedFieldWithinTheBudget() {
        var slowest = new long[1];
        SimulatedTournaments.play(Profiles.olympiad(NumberOfRounds.of(11)), 190, 7L, tournament -> {
            var started = System.nanoTime();
            assertThat(describe(pairing(tournament, Matchings.BLOSSOM, OlympiadTeams.toBePaired(tournament))))
                    .isNotEqualTo("blocked");
            slowest[0] = Math.max(slowest[0], System.nanoTime() - started);
        });
        assertThat(slowest[0]).isLessThan(1_000_000_000L);
    }

    private static void assertRound(Tournament tournament) {
        var teams = OlympiadTeams.toBePaired(tournament);
        var matched = pairing(tournament, Matchings.BLOSSOM, teams);
        var enumerated = pairing(tournament, new LiteralEnumeration(), teams);
        assertThat(describe(matched)).as("%s", teams).isEqualTo(describe(enumerated));
        matched.ifPresentOrElse(
                outcome -> assertLegal(teams, outcome),
                () -> assertThat(hasAnyLegalPairing(teams)).isFalse());
    }

    private static Optional<OlympiadProcedure.Outcome> pairing(
            Tournament tournament, Matchings matchings, List<Team> teams) {
        try {
            return Optional.of(
                    new OlympiadSystem(matchings).procedure(tournament).pair(teams));
        } catch (UnpairableRound e) {
            return Optional.empty();
        }
    }

    private static String describe(Optional<OlympiadProcedure.Outcome> outcome) {
        return outcome.map(found -> found.games().stream()
                                .map(game -> game.white() + "-" + game.black())
                                .toList()
                        + " bye " + found.bye())
                .orElse("blocked");
    }

    /** No rematch, 7.3 outside groups where 7.4 disregarded it, everyone seated once, an eligible bye at most. */
    private static void assertLegal(List<Team> teams, OlympiadProcedure.Outcome outcome) {
        var disregarded = outcome.groups().stream()
                .filter(group -> group.result().limits() == ColourLimits.DISREGARDED)
                .flatMap(group -> group.result().pairs().stream())
                .flatMap(pair -> java.util.stream.Stream.of(pair.first(), pair.second()))
                .toList();
        var seated = new ArrayList<Team>();
        for (var game : outcome.games()) {
            assertThat(game.white().hasMet(game.black())).isFalse();
            if (!disregarded.contains(game.white())) {
                assertThat(game.white().mayReceive(Colour.WHITE)).isTrue();
                assertThat(game.black().mayReceive(Colour.BLACK)).isTrue();
            }
            seated.add(game.white());
            seated.add(game.black());
        }
        outcome.bye().ifPresent(bye -> {
            assertThat(bye.mayReceiveBye()).isTrue();
            seated.add(bye);
        });
        assertThat(seated).containsExactlyInAnyOrderElementsOf(teams);
    }

    private static boolean hasAnyLegalPairing(List<Team> teams) {
        var enumeration = new LiteralEnumeration();
        if (teams.size() % 2 == 0) {
            return enumeration.canPairAll(teams, Team::mayMeet);
        }
        return teams.stream()
                .filter(Team::mayReceiveBye)
                .anyMatch(bye -> enumeration.canPairAll(
                        teams.stream().filter(team -> team != bye).toList(), Team::mayMeet));
    }
}
