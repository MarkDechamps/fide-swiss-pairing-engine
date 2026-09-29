package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team seating of a bracket's members into pairs (C.04.6 3.6.1), Known Divergence KD-1 (ADR 0009): whether the
 * top member of a pair is the team with the higher score and, on equal scores, the smaller TPN (the default, the
 * reference app Gacrux's order), or the team with the smaller TPN whatever the score (the literal text, ruling G1).
 */
public enum BracketSeating implements Interpretation {
    SCORE_THEN_TPN,
    TPN;

    public static BracketSeating scoreThenTpn() {
        return SCORE_THEN_TPN;
    }

    public static BracketSeating tpn() {
        return TPN;
    }
}
