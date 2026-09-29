package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;

/**
 * Article 7, for board 1 of each match, decided for the pair's higher ranked team (3.2):
 *
 * <ol>
 *   <li>7.3: a colour that would break a limit for either team is not given, where the other is possible;
 *   <li>7.5.1, equalisation: with different colour differences, the team with the larger one gets Black;
 *   <li>7.5.2, 7.6, alternation: from the latest round in which the two had different colours, each gets the colour
 *       the other had then;
 *   <li>7.6: if they never had different colours, the higher ranked team alternates from its last colour (the lower
 *       ranked one, if only it has a colour);
 *   <li>7.2: with no colours at all, the higher ranked team gets the colour drawn by lot if its pairing number is
 *       odd, the other colour if it is even.
 * </ol>
 *
 * 7.7: rounds without a played match give no colour.
 */
final class ColourAllocation {

    private final InitialColour lot;
    private final int round;

    /** @param round the number of the round being paired */
    ColourAllocation(InitialColour lot, int round) {
        this.lot = lot;
        this.round = round;
    }

    Game allocate(Pair pair) {
        var higher = pair.higherRanked();
        var lower = pair.lowerRanked();
        var decision = colourOfHigher(higher, lower);
        return decision.colour() == Colour.WHITE
                ? new Game(higher, lower, decision.article())
                : new Game(lower, higher, decision.article());
    }

    private record Decision(Colour colour, String article) {}

    private Decision colourOfHigher(Team higher, Team lower) {
        var whiteFits = ColourLimits.fits(higher, lower, Colour.WHITE);
        var blackFits = ColourLimits.fits(higher, lower, Colour.BLACK);
        if (whiteFits != blackFits) {
            return new Decision(whiteFits ? Colour.WHITE : Colour.BLACK, "D.02 7.3");
        }
        if (higher.colourDifference() != lower.colourDifference()) {
            var colour = higher.colourDifference() > lower.colourDifference() ? Colour.BLACK : Colour.WHITE;
            return new Decision(colour, "D.02 7.5.1");
        }
        for (var earlier = round - 1; earlier >= 1; earlier--) {
            var own = higher.colourIn(earlier);
            var other = lower.colourIn(earlier);
            if (own.isPresent() && other.isPresent() && own.get() != other.get()) {
                return new Decision(other.get(), "D.02 7.6");
            }
        }
        if (higher.lastColour().isPresent()) {
            return new Decision(higher.lastColour().get().opposite(), "D.02 7.6");
        }
        if (lower.lastColour().isPresent()) {
            return new Decision(lower.lastColour().get(), "D.02 7.6");
        }
        return new Decision(lot.forPairingNumber(higher.pairingNumber()), "D.02 7.2");
    }
}
