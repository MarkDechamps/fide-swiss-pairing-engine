package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.DutchPlayerMother.player;
import static io.github.markdechamps.fideswiss.dutch.DutchPlayerMother.players;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** C.04.3 (2026) Article 4: the order in which a bracket's candidates are generated. */
class DutchGenerationOrderTest {

    @Nested
    class InBracketSequenceNumbers {

        @Test
        void numberTheMovedDownPlayersFirstThenTheResidentsInPairingOrder() {
            var mdp = player(4, "2");
            var residents = List.of(player(1, "1.5"), player(2, "1.5"), player(3, "1.5"));

            var bracket = new Bracket(List.of(mdp), residents);

            // 4.1.1 with 1.2: BSN 1 is the MDP, whose score is higher than every resident's.
            assertThat(bsns(bracket.playersInBsnOrder())).containsExactly(4, 1, 2, 3);
        }
    }

    @Nested
    class TranspositionsOfS2 {

        @Test
        void giveAnElevenPlayerHomogeneousBracket720InLexicographicOrderOfTheFirstFiveBsns() {
            var bracket = players(11, "1");

            var transpositions = firstBsnsOfEachTransposition(bracket.subList(0, 5), bracket.subList(5, 11));

            // 4.2.2's worked example: "6-7-8-9-10, 6-7-8-9-11, 6-7-8-10-11, ..., 6-11-10-9-8, 7-6-8-9-10, ...,
            // 11-10-9-8-7 (720 transpositions)". The third one listed is a slip in the text (also in the pre-2026
            // text): lexicographic order, and the count of 720, put 6-7-8-10-9 third (Readable Dutch algorithm).
            assertThat(transpositions).hasSize(720);
            assertThat(transpositions.subList(0, 3))
                    .containsExactly(List.of(6, 7, 8, 9, 10), List.of(6, 7, 8, 9, 11), List.of(6, 7, 8, 10, 9));
            var lastStartingWith6 = transpositions.indexOf(List.of(6, 11, 10, 9, 8));
            assertThat(transpositions.get(lastStartingWith6 + 1)).isEqualTo(List.of(7, 6, 8, 9, 10));
            assertThat(transpositions.getLast()).isEqualTo(List.of(11, 10, 9, 8, 7));
        }

        @Test
        void giveAnElevenPlayerBracketWithTwoMdps72OrdersOfTheResidentsTheyMeet() {
            var mdps = List.of(player(1, "2"), player(2, "2"));
            var residents = Stream.iterate(3, bsn -> bsn <= 11, bsn -> bsn + 1)
                    .map(bsn -> player(bsn, "1"))
                    .toList();

            var transpositions = firstBsnsOfEachTransposition(mdps, residents);

            // 4.2.2's worked example: "3-4, 3-5, 3-6, ..., 3-11, 4-3, 4-5, ..., 11-10 (72 transpositions)".
            assertThat(transpositions).hasSize(72);
            assertThat(transpositions.subList(0, 3)).containsExactly(List.of(3, 4), List.of(3, 5), List.of(3, 6));
            assertThat(transpositions.subList(7, 10)).containsExactly(List.of(3, 11), List.of(4, 3), List.of(4, 5));
            assertThat(transpositions.getLast()).isEqualTo(List.of(11, 10));
        }

        @Test
        void changeOnlyTheOrderOfS2AndLeaveTheRestInBsnOrder() {
            var bracket = players(5, "1");

            var first = Transpositions.of(
                            bracket.subList(0, 2),
                            bracket.subList(2, 5),
                            (a, b) -> true,
                            Transpositions.BranchTest.EVERY_BRANCH)
                    .map(DutchGenerationOrderTest::bsns)
                    .toList();

            // 4.2.1: a transposition reorders S2 only; 4.2.2: the S2 players after the first N1 do not count.
            assertThat(first)
                    .containsExactly(
                            List.of(3, 4, 5),
                            List.of(3, 5, 4),
                            List.of(4, 3, 5),
                            List.of(4, 5, 3),
                            List.of(5, 3, 4),
                            List.of(5, 4, 3));
        }
    }

    @Nested
    class ResidentExchanges {

