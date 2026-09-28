package io.github.markdechamps.fideswiss.tournament;

/** What a Late Entry scores for the rounds it missed: nothing (GHR 2.4), unless the tournament rules award points. */
public final class MissedRounds {

    private final Bye bye;

    private MissedRounds(Bye bye) {
        this.bye = bye;
    }

    public static MissedRounds zeroPoints() {
        return new MissedRounds(Bye.NOT_YET_ENTERED);
    }

    public static MissedRounds halfPointByes() {
        return new MissedRounds(Bye.HALF_POINT);
    }

    Bye bye() {
        return bye;
    }
}
