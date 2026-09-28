package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.DutchPlayerMother.player;
import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** C.04.3 (2026) Article 1: the definitions the pairing procedure is written in. */
class DutchDefinitionsTest {

    @Nested
    class OrderAndBrackets {

        @Test
        void ranksPlayersByScoreThenByPairingNumber() {
            var players = List.of(player(1, "1"), player(2, "2"), player(3, "1"), player(4, "2"));

            var ranked = players.stream().sorted(PairingOrder.RANKING).toList();

            // 1.2.
            assertThat(ranked).extracting(Player::id).containsExactly(id(2), id(4), id(1), id(3));
        }

        @Test
        void groupsPlayersOfTheSameScoreIntoScoregroupsFromTheTopDown() {
            var ranked = List.of(player(2, "2"), player(4, "2"), player(1, "1"), player(3, "0"));

            var scoregroups = DutchProcedure.scoregroupsFromTheTop(ranked);

            // 1.3.1.
            assertThat(scoregroups)
                    .extracting(group -> group.stream().map(Player::id).toList())
                    .containsExactly(List.of(id(2), id(4)), List.of(id(1)), List.of(id(3)));
        }

        @Test
        void callsABracketHeterogeneousOnlyWhenPlayersMovedDownIntoIt() {
            var residents = List.of(player(2, "1"), player(3, "1"));

            // 1.3.2, 1.3.3.
            assertThat(new Bracket(List.of(), residents).isHomogeneous()).isTrue();
            assertThat(new Bracket(List.of(player(1, "2")), residents).isHomogeneous())
                    .isFalse();
        }
    }

    @Nested
    class Floats {

        private final Dutch2026FloatRule floats = new Dutch2026FloatRule();

        @Test
        void giveTheHigherScoredPlayerOfAGameADownfloatAndTheOtherAnUpfloat() {
            var game = new RoundRecord.Game(id(2), Colour.WHITE, GameResult.DRAW, Points.of("0.5"));

            // 1.4.2.
            assertThat(floats.floatOf(game, score("2"), score("1.5"), Points.ZERO))
                    .isEqualTo(FloatDirection.DOWN);
            assertThat(floats.floatOf(game, score("1.5"), score("2"), Points.ZERO))
                    .isEqualTo(FloatDirection.UP);
            assertThat(floats.floatOf(game, score("2"), score("2"), Points.ZERO))
                    .isEqualTo(FloatDirection.NONE);
        }

        @Test
        void giveADownfloatForThePairingAllocatedBye() {
            var bye = new RoundRecord.NoBoard(Bye.PAIRING_ALLOCATED, Points.of(1));

            // 1.4.3.
            assertThat(floats.floatOf(bye, score("0"), score("0"), Points.ZERO)).isEqualTo(FloatDirection.DOWN);
        }

        @Test
        void giveADownfloatForMoreThanALossWithoutPlaying() {
            var forfeitWin = new RoundRecord.Forfeit(id(2), GameResult.WIN, Points.of(1));
            var halfPointBye = new RoundRecord.NoBoard(Bye.HALF_POINT, Points.of("0.5"));

            // 1.4.3: "without playing in a round, scores more points than those rewarded for a loss".
            assertThat(floats.floatOf(forfeitWin, score("1"), score("1"), Points.ZERO))
                    .isEqualTo(FloatDirection.DOWN);
            assertThat(floats.floatOf(halfPointBye, score("1"), score("0"), Points.ZERO))
                    .isEqualTo(FloatDirection.DOWN);
        }

        @Test
        void giveNoOtherFloat() {
            var forfeitLoss = new RoundRecord.Forfeit(id(2), GameResult.LOSS, Points.ZERO);
            var zeroPointBye = new RoundRecord.NoBoard(Bye.ZERO_POINT, Points.ZERO);

            // 1.4.4: a forfeit loss against a lower-scored opponent is no game between different scores (1.4.2).
            assertThat(floats.floatOf(forfeitLoss, score("2"), score("1"), Points.ZERO))
                    .isEqualTo(FloatDirection.NONE);
            assertThat(floats.floatOf(zeroPointBye, score("1"), score("0"), Points.ZERO))
                    .isEqualTo(FloatDirection.NONE);
        }
    }

    @Nested
    class ColourPreferences {

        @Test
        void measureTheColourDifferenceAsWhitesMinusBlacks() {
            // 1.6.
            assertThat(ColourPreference.colourDifference(List.of(Colour.WHITE, Colour.WHITE, Colour.BLACK)))
                    .isEqualTo(1);
            assertThat(player(1, "0", "BBWB").colourDifference()).isEqualTo(-2);
        }

        @Test
        void areAbsoluteBeyondAColourDifferenceOfOne() {
            // 1.7.1: "greater than +1 or less than -1".
            assertThat(player(1, "0", "WBWW").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.BLACK, PreferenceStrength.ABSOLUTE));
            assertThat(player(1, "0", "BWBB").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.WHITE, PreferenceStrength.ABSOLUTE));
        }

        @Test
        void areAbsoluteAfterTheSameColourInTheTwoLatestRoundsPlayed() {
            // 1.7.1: "the two latest rounds they played"; the unplayed round drops out (GHR 3.4).
            assertThat(player(1, "0", "BWW").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.BLACK, PreferenceStrength.ABSOLUTE));
            assertThat(player(1, "0", "BW-W").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.BLACK, PreferenceStrength.ABSOLUTE));
        }

        @Test
        void areStrongAtAColourDifferenceOfOneForTheColourThatReducesIt() {
            // 1.7.2.
            assertThat(player(1, "0", "W").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.BLACK, PreferenceStrength.STRONG));
            assertThat(player(1, "0", "WBW").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.BLACK, PreferenceStrength.STRONG));
            assertThat(player(1, "0", "BWB").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.WHITE, PreferenceStrength.STRONG));
        }

        @Test
        void areMildAtAColourDifferenceOfZeroForTheColourThatAlternates() {
            // 1.7.3.
            assertThat(player(1, "0", "WB").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.WHITE, PreferenceStrength.MILD));
            assertThat(player(1, "0", "BWBW").colourPreference())
                    .isEqualTo(ColourPreference.of(Colour.BLACK, PreferenceStrength.MILD));
        }

        @Test
        void doNotExistBeforeTheFirstGamePlayed() {
            // 1.7.4: an unplayed round gives no colour.
            assertThat(player(1, "1", "").colourPreference()).isEqualTo(ColourPreference.NONE);
            assertThat(player(1, "1", "-").colourPreference()).isEqualTo(ColourPreference.NONE);
        }
    }

    @Nested
    class Topscorers {

        @Test
        void scoreOverHalfTheMaximumWhenTheFinalRoundIsPaired() {
            var finalRound =
                    new RoundToPair(RoundNumber.of(5), NumberOfRounds.of(5), InitialColour.white(), Points.of(1));

            // 1.8: four rounds played, so over 2 points.
            assertThat(finalRound.isTopscorer(player(1, "2.5"))).isTrue();
            assertThat(finalRound.isTopscorer(player(2, "2"))).isFalse();
        }

        @Test
        void doNotExistBeforeTheFinalRound() {
            var penultimate =
                    new RoundToPair(RoundNumber.of(4), NumberOfRounds.of(5), InitialColour.white(), Points.of(1));

            // 1.8: "when pairing the final round of the tournament".
            assertThat(penultimate.isTopscorer(player(1, "3"))).isFalse();
        }
    }

    private static PairingScore score(String points) {
        return new PairingScore(Points.of(points));
    }
}
