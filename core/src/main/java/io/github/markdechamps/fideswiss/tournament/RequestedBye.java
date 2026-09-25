package io.github.markdechamps.fideswiss.tournament;

/** A round a participant asked in advance not to be paired in, scored as the tournament rules allow (GHR 3.3). */
public final class RequestedBye {

    private final Bye bye;

    private RequestedBye(Bye bye) {
        this.bye = bye;
    }

    public static RequestedBye full() {
        return new RequestedBye(Bye.FULL_POINT);
    }

    public static RequestedBye half() {
        return new RequestedBye(Bye.HALF_POINT);
    }

    public static RequestedBye zero() {
        return new RequestedBye(Bye.ZERO_POINT);
    }

    public Bye bye() {
        return bye;
    }
}
