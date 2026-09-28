package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.DutchPlayerMother.player;
import static io.github.markdechamps.fideswiss.dutch.FloatDirection.DOWN;
import static io.github.markdechamps.fideswiss.dutch.FloatDirection.NONE;
import static io.github.markdechamps.fideswiss.dutch.FloatDirection.UP;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * C.04.3 (2026) Articles 2.3 and 2.4: how one candidate fails each of [C5]–[C21]. The pairings that follow from
 * them are in {@link DutchPairingCriteriaTest}.
 */
class DutchQualityCriteriaTest {

    private static final RoundToPair SIXTH_OF_NINE = round(6, 9);
    private static final RoundToPair FINAL_ROUND = round(6, 6);

    @Test
    void comeInTheirPriorityOrder() {
        // 2.3.1, 2.4.1-2.4.16; 3.8.1 compares candidates in this order.
        assertThat(Dutch2026Criteria.inPriorityOrder())
                .extracting(CandidateCriterion::article)
                .containsExactly(
                        "C5", "C6", "C7", "C8", "C9", "C10", "C11", "C12", "C13", "C14", "C15", "C16", "C17", "C18",
                        "C19", "C20", "C21");
    }

    @Nested
    class Downfloaters {

        private final Player first = player(1, "2");
        private final Player second = player(2, "1.5");
        private final Player third = player(3, "1.5");
        private final Player fourth = player(4, "1.5");
        private final Bracket bracket = new Bracket(List.of(first), List.of(second, third, fourth));

        @Test
        void areCountedByC6() {
            var candidate = new Candidate(List.of(new Pair(first, second)), List.of(third, fourth));

            // 2.4.1: minimise the number of downfloaters.
            assertThat(failure("C6", bracket, candidate)).hasToString("[2]");
        }

        @Test
        void haveTheirScoresComparedInDescendingOrderByC7() {
            var candidate = new Candidate(List.of(new Pair(third, second)), List.of(first, fourth));

            // 2.4.2: "the scores of the downfloaters, taken in descending order".
            assertThat(failure("C7", bracket, candidate)).hasToString("[2, 1.5]");
            assertThat(failure("C7", bracket, candidate))
                    .isGreaterThan(failure("C7", bracket, new Candidate(List.of(), List.of(third, fourth))));
        }
    }

    @Nested
    class UnplayedGamesOfThePairingAllocatedByeAssignee {

        private final Player paired = player(1, "1", "WB");
        private final Player opponent = player(2, "1", "BW");
        private final Player absentTwice = player(3, "1", "W-B-");
        private final Bracket bracket = new Bracket(List.of(), List.of(paired, opponent, absentTwice));
        private final Candidate candidate = new Candidate(List.of(new Pair(paired, opponent)), List.of(absentTwice));

        @Test
        void areCountedByC9WhenTheOneDownfloaterWillReceiveTheBye() {
            var lookahead = new FixedLookahead(Optional.of(absentTwice));

            // 2.4.4: minimise the unplayed games of the PAB assignee.
            assertThat(failure("C9", bracket, candidate, SIXTH_OF_NINE, lookahead))
                    .hasToString("[2]");
        }

        @Test
        void doNotCountWhenSomeoneBelowWillReceiveTheBye() {
            var lookahead = new FixedLookahead(Optional.of(player(9, "0")));

            // The note to [C9], read as: the one downfloater is bound to receive the PAB (a documented reading).
            assertThat(failure("C9", bracket, candidate, SIXTH_OF_NINE, lookahead))
                    .isEqualTo(Failure.NONE);
        }

        @Test
        void doNotCountWhenMoreThanOnePlayerFloats() {
            var twoFloat = new Candidate(List.of(), List.of(opponent, absentTwice));
            var lookahead = new FixedLookahead(Optional.of(absentTwice));

            // The note to [C9]: "brackets that downfloat exactly one player".
            assertThat(failure("C9", bracket, twoFloat, SIXTH_OF_NINE, lookahead))
                    .isEqualTo(Failure.NONE);
        }
    }

    @Nested
    class TopscorersColours {

        @Test
        void countAColourDifferenceBeyondTwoInC10() {
            var topscorer = player(1, "3.5", "WBWW");
            var widerTopscorer = player(2, "3.5", "WWWBW");
            var bracket = new Bracket(List.of(), List.of(topscorer, widerTopscorer));
            var candidate = new Candidate(List.of(new Pair(topscorer, widerTopscorer)), List.of());

            // 2.4.5: 5.2.2 gives 2 (+3) Black, so 1 goes from +2 to +3.
            assertThat(failure("C10", bracket, candidate, FINAL_ROUND)).hasToString("[1]");
        }

