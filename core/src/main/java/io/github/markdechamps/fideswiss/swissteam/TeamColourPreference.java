package io.github.markdechamps.fideswiss.swissteam;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Optional;

/** 1.7: a colour and how strongly a team wants it; Type A preferences all count as strong. */
record TeamColourPreference(Colour colour, Strength strength) {

    enum Strength {
        MILD,
        STRONG
    }

    static TeamColourPreference strong(Colour colour) {
        return new TeamColourPreference(colour, Strength.STRONG);
    }

    static TeamColourPreference mild(Colour colour) {
        return new TeamColourPreference(colour, Strength.MILD);
    }

    boolean isStrong() {
        return strength == Strength.STRONG;
    }

    static boolean sameColour(Optional<TeamColourPreference> a, Optional<TeamColourPreference> b) {
        return a.isPresent() && b.isPresent() && a.get().colour() == b.get().colour();
    }
}
