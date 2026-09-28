package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team Type B colour preference, ruling A1 (C.04.6 1.7.2): a team with colour difference 0 that had the same
 * colour in its last two played matches, when pairing the last round, has a strong preference (the default and
 * Gacrux's reading: the no-preference sentence only closes the mild clauses) or none.
 */
public enum LastRoundZeroCdTypeB implements Interpretation {
    STRONG,
    NONE;

    public static LastRoundZeroCdTypeB strong() {
        return STRONG;
    }

    public static LastRoundZeroCdTypeB none() {
        return NONE;
    }
}
