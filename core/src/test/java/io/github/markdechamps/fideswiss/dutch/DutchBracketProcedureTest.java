package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.DutchPairingCriteriaTest.bracketSteps;
import static io.github.markdechamps.fideswiss.dutch.RecordedRoundMother.withRound;
import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.TraceStep.BracketStep;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** C.04.3 (2026) Article 3 and 1.9: how one bracket is paired, and the round bracket by bracket. */
class DutchBracketProcedureTest {

    @Nested
    class GivenAHomogeneousBracket {

        @Test
        void acceptsTheFirstPerfectCandidate() {
            var tournament = withRound(TournamentMother.individualSwiss(6, 5), "1-4 ½", "2-5 ½", "3-6 ½");

            var pairing = tournament.pairNextRound();

            // 3.4.1: 5-6-4 is the first transposition to pass [C1] and it fails no criterion.
            assertThatPairing(pairing).hasBoards("5-1", "6-2", "4-3");
            assertThat(bracketSteps(pairing))
                    .singleElement()
                    .extracting(BracketStep::criteria)
                    .isEqualTo("");
        }

        @Test
        void floatsThePlayerLeftOverWhenItsSizeIsOdd() {
            var pairing = TournamentMother.individualSwiss(5, 3).pairNextRound();

            // 3.1.2: MaxPairs = 2; 3.2.2 S1 = 1, 2; 3.3.2: the unpaired 5 downfloats from the last bracket and
            // receives the PAB (1.9.1).
            assertThatPairing(pairing).hasBoards("1-3", "4-2").givesPairingAllocatedByeTo("5");
            assertThat(bracketSteps(pairing).getFirst().downfloaters()).containsExactly(id(5));
        }
    }

    @Nested
    class GivenAHeterogeneousBracket {

        @Test
        void pairsTheMdpFirstAndTheRemainderAsAHomogeneousBracket() {
            var tournament = withRound(TournamentMother.individualSwiss(6, 5), "1-4 1-0", "2-5 ½", "3-6 ½");

            var pairing = tournament.pairNextRound();

            // 1.4.1: 1 floats down alone and is an MDP of the ½ bracket. 3.2.2: S1 = 1, S2 = 2, 3, 5, 6. 3.7.2:
            // the MDP-Pairings 1-2 and 1-3 cannot grant both Black preferences ([C12]), so the third, 1-5, is
            // kept; 3.7.1: its remainder 2, 3, 6 (1.3.4) pairs 2-6 on its second transposition, and 3 floats.
            assertThatPairing(pairing).hasBoards("5-1", "6-2", "4-3");
            var halfPointBracket = bracketSteps(pairing).get(1);
            assertThat(halfPointBracket.movedDown()).containsExactly(id(1));
            assertThat(halfPointBracket.pairs()).containsExactly(List.of(id(1), id(5)), List.of(id(2), id(6)));
            assertThat(halfPointBracket.downfloaters()).containsExactly(id(3));
        }

        @Test
        void leavesAnMdpWhoCannotBePairedInTheLimboAndTakesTheNextMdpSet() {
            var tournament = TournamentMother.individualSwiss(6, 7);
            tournament = withRound(tournament, "1-4 1-0", "2-5 1-0", "3-6 1-0");
            tournament = withRound(tournament, "3-1 0-1", "2-6 1-0", "4-5 ½");
            tournament = withRound(tournament, "1-2 ½", "3-4 1-0", "5-6 ½");

            var pairing = tournament.pairNextRound();

            // 1 and 2 (2½) have met, so both float into the 2-point bracket of 3 alone. 3.1.3: M0 = 2, M1 = 1.
            // 4.4.2: the MDP set {1} has no transposition, as 1 has met 3; 3.7.3: the next set, {2}, pairs 2-3
            // and 1 stays in the Limbo (3.2.4), floating on to meet 5.
            assertThatPairing(pairing).hasBoards("2-3", "5-1", "6-4");
            var twoPointBracket = bracketSteps(pairing).get(1);
            assertThat(twoPointBracket.movedDown()).containsExactly(id(1), id(2));
            assertThat(twoPointBracket.pairs()).containsExactly(List.of(id(2), id(3)));
            assertThat(twoPointBracket.downfloaters()).containsExactly(id(1));
        }

        @Test
        void takesTheEarliestBestCandidateWhenNoneIsPerfect() {
            var tournament = TournamentMother.individualSwiss(6, 7);
            tournament = withRound(tournament, "1-3 ½", "2-4 ½", "5-6 ½");
            tournament = withRound(tournament, "5-1 ½", "6-2 ½", "3 ½bye", "4 ½bye");
            tournament = withRound(tournament, "1-6 1-0", "2-5 1-0", "3 ½bye", "4 ½bye");

            var pairing = tournament.pairNextRound();

            // 3.8.1: in the 1-point bracket, 3-5/4-6 and 3-6/4-5 both leave one White preference ungranted and
            // tie on every later criterion, so the earlier transposition wins.
            assertThatPairing(pairing).hasBoards("2-1", "3-5", "4-6");
            assertThat(bracketSteps(pairing).get(2).criteria()).startsWith("C12=[1] C13=[1]");
        }
    }

    @Test
    void pairsTheBracketsFromTheTopScoregroupDown() {
        var tournament = TournamentMother.individualSwiss(8, 5);
        tournament = withRound(tournament, "1-6 1-0", "2-7 1-0", "3-4 1-0", "5-8 ½");
        tournament = withRound(tournament, "7-1 ½", "6-2 ½", "5-3 ½", "4-8 1-0");

        var pairing = tournament.pairNextRound();

        // 1.9.2: the 1½, 1 and ½ brackets, in that order, each receiving the previous one's downfloater.
        assertThat(bracketSteps(pairing))
                .extracting(BracketStep::residents)
                .containsExactly(List.of(id(1), id(2), id(3)), List.of(id(4), id(5)), List.of(id(6), id(7), id(8)));
        assertThat(bracketSteps(pairing))
                .extracting(BracketStep::movedDown)
                .containsExactly(List.of(), List.of(id(2)), List.of(id(5)));
    }
}
