package io.github.markdechamps.fideswiss.tournament;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbsenceTest {

    @Nested
    class GivenARequestedBye {

        @Test
        void leavesTheParticipantUnpairedAndUnnumbered() {
            var tournament =
                    TournamentMother.individualSwiss(5, 3).requestBye(id(3), RoundNumber.FIRST, RequestedBye.half());

            var pairing = tournament.pairNextRound();

            // GHR 3.3; ADR 0006: 3 is not taken into account, so 4 and 5 hold Pairing Numbers 3 and 4.
            assertThatPairing(pairing).hasBoards("1-4", "5-2").givesNoPairingAllocatedBye();
            assertThat(pairing.unpaired()).isEqualTo(Map.of(id(3), Bye.HALF_POINT));
            assertThat(pairing.pairingNumbers().of(id(3))).isEmpty();
        }

        @Test
        void scoresTheByeWhenTheRoundIsRecorded() {
            var tournament =
                    TournamentMother.individualSwiss(5, 3).requestBye(id(3), RoundNumber.FIRST, RequestedBye.half());

            var round = tournament
                    .pairNextRound()
                    .completedWith(Map.of(BoardNumber.of(1), GameOutcome.DRAW, BoardNumber.of(2), GameOutcome.DRAW));

            assertThat(round.byeOf(id(3))).contains(Bye.HALF_POINT);
        }

        @Test
        void mayNotBeRequestedForARoundAlreadyRecorded() {
            var tournament = TournamentMother.playNextRound(TournamentMother.individualSwiss(2, 3), GameOutcome.DRAW);

            assertThatThrownBy(() -> tournament.requestBye(id(1), RoundNumber.FIRST, RequestedBye.zero()))
                    .isInstanceOf(InvalidTournamentException.class);
        }
    }

    @Nested
    class GivenAWithdrawal {

        @Test
        void noLongerPairsTheParticipantFromThatRoundOn() {
            var tournament = TournamentMother.individualSwiss(5, 3).withdraw(id(5), RoundNumber.FIRST);

            var pairing = tournament.pairNextRound();

            // GHR 3.2.
            assertThatPairing(pairing).hasBoards("1-3", "4-2").givesNoPairingAllocatedBye();
            assertThat(pairing.unpaired()).isEqualTo(Map.of(id(5), Bye.WITHDRAWN));
        }
    }
}
