package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;

/** What the Burstein rules need to know about the round being paired. */
record RoundToPair(RoundNumber round, NumberOfRounds numberOfRounds, InitialColour initialColour) {

    /** 1.6.2: half the number of rounds, rounded down, or four, whichever is lower. */
    int seedingRounds() {
        return Math.min(numberOfRounds.value() / 2, 4);
    }

    /** 1.6.1: a seeding round is paired by the Dutch System. */
    boolean isSeedingRound() {
        return round.value() <= seedingRounds();
    }

    /** [C1] and [C3]. */
    boolean mayMeet(Player a, Player b) {
        return !a.hasMet(b) && !haveTheSameAbsolutePreference(a, b);
    }

    /**
     * [C8] per pair: two players wanting the same colour leave one of them without it, whatever Article 5 does. A
     * player with no preference (1.5.4) is granted either colour.
     */
    static long sameColourWanted(Player a, Player b) {
        var first = a.preference();
        var second = b.preference();
        return first.isPresent()
                        && second.isPresent()
                        && first.get().colour() == second.get().colour()
                ? 1
                : 0;
    }

    private static boolean haveTheSameAbsolutePreference(Player a, Player b) {
        var first = a.preference();
        var second = b.preference();
        return first.isPresent()
                && second.isPresent()
                && first.get().isAbsolute()
                && second.get().isAbsolute()
                && first.get().colour() == second.get().colour();
    }
}
