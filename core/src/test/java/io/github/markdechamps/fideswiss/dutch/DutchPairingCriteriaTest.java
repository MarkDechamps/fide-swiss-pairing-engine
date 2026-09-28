package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.RecordedRoundMother.withRound;
import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing.ProposedBoard;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep.BracketStep;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * C.04.3 (2026) Article 2 through whole rounds: pairings whose shape only the cited criterion explains, each
 * derived by hand from the text. How each criterion scores one candidate is in {@link DutchQualityCriteriaTest}.
 */
class DutchPairingCriteriaTest {

    @Nested
    class TheAbsoluteCriteria {

        @Test
        void neverPairTwoPlayersWhoHaveMet() {
            var tournament = withRound(TournamentMother.individualSwiss(6, 5), "1-4 ½", "2-5 ½", "3-6 ½");

            var pairing = tournament.pairNextRound();

            // [C1]: S1 = 1-3, S2 = 4-6; transpositions 4-5-6, 4-6-5 and 5-4-6 each repeat a game, so 5-6-4 is
            // the first candidate (4.2.2) and it grants every preference (5.2.1).
            assertThatPairing(pairing).hasBoards("5-1", "6-2", "4-3");
        }

        @Test
        void giveThePairingAllocatedByeOnlyToAPlayerWhoHasNotHadOne() {
            var tournament = TournamentMother.individualSwiss(3, 4);
            tournament = withRound(tournament, "1-2 1-0", "3 PAB");
            tournament = withRound(tournament, "3-1 0-1", "2 PAB");

            var pairing = tournament.pairNextRound();

            // [C2]: 2 and 3 have had the PAB, so the leader gets it, the lowest score [C5] can still reach.
            assertThatPairing(pairing).hasBoards("2-3").givesPairingAllocatedByeTo("1");
        }

        @Test
        void neverPairTwoNonTopscorersWithTheSameAbsolutePreference() {
            var tournament = TournamentMother.individualSwiss(6, 5);
            tournament = withRound(tournament, "1-4 ½", "2-5 ½", "3-6 ½");
            tournament = withRound(tournament, "1-5 ½", "2-6 ½", "3-4 ½");

            var pairing = tournament.pairNextRound();

            // [C3]: 1-3 have had White twice and 4-6 Black twice (1.7.1), so only 1-6, 2-4 and 3-5 are left.
            assertThatPairing(pairing).hasBoards("6-1", "4-2", "5-3");
            assertThat(tournament.check(proposed(pairing)).violations()).isEmpty();
        }

        @Test
        void leaveTheChiefArbiterToDecideWhenNoPairingMeetsThem() {
            var tournament = TournamentMother.individualSwiss(4, 5);
            tournament = withRound(tournament, "1-3 ½", "2-4 ½");
            var stuck = withRound(tournament, "1-4 ½", "2-3 ½");

            // 1.9.3: 1-2 and 3-4 are the only games left, and [C3] forbids both.
            assertThatThrownBy(stuck::pairNextRound)
                    .isInstanceOf(NoLegalPairingException.class)
                    .extracting(exception -> ((NoLegalPairingException) exception)
                            .problems()
                            .getFirst()
                            .article())
                    .isEqualTo(Optional.of("C.04.3 1.9.3"));
        }
    }

    @Nested
    class TheCompletionCriterion {

        @Test
        void floatsABracketThatCouldPairItselfWhenTheRoundCouldNotBeCompletedOtherwise() {
            var tournament = TournamentMother.individualSwiss(6, 7);
            tournament = withRound(tournament, "1-3 ½", "2-4 ½", "5-6 ½");
            tournament = withRound(tournament, "5-1 ½", "6-2 ½", "3 ½bye", "4 ½bye");
            tournament = withRound(tournament, "1-6 1-0", "2-5 1-0", "3 ½bye", "4 ½bye");

            var pairing = tournament.pairNextRound();

            // [C4]: 3-4 (1½) could meet, but that would leave 5 and 6, who have met; so both float (3.3.2) and
            // 3-5, 4-6 is the first MDP-Pairing (4.2.2), no candidate beating it on [C12]-[C21] (3.8.1).
            assertThatPairing(pairing).hasBoards("2-1", "3-5", "4-6");
            assertThat(bracketSteps(pairing).get(1).downfloaters())
                    .extracting(Object::toString)
                    .containsExactly("3", "4");
        }
    }

    @Nested
    class TheByeCriteria {

        @Test
        void giveTheByeToThePlayerWithFewerUnplayedGames() {
            var tournament = withRound(TournamentMother.individualSwiss(7, 5), "1-2 1-0", "4-3 0-1", "5-6 +-", "7 PAB");

            var pairing = tournament.pairNextRound();

            // [C5]: the PAB goes to a zero. [C9]: 6 lost by forfeit, so 2-6 (the second transposition) leaves 4,
            // who played; the first, 2-4, would have left 6, and no colour or float criterion tells them apart.
            assertThatPairing(pairing).hasBoards("5-1", "3-7", "2-6").givesPairingAllocatedByeTo("4");
        }
    }

    @Nested
    class TheQualityCriteria {

        @Test
        void floatThePlayerWithWhomTheFollowingBracketPairsBest() {
            var tournament = TournamentMother.individualSwiss(8, 5);
            tournament = withRound(tournament, "1-6 1-0", "2-7 1-0", "3-4 1-0", "5-8 ½");
            tournament = withRound(tournament, "7-1 ½", "6-2 ½", "5-3 ½", "4-8 1-0");

            var pairing = tournament.pairNextRound();

            // [C8]: in the 1½ bracket the first candidate, 1-2, would float 3, who has met both 4 and 5, so the
            // 1-point bracket would float 3 on (1½) instead of a resident (1). 1-3 floats 2, who meets 4.
            assertThatPairing(pairing).hasBoards("1-3", "2-4", "6-5", "8-7");
        }

        @Test
        void grantColourPreferencesOverTheFirstTransposition() {
            var tournament = withRound(TournamentMother.individualSwiss(6, 5), "1-4 ½", "5-2 ½", "6-3 ½");

            var pairing = tournament.pairNextRound();

            // [C12], [C13]: 1 wants Black and in S2 only 4 (met) wants White, so no transposition is perfect. The
            // first exchange (3<->4, 4.3.2) gives S1 = 1, 2, 4 and S2 = 3, 5, 6, and 1-3, 2-6, 4-5 grants all.
            assertThatPairing(pairing).hasBoards("3-1", "2-6", "4-5");
        }
    }

    static List<BracketStep> bracketSteps(RoundPairing pairing) {
        return pairing.trace().steps().stream()
                .filter(BracketStep.class::isInstance)
                .map(BracketStep.class::cast)
                .toList();
    }

    private static ProposedPairing proposed(RoundPairing pairing) {
        return ProposedPairing.of(
                pairing.boards().stream()
                        .map(board -> new ProposedBoard(board.white(), board.black()))
                        .toList(),
                pairing.pairingAllocatedBye());
    }
}