        @Test
        void countTheSameColourAThirdTimeInARowInC11() {
            var topscorer = player(1, "3.5", "BWW");
            var widerTopscorer = player(2, "3.5", "WWBWW");
            var bracket = new Bracket(List.of(), List.of(topscorer, widerTopscorer));
            var candidate = new Candidate(List.of(new Pair(topscorer, widerTopscorer)), List.of());

            // 2.4.6: 1 gets White a third time in a row, but its difference only reaches +2 ([C10]).
            assertThat(failure("C11", bracket, candidate, FINAL_ROUND)).hasToString("[1]");
            assertThat(failure("C10", bracket, candidate, FINAL_ROUND)).hasToString("[0]");
        }

        @Test
        void countNothingBeforeTheFinalRound() {
            var leader = player(1, "3.5", "WBWW");
            var chaser = player(2, "3.5", "WWWBW");
            var bracket = new Bracket(List.of(), List.of(leader, chaser));
            var candidate = new Candidate(List.of(new Pair(leader, chaser)), List.of());

            // [C10], [C11] with 1.8: only the final round has topscorers.
            assertThat(failure("C10", bracket, candidate, SIXTH_OF_NINE)).hasToString("[0]");
            assertThat(failure("C11", bracket, candidate, SIXTH_OF_NINE)).hasToString("[0]");
        }
    }

    @Nested
    class ColourPreferences {

        @Test
        void countEveryPlayerNotGettingItsPreferenceInC12() {
            var mild = player(1, "1", "WB");
            var strong = player(2, "1", "BWB");
            var bracket = new Bracket(List.of(), List.of(mild, strong));
            var candidate = new Candidate(List.of(new Pair(mild, strong)), List.of());

            // 2.4.7, 2.4.8: 5.2.2 grants 2's strong preference for White, so only 1's mild one fails.
            assertThat(failure("C12", bracket, candidate)).hasToString("[1]");
            assertThat(failure("C13", bracket, candidate)).hasToString("[0]");
        }

        @Test
        void countStrongPreferencesNotGrantedAgainInC13() {
            var strong = player(1, "1", "B");
            var alsoStrong = player(2, "1", "BWB");
            var bracket = new Bracket(List.of(), List.of(strong, alsoStrong));
            var candidate = new Candidate(List.of(new Pair(strong, alsoStrong)), List.of());

            // 2.4.8: both strongly prefer White; 5.2.4 gives it to 1.
            assertThat(failure("C12", bracket, candidate)).hasToString("[1]");
            assertThat(failure("C13", bracket, candidate)).hasToString("[1]");
        }
    }

    @Nested
    class Floats {

        @Test
        void countResidentDownfloatersWhoDownfloatedOneOrTwoRoundsBefore() {
            var first = player(1, "2");
            var second = player(2, "2");
            var lastRound = player(3, "2", "", List.of(NONE, DOWN));
            var twoRoundsAgo = player(4, "2", "", List.of(DOWN, NONE));
            var bracket = new Bracket(List.of(), List.of(first, second, lastRound, twoRoundsAgo));
            var candidate = new Candidate(List.of(new Pair(first, second)), List.of(lastRound, twoRoundsAgo));

            // 2.4.9 [C14] and 2.4.11 [C16].
            assertThat(failure("C14", bracket, candidate)).hasToString("[1]");
            assertThat(failure("C16", bracket, candidate)).hasToString("[1]");
        }

        @Test
        void doNotCountAnMdpFloatingOnAsAResidentDownfloater() {
            var mdp = player(1, "3", "", List.of(NONE, DOWN));
            var first = player(2, "2");
            var second = player(3, "2");
            var bracket = new Bracket(List.of(mdp), List.of(first, second));
            var candidate = new Candidate(List.of(new Pair(first, second)), List.of(mdp));

            // 2.4.9: "resident downfloaters" only.
            assertThat(failure("C14", bracket, candidate)).hasToString("[0]");
        }

