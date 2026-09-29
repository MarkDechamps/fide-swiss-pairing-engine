package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Optional;

/** 1.5: the colour a player should ideally receive, and how strongly. */
record ColourPreference(Colour colour, Strength strength) {

    /** 1.5.1–1.5.3, strongest first. */
    enum Strength {
        ABSOLUTE,
        STRONG,
        MILD;

        boolean isStrongerThan(Strength other) {
            return compareTo(other) < 0;
        }
    }

    /** 1.5.4: a player who did not play any games has no colour preference. */
    static Optional<ColourPreference> of(Player player) {
        var colours = player.playedColours();
        if (colours.isEmpty()) {
            return Optional.empty();
        }
        var difference = player.colourDifference();
        var last = colours.getLast();
        var sameColourTwice = colours.size() >= 2 && colours.get(colours.size() - 2) == last;
        if (difference < -1) {
            return Optional.of(new ColourPreference(Colour.WHITE, Strength.ABSOLUTE));
        }
        if (difference > 1) {
            return Optional.of(new ColourPreference(Colour.BLACK, Strength.ABSOLUTE));
        }
        if (sameColourTwice) {
            return Optional.of(new ColourPreference(last.opposite(), Strength.ABSOLUTE));
        }
        if (difference != 0) {
            return Optional.of(new ColourPreference(difference < 0 ? Colour.WHITE : Colour.BLACK, Strength.STRONG));
        }
        return Optional.of(new ColourPreference(last.opposite(), Strength.MILD));
    }

    boolean isAbsolute() {
        return strength == Strength.ABSOLUTE;
    }
}
