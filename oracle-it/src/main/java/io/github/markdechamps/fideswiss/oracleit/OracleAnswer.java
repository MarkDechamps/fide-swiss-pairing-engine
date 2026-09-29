package io.github.markdechamps.fideswiss.oracleit;

/** What an Oracle said about one round: a pairing, or a refusal (no legal pairing, a file it cannot read). */
public sealed interface OracleAnswer {

    record Paired(OraclePairing pairing) implements OracleAnswer {}

    record Refused(int exitCode, String message) implements OracleAnswer {}
}
