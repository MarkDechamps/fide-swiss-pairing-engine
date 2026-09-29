package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team: the game result the Pairing-Allocated Bye scores on every board (C.04.6 1.4), Known Divergence KD-2
 * (ADR 0009). The default is a win, as the reference app Gacrux scores it when TRF {@code 162} has no {@code P};
 * {@code DRAW} is the literal text (as many points as are rewarded for a draw) and {@code LOSS} scores nothing. The
 * match points of a PAB stay those of a drawn match unless {@code 320} says otherwise. A {@code P} in {@code 162}
 * that equals a win, a draw or a loss selects the matching value.
 */
public enum PabValue implements Interpretation {
    WIN,
    DRAW,
    LOSS;

    public static PabValue win() {
        return WIN;
    }

    public static PabValue draw() {
        return DRAW;
    }

    public static PabValue loss() {
        return LOSS;
    }
}