        @Test
        void countMdpOpponentsWhoUpfloatedOneOrTwoRoundsBefore() {
            var mdp = player(1, "3");
            var otherMdp = player(2, "3");
            var lastRound = player(3, "2", "", List.of(NONE, UP));
            var twoRoundsAgo = player(4, "2", "", List.of(UP, NONE));
            var bracket = new Bracket(List.of(mdp, otherMdp), List.of(lastRound, twoRoundsAgo));
            var candidate =
                    new Candidate(List.of(new Pair(mdp, lastRound), new Pair(otherMdp, twoRoundsAgo)), List.of());

            // 2.4.10 [C15] and 2.4.12 [C17].
            assertThat(failure("C15", bracket, candidate)).hasToString("[1]");
            assertThat(failure("C17", bracket, candidate)).hasToString("[1]");
        }
    }

    @Nested
    class ScoreDifferences {

        @Test
        void ofMdpsWhoDownfloatedAreComparedInC18AndC20() {
            var lastRound = player(1, "3", "", List.of(NONE, DOWN));
            var twoRoundsAgo = player(2, "2.5", "", List.of(DOWN, NONE));
            var resident = player(3, "2");
            var otherResident = player(4, "2");
            var bracket = new Bracket(List.of(lastRound, twoRoundsAgo), List.of(resident, otherResident));
            var candidate = new Candidate(
                    List.of(new Pair(lastRound, resident), new Pair(twoRoundsAgo, otherResident)), List.of());

            // 2.4.13 [C18] and 2.4.15 [C20]: the MDP's score minus its opponent's.
            assertThat(failure("C18", bracket, candidate)).hasToString("[1]");
            assertThat(failure("C20", bracket, candidate)).hasToString("[0.5]");
        }

        @Test
        void ofAnMdpFloatingOnExceedAnyResidentsAndGrowWithItsScore() {
            var floatingOn = player(1, "3", "", List.of(NONE, DOWN));
            var first = player(2, "2");
            var second = player(3, "2");
            var bracket = new Bracket(List.of(floatingOn), List.of(first, second));
            var candidate = new Candidate(List.of(new Pair(first, second)), List.of(floatingOn));

            // [C18], the documented reading after [ANN p.31]: a Limbo MDP's difference is greater than any
            // resident's and grows with its own score.
            assertThat(failure("C18", bracket, candidate)).hasToString("[1003]");
        }

        @Test
        void ofMdpOpponentsWhoUpfloatedAreComparedInC19AndC21() {
            var mdp = player(1, "3");
            var otherMdp = player(2, "3");
            var lastRound = player(3, "2", "", List.of(NONE, UP));
            var twoRoundsAgo = player(4, "1.5", "", List.of(UP, NONE));
            var bracket = new Bracket(List.of(mdp, otherMdp), List.of(lastRound, twoRoundsAgo));
            var candidate =
                    new Candidate(List.of(new Pair(mdp, lastRound), new Pair(otherMdp, twoRoundsAgo)), List.of());

            // 2.4.14 [C19] and 2.4.16 [C21].
            assertThat(failure("C19", bracket, candidate)).hasToString("[1]");
            assertThat(failure("C21", bracket, candidate)).hasToString("[1.5]");
        }
    }

    private record FixedLookahead(Optional<Player> assignee) implements Lookahead {

        static final FixedLookahead NO_BYE = new FixedLookahead(Optional.empty());

        @Override
        public boolean isLastBracket() {
            return false;
        }

        @Override
        public Optional<Player> pairingAllocatedByeAssignee(List<Player> downfloaters) {
            return assignee;
        }

        @Override
        public Failure followingBracketOutcome(List<Player> downfloaters) {
            return Failure.NONE;
        }
    }

    private static Failure failure(String article, Bracket bracket, Candidate candidate) {
        return failure(article, bracket, candidate, SIXTH_OF_NINE);
    }

    private static Failure failure(String article, Bracket bracket, Candidate candidate, RoundToPair round) {
        return failure(article, bracket, candidate, round, FixedLookahead.NO_BYE);
    }

    private static Failure failure(
            String article, Bracket bracket, Candidate candidate, RoundToPair round, Lookahead lookahead) {
        var assessment = new CandidateAssessment(candidate, bracket, round, lookahead, new ColourAllocation(round));
        return Dutch2026Criteria.inPriorityOrder().stream()
                .filter(criterion -> criterion.article().equals(article))
                .findFirst()
                .orElseThrow()
                .failureOf(assessment);
    }

    private static RoundToPair round(int round, int of) {
        return new RoundToPair(RoundNumber.of(round), NumberOfRounds.of(of), InitialColour.white(), Points.of(1));
    }
}
