package io.github.markdechamps.fideswiss.standings;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** C.07 art. 16 on hand-built crosstables; every expected value is worked out in the comment beside it. */
class UnplayedRoundsTest {

    /**
     * 1 beats 2 and 3 beats 4; 1 takes a half-point bye (16.2.3, since it plays again), 2 beats 3, 4 gets the PAB;
     * 1 beats 3, 2 beats 4. Scores: 1 2½, 2 2, 3 1, 4 1.
     */
    private static CrossTable halfPointByeInRoundTwo(String tieBreaks, TieBreakEdition edition) {
        return CrossTable.of(4, 3, tieBreaks, edition)
                .round("1-2 1-0", "3-4 1-0")
                .round("1 HPB", "2-3 1-0", "4 PAB")
                .round("1-3 1-0", "2-4 1-0");
    }

    private static BigDecimal valueOf(Standings standings, int participant, int tieBreak) {
        return standings
                .standing(id(participant))
                .tieBreakValues()
                .get(tieBreak)
                .value()
                .orElseThrow();
    }

    @Nested
    class GivenAnOwnRequestedBye {

        @Test
        void scoresItAgainstADummyCappedAtADrawPerRound() {
            var standings = halfPointByeInRoundTwo("BH", TieBreakEdition.EDITION_2026_03)
                    .standings();

            // 16.4.2: the dummy scores min(2½, ½ × 3) = 1½, so BH = 2 + 1½ + 1.
            assertThat(valueOf(standings, 1, 0)).isEqualByComparingTo("4.5");
        }

        @Test
        void scoresTheDummyAtTheOwnScoreUnderTheEdition2024() {
            var standings = halfPointByeInRoundTwo("BH", TieBreakEdition.EDITION_2024_08)
                    .standings();

            // C.07 2024-08 16.4: the dummy scores 2½ uncapped, so BH = 2 + 2½ + 1.
            assertThat(valueOf(standings, 1, 0)).isEqualByComparingTo("5.5");
        }

        @Test
        void cutsTheVoluntaryUnplayedRoundBeforeTheLowestOpponent() {
            var standings = halfPointByeInRoundTwo("BH/C1", TieBreakEdition.EDITION_2026_03)
                    .standings();

            // 16.5.1: the bye's 1½ is cut instead of the lowest value, 1 (from 3), so BH/C1 = 2 + 1.
            assertThat(valueOf(standings, 1, 0)).isEqualByComparingTo("3");
            assertThat(standings.standing(id(1)).tieBreakValues().get(0).contributions())
                    .filteredOn(TieBreakContribution::cut)
                    .extracting(TieBreakContribution::counterpart)
                    .containsExactly("dummy");
        }

        @Test
        void multipliesTheDummyByThePointsAwardedInSonnebornBerger() {
            var standings = halfPointByeInRoundTwo("SB", TieBreakEdition.EDITION_2026_03)
                    .standings();

            // 9.1 with 16.4: 2 × 1 + 1½ × ½ + 1 × 1.
            assertThat(valueOf(standings, 1, 0)).isEqualByComparingTo("3.75");
        }

        @Test
        void countsAPairingAllocatedByeAgainstTheUncappedOwnScoreBelowTheCap() {
            var standings = halfPointByeInRoundTwo("BH", TieBreakEdition.EDITION_2026_03)
                    .standings();

            // 4: 3 (1) + dummy min(1, 1½) = 1 + 2 (2).
            assertThat(valueOf(standings, 4, 0)).isEqualByComparingTo("4");
        }
    }

    @Nested
    class GivenAWithdrawal {

        @Test
        void countsTheRoundsAfterItAsDrawsForTheOpponents() {
            // 1 beats 2, 3 draws 4; 1 beats 3, 2 beats 4; 4 withdraws, 3 gets the PAB, 1 draws 2.
            var standings = CrossTable.of(4, 3, "BH")
                    .round("1-2 1-0", "3-4 ½")
                    .round("1-3 1-0", "2-4 1-0")
                    .round("4 OUT", "3 PAB", "1-2 ½")
                    .standings();

            // 16.3.2: 4's Adjusted Score is ½ + 0 + ½ = 1; 3's BH = 1 + 1's 2½ + dummy min(1½, 1½).
            assertThat(valueOf(standings, 3, 0)).isEqualByComparingTo("5");
        }
    }

    @Nested
    class GivenAForfeitWin {

        @Test
        void capsTheDummyAtTheScheduledOpponentsAdjustedScore() {
            // 1 wins by forfeit against 2 and 3 beats 4; then 1 beats 3 and 4 beats 2.
            var table = CrossTable.of(4, 2, "BH").round("1-2 +-", "3-4 1-0").round("1-3 1-0", "4-2 1-0");

            // 16.4.1: the dummy scores min(2, 2's Adjusted Score 0) = 0, so BH = 0 + 1.
            assertThat(valueOf(table.standings(), 1, 0)).isEqualByComparingTo("1");
        }

        @Test
        void countsTheScheduledOpponentWithSlashP() {
            var standings = CrossTable.of(4, 2, "BH/P")
                    .round("1-2 +-", "3-4 1-0")
                    .round("1-3 1-0", "2-4 1-0")
                    .standings();

            // Tie-break interpretation rulings #3: 2's Adjusted Score 1 counts, so BH = 1 + 1.
            assertThat(valueOf(standings, 1, 0)).isEqualByComparingTo("2");
        }
    }

    @Nested
    class GivenForeBuchholz {

        @Test
        void drawsEveryPairedGameOfTheFinalRound() {
            var table = CrossTable.of(6, 2, "FB, BH")
                    .round("1-2 1-0", "3-4 1-0", "5-6 1-0")
                    .round("1-3 1-0", "2-5 1-0", "4-6 1-0");

            // 8.3: with round 2 drawn, 4 has ½ and 1 has 1½, so FB = 2 where BH = 1 + 2 = 3.
            var standings = table.standings();
            assertThat(valueOf(standings, 3, 0)).isEqualByComparingTo("2");
            assertThat(valueOf(standings, 3, 1)).isEqualByComparingTo("3");
        }
    }
}
