package io.github.markdechamps.fideswiss.doubleswiss;

import static io.github.markdechamps.fideswiss.doubleswiss.DoubleSwissPlayerMother.player;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupProcedure;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupRound;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** The worked examples of C.04.5 (2026), 3.5.4 and 3.6.2. */
class DoubleSwissHandbookExamplesTest {

    @Test
    void identifiesAPairingByItsTopMembersThenTheirBottomMembers() {
        // C.04.5 3.6.2: "If 11-24 16-6 10-9 8-4 is a pairing, its identifier is 4 6 9 11 8 16 10 24."
        var players =
                Stream.of(11, 24, 16, 6, 10, 9, 8, 4).collect(Collectors.toMap(tpn -> tpn, tpn -> player(tpn, "0")));
        var pairing = List.of(pair(players, 11, 24), pair(players, 16, 6), pair(players, 10, 9), pair(players, 8, 4));

        assertThat(LiteralDoubleSwiss.identifier(pairing)).containsExactly(4, 6, 9, 11, 8, 16, 10, 24);
    }

    @Test
    void pairsABracketByTheFirstIdentifier() {
        // C.04.5 3.6.3: with every pairing legal, 1 2 3 4 5 6 7 8 comes first: 1-5, 2-6, 3-7, 4-8.
        var players =
                Stream.of(1, 2, 3, 4, 5, 6, 7, 8).map(tpn -> player(tpn, "0")).toList();

        var pairing = TopScoregroupProcedure.pair(round(players)).orElseThrow();

        assertThat(pairsOf(pairing.pairs())).containsExactly("1-5", "2-6", "3-7", "4-8");
    }

    @Test
    void choosesTheFirstSetOfUpfloatersInTheOrderOfTheExample() {
        // C.04.5 3.5.4: "2,6,8 have 3 points, and 1,3,5 have 2.5 points. [C4] determines that a set of three
        // upfloaters is needed, and [C5] determines that two upfloaters must have 3 points and the other 2.5":
        // {2,6,1} < {2,6,3} < ... < {6,8,5}. Here the residents 4, 7 and 9 (4 points) have all met, so each needs an
        // upfloater, and 10 (0 points) has met 1, 3 and 5, so {2,6,8} would leave 10 unpairable ([C3]).
        var players = List.of(
                player(4, "4", 7, 9),
                player(7, "4", 4, 9),
                player(9, "4", 4, 7),
                player(2, "3"),
                player(6, "3"),
                player(8, "3"),
                player(1, "2.5", 10),
                player(3, "2.5", 10),
                player(5, "2.5", 10),
                player(10, "0", 1, 3, 5));

        var pairing = TopScoregroupProcedure.pair(round(players)).orElseThrow();

        assertThat(pairing.brackets().getFirst().upfloaters())
                .map(Contender::tpn)
                .containsExactly(2, 6, 1);
    }

    private static TopScoregroupRound round(List<Contender> players) {
        return new TopScoregroupRound(
                players, false, UpfloaterLookAhead.parityMinimum(), DoubleSwissCriteria.of(false));
    }

    private static ContenderPair pair(Map<Integer, Contender> players, int one, int other) {
        return ContenderPair.of(players.get(one), players.get(other));
    }

    private static List<String> pairsOf(List<ContenderPair> pairs) {
        return pairs.stream()
                .map(pair -> pair.top().tpn() + "-" + pair.bottom().tpn())
                .sorted()
                .toList();
    }
}