        @Test
        void swapEquallySizedGroupsBetweenTheOriginalSubgroupsAndReSortThem() {
            var bracket = players(7, "1");

            var subgroups = new ResidentExchange(List.of(3), List.of(4)).applyTo(bracket, 3);

            // 4.3.1; 3.6.1: after the exchange both subgroups are in 1.2 order again.
            assertThat(bsns(subgroups.s1())).containsExactly(1, 2, 4);
            assertThat(bsns(subgroups.s2())).containsExactly(3, 5, 6, 7);
        }

        @Test
        void comeInTheOrderOfTheFourComparisonRules() {
            var exchanges = ResidentExchange.inOrder(3, 4)
                    .map(ResidentExchange::toString)
                    .toList();

            // 4.3.2, S1 = 1-3 and S2 = 4-7. Rule 1: every single swap comes before any double one. Rule 2: the
            // smallest difference of the sums moved (3<->4 is 1; 3<->5 and 2<->4 are 2). Rule 3: on a tie, the
            // largest differing BSN moved from S1 (3<->5 before 2<->4). Rule 4: on a tie, the smallest differing
            // BSN moved from S2 ([2, 3]<->[4, 7] before [2, 3]<->[5, 6]).
            assertThat(exchanges.subList(0, 12))
                    .containsExactly(
                            "[3]<->[4]",
                            "[3]<->[5]",
                            "[2]<->[4]",
                            "[3]<->[6]",
                            "[2]<->[5]",
                            "[1]<->[4]",
                            "[3]<->[7]",
                            "[2]<->[6]",
                            "[1]<->[5]",
                            "[2]<->[7]",
                            "[1]<->[6]",
                            "[1]<->[7]");
            assertThat(exchanges.subList(12, 19))
                    .containsExactly(
                            "[2, 3]<->[4, 5]",
                            "[2, 3]<->[4, 6]",
                            "[1, 3]<->[4, 5]",
                            "[2, 3]<->[4, 7]",
                            "[2, 3]<->[5, 6]",
                            "[1, 3]<->[4, 6]",
                            "[1, 2]<->[4, 5]");
        }
    }

    @Nested
    class MovedDownPlayerSets {

        @Test
        void comeInOrderOfTheirSmallestDifferingBsn() {
            var mdps = players(4, "2");

            var sets = MdpSets.validInOrder(mdps, mdps.subList(0, 2))
                    .map(DutchGenerationOrderTest::bsns)
                    .toList();

            // 4.4.2: M1 = 2 of four MDPs on the same score, so every pair is valid; {1,3} < {1,4} < {3,4}.
            assertThat(sets)
                    .containsExactly(
                            List.of(1, 2), List.of(1, 3), List.of(1, 4), List.of(2, 3), List.of(2, 4), List.of(3, 4));
        }

        @Test
        void areValidOnlyWhenTheLimboTheyLeaveCompliesWithC7() {
            var mdps = List.of(player(1, "3"), player(2, "2.5"), player(3, "2.5"));
            var lowestAchievableLimbo = List.of(mdps.get(2));

            var sets = MdpSets.validInOrder(mdps, lowestAchievableLimbo)
                    .map(DutchGenerationOrderTest::bsns)
                    .toList();

            // 4.4.1: M1 = 2; leaving the 3-point MDP in the Limbo floats a higher score than needed ([C7]).
            assertThat(sets).containsExactly(List.of(1, 2), List.of(1, 3));
        }

        @Test
        void areJudgedOnTheLimboThatCanActuallyBeAchieved() {
            var mdps = List.of(player(1, "3"), player(2, "2.5"), player(3, "2.5"));
            var achievableLimbo = List.of(mdps.getFirst());

            var sets = MdpSets.validInOrder(mdps, achievableLimbo)
                    .map(DutchGenerationOrderTest::bsns)
                    .toList();

            // 4.4.1, the documented reading: when the 3-point MDP can meet no resident, it stays in the Limbo
            // whatever its score, and the one set that leaves it there is the valid one.
            assertThat(sets).containsExactly(List.of(2, 3));
        }
    }

    private static List<List<Integer>> firstBsnsOfEachTransposition(List<Player> s1, List<Player> s2) {
        return Transpositions.of(s1, s2, (a, b) -> true, Transpositions.BranchTest.EVERY_BRANCH)
                .map(order -> bsns(order.subList(0, s1.size())))
                .toList();
    }

    /** Every player here is numbered by BSN, so its Pairing Number is its BSN. */
    private static List<Integer> bsns(List<Player> players) {
        return players.stream().map(player -> player.pairingNumber().value()).toList();
    }
}
