package io.github.markdechamps.fideswiss.standings;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Hand-worked team competition: four teams, four boards, a round robin in which every team ends on 3 match points.
 *
 * <pre>
 *   R1  1-2: 1 1 0 ½ (2.5-1.5)   3-4: 1 0 ½ ½ (2-2)
 *   R2  1-3: 0 ½ ½ 1 (2-2)       2-4: 1 1 1 0 (3-1)
 *   R3  1-4: ½ 0 0 1 (1.5-2.5)   2-3: 0 1 ½ ½ (2-2)
 * </pre>
 *
 * Game points: team 1 has 6, team 2 has 6.5, team 3 has 6, team 4 has 5.5.
 */
class TeamTieBreaksTest {

    private static TeamTable roundRobin(String tieBreaks) {
        return roundRobin(PrimaryScore.MATCH_POINTS, tieBreaks);
    }

    private static TeamTable roundRobin(PrimaryScore primary, String tieBreaks) {
        return TeamTable.of(4, 3, 4, primary, tieBreaks)
                .round("1-2 1 1 0 ½", "3-4 1 0 ½ ½")
                .round("1-3 0 ½ ½ 1", "2-4 1 1 1 0")
                .round("1-4 ½ 0 0 1", "2-3 0 1 ½ ½");
    }

    private static BigDecimal value(String tieBreaks, int team) {
        var standings = roundRobin(tieBreaks).standings();
        return standings.standing(id(team)).tieBreakValues().get(0).value().orElseThrow();
    }

    @Test
    void everyTeamHasThreeMatchPoints() {
        var standings = roundRobin("MPvGP").standings();

        assertThat(standings.ranked())
                .allSatisfy(standing -> assertThat(standing.score().toString()).isEqualTo("3"));
    }

    @Test
    void matchPointsOrGamePointsIsTheSecondaryScore() {
        // C.07 13.1: game points, as match points decide the competition.
        assertThat(value("MPvGP", 2)).isEqualByComparingTo("6.5");
        assertThat(value("MPvGP", 4)).isEqualByComparingTo("5.5");
        assertThat(roundRobin("MPvGP").standings().standing(id(2)).rank()).isEqualTo(Rank.of(1));
        assertThat(roundRobin("MPvGP").standings().standing(id(4)).rank()).isEqualTo(Rank.of(4));
    }

    @Test
    void matchPointsOrGamePointsIsMatchPointsWhenGamePointsDecide() {
        assertThat(roundRobin(PrimaryScore.GAME_POINTS, "MPvGP")
                        .standings()
                        .standing(id(1))
                        .tieBreakValues()
                        .get(0)
                        .value())
                .contains(new BigDecimal("3"));
    }

    @Test
    void extendedSonnebornBergerMultipliesTheOpponentsTotalByTheScoreAgainstIt() {
        // C.07 13.2.1-13.2.4, worked by hand for team 1 (opponents 2, 3, 4: MP 3, 3, 3; GP 6.5, 6, 5.5).
        assertThat(value("EMMSB", 1)).isEqualByComparingTo("9"); // 3*2 + 3*1 + 3*0
        assertThat(value("EMGSB", 1)).isEqualByComparingTo("18"); // 3 MP each opponent * 6 GP scored
        assertThat(value("EGMSB", 1)).isEqualByComparingTo("19"); // 6.5*2 + 6*1 + 5.5*0
        assertThat(value("EGGSB", 1)).isEqualByComparingTo("36.5"); // 6.5*2.5 + 6*2 + 5.5*1.5
        assertThat(value("EGGSB", 4)).isEqualByComparingTo("33.5"); // 6*2 + 6.5*1 + 6*2.5
    }

    @Test
    void cutOneExcludesTheProductOfTheOpponentWithTheLowestTotal() {
        // C.07 14.1.2: EGMSB/C1 leaves out the opponent with the lowest GP (team 4 for team 1, product 0).
        assertThat(value("EGMSB/C1", 1)).isEqualByComparingTo("19");
        // Team 3: opponents 1 (6 * 1), 2 (6.5 * 1), 4 (5.5 * 1); the lowest GP is team 4's, product 5.5.
        assertThat(value("EGMSB", 3)).isEqualByComparingTo("18");
        assertThat(value("EGMSB/C1", 3)).isEqualByComparingTo("12.5");
    }

