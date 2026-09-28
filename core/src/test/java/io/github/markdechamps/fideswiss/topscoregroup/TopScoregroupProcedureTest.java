package io.github.markdechamps.fideswiss.topscoregroup;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TopScoregroupProcedureTest {

    @Nested
    class GivenAnOddNumberOfContenders {

        @Test
        void givesThePabToTheLowestScoreThenTheMostMatchesPlayedThenTheLargestTpn() {
            // C.04.6 3.4.2-3.4.4: 4 and 5 share the lowest score; 4 played more matches.
            var contenders = List.of(
                    contender(1, "2", 2),
                    contender(2, "2", 2),
                    contender(3, "1", 2),
                    contender(4, "0", 2),
                    contender(5, "0", 1));

            var pairing = TopScoregroupProcedure.pair(round(contenders)).orElseThrow();

            assertThat(pairing.pairingAllocatedBye()).map(Contender::tpn).contains(4);
        }

        @Test
        void skipsAContenderThatMayNotReceiveIt() {
            // C.04.6 [C2]: 5 already had a PAB, so the next in 3.4 order gets it.
            var contenders = List.of(
                    contender(1, "1", 1),
                    contender(2, "1", 1),
                    contender(3, "0", 1),
                    contender(4, "0", 1),
                    barred(contender(5, "0", 1)));

            var pairing = TopScoregroupProcedure.pair(round(contenders)).orElseThrow();

            assertThat(pairing.pairingAllocatedBye()).map(Contender::tpn).contains(4);
        }
    }

    @Nested
    class GivenABracketWithAnUpfloaterAboveAResident {

        // Gacrux probe case 1, round 2: 1, 2 and 4 on 2, 3 and 7 on 1, the rest on 0.
        private final List<Contender> contenders = List.of(
                met(contender(1, "2", 1), 5),
                met(contender(2, "2", 1), 6),
                met(contender(3, "1", 1), 7),
                met(contender(4, "2", 1), 8),
                met(contender(5, "0", 1), 1),
                met(contender(6, "0", 1), 2),
                met(contender(7, "1", 1), 3),
                met(contender(8, "0", 1), 4));

        @Test
        void floatsTheHighestScoreThenTheSmallestTpn() {
            // C.04.6 3.5.3: 3 and 7 are on 1; 3 has the smaller TPN.
            var pairing = TopScoregroupProcedure.pair(round(contenders)).orElseThrow();

            assertThat(pairing.brackets().getFirst().upfloaters())
                    .map(Contender::tpn)
                    .containsExactly(3);
        }

        @Test
        void seatsTheSmallerTpnAsTopMemberWhateverTheScore() {
            // C.04.6 3.6.1 (ruling G1): the upfloater 3 is a top member, so the first identifier is 1-3, 2-4.
            var pairing = TopScoregroupProcedure.pair(round(contenders)).orElseThrow();

            assertThat(pairsOf(pairing)).containsExactly("1-3", "2-4", "5-7", "6-8");
        }
    }

    @Test
    void cannotCompleteARoundWhoseContendersHaveAllMet() {
        // C.04.6 3.3.3.
        var contenders = List.of(met(contender(1, "1", 1), 2), met(contender(2, "0", 1), 1));

        assertThat(TopScoregroupProcedure.pair(round(contenders))).isEmpty();
    }

    private static TopScoregroupRound round(List<Contender> contenders) {
        return new TopScoregroupRound(contenders, false, UpfloaterLookAhead.parityMinimum(), List.of());
    }

    private static List<String> pairsOf(TopScoregroupProcedure.RoundPairing pairing) {
        return pairing.pairs().stream()
                .map(pair -> pair.top().tpn() + "-" + pair.bottom().tpn())
                .sorted()
                .toList();
    }

    private static Contender contender(int tpn, String score, int matchesPlayed) {
        var colours = new ArrayList<Colour>();
        for (var match = 0; match < matchesPlayed; match++) {
            colours.add(match % 2 == 0 ? Colour.WHITE : Colour.BLACK);
        }
        return new Contender(
                id(tpn), tpn, Points.of(score), Points.of(score), matchesPlayed, colours, Set.of(), false, false);
    }

    private static Contender met(Contender contender, int... opponents) {
        var met = Arrays.stream(opponents)
                .mapToObj(TopScoregroupProcedureTest::id)
                .collect(Collectors.toSet());
        return new Contender(
                contender.id(),
                contender.tpn(),
                contender.score(),
                contender.secondaryScore(),
                contender.matchesPlayed(),
                contender.colours(),
                met,
                contender.pairingAllocatedByeBarred(),
                contender.floatedInPreviousRound());
    }

    private static Contender barred(Contender contender) {
        return new Contender(
                contender.id(),
                contender.tpn(),
                contender.score(),
                contender.secondaryScore(),
                contender.matchesPlayed(),
                contender.colours(),
                contender.met(),
                true,
                contender.floatedInPreviousRound());
    }

    private static ParticipantId id(int tpn) {
        return ParticipantId.of(String.valueOf(tpn));
    }
}
