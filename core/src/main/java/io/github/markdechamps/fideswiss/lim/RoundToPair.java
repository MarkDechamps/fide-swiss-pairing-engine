package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.math.BigDecimal;

/** What the Lim rules need to know about the round being paired. */
record RoundToPair(
        RoundNumber round,
        NumberOfRounds numberOfRounds,
        InitialColour initialColour,
        Points winValue,
        MaxiTournament maxiTournament) {

    private static final int MAXI_RATING_BAND = 100;

    boolean isFirstRound() {
        return round.equals(RoundNumber.FIRST);
    }

    boolean isLastRound() {
        return numberOfRounds.isLast(round);
    }

    boolean isMaxiTournament() {
        return maxiTournament == MaxiTournament.DECLARED;
    }

    /** 2.2, read as half of what the rounds played can give: rounds played × a win's points ÷ 2. */
    PairingScore medianScore() {
        var total = winValue.times(round.value() - 1).toBigDecimal();
        return new PairingScore(Points.of(total.divide(BigDecimal.valueOf(2))));
    }

    /**
     * 2.1: the players have not met, and some colour allocation keeps both inside 5.1.1 and 5.1.2. In the last
     * round Article 6 puts Basic Rule 5 above the colour limits, so there only the meeting counts.
     */
    boolean compatible(Player a, Player b) {
        if (a == b || a.hasMet(b)) {
            return false;
        }
        if (isLastRound()) {
            return true;
        }
        return a.mayReceive(Colour.WHITE) && b.mayReceive(Colour.BLACK)
                || a.mayReceive(Colour.BLACK) && b.mayReceive(Colour.WHITE);
    }

    /** 5.2: both players can get the colour due to them. */
    static boolean dueColoursSuit(Player a, Player b) {
        var dueA = a.dueColour();
        var dueB = b.dueColour();
        return dueA.isEmpty() || dueB.isEmpty() || dueA.get() != dueB.get();
    }

    /** 3.2.3, 3.8, 5.7: a Maxi-tournament allows these choices only between players rated within 100 points. */
    static boolean withinMaxiBand(Player a, Player b) {
        return Math.abs(a.rating().valueOrZero() - b.rating().valueOrZero()) <= MAXI_RATING_BAND;
    }
}
