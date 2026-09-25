package io.github.markdechamps.fideswiss.tournament;

/** Why a participant was not on a board in a round, which also decides what the round scores. */
public enum Bye {
    /** The bye the system gives the one participant left over (C.04.1 Art. 3). */
    PAIRING_ALLOCATED,
    /** A requested bye scored as a win. */
    FULL_POINT,
    /** A requested bye scored as a draw. */
    HALF_POINT,
    /** A requested bye scored as a loss (GHR 3.3). */
    ZERO_POINT,
    /** Any round after a Withdrawal (GHR 3.2), a zero-point bye for tie-breaks (C.07 16.1). */
    WITHDRAWN,
    /** A round before a Late Entry arrives, scored as a loss unless the tournament rules award points (GHR 2.4). */
    NOT_YET_ENTERED;

    /** Whether the participant chose not to play, as opposed to the system leaving it over. */
    public boolean isRequested() {
        return this == FULL_POINT || this == HALF_POINT || this == ZERO_POINT;
    }
}
