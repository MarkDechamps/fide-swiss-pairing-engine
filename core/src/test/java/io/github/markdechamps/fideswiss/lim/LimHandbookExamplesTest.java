package io.github.markdechamps.fideswiss.lim;

import static io.github.markdechamps.fideswiss.lim.LimPlayerMother.player;
import static io.github.markdechamps.fideswiss.lim.LimPlayerMother.round;
import static io.github.markdechamps.fideswiss.lim.LimPlayerMother.score;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.Rating;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** The worked examples of C.04.4.3 (2026), and one fixture per rule, each checked against what the text says. */
class LimHandbookExamplesTest {

    private static final Reachability MATCHING = Reachability.MATCHING;

    @Nested
    class GivenTheExchangeTables {

        @ParameterizedTest(name = "4.2 column {0}")
        @CsvSource({"1, 1-4 2-5 3-6", "2, 1-5 2-4 3-6", "3, 1-6 2-4 3-5", "4, 1-3 2-5 4-6", "5, 1-2 3-5 4-6"})
        void playerOneTriesTheBottomHalfThenTheTopHalfUpwards(int column, String pairs) {
            // 4.2: #1 tries 4, 5, 6, 3, 2; each column is what the scoregroup becomes.
            var met = new int[7][];
            met[1] = List.of(4, 5, 6, 3).subList(0, column - 1).stream()
                    .mapToInt(Integer::intValue)
                    .toArray();

            assertThat(pairSix(met)).isEqualTo(pairs);
        }

        @ParameterizedTest(name = "4.3 column {0}")
        @CsvSource({"1, 1-6 2-4 3-5", "2, 1-6 2-5 3-4", "3, 1-6 2-3 4-5", "4, 1-3 2-6 4-5", "5, 1-2 3-5 4-6"})
        void playerTwoTriesTheRestOnceOneHasAnOpponent(int column, String pairs) {
            // 4.3: #1 has found #6; #2 tries 4, 5, 3, then 6 (moving #1 to 3), then 1.
            var met = new int[7][];
            met[1] = new int[] {4, 5};
            met[2] = List.of(4, 5, 3, 6).subList(0, column - 1).stream()
                    .mapToInt(Integer::intValue)
                    .toArray();

            assertThat(pairSix(met)).isEqualTo(pairs);
        }
    }

    @Nested
    class GivenPlayersWithoutASuitableOpponent {

        @Test
        void keepsAsManyPairingsAsTheScoregroupCanHave() {
            // 4.4, 4.4.2: #1 and #2 have met each other and all but #6; 1-6 stays, #2 floats with the lowest
            // numbered left (#5).
            var met = new int[8][];
            met[1] = new int[] {2, 3, 4, 5};
            met[2] = new int[] {1, 3, 4, 5};

            var result = downward(round(2, false)).pair(group(6, met, 4), List.of(), adjacent(4, 7, 8));

            assertThat(show(result.pairs())).isEqualTo("1-6 3-4");
            assertThat(numbers(result.floaters())).containsExactly(2, 5);
        }

        @Test
        void exchangesThePlayerWithoutAnOpponentWithTheOneThatWouldMakeTheNumberEven() {
            // 4.4.1: with seven players #7 would float to make the number even, but #2 is exchanged with it.
            var met = new int[8][];
            met[1] = new int[] {2, 3, 4, 5};
            met[2] = new int[] {1, 3, 4, 5, 6, 7};

            var result = downward(round(2, false)).pair(group(7, met, 4), List.of(), adjacent(4, 8, 9));

            assertThat(numbers(result.floaters())).containsExactly(2);
        }
    }

    @Nested
    class GivenRoundOne {

        @Test
        void pairsTheTopHalfAgainstTheBottomHalfWithColoursByLot() {
            // 7.2: forty players, #1 White: 1 v 21, 22 v 2, 3 v 23, 24 v 4, ... 40 v 20; #1 Black: the mirror.
            var players = IntStream.rangeClosed(1, 40)
                    .mapToObj(i -> player(i, 0, "", 2600 - i))
                    .toList();

            var white = games(new LimProcedure(round(1, false, 9, InitialColour.white()), MATCHING).pair(players));
            var black = games(new LimProcedure(round(1, false, 9, InitialColour.black()), MATCHING).pair(players));

            assertThat(white).startsWith("1-21", "22-2", "3-23", "24-4").endsWith("40-20");
            assertThat(black).startsWith("21-1", "2-22", "23-3", "4-24").endsWith("20-40");
        }

        @Test
        void givesTheByeToTheLowestRatedPlayer() {
            // 7.1: the lowest rated, not the lowest ranked.
            var players = new ArrayList<>(IntStream.rangeClosed(1, 39)
                    .mapToObj(i -> player(i, 0, "", 2600 - i))
                    .toList());
            players.set(10, player(11, 0, "", 1000));

            var outcome = new LimProcedure(round(1, false), MATCHING).pair(players);

            assertThat(outcome.pairingAllocatedBye()).map(Player::tpn).contains(11);
        }

