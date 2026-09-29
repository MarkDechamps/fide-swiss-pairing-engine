package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team and Double-Swiss [C6] (C.04.6 and C.04.5 2.3.3), ruling A6: whether the following scoregroup must
 * reach its parity minimum of upfloaters (pass or fail, the default and Gacrux's reading), or needs as few as it
 * can (graded).
 */
public enum UpfloaterLookAhead implements Interpretation {
    PARITY_MINIMUM,
    GRADED;

    public static UpfloaterLookAhead parityMinimum() {
        return PARITY_MINIMUM;
    }

    public static UpfloaterLookAhead graded() {
        return GRADED;
    }
}
