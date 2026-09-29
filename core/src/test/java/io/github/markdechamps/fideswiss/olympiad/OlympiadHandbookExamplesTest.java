package io.github.markdechamps.fideswiss.olympiad;

import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.games;
import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.pair;
import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.pairs;
import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.rated;
import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.sorted;
import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.team;
import static io.github.markdechamps.fideswiss.olympiad.OlympiadTeamMother.withoutBye;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** The examples of the Olympiad Pairing Rules (D.02, 2022), and one fixture per article the procedure reads. */
class OlympiadHandbookExamplesTest {

    @Nested
    class GivenTheExamples {

        @Test
        void triesTheFifteenPairingsOfSixTeamsInTheTextsOrder() {
            // 9.3's table; its row 4 prints "3 x 5" where only 3 x 6 is left.
            var teams = IntStream.rangeClosed(1, 6)
                    .mapToObj(number -> team(number, 0, ""))
                    .toList();

            assertThat(allInOrder(teams))
                    .containsExactly(
                            "1-4 2-5 3-6",
                            "1-4 2-6 3-5",
                            "1-4 2-3 5-6",
                            "1-5 2-4 3-6",
                            "1-5 2-6 3-4",
                            "1-5 2-3 4-6",
                            "1-6 2-4 3-5",
                            "1-6 2-5 3-4",
                            "1-6 2-3 4-5",
                            "1-3 2-5 4-6",
                            "1-3 2-6 4-5",
                            "1-3 2-4 5-6",
                            "1-2 3-5 4-6",
                            "1-2 3-6 4-5",
                            "1-2 3-4 5-6");
        }

        @Test
        void takesTheNextPairingOfTheTableWhenTheFirstIsARematch() {
            // 9.3: 1 has met 4, so 1 x 5 is tried next and the subgroup 2, 3, 4, 6 is paired 2 x 4, 3 x 6.
            var teams = List.of(
                    team(1, 0, "", 4),
                    team(2, 0, ""),
                    team(3, 0, ""),
                    team(4, 0, "", 1),
                    team(5, 0, ""),
                    team(6, 0, ""));

            var paired = new Scrutiny(Direction.DOWNWARD, ColourLimits.OBSERVED, Matchings.BLOSSOM).pair(teams);

            assertThat(paired.stream().map(p -> p.first() + "-" + p.second()).toList())
                    .containsExactly("1-5", "2-4", "3-6");
        }

        @Test
        void findsTheMedianGroupOfEightyEightTeams() {
            // 6.4: places 43 and 44 have 11 points, 45 and 46 have 10; the median team is 45, so 10 points.
            var teams = IntStream.rangeClosed(1, 88)
                    .mapToObj(place -> team(place, place <= 40 ? 12 : place <= 44 ? 11 : place <= 60 ? 10 : 5, ""))
                    .toList();

            assertThat(OlympiadProcedure.medianGroup(teams)).isEqualTo(Points.of(10));
        }
    }

    @Nested
    class GivenFloaters {

        @Test
        void floatsTheHighestRankedTeamOfAnOddGroupBelowTheMedianUpToTheLowestRankedOpponentItMayMeet() {
            // 8.2.1: the fifth team is the median, so the 2-MP group is the Median Group. 8 floats up and plays the
            // lowest ranked team it has not met, 6 (it met 7); 5-7 is left.
            var teams = List.of(
                    team(1, 4, "W"),
                    team(2, 4, "B"),
                    team(3, 4, "W"),
                    team(4, 4, "B"),
                    team(5, 2, "W"),
                    team(6, 2, "B"),
                    team(7, 2, "W", 8),
                    team(8, 0, "B", 7));

            assertThat(pairs(pair(teams))).containsExactlyElementsOf(sorted("1-3 2-4 6-8 5-7"));
        }

