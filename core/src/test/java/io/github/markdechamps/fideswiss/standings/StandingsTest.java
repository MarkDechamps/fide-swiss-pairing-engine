package io.github.markdechamps.fideswiss.standings;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StandingsTest {

    /** 1 beats 2, 3 draws 4; then 1 draws 3, 2 beats 4. Scores 1.5, 1, 1, 0.5; every Buchholz is 2. */
    private static CrossTable twoRounds(String tieBreaks) {
        return CrossTable.of(4, 2, tieBreaks).round("1-2 1-0", "3-4 ½").round("1-3 ½", "2-4 1-0");
    }

    @Nested
    class GivenDifferentScores {

        @Test
        void ranksByScoreFirst() {
            var standings = twoRounds("BH").standings();

            assertThat(standings.ranked())
                    .extracting(standing -> standing.participant().id())
                    .startsWith(id(1))
                    .endsWith(id(4));
            assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(1));
            assertThat(standings.compare(id(4), id(1)).decidedBy()).isInstanceOf(DecidingTieBreak.ByScore.class);
        }
    }

    @Nested
    class GivenATieOnScore {

        @Test
        void breaksItWithTheFirstTieBreakThatDiffers() {
            var standings = twoRounds("BH, SB").standings();

            // C.07 4.2: BH is 2 for both, SB separates them: 3 has ½·½ + ½·1½ = 1, 2 has 1·½ + 0 = ½.
            assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
            assertThat(standings.standing(id(2)).rank()).isEqualTo(Rank.of(3));
            var decided = (DecidingTieBreak.ByTieBreak)
                    standings.compare(id(2), id(3)).decidedBy();
            assertThat(decided.code().acronym()).isEqualTo("SB");
            assertThat(decided.higher().value()).contains(new BigDecimal("1"));
            assertThat(standings.standing(id(2)).decidedBy()).containsInstanceOf(DecidingTieBreak.ByTieBreak.class);
        }

        @Test
        void sharesTheRankWhenEveryTieBreakIsEqual() {
            var standings = twoRounds("BH").standings();

            // C.07 4.2: the library never draws lots.
            assertThat(standings.standing(id(2)).rank()).isEqualTo(Rank.of(2));
            assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
            assertThat(standings.standing(id(4)).rank()).isEqualTo(Rank.of(4));
            assertThat(standings.compare(id(2), id(3)).decidedBy()).isInstanceOf(DecidingTieBreak.SharedRank.class);
        }
    }

    @Nested
    class GivenAnEarlierRound {

        @Test
        void countsOnlyTheRoundsUpToIt() {
            var standings = twoRounds("BH").standingsAfter(1);

            assertThat(standings.afterRounds()).isEqualTo(1);
            assertThat(standings.standing(id(2)).score().toString()).isEqualTo("0");
        }
    }
}
