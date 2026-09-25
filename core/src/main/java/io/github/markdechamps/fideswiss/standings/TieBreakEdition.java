package io.github.markdechamps.fideswiss.standings;

/** A dated release of the FIDE Tie-break Regulations (C.07), chosen independently of the Swiss Rules Edition. */
public enum TieBreakEdition {
    /** Applied from 1 March 2026: the Dummy Opponent is capped (16.4), and STD, TPN and RTNG exist. */
    EDITION_2026_03,
    /** Applied from 1 August 2024 to 28 February 2026: the Dummy Opponent scores the participant's own score. */
    EDITION_2024_08
}
