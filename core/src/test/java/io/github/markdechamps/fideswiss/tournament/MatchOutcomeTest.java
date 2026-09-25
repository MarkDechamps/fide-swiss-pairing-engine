package io.github.markdechamps.fideswiss.tournament;

import static io.github.markdechamps.fideswiss.tournament.GameOutcome.BLACK_WINS;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.BLACK_WINS_BY_FORFEIT;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.DOUBLE_FORFEIT;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.DRAW;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS_BY_FORFEIT;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MatchOutcomeTest {

    private final ScoringScheme teamScoring = ScoringScheme.teams();

    @Nested
    class GivenATeamMatch {

        private final MatchOutcome match = MatchOutcome.ofGames(List.of(WHITE_WINS, DRAW, DRAW, BLACK_WINS));

        @Test
        void addsUpTheGamePointsOfEachSide() {
            assertThat(teamScoring.gamePointsFor(match, Colour.WHITE)).isEqualTo(Points.of(2));
            assertThat(teamScoring.gamePointsFor(match, Colour.BLACK)).isEqualTo(Points.of(2));
        }

        @Test
        void scoresMatchPointsFromTheGamePoints() {
            // C.04.6 1.2.2: match points are the primary score by default, 2 / 1 / 0.
            var won = MatchOutcome.ofGames(List.of(WHITE_WINS, DRAW, DRAW, DRAW));

            assertThat(teamScoring.pointsFor(match, Colour.WHITE)).isEqualTo(Points.of(1));
            assertThat(teamScoring.pointsFor(won, Colour.WHITE)).isEqualTo(Points.of(2));
            assertThat(teamScoring.pointsFor(won, Colour.BLACK)).isEqualTo(Points.ZERO);
        }

        @Test
        void scoresGamePointsWhenTheyAreThePrimaryScore() {
            var gamePoints = teamScoring.withPrimaryScore(PrimaryScore.GAME_POINTS);

            assertThat(gamePoints.pointsFor(MatchOutcome.ofGames(List.of(WHITE_WINS, DRAW)), Colour.WHITE))
                    .isEqualTo(Points.of("1.5"));
        }

        @Test
        void isPlayedWhenAtLeastOneBoardWasPlayed() {
            // Swiss Team interpretation rulings A3.
            var oneBoardPlayed = MatchOutcome.ofGames(List.of(WHITE_WINS_BY_FORFEIT, DRAW));

            assertThat(oneBoardPlayed.isPlayed()).isTrue();
            assertThat(oneBoardPlayed.isMeeting()).isTrue();
        }

        @Test
        void isAForfeitWhenNoBoardWasPlayed() {
            var forfeited = MatchOutcome.ofGames(List.of(WHITE_WINS_BY_FORFEIT, WHITE_WINS_BY_FORFEIT));

            assertThat(forfeited.isPlayed()).isFalse();
            assertThat(forfeited.isMeeting()).isFalse();
            assertThat(forfeited.resultOf(Colour.WHITE)).isEqualTo(GameResult.WIN);
        }
    }

    @Nested
    class GivenADoubleSwissMatch {

        @Test
        void scoresTheOddPlayedResultsOfThePreface() {
            var match = MatchOutcome.ofGames(List.of(GameOutcome.WHITE_HALF_BLACK_ZERO, GameOutcome.BOTH_ZERO));

            assertThat(ScoringScheme.standard().pointsFor(match, Colour.WHITE)).isEqualTo(Points.of("0.5"));
            assertThat(ScoringScheme.standard().pointsFor(match, Colour.BLACK)).isEqualTo(Points.ZERO);
            assertThat(match.isPlayed()).isTrue();
        }

        @Test
        void isAMeetingWithoutAColourWhenEachPlayerForfeitsOneGame() {
            // C.04.5 Preface: forfeited only when a player forfeits both games; 1.6: no game played, no colour.
            var match = MatchOutcome.ofGames(List.of(WHITE_WINS_BY_FORFEIT, BLACK_WINS_BY_FORFEIT));

            assertThat(match.isMeeting()).isTrue();
            assertThat(match.isPlayed()).isFalse();
        }

        @Test
        void isForfeitedWhenBothGamesAreForfeitedByOnePlayerOrBoth() {
            assertThat(MatchOutcome.ofGames(List.of(BLACK_WINS_BY_FORFEIT, BLACK_WINS_BY_FORFEIT))
                            .isMeeting())
                    .isFalse();
            assertThat(MatchOutcome.ofGames(List.of(DOUBLE_FORFEIT, DOUBLE_FORFEIT))
                            .isMeeting())
                    .isFalse();
        }
    }
}
