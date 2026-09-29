package io.github.markdechamps.fideswiss.doubleswiss;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupProcedure;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupRound;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.FloatScore;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SimulatedTournaments;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * No Oracle exists for Double-Swiss, so the Top-Scoregroup Procedure with its criteria must agree with plain
 * enumeration of C.04.5 on small fields: in simulated two-game tournaments and on random histories.
 */
class DoubleSwissAgainstLiteralEnumerationTest {

    @Test
    void agreesInEveryRoundOfSimulatedTournaments() {
        var rounds = new int[1];
        for (var seed = 0; seed < 150; seed++) {
            var random = new Random(seed);
            var settings = Profiles.doubleSwiss(NumberOfRounds.of(5 + random.nextInt(7)));
            SimulatedTournaments.play(settings, 6 + random.nextInt(9), seed, tournament -> {
                rounds[0]++;
                assertAgreement(tournament, UpfloaterLookAhead.parityMinimum());
                assertAgreement(tournament, UpfloaterLookAhead.graded());
            });
        }
        assertThat(rounds[0]).isGreaterThan(800);
    }

    @Test
    void agreesUnderBakuAcceleration() {
        var rounds = new int[1];
        for (var seed = 0; seed < 50; seed++) {
            var random = new Random(seed);
            var settings = Profiles.doubleSwiss(NumberOfRounds.of(7 + random.nextInt(3)))
                    .with(Acceleration.baku());
            SimulatedTournaments.play(settings, 8 + random.nextInt(7), seed, tournament -> {
                rounds[0]++;
                assertAgreement(tournament, UpfloaterLookAhead.parityMinimum());
            });
        }
        assertThat(rounds[0]).isGreaterThan(300);
    }

    @Test
    void agreesOnRandomHistories() {
        var random = new Random(20260929L);
        for (var sample = 0; sample < 3_000; sample++) {
            var players = randomPlayers(random, 4 + random.nextInt(11));
            var lastRound = random.nextInt(6) == 0;
            var lookAhead = random.nextBoolean() ? UpfloaterLookAhead.parityMinimum() : UpfloaterLookAhead.graded();
            assertSameOutcome(players, lastRound, lookAhead);
        }
    }

    private static void assertAgreement(Tournament tournament, UpfloaterLookAhead lookAhead) {
        var players = new PlayersToPair(tournament, FloatScore.pairing()).toBePaired(tournament.pairingNumbers());
        var lastRound = tournament.settings().numberOfRounds().isLast(tournament.nextRound());
        assertSameOutcome(players, lastRound, lookAhead);
    }

    private static void assertSameOutcome(List<Contender> players, boolean lastRound, UpfloaterLookAhead lookAhead) {
        var search = TopScoregroupProcedure.pair(
                        new TopScoregroupRound(players, lastRound, lookAhead, DoubleSwissCriteria.of(lastRound)))
                .map(pairing -> describe(pairing.pairs(), pairing.pairingAllocatedBye()));
        var literal = new LiteralDoubleSwiss(players, lastRound, lookAhead)
                .pair()
                .map(outcome -> describe(outcome.pairs(), outcome.pairingAllocatedBye()));
        assertThat(search).as("%s %s", lookAhead, players).isEqualTo(literal);
    }

    private static String describe(List<ContenderPair> pairs, Optional<Contender> bye) {
        return pairs.stream()
                        .map(pair -> pair.top().tpn() + "-" + pair.bottom().tpn())
                        .sorted()
                        .toList()
                + " PAB " + bye.map(Contender::tpn);
    }

    /** Scores with many ties, opponents met, [C2] facts and previous-round floats at random. */
    private static List<Contender> randomPlayers(Random random, int count) {
        var met = new ArrayList<Set<ParticipantId>>();
        for (var index = 0; index < count; index++) {
            met.add(new HashSet<>());
        }
        for (var a = 1; a <= count; a++) {
            for (var b = a + 1; b <= count; b++) {
                if (random.nextInt(4) == 0) {
                    met.get(a - 1).add(DoubleSwissPlayerMother.id(b));
                    met.get(b - 1).add(DoubleSwissPlayerMother.id(a));
                }
            }
        }
        var players = new ArrayList<Contender>();
        for (var tpn = 1; tpn <= count; tpn++) {
            var colours = new ArrayList<Colour>();
            for (var match = random.nextInt(5); match > 0; match--) {
                colours.add(random.nextBoolean() ? Colour.WHITE : Colour.BLACK);
            }
            var score = Points.of(random.nextInt(5)).plus(random.nextInt(3) == 0 ? Points.of("0.5") : Points.ZERO);
            players.add(new Contender(
                    DoubleSwissPlayerMother.id(tpn),
                    tpn,
                    score,
                    score,
                    colours.size() + random.nextInt(2),
                    colours,
                    met.get(tpn - 1),
                    random.nextInt(5) == 0,
                    random.nextInt(3) == 0));
        }
        return players;
    }
}
