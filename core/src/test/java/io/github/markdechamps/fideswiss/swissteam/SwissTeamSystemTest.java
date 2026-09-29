package io.github.markdechamps.fideswiss.swissteam;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.DRAW;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SwissTeamSystemTest {

    @Nested
    class GivenRoundOne {

        @Test
        void pairsTheTopHalfAgainstTheBottomHalfWithColoursByTheFirstTeamsTpn() {
            var tournament = teams(4, 5);

            var pairing = tournament.pairNextRound();

            // C.04.6 3.6: the first identifier is 1 2 3 4; 4.3.1: an odd first-team gets the initial-colour.
            assertThatPairing(pairing).hasBoards("1-3", "4-2").givesNoPairingAllocatedBye();
        }

        @Test
        void givesThePabToTheLargestTpnWhenAllScoresAreEqual() {
            var tournament = teams(5, 5);

            var pairing = tournament.pairNextRound();

            // C.04.6 3.4.4; the PAB is assigned first (3.3.2), so 1-3 and 4-2 are paired among the other four.
            assertThatPairing(pairing).hasBoards("1-3", "4-2").givesPairingAllocatedByeTo("5");
        }
    }

    @Test
    void seatsAnUpfloaterWithASmallerTpnAsTopMember() {
        // Gacrux probe case 1 (ruling G1): 1, 2 and 4 won, 3 and 7 drew; the upfloater 3 is a top member, so the
        // bracket pairs 1-3 and 2-4, which patched Gacrux colours 3-1 and 4-2.
        var tournament = teams(8, 5)
                .withRound(Round.of(
                        RoundNumber.FIRST,
                        List.of(
                                match(1, 1, 5, WHITE_WINS, WHITE_WINS),
                                match(2, 2, 6, WHITE_WINS, WHITE_WINS),
                                match(3, 3, 7, DRAW, DRAW),
                                match(4, 4, 8, WHITE_WINS, WHITE_WINS)),
                        Map.of()));

        var pairing = tournament.pairNextRound();

        assertThatPairing(pairing).hasBoards("4-2", "3-1", "7-5", "6-8");
    }

    @Test
    void valuesThePabAtADrawnMatch() {
        // C.04.6 1.4: 1 match point under match points, 2 game points over four boards under game points.
        var teams = Profiles.teamSwiss(NumberOfRounds.of(5));
        var gamePoints = teams.with(ScoringScheme.teams().withPrimaryScore(PrimaryScore.GAME_POINTS));

        assertThat(teams.pairingAllocatedByeValue()).isEqualTo(Points.of(1));
        assertThat(gamePoints.pairingAllocatedByeValue()).isEqualTo(Points.of(2));
    }

    @Test
    void pairsTeams() {
        assertThat(PairingSystems.swissTeam().competitionType()).isEqualTo(CompetitionType.TEAM);
    }

    @Test
    void takesItsInterpretations() {
        // ADR 0003: ruling A6's graded reading.
        var settings = Profiles.teamSwiss(NumberOfRounds.of(5)).with(UpfloaterLookAhead.graded());

        assertThat(settings.pairingSystem())
                .isEqualTo(SwissTeamSystem.of(ColourPreferenceType.TYPE_A).with(UpfloaterLookAhead.graded()));
    }

    @Test
    void refusesBakuOverGamePoints() {
        // C.04.7 1.4.4.
        var settings = Profiles.teamSwiss(NumberOfRounds.of(9))
                .with(ScoringScheme.teams().withPrimaryScore(PrimaryScore.GAME_POINTS))
                .with(Acceleration.baku());

        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(8)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("C.04.7 1.4.4");
    }

    @Test
    void refusesAScoringSchemeWithoutMatchPoints() {
        // C.04.6 1.2.
        var settings = Profiles.teamSwiss(NumberOfRounds.of(5)).with(ScoringScheme.standard());

        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(8)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("C.04.6 1.2");
    }

    private static Tournament teams(int count, int rounds) {
        return Tournament.of(Profiles.teamSwiss(NumberOfRounds.of(rounds)), TournamentMother.participants(count));
    }

    /** A two-board match whose board-1 White is {@code white}; each game is seen from that team's side. */
    private static Board match(int number, int white, int black, GameOutcome... games) {
        return Board.of(number, String.valueOf(white), String.valueOf(black), MatchOutcome.ofGames(List.of(games)));
    }
}
