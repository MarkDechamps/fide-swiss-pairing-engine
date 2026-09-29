package io.github.markdechamps.fideswiss.oracleit;

/** What an Oracle's own checker says about a whole tournament file. */
public sealed interface OracleVerdict {

    /** Every round of the file is what the Oracle would have paired. */
    record Accepted() implements OracleVerdict {}

    /** The file is refused or some round differs; {@code report} is everything the program printed. */
    record Rejected(int exitCode, String report) implements OracleVerdict {}
}
