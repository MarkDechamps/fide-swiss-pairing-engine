package io.github.markdechamps.fideswiss.tournament;

/** Which points of a match are the (primary) score; the other is the secondary score (C.04.6 1.2). */
public enum PrimaryScore {
    MATCH_POINTS,
    GAME_POINTS;

    public PrimaryScore other() {
        return this == MATCH_POINTS ? GAME_POINTS : MATCH_POINTS;
    }
}
