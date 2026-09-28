package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team and Double-Swiss floaters under acceleration (C.04.6 1.5, [C7], [C10]), ruling A8: whether a floater
 * of the previous round is judged on the Pairing Scores of that round (the default, Gacrux's reading and the Dutch
 * precedent) or on the real scores.
 */
public enum FloatScore implements Interpretation {
    PAIRING,
    REAL;

    public static FloatScore pairing() {
        return PAIRING;
    }

    public static FloatScore real() {
        return REAL;
    }
}
