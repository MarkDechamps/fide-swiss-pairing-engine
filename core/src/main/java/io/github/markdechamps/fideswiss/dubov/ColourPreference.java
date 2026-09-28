package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.Colour;

/** 1.6: the colour a player should ideally receive, and how strongly. In Dubov every player has one (1.6.4). */
record ColourPreference(Colour colour, Strength strength) {

    /** 1.6.1–1.6.3, strongest first. */
    enum Strength {
        ABSOLUTE,
        STRONG,
        MILD;

        boolean isStrongerThan(Strength other) {
            return compareTo(other) < 0;
        }
    }

    static ColourPreference of(Player player) {
        var colours = player.playedColours();
        if (colours.isEmpty()) {
            return new ColourPreference(Colour.BLACK, Strength.MILD);
        }
        var difference = player.colourDifference();
        var last = colours.getLast();
        var sameColourTwice = colours.size() >= 2 && colours.get(colours.size() - 2) == last;
        if (difference < -1) {
            return new ColourPreference(Colour.WHITE, Strength.ABSOLUTE);
        }
        if (difference > 1) {
            return new ColourPreference(Colour.BLACK, Strength.ABSOLUTE);
        }
        if (sameColourTwice) {
            return new ColourPreference(last.opposite(), Strength.ABSOLUTE);
        }
        if (difference != 0) {
            return new ColourPreference(difference < 0 ? Colour.WHITE : Colour.BLACK, Strength.STRONG);
        }
        return new ColourPreference(last.opposite(), Strength.MILD);
    }

    boolean isAbsolute() {
        return strength == Strength.ABSOLUTE;
    }
}
