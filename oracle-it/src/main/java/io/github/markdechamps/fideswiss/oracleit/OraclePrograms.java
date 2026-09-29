package io.github.markdechamps.fideswiss.oracleit;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Where the pinned Oracle programs are: a system property (for example {@code -Dfideswiss.oracle.bbp6=/opt/bbp6}) or
 * else an environment variable ({@code FIDESWISS_ORACLE_BBP6}). Nothing is downloaded here; a program that is not
 * configured is absent, and its gate is skipped with a message that says how to configure it.
 */
public enum OraclePrograms {
    /** bbpPairings v6.0.0, the Oracle of the Dutch System 2026. */
    BBP_V6("bbpPairings v6.0.0", "bbp6", OracleDialect.BBP, List.of("--dutch")),
    /** bbpPairings v5.0.1, an Oracle of the Dutch System 2017 (the pre-2026 Swiss Rules Edition). */
    BBP_V5("bbpPairings v5.0.1", "bbp5", OracleDialect.BBP, List.of("--dutch")),
    /** JaVaFo 2.2, an Oracle of the Dutch System 2017; a {@code .jar} is run with {@code java -jar}. */
    JAVAFO("JaVaFo 2.2", "javafo", OracleDialect.JAVAFO, List.of()),
    /**
     * Gacrux (TieBreakServer) @ 6419149 unpatched, the Oracle of the Swiss Team System 2026 and the reference app its
     * defaults follow (ADR 0009); the path is its clone, and Python with {@code networkx} is {@code python3} or
     * {@code fideswiss.oracle.gacrux.python}.
     */
    GACRUX("Gacrux @ 6419149", "gacrux", OracleDialect.GACRUX, List.of()),
    /**
     * The same clone with the {@code tpn-order} patch (Ruling G1), the Oracle of the literal reading of C.04.6 3.6.1
     * ({@code bracket-seating=tpn}); it has the configuration of {@link #GACRUX}.
     */
    GACRUX_TPN_ORDER("Gacrux @ 6419149 (tpn-order)", "gacrux", OracleDialect.GACRUX, List.of());

    private final String label;
    private final String key;
    private final OracleDialect dialect;
    private final List<String> flags;

    OraclePrograms(String label, String key, OracleDialect dialect, List<String> flags) {
        this.label = label;
        this.key = key;
        this.dialect = dialect;
        this.flags = flags;
    }

    public String property() {
        return "fideswiss.oracle." + key;
    }

    public String variable() {
        return "FIDESWISS_ORACLE_" + key.toUpperCase();
    }

    /** The message a skipped test shows. */
    public String absence() {
        return label + " is not configured: set -D" + property() + "=<path> or " + variable() + "=<path>";
    }

    public Optional<PairingOracle> locate() {
        return locate(System::getProperty, System::getenv);
    }

    public Optional<PairingOracle> locate(UnaryOperator<String> properties, UnaryOperator<String> environment) {
        var path = Optional.ofNullable(properties.apply(property()))
                .or(() -> Optional.ofNullable(environment.apply(variable())));
        return path.filter(value -> !value.isBlank()).map(this::program);
    }

    private PairingOracle program(String path) {
        if (dialect == OracleDialect.GACRUX) {
            return new GacruxProgram(label, Path.of(path), python(), this == GACRUX_TPN_ORDER);
        }
        return external(path);
    }

    private ExternalPairingProgram external(String path) {
        var command = path.endsWith(".jar") ? List.of(java(), "-jar", path) : List.of(path);
        return new ExternalPairingProgram(label, command, flags, dialect);
    }

    /** The program that also checks and generates tournaments (bbpPairings, JaVaFo; not Gacrux). */
    public Optional<ExternalPairingProgram> locateChecker() {
        return dialect == OracleDialect.GACRUX
                ? Optional.empty()
                : Optional.ofNullable(System.getProperty(property()))
                        .or(() -> Optional.ofNullable(System.getenv(variable())))
                        .filter(value -> !value.isBlank())
                        .map(this::external);
    }

    /** The Python that has Gacrux's requirements: {@code -Dfideswiss.oracle.gacrux.python} or {@code FIDESWISS_ORACLE_GACRUX_PYTHON}. */
    private static String python() {
        return Optional.ofNullable(System.getProperty("fideswiss.oracle.gacrux.python"))
                .or(() -> Optional.ofNullable(System.getenv("FIDESWISS_ORACLE_GACRUX_PYTHON")))
                .filter(value -> !value.isBlank())
                .orElse("python3");
    }

    private static String java() {
        return ProcessHandle.current().info().command().orElse("java");
    }
}
