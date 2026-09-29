package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.tournament.Colour;

/** Whether a group is paired inside the board-1 colour limits of 7.3, or with them disregarded (7.4). */
enum ColourLimits {
    OBSERVED,
    DISREGARDED;

    /** 6.1, and while the limits are observed, some colour for board 1 keeps both teams inside 7.3. */
    boolean compatible(Team one, Team other) {
        if (!one.mayMeet(other)) {
            return false;
        }
        return this == DISREGARDED || fits(one, other, Colour.WHITE) || fits(one, other, Colour.BLACK);
    }

    /** 7.3: {@code one} may have the colour on board 1 and {@code other} the opposite one. */
    static boolean fits(Team one, Team other, Colour colourOfOne) {
        return one.mayReceive(colourOfOne) && other.mayReceive(colourOfOne.opposite());
    }
}