        @Test
        void floatsTheSecondHighestWhenTheRestOfTheGroupCouldNotBePaired() {
            // 8.2.2: floating 8 would leave 9 and 10, who have met; so 9 floats, and 8 plays 10.
            var teams = List.of(
                    team(1, 6, "W"),
                    team(2, 6, "B"),
                    team(3, 6, "W"),
                    team(4, 6, "B"),
                    team(5, 6, "W"),
                    team(6, 4, "B"),
                    team(7, 4, "W"),
                    team(8, 2, "B"),
                    team(9, 2, "W", 10),
                    team(10, 2, "B", 9),
                    team(11, 0, "W"),
                    team(12, 0, "B"));

            assertThat(pairs(pair(teams))).containsExactlyElementsOf(sorted("1-3 2-4 5-6 11-12 8-10 7-9"));
        }

        @Test
        void floatsATeamThatCanPlaySomeoneInTheGroupAbove() {
            // 8.2.3: 8 has played both teams of the 4-MP group above, so 9 floats instead.
            var teams = List.of(
                    team(1, 6, "W"),
                    team(2, 6, "B"),
                    team(3, 6, "W"),
                    team(4, 6, "B"),
                    team(5, 6, "W"),
                    team(6, 4, "BW", 8),
                    team(7, 4, "WB", 8),
                    team(8, 2, "WB", 6, 7),
                    team(9, 2, "W"),
                    team(10, 2, "B"),
                    team(11, 0, "W"),
                    team(12, 0, "B"));

            assertThat(pairs(pair(teams))).containsExactlyElementsOf(sorted("1-3 2-4 5-6 11-12 8-10 7-9"));
        }

        @Test
        void meetsAFloaterFromAboveInTheMedianGroupWhenNoTeamBelowCanReachIt() {
            // 8.2.4: no team of the 2-MP group can play the 4-MP median group; a floater from above meets it there.
            var teams = List.of(
                    team(1, 6, "W"),
                    team(2, 6, "B"),
                    team(3, 6, "W"),
                    team(4, 6, "B"),
                    team(5, 6, "W"),
                    team(6, 4, "WB", 8, 9, 10),
                    team(7, 4, "BW", 8, 9, 10),
                    team(8, 2, "BW", 6, 7),
                    team(9, 2, "WB", 6, 7),
                    team(10, 2, "B", 6, 7));

            assertThat(pairs(pair(teams))).containsExactlyElementsOf(sorted("1-3 2-4 9-10 5-8 6-7"));
        }

        @Test
        void floatsTheLowestRankedTeamOfAnOddGroupAboveTheMedianDown() {
            // 8.3.1: 3 (the lowest of the odd 6-MP group) has met 4, so it plays 5; 8 floats up to 7.
            var teams = List.of(
                    team(1, 6, "W"),
                    team(2, 6, "B"),
                    team(3, 6, "W", 4),
                    team(4, 4, "B", 3),
                    team(5, 4, "W"),
                    team(6, 4, "B"),
                    team(7, 4, "W"),
                    team(8, 0, "B"));

            assertThat(pairs(pair(teams))).containsExactlyElementsOf(sorted("1-2 3-5 7-8 4-6"));
        }

        @Test
        void floatsATeamThatHasPlayedEveryTeamOfItsGroup() {
            // 9.4: 1 has played 2, 3 and 4, so it floats down; 9.5 keeps 2-3, and 4 floats too.
            var teams = List.of(
                    team(1, 6, "WBW", 2, 3, 4),
                    team(2, 6, "BWB", 1),
                    team(3, 6, "WBW", 1),
                    team(4, 6, "BWB", 1),
                    team(5, 4, "W"),
                    team(6, 4, "B"),
                    team(7, 4, "W"),
                    team(8, 4, "B"));

            assertThat(pairs(pair(teams))).containsExactlyElementsOf(sorted("2-3 1-5 4-6 7-8"));
        }
    }

    @Nested
    class GivenColours {

        @Test
        void givesTheOddTeamsOfTheTopHalfTheColourDrawnByLotInRoundOne() {
            // 7.2: 1 draws White, 3 has it too, 2 and 4 have Black.
            var teams = IntStream.rangeClosed(1, 8)
                    .mapToObj(number -> team(number, 0, ""))
                    .toList();

            assertThat(games(pair(teams))).containsExactlyInAnyOrder("1-5", "6-2", "3-7", "8-4");
        }

