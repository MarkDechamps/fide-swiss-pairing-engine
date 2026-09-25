package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;

/** What the Dutch rules need to know about the round being paired. */
record RoundToPair(RoundNumber round, NumberOfRounds numberOfRounds, InitialColour initialColour, Points winValue) {

    /** 1.8: more than 50% of the maximum possible score, only when pairing the final round. */
    boolean isTopscorer(Player player) {
        var maximum = winValue.times(round.value() - 1);
        return numberOfRounds.isLast(round) && player.score().points().times(2).isGreaterThan(maximum);
    }
}
