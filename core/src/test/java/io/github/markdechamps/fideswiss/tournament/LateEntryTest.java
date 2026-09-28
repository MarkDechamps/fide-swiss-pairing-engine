package io.github.markdechamps.fideswiss.tournament;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LateEntryTest {

    private static final Participant LATECOMER =
            Participant.of(ParticipantId.of("L"), Name.of("Latecomer"), Rating.of(2475));

    @Nested
    class GivenAParticipantEnteringForRoundTwo {

        private final Tournament afterRoundOne = TournamentMother.playNextRound(
                TournamentMother.individualSwiss(4, 5), GameOutcome.WHITE_WINS, GameOutcome.DRAW);

        @Test
        void isPairedAndNumberedFromItsFirstRound() {
            var tournament = afterRoundOne.enterLate(LATECOMER, RoundNumber.of(2));

            var pairing = tournament.pairNextRound();

            // GHR 2.4: ranked by strength, the latecomer (2475) takes #3 and everyone below moves down one.
            assertThat(pairing.pairingNumbers().inOrder()).containsExactly(id(1), id(2), LATECOMER.id(), id(3), id(4));
            assertThat(pairing.boards().size() * 2
                            + (pairing.pairingAllocatedBye().isPresent() ? 1 : 0))
                    .isEqualTo(5);
        }

        @Test
        void scoresNothingForTheMissedRoundsByDefault() {
            var tournament = afterRoundOne.enterLate(LATECOMER, RoundNumber.of(2));

            assertThat(TournamentHistory.of(tournament).of(LATECOMER.id()).score())
                    .isEqualTo(Score.ZERO);
        }

        @Test
        void scoresHalfPointByesForTheMissedRoundsWhenTheRulesSaySo() {
            var tournament = afterRoundOne.enterLate(LATECOMER, RoundNumber.of(2), MissedRounds.halfPointByes());

            assertThat(TournamentHistory.of(tournament).of(LATECOMER.id()).score())
                    .isEqualTo(Score.of("0.5"));
        }

        @Test
        void mayNotEnterForARoundAlreadyRecorded() {
            assertThatThrownBy(() -> afterRoundOne.enterLate(LATECOMER, RoundNumber.FIRST))
                    .isInstanceOf(InvalidTournamentException.class);
        }
    }

    @Nested
    class GivenAParticipantEnteringForALaterRound {

        @Test
        void isLeftOutOfEarlierRoundsAsNotYetEntered() {
            var tournament = TournamentMother.individualSwiss(4, 5).enterLate(LATECOMER, RoundNumber.of(3));

            var pairing = tournament.pairNextRound();

            assertThat(pairing.unpaired()).isEqualTo(Map.of(LATECOMER.id(), Bye.NOT_YET_ENTERED));
            assertThat(pairing.pairingNumbers().of(LATECOMER.id())).isEmpty();
        }
    }

    @Nested
    class GivenBakuAcceleration {

        @Test
        void addsALateEntryRankedAboveTheLastAcceleratedParticipantToTheAcceleratedGroup() {
            // C.04.7 1.3: 8 starters make a group of 4, ending with 4 (2460); the latecomer (2475) ranks above.
            var tournament = TournamentMother.playNextRound(
                            Tournament.of(
                                    Profiles.acceleratedOpen(NumberOfRounds.of(9)), TournamentMother.participants(8)),
                            GameOutcome.DRAW,
                            GameOutcome.DRAW,
                            GameOutcome.DRAW,
                            GameOutcome.DRAW)
                    .enterLate(LATECOMER, RoundNumber.of(2));

            assertThat(tournament.virtualPointsOf(LATECOMER.id(), RoundNumber.of(2)))
                    .isEqualTo(Points.of(1));
            assertThat(tournament.virtualPointsOf(id(4), RoundNumber.of(2))).isEqualTo(Points.of(1));
            assertThat(tournament.virtualPointsOf(id(5), RoundNumber.of(2))).isEqualTo(Points.ZERO);
        }
    }
}
