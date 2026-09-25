package io.github.markdechamps.fideswiss.tournament;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CorrectionTest {

    private final Tournament afterRoundOne = TournamentMother.playNextRound(
            TournamentMother.individualSwiss(4, 9), GameOutcome.WHITE_WINS, GameOutcome.DRAW);

    @Nested
    class GivenACorrectedOutcome {

        @Test
        void changesTheScoresFromThenOn() {
            var corrected = afterRoundOne.withCorrectedOutcome(RoundNumber.FIRST, id(1), GameOutcome.BLACK_WINS);

            var history = TournamentHistory.of(corrected);
            assertThat(history.of(id(1)).score()).isEqualTo(Score.ZERO);
            assertThat(history.of(id(3)).score()).isEqualTo(Score.of("1"));
        }
    }

    @Nested
    class GivenCorrectedColours {

        @Test
        void swapsTheColoursOfTheBoardKeepingWhoWon() {
            var corrected = afterRoundOne.withCorrectedColours(RoundNumber.FIRST, BoardNumber.of(1));

            var board = corrected.rounds().getFirst().board(BoardNumber.of(1)).orElseThrow();
            assertThat(board.white()).isEqualTo(id(3));
            assertThat(board.outcome()).isEqualTo(GameOutcome.BLACK_WINS);
            assertThat(TournamentHistory.of(corrected).of(id(1)).score()).isEqualTo(Score.of("1"));
        }
    }

    @Nested
    class GivenACorrectedRating {

        @Test
        void renumbersBeforeTheFourthRoundIsPaired() {
            var corrected = afterRoundOne.withCorrectedRating(id(4), Rating.of(2600));

            // GHR 2.3: TPNs may be reassigned.
            assertThat(corrected.pairingNumbers().inOrder().getFirst()).isEqualTo(id(4));
        }

        @Test
        void keepsTheNumbersOnceTheFourthRoundHasBeenPaired() {
            var afterRoundFour = TournamentMother.individualSwiss(8, 9);
            for (var round = 1; round <= 4; round++) {
                afterRoundFour = TournamentMother.playNextRound(
                        afterRoundFour,
                        GameOutcome.WHITE_WINS,
                        GameOutcome.DRAW,
                        GameOutcome.BLACK_WINS,
                        GameOutcome.DRAW);
            }

            var corrected = afterRoundFour.withCorrectedRating(id(4), Rating.of(2600));

            // GHR 2.3: no modification of a TPN for this reason after the fourth round has been paired.
            assertThat(corrected.pairingNumbers().inOrder().getFirst()).isEqualTo(id(1));
            assertThat(corrected.participant(id(4)).rating()).isEqualTo(Rating.of(2600));
        }
    }
}
