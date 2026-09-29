package io.github.markdechamps.fideswiss.oracleit;

/**
 * The port to an Oracle (Verification strategy): an external program that pairs the next round of a TRF file and
 * whose answer must equal ours. Adapters run the program as a separate process; nothing of it is linked or copied.
 */
public interface PairingOracle {

    /** The name and pinned version, for reports. */
    String name();

    /** Pairs the next round of {@code trf} (already in the dialect this Oracle reads, see {@link OracleDialect}). */
    OracleAnswer pair(String trf);

    /** The dialect its input is written in. */
    OracleDialect dialect();
}
