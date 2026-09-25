package io.github.markdechamps.fideswiss.pairing;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PairingCheckTest {

    @Nested
    class GivenTheSystemsOwnPairing {

        @Test
        void isLegalAndTheSystemPairing() {
            var tournament = TournamentMother.individualSwiss(4, 3);
            var proposed = ProposedPairing.of(tournament.pairNextRound());

            var check = tournament.check(proposed);

            assertThat(check.isLegal()).isTrue();
            assertThat(check.isSystemPairing()).isTrue();
            assertThat(check.differences()).isEmpty();
        }
    }

    @Nested
    class GivenAnotherLegalPairing {

        @Test
        void isLegalButNotTheSystemPairing() {
            var tournament = TournamentMother.individualSwiss(4, 3);

            var check = tournament.check(proposal(List.of("1-2", "3-4"), Optional.empty()));

            assertThat(check.isLegal()).isTrue();
            assertThat(check.isSystemPairing()).isFalse();
            assertThat(check.differences()).isNotEmpty();
        }
    }

    @Nested
    class GivenARematch {

        @Test
        void citesTheBasicRuleThatForbidsIt() {
            var tournament = afterRoundOne();

            var check = tournament.check(proposal(List.of("3-1", "2-4"), Optional.empty()));

            assertThat(check.isLegal()).isFalse();
            assertThat(check.violations()).extracting(Violation::article).contains("C.04.1 Art. 2");
            assertThat(check.violations().getFirst().participants()).containsExactlyInAnyOrder(id(1), id(3));
        }
    }

    @Nested
    class GivenAnIncompletePairing {

        @Test
        void reportsTheParticipantsLeftOut() {
            var tournament = TournamentMother.individualSwiss(4, 3);

            var check = tournament.check(proposal(List.of("1-3"), Optional.empty()));

            assertThat(check.isLegal()).isFalse();
            assertThat(check.violations())
                    .flatExtracting(Violation::participants)
                    .containsExactlyInAnyOrder(id(2), id(4));
        }
    }

    @Nested
    class GivenASecondPairingAllocatedBye {

        @Test
        void citesTheBasicRuleThatForbidsIt() {
            var tournament = TournamentMother.playNextRound(
                    TournamentMother.individualSwiss(5, 3), GameOutcome.DRAW, GameOutcome.DRAW);

            var check = tournament.check(proposal(List.of("1-2", "3-4"), Optional.of("5")));

            assertThat(check.violations()).extracting(Violation::article).contains("C.04.1 Art. 4");
        }
    }

    private static Tournament afterRoundOne() {
        return TournamentMother.playNextRound(
                TournamentMother.individualSwiss(4, 3), GameOutcome.WHITE_WINS, GameOutcome.WHITE_WINS);
    }

    private static ProposedPairing proposal(List<String> boards, Optional<String> bye) {
        return ProposedPairing.of(
                boards.stream()
                        .map(board -> board.split("-"))
                        .map(ids -> new ProposedPairing.ProposedBoard(
                                id(Integer.parseInt(ids[0])), id(Integer.parseInt(ids[1]))))
                        .toList(),
                bye.map(value -> id(Integer.parseInt(value))));
    }
}
