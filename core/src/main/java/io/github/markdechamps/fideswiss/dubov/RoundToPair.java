package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;

/** What the Dubov rules need to know about the round being paired. */
record RoundToPair(RoundNumber round, NumberOfRounds numberOfRounds, InitialColour initialColour) {

    boolean isLastRound() {
        return numberOfRounds.isLast(round);
    }

    /** 1.8.2: MaxT = 2 + [Rnds/5], Rnds being the planned number of rounds. */
    int maxT() {
        return 2 + numberOfRounds.value() / 5;
    }

    /** 1.8.1. */
    boolean isMaximumUpfloater(Player player) {
        return player.upfloats() >= maxT();
    }

    /** [C1] and [C3], the latter with no topscorer exception. */
    boolean mayMeet(Player a, Player b) {
        return !a.hasMet(b) && !haveTheSameAbsolutePreference(a, b);
    }

    /** [C7] per pair: two players wanting the same colour leave one of them without it, whatever Article 5 does. */
    static long sameColourWanted(Player a, Player b) {
        return a.preference().colour() == b.preference().colour() ? 1 : 0;
    }

    private static boolean haveTheSameAbsolutePreference(Player a, Player b) {
        var first = a.preference();
        var second = b.preference();
        return first.isAbsolute() && second.isAbsolute() && first.colour() == second.colour();
    }
}