        @Test
        void countsAnUnratedPlayerAsTheLowestRated() {
            var players = new ArrayList<>(IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> player(i, 0, "", 2600 - i))
                    .toList());
            var unrated = players.get(1);
            players.set(
                    1,
                    new Player(
                            unrated.id(),
                            unrated.pairingNumber(),
                            Rating.unrated(),
                            unrated.pairingScore(),
                            List.of(),
                            Set.of(),
                            true,
                            false));

            var outcome = new LimProcedure(round(1, false), MATCHING).pair(players);

            assertThat(outcome.pairingAllocatedBye()).map(Player::tpn).contains(2);
        }
    }

    @Test
    void pairsTheHigherScoresDownTheLowerUpAndTheMedianLast() {
        // 8.3 (2.2): round 2 pairs 1 point, then 0 points, then the median 0.5 last.
        var scores = Set.of(score(0), score(1), score(2));

        assertThat(LimProcedure.higherScores(scores, score(1))).containsExactly(score(2));
        assertThat(LimProcedure.lowerScores(scores, score(1))).containsExactly(score(0));
        var later = IntStream.rangeClosed(0, 8).mapToObj(LimPlayerMother::score).collect(Collectors.toSet());
        assertThat(LimProcedure.higherScores(later, score(4))).containsExactly(score(8), score(7), score(6), score(5));
        assertThat(LimProcedure.lowerScores(later, score(4))).containsExactly(score(0), score(1), score(2), score(3));
    }

    @Test
    void cracksTheLastPairingOfTheLowerSideWhenMoreFloatersCameFromAbove() {
        // 2.6.1: round 3, median 1 point; all of 2 points have met #4, the only median resident, so #3 floats down
        // and blocks the median; the last pairing of the lower side is cracked.
        var players = List.of(
                player(1, 4, "WB", 2500, 4),
                player(2, 4, "BW", 2400, 4),
                player(3, 4, "WB", 2300, 4),
                player(4, 2, "BW", 2200, 1, 2, 3),
                player(5, 0, "WB", 2100),
                player(6, 0, "BW", 2000));

        var outcome = new LimProcedure(round(3, false, 5), MATCHING).pair(players);

        assertThat(outcome.crackedPairings()).isEqualTo(1);
        assertThat(sortedPairs(outcome)).containsExactly("1-2", "3-6", "4-5");
    }

    @Nested
    class GivenColours {

        private final ColourAllocation colours = new ColourAllocation(round(3, false));

        @Test
        void givesTheHigherRankedItsDueColourInTheMedianOrAbove() {
            // 5.4.
            var game = colours.allocate(new Pair(player(3, 2, "WB", 2300), player(7, 2, "WB", 2200)), true);

            assertThat(game.white().tpn() + "-" + game.black().tpn()).isEqualTo("3-7");
        }

        @Test
        void givesTheLowerRankedItsDueColourBelowTheMedian() {
            var game = colours.allocate(new Pair(player(3, 2, "WB", 2300), player(7, 2, "WB", 2200)), false);

            assertThat(game.white().tpn() + "-" + game.black().tpn()).isEqualTo("7-3");
        }

        @Test
        void goesBackThroughTheHistoryToTheLatestDifference() {
            var game = colours.allocate(new Pair(player(4, 2, "BWB", 2250), player(8, 2, "WBB", 2150)), true);

            assertThat(game.white().tpn() + "-" + game.black().tpn()).isEqualTo("8-4");
        }
    }

    @Test
    void ignoresTheColourLimitsInTheLastRound() {
        // Article 6: in the last round players with the same score who have not met are compatible.
        var a = player(1, 4, "BWW", 2300);
        var b = player(2, 4, "WBWW", 2200);

        assertThat(round(5, false, 9).compatible(a, b)).isFalse();
        assertThat(round(9, false, 9).compatible(a, b)).isTrue();
    }

    @Nested
    class GivenAChoiceOfFloater {

        @Test
        void avoidsAPlayerWhoFloatedTheRoundBefore() {
            // 3.10: the lowest numbered player floated last round, so the next one floats instead.
            var members = IntStream.rangeClosed(1, 5)
                    .mapToObj(i -> player(i, 4, "", 2500 - i, i == 5))
                    .toList();

            var result = downward(round(3, false)).pair(members, List.of(), adjacent(2, 6, 7));

            assertThat(numbers(result.floaters())).containsExactly(4);
        }

        @Test
        void floatsAPlayerDueTheColourMorePlayersAreDue() {
            // 3.2.2: three due White, two due Black: a player due White floats, the lowest numbered of them.
            var members = List.of(
                    player(1, 4, "B", 2500),
                    player(2, 4, "W", 2400),
                    player(3, 4, "B", 2300),
                    player(4, 4, "B", 2200),
                    player(5, 4, "W", 2100));

            var down = downward(round(2, false)).pair(members, List.of(), adjacent(2, 6, 7));
            var up = new ScoregroupPairing(round(2, false), MATCHING, Direction.UPWARD)
                    .pair(members, List.of(), adjacent(6, 6, 7));

            assertThat(numbers(down.floaters())).containsExactly(4);
            assertThat(numbers(up.floaters())).containsExactly(1);
        }

        @Test
        void limitsTheColourChoiceInAMaxiTournament() {
            // 3.2.3: the colour choice (#4) is rated more than 100 above the lowest numbered (#5), so #5 floats.
            var members = List.of(
                    player(1, 4, "B", 2500),
                    player(2, 4, "W", 2400),
                    player(3, 4, "B", 2300),
                    player(4, 4, "B", 2200),
                    player(5, 4, "W", 2050));

            var plain = downward(round(2, false)).pair(members, List.of(), adjacent(2, 6, 7));
            var maxi = downward(round(2, true)).pair(members, List.of(), adjacent(2, 6, 7));

            assertThat(numbers(plain.floaters())).containsExactly(4);
            assertThat(numbers(maxi.floaters())).containsExactly(5);
        }

        @Test
        void avoidsAFloaterWithoutACompatibleOpponentBelow() {
            // 3.9: the lowest numbered player has no compatible opponent below (type c), so a type d player floats.
            var members = List.of(player(1, 4, "", 2500), player(2, 4, "", 2400), player(3, 4, "", 2300, 6, 7));

            var result = downward(round(2, false)).pair(members, List.of(), adjacent(2, 6, 7));

            assertThat(numbers(result.floaters())).containsExactly(2);
        }
    }

    @Nested
    class GivenAColourExchange {

        private final List<Player> members = List.of(
                player(1, 2, "B", 2500), player(2, 2, "B", 2400), player(3, 2, "B", 2300), player(4, 2, "W", 2150));

        @Test
        void exchangesForTheOpponentDueTheOtherColour() {
            // 5.2: #1 is due White like #3, the first available, so #4, due Black, is taken instead.
            var pairs = new Scrutiny(round(2, false), MATCHING, Direction.DOWNWARD, members).pair(List.of());

            assertThat(show(pairs)).isEqualTo("1-4 2-3");
        }

        @Test
        void exchangesOnlyWithinAHundredPointsInAMaxiTournament() {
            // 5.7: #4 is rated 150 below #3, the first available, so #1 keeps #3.
            var pairs = new Scrutiny(round(2, true), MATCHING, Direction.DOWNWARD, members).pair(List.of());

            assertThat(show(pairs)).isEqualTo("1-3 2-4");
        }
    }

    private static ScoregroupPairing downward(RoundToPair round) {
        return new ScoregroupPairing(round, MATCHING, Direction.DOWNWARD);
    }

    private static String pairSix(int[][] met) {
        return show(new Scrutiny(round(2, false), MATCHING, Direction.DOWNWARD, group(6, met, 4)).pair(List.of()));
    }

    /** Players 1..n with the given score and no colours; met[i] lists whom #i has met (made symmetric). */
    private static List<Player> group(int n, int[][] met, int halfPoints) {
        var players = new ArrayList<Player>();
        for (var i = 1; i <= n; i++) {
            var id = i;
            var opponents = IntStream.rangeClosed(1, n)
                    .filter(j -> contains(met, id, j) || contains(met, j, id))
                    .boxed()
                    .collect(Collectors.toSet());
            players.add(LimPlayerMother.withOpponents(player(i, halfPoints, "", 2500 - 10 * i), opponents));
        }
        return players;
    }

    private static boolean contains(int[][] met, int who, int opponent) {
        return who < met.length && met[who] != null && Arrays.stream(met[who]).anyMatch(o -> o == opponent);
    }

    private static List<Player> adjacent(int halfPoints, int... ids) {
        return Arrays.stream(ids).mapToObj(i -> player(i, halfPoints, "", 1500)).toList();
    }

    private static String show(List<Pair> pairs) {
        return pairs.stream()
                .map(p -> Math.min(p.first().tpn(), p.second().tpn()) + "-"
                        + Math.max(p.first().tpn(), p.second().tpn()))
                .sorted()
                .collect(Collectors.joining(" "));
    }

    private static List<Integer> numbers(List<Player> players) {
        return players.stream().map(Player::tpn).sorted().toList();
    }

    private static List<String> games(LimProcedure.Outcome outcome) {
        return outcome.games().stream()
                .map(game -> game.white().tpn() + "-" + game.black().tpn())
                .toList();
    }

    private static List<String> sortedPairs(LimProcedure.Outcome outcome) {
        return outcome.games().stream()
                .map(g -> Math.min(g.white().tpn(), g.black().tpn()) + "-"
                        + Math.max(g.white().tpn(), g.black().tpn()))
                .sorted()
                .toList();
    }
}
