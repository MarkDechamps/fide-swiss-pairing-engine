package io.github.markdechamps.fideswiss.dubov;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.SimulatedTournaments;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** No Oracle exists for Dubov, so its search must agree with plain enumeration of the text on small fields. */
class DubovAgainstLiteralEnumerationTest {

    @Test
    void agreesInEveryRoundOfSimulatedTournaments() {
        var rounds = new int[1];
        for (var seed = 0; seed < 120; seed++) {
            var random = new Random(seed);
            SimulatedTournaments.play(
                    PairingSystems.dubov(), 6 + random.nextInt(9), 5 + random.nextInt(5), seed, tournament -> {
                        rounds[0]++;
                        assertAgreement(tournament);
                    });
        }
        assertThat(rounds[0]).isGreaterThan(500);
    }

    @Test
    void agreesInEveryRoundOfSimulatedTournamentsUnderBakuAcceleration() {
        var rounds = new int[1];
        for (var seed = 0; seed < 60; seed++) {
            var random = new Random(seed);
            var settings = Profiles.acceleratedOpen(NumberOfRounds.of(7 + random.nextInt(3)))
                    .with(PairingSystems.dubov());
            SimulatedTournaments.play(settings, 8 + random.nextInt(7), seed, tournament -> {
                rounds[0]++;
                assertAgreement(tournament);
            });
        }
        assertThat(rounds[0]).isGreaterThan(300);
    }

    @Test
    void agreesOnRandomHistories() {
        var random = new Random(20260925L);
        for (var sample = 0; sample < 1_500; sample++) {
            var round = new RoundToPair(
                    RoundNumber.of(2 + random.nextInt(8)),
                    NumberOfRounds.of(9 + random.nextInt(2)),
                    InitialColour.white());
            assertSameOutcome(round, randomPlayers(random, 5 + random.nextInt(8)));
        }
    }

    private static void assertAgreement(Tournament tournament) {
        assertSameOutcome(DubovSystem.roundOf(tournament), DubovSystem.playersToPair(tournament));
    }

    private static void assertSameOutcome(RoundToPair round, List<Player> players) {
        var search = describe(() -> new DubovProcedure(round).pair(players));
        var literal = describe(() -> new LiteralDubov(round).pair(players));
        assertThat(search).as("%s", players).isEqualTo(literal);
    }

    private static String describe(java.util.function.Supplier<DubovProcedure.Outcome> pairing) {
        try {
            var outcome = pairing.get();
            var games = outcome.games().stream()
                    .map(game -> game.white().id() + "-" + game.black().id())
                    .sorted()
                    .toList();
            return games + " PAB " + outcome.pairingAllocatedBye().map(Player::id);
        } catch (NoRoundPairingException e) {
            return "no legal round-pairing";
        }
    }

    /** Players with random scores, colours, opponents met, upfloat counts and PAB eligibility. */
    private static List<Player> randomPlayers(Random random, int count) {
        var players = new ArrayList<Player>();
        var met = new ArrayList<Set<Integer>>();
        for (var index = 0; index < count; index++) {
            met.add(new HashSet<>());
        }
        for (var a = 1; a <= count; a++) {
            for (var b = a + 1; b <= count; b++) {
                if (random.nextInt(4) == 0) {
                    met.get(a - 1).add(b);
                    met.get(b - 1).add(a);
                }
            }
        }
        for (var number = 1; number <= count; number++) {
            var games = random.nextInt(5);
            var colours = new ArrayList<Colour>();
            for (var game = 0; game < games; game++) {
                colours.add(random.nextBoolean() ? Colour.WHITE : Colour.BLACK);
            }
            var player = DubovPlayerMother.player(
                    number, random.nextInt(4), colours, met.get(number - 1), random.nextInt(6));
            players.add(new Player(
                    player.id(),
                    player.pairingNumber(),
                    player.rating(),
                    player.pairingScore(),
                    player.playedColours(),
                    player.met(),
                    colours.stream().map(colour -> 1800 + random.nextInt(600)).toList(),
                    random.nextInt(5) != 0,
                    player.upfloats(),
                    random.nextInt(4) == 0));
        }
        return players;
    }
}
