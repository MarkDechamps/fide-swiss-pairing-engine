package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.DutchPlayerMother.player;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** C.04.3 (2026) Article 5: the colour rules, applied to one pair in descending priority. */
class DutchColourAllocationTest {

    private static final ColourAllocation WHITE_DRAWN = colours(InitialColour.white());

    @Nested
    class BothPreferences {

        @Test
        void areGrantedWhenTheyDiffer() {
            var pair = new Pair(player(1, "1", "W"), player(2, "1", "B"));

            // 5.2.1.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("2-1 by C.04.3 5.2.1");
        }

        @Test
        void areGrantedWhenOnlyOnePlayerHasOne() {
            var pair = new Pair(player(1, "1", "-"), player(2, "1", "W"));

            // 5.2.1 with 1.7.4: the player without a preference takes the other colour.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("1-2 by C.04.3 5.2.1");
        }
    }

    @Nested
    class TheStrongerPreference {

        @Test
        void isGrantedWhenBothWantTheSameColour() {
            var pair = new Pair(player(1, "1", "BW"), player(2, "1", "WBW"));

            // 5.2.2: 2's strong preference for Black beats 1's mild one.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("1-2 by C.04.3 5.2.2");
        }

        @Test
        void goesToTheWiderColourDifferenceWhenBothAreAbsolute() {
            var pair = new Pair(player(1, "3", "WW"), player(2, "3", "BWW"));

            // 5.2.2: two absolute preferences (only topscorers may meet so) go to the wider difference, +2 over +1.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("2-1 by C.04.3 5.2.2");
        }
    }

    @Nested
    class Alternation {

        @Test
        void followsTheMostRecentRoundInWhichTheColoursDiffered() {
            var pair = new Pair(player(1, "2", "WBBW"), player(2, "2", "BWBW"));

            // 5.2.3: rounds 4 and 3 were the same for both; in round 2, 1 had Black and 2 had White.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("1-2 by C.04.3 5.2.3");
        }

        @Test
        void alignsTheLatestPlayedGamesSoUnplayedRoundsCountAsTheEarliest() {
            var pair = new Pair(player(1, "2", "--WB"), player(2, "2", "BWWB"));

            // 5.2.3 with GHR 3.4: "uuWB" matches "BWWB" in every round both played, so 5.2.4 decides.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("1-2 by C.04.3 5.2.4");
        }
    }

    @Nested
    class TheHigherRankedPlayer {

        @Test
        void getsItsOwnPreference() {
            var pair = new Pair(player(1, "1", "BW"), player(2, "1", "BW"));

            // 5.2.4: the preferred colour of the higher-ranked, not White ([ANN p.46]).
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("2-1 by C.04.3 5.2.4");
        }

        @Test
        void isRankedByScoreBeforePairingNumber() {
            var pair = new Pair(player(1, "1", "BW"), player(2, "1.5", "BW"));

            // 5.2.4 with 1.2: 2 is higher-ranked on score and gets its mild preference for Black.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("1-2 by C.04.3 5.2.4");
        }

        @Test
        void takesTheInitialColourWithAnOddPairingNumber() {
            var pair = new Pair(player(1, "0"), player(2, "0"));

            // 5.2.5.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("1-2 by C.04.3 5.2.5");
            assertThat(describe(colours(InitialColour.black()).allocate(pair))).isEqualTo("2-1 by C.04.3 5.2.5");
        }

        @Test
        void takesTheOppositeColourWithAnEvenPairingNumber() {
            var pair = new Pair(player(2, "1"), player(3, "0"));

            // 5.2.5.
            assertThat(describe(WHITE_DRAWN.allocate(pair))).isEqualTo("3-2 by C.04.3 5.2.5");
        }
    }

    private static ColourAllocation colours(InitialColour initialColour) {
        return new ColourAllocation(
                new RoundToPair(RoundNumber.of(5), NumberOfRounds.of(9), initialColour, Points.of(1)));
    }

    private static String describe(AllocatedPair pair) {
        return pair.white().pairingNumber().value() + "-"
                + pair.black().pairingNumber().value() + " by " + pair.article();
    }
}
