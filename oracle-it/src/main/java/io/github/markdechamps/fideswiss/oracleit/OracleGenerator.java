package io.github.markdechamps.fideswiss.oracleit;

/** The port to an Oracle's own random tournament generator; the file it returns is in its own dialect. */
public interface OracleGenerator {

    String name();

    /** Whether the program takes a seed; without one two runs of the same {@code seed} differ. */
    boolean isReproducible();

    /**
     * A tournament the program played by itself, paired by its own rules.
     *
     * @param configuration {@code Key=Value} lines of the program's generator configuration; may be empty
     */
    String generate(long seed, String configuration);
}
