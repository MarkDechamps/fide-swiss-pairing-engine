package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Optional;

/** Dutch Article 5.2: the colour rules, applied in descending priority. */
final class ColourAllocation {

    private final RoundToPair round;

    ColourAllocation(RoundToPair round) {
        this.round = round;
    }

    AllocatedPair allocate(Pair pair) {
        var higher = pair.higherRanked();
        var lower = pair.lowerRanked();
        var decision = colourOfHigherRanked(higher, lower);
        return decision.colour() == Colour.WHITE
                ? new AllocatedPair(higher, lower, decision.article())
                : new AllocatedPair(lower, higher, decision.article());
    }

    private record Decision(Colour colour, String article) {}

    private Decision colourOfHigherRanked(Player higher, Player lower) {
        return grantBothPreferences(higher, lower)
                .map(colour -> new Decision(colour, "C.04.3 5.2.1"))
                .or(() -> grantStrongerPreference(higher, lower).map(colour -> new Decision(colour, "C.04.3 5.2.2")))
                .or(() -> alternateToMostRecentDifference(higher, lower)
                        .map(colour -> new Decision(colour, "C.04.3 5.2.3")))
                .or(() -> higher.colourPreference().colour().map(colour -> new Decision(colour, "C.04.3 5.2.4")))
                .orElseGet(() ->
                        new Decision(round.initialColour().forPairingNumber(higher.pairingNumber()), "C.04.3 5.2.5"));
    }

    /** 5.2.1, with 1.7.4: a player without a preference takes the other colour. */
    private static Optional<Colour> grantBothPreferences(Player higher, Player lower) {
        var higherWants = higher.colourPreference().colour();
        var lowerWants = lower.colourPreference().colour();
        if (higherWants.isPresent() && lowerWants.isPresent()) {
            return higherWants.equals(lowerWants) ? Optional.empty() : higherWants;
        }
        return higherWants.or(() -> lowerWants.map(Colour::opposite));
    }

    /** 5.2.2; two absolute preferences (topscorers only) go to the wider colour difference. */
    private static Optional<Colour> grantStrongerPreference(Player higher, Player lower) {
        var higherPreference = higher.colourPreference();
        var lowerPreference = lower.colourPreference();
        if (higherPreference.isStrongerThan(lowerPreference)) {
            return higherPreference.colour();
        }
        if (lowerPreference.isStrongerThan(higherPreference)) {
            return lowerPreference.colour().map(Colour::opposite);
        }
        if (higherPreference.isAbsolute()) {
            return grantWiderColourDifference(higher, lower, higherPreference);
        }
        return Optional.empty();
    }

    private static Optional<Colour> grantWiderColourDifference(Player higher, Player lower, ColourPreference shared) {
        var higherWidth = Math.abs(higher.colourDifference());
        var lowerWidth = Math.abs(lower.colourDifference());
        if (higherWidth == lowerWidth) {
            return Optional.empty();
        }
        return higherWidth > lowerWidth ? shared.colour() : shared.colour().map(Colour::opposite);
    }

    /** 5.2.3 on played games only, unplayed rounds moved to the front (GHR 3.4). */
    private static Optional<Colour> alternateToMostRecentDifference(Player higher, Player lower) {
        var higherColours = higher.playedColours();
        var lowerColours = lower.playedColours();
        for (var back = 1; back <= Math.min(higherColours.size(), lowerColours.size()); back++) {
            var higherColour = higherColours.get(higherColours.size() - back);
            if (higherColour != lowerColours.get(lowerColours.size() - back)) {
                return Optional.of(higherColour.opposite());
            }
        }
        return Optional.empty();
    }
}
