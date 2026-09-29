package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team under Baku acceleration: the secondary score that decides the first-team for colours (C.04.6 4.2.2),
 * Known Divergence KD-3 (ADR 0009). The default adds the round's Virtual Points, in match points, to the game
 * points, as the reference app Gacrux does; {@code REAL} keeps the real game points (C.04.7 1.5, the literal text).
 */
public enum BakuSecondaryScore implements Interpretation {
    VIRTUAL_MATCH_POINTS,
    REAL;

    public static BakuSecondaryScore virtualMatchPoints() {
        return VIRTUAL_MATCH_POINTS;
    }

    public static BakuSecondaryScore real() {
        return REAL;
    }
}