        @Test
        void equalisesBeforeItAlternates() {
            // 7.5.1: 1 is at +1 and had Black last; equalisation gives it Black although alternation gives White.
            assertThat(colours(team(1, 2, "WW-B"), team(2, 2, "BWBW"))).isEqualTo("2-1");
        }

        @Test
        void alternatesFromTheLatestRoundWithDifferentColours() {
            // 7.6: equal differences; round 2 is the latest with different colours (1 had B, 2 had W): 1 gets W.
            assertThat(colours(team(1, 4, "WBWB"), team(2, 4, "BWWB"))).isEqualTo("1-2");
        }

        @Test
        void alternatesTheHigherRankedTeamWhenTheColoursWereAlwaysTheSame() {
            // 7.6, second sentence: 1 had White last, so it gets Black.
            assertThat(colours(team(1, 4, "BW"), team(2, 4, "BW"))).isEqualTo("2-1");
        }

        @Test
        void disregardsTheColourLimitsWhenTheGroupWouldOtherwiseFloatTeams() {
            // 7.4: 1 and 2 both had White twice running, so 7.3 lets them not meet; the 4-MP group would float both.
            var teams = List.of(team(1, 4, "WW"), team(2, 4, "WW"), team(3, 2, "BW"), team(4, 2, "WB"));

            var outcome = pair(teams);

            assertThat(pairs(outcome)).containsExactly("1-2", "3-4");
            assertThat(outcome.groups().getFirst().result().limits()).isEqualTo(ColourLimits.DISREGARDED);
        }

        private String colours(Team higher, Team lower) {
            var round = higher.board1().size() + 1;
            var game = new ColourAllocation(InitialColour.white(), round).allocate(new Pair(higher, lower));
            return game.white() + "-" + game.black();
        }
    }

    @Test
    void givesTheByeToTheLowestRankedEligibleTeam() {
        // 4.1, 4.2: the two lowest ranked teams are ineligible, so 5 gets the bye.
        var teams = new ArrayList<>(IntStream.rangeClosed(1, 5)
                .mapToObj(number -> team(number, 2, "W"))
                .toList());
        teams.add(withoutBye(team(6, 2, "W")));
        teams.add(withoutBye(team(7, 2, "-")));
        teams.add(team(8, 4, "B"));
        teams.add(team(9, 4, "B"));

        var outcome = pair(teams);

        assertThat(outcome.bye().map(Team::id)).contains(ParticipantId.of("5"));
    }

    @Test
    void publishesTheMatchesByMatchpointsThenTheirSumThenTheAverageRating() {
        // 11.1: 3-4 (4 and 2 MP) and 1-2 (4 and 4 MP) share the higher team's matchpoints; the sum puts 1-2 first.
        // 7-8 and 5-6 share both; 7 has the higher average rating.
        var games = new ArrayList<>(List.of(
                new Game(team(3, 4, ""), team(4, 2, ""), ""),
                new Game(team(5, 2, ""), team(6, 2, ""), ""),
                new Game(rated(team(7, 2, ""), 2700), team(8, 2, ""), ""),
                new Game(team(1, 4, ""), team(2, 4, ""), "")));

        games.sort(PublicationOrder.ORDER);

        assertThat(games.stream().map(game -> game.white() + "-" + game.black()).toList())
                .containsExactly("1-2", "3-4", "7-8", "5-6");
    }

    private static List<String> allInOrder(List<Team> teams) {
        if (teams.isEmpty()) {
            return List.of("");
        }
        var arranged = teams.stream().sorted(Team.RANKING).toList();
        var result = new ArrayList<String>();
        for (var opponent : ExchangeOrder.forFirstOf(arranged)) {
            var rest = new ArrayList<>(arranged);
            rest.remove(arranged.getFirst());
            rest.remove(opponent);
            for (var tail : allInOrder(rest)) {
                result.add((arranged.getFirst() + "-" + opponent + " " + tail).trim());
            }
        }
        return result;
    }
}
