package io.github.markdechamps.fideswiss.tournament;

/**
 * Swiss Team tie-break EDEBT (C.07 13.3.2 with 12.1), ADR 0009: which of two teams tied in every score ranks first
 * on Board Count. The default is the higher Board Count, as the reference app Gacrux ranks it; {@code LOWER} is the
 * literal text of 12.1 (the lower the sum, the higher the team ranks). It concerns EDEBT and EDEBB only; the
 * stand-alone {@code BC} tie-break always ranks the lower first.
 */
public enum EdebtBoardCount implements Interpretation {
    HIGHER,
    LOWER;

    public static EdebtBoardCount higher() {
        return HIGHER;
    }

    public static EdebtBoardCount lower() {
        return LOWER;
    }
}