    @Test
    void extendedDirectEncounterBreaksTheTieByMatchPointsThenGamePoints() {
        // C.07 13.3.1: everyone met everyone; match points among them tie, game points order 2, {1, 3}, 4.
        var standings = roundRobin("EDE").standings();

        assertThat(standings.standing(id(2)).rank()).isEqualTo(Rank.of(1));
        assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(4)).rank()).isEqualTo(Rank.of(4));
    }

    @Test
    void theKnockOutTieBreaksSeparateExactlyTwoTeamsTiedOnMatchAndGamePoints() {
        // C.07 13.3.2: teams 1 and 3 have 3 MP and 6 GP. Top board: team 3 has 3 on board 1, team 1 has 1.5.
        var standings = roundRobin("EDET").standings();

        assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(3));
        assertThat(standings.standing(id(3)).decidedBy().orElseThrow()).hasToString("EDET 4 > 3");
    }

    @Test
    void boardCountIsNotUsableWhenTheTiedTeamsHaveDifferentGamePoints() {
        // C.07 12.1: "It can only be used when all tied teams have the same number of game points."
        var ties = roundRobin("BC").standings();

        assertThat(ties.standing(id(1)).tieBreakValues().get(0).value()).isEmpty();
    }

    @Test
    void boardCountAppliesAmongTeamsWithTheSameGamePoints() {
        // 6 = 6: the teams 1 and 3 only, once MPvGP has split off 2 and 4.
        var standings = roundRobin("MPvGP, BC").standings();

        assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(3));
        assertThat(standings.standing(id(3)).tieBreakValues().get(1).value().orElseThrow())
                .isEqualByComparingTo("12.5");
        assertThat(standings.standing(id(1)).tieBreakValues().get(1).value().orElseThrow())
                .isEqualByComparingTo("16");
    }

    @Test
    void bottomBoardEliminationCountsEveryBoardButTheBottomOne() {
        // C.07 12.3: team 1 has 3.5 without board 4, team 3 has 5.
        var standings = roundRobin("MPvGP, EDEB").standings();

        assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(3));
    }

    @Test
    void scoresAndScheduleStrengthAddsTheBuchholzOverTheNormalisingFactorToTheSecondaryScore() {
        // C.07 13.4: factor = (3 rounds * 2 MP) / 4 GP = 1.5, towards zero 1; team 2: 6.5 + 9 / 1.
        assertThat(value("SSSC", 2)).isEqualByComparingTo("15.5");
        // /K2 states the factor: 6.5 + 9 / 2.
        assertThat(value("SSSC/K2", 2)).isEqualByComparingTo("11");
    }

    @Test
    void anIndividualTieBreakTakesTheTeamScoreOfItsCode() {
        // C.07 13: BH:GP sums the opponents' game points, BH:GP/C1 cuts the lowest; PS:GP sums 2.5, 4.5, 6.
        assertThat(value("BH:GP", 1)).isEqualByComparingTo("18");
        assertThat(value("BH:GP/C1", 1)).isEqualByComparingTo("12.5");
        assertThat(value("PS:GP", 1)).isEqualByComparingTo("13");
        assertThat(value("BH:MP", 1)).isEqualByComparingTo("9");
        assertThat(value("BH", 1)).isEqualByComparingTo("9");
    }

    @Test
    void winCountsWonMatchesInMatchPoints() {
        assertThat(value("WIN:MP", 1)).isEqualByComparingTo("1");
        assertThat(value("WIN", 3)).isEqualByComparingTo("0");
    }

    @Test
    void codesRoundTripWithTheirTeamScoreAndFactor() {
        assertThat(TieBreakList.parse("BH:GP/C1/P, SSSC/F/P/K4, EDET/P, EGGSB/C2")
                        .toString())
                .isEqualTo("BH:GP/C1/P, SSSC/K4/F/P, EDET/P, EGGSB/C2");
    }

    @Test
    void refusesATeamTieBreakInAnIndividualTournament() {
        var settings = io.github.markdechamps.fideswiss.tournament.Profiles.individualSwiss(
                        io.github.markdechamps.fideswiss.tournament.NumberOfRounds.of(1))
                .with(TieBreakList.parse("MPvGP"));
        var tournament = io.github.markdechamps.fideswiss.tournament.Tournament.of(
                settings, io.github.markdechamps.fideswiss.tournament.TournamentMother.participants(2));

        assertThatThrownBy(tournament::standings)
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("team competition");
    }

    @Test
    void refusesWrongModifiers() {
        assertThatThrownBy(() -> TieBreakList.parse("ESB")).hasMessageContaining("EMMSB");
        assertThatThrownBy(() -> TieBreakList.parse("EMMSB/M1")).hasMessageContaining("Cut-1 and Cut-2 only");
        assertThatThrownBy(() -> TieBreakList.parse("SB:MP")).hasMessageContaining("no team score");
        assertThatThrownBy(() -> TieBreakList.parse("WIN:GP")).hasMessageContaining("only :MP");
        assertThatThrownBy(() -> TieBreakList.parse("BH/K4")).hasMessageContaining("/K applies only to SSSC");
    }
}
