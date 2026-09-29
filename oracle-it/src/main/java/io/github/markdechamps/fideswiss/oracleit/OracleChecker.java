package io.github.markdechamps.fideswiss.oracleit;

/**
 * The port to an Oracle's checker mode (Verification strategy, "their checker on our pairings"): the program reads a
 * whole tournament file and says whether every recorded round is its own pairing.
 */
public interface OracleChecker {

    String name();

    /** The dialect its input is written in. */
    OracleDialect dialect();

    OracleVerdict check(String trf);
}
