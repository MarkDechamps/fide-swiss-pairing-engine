package io.github.markdechamps.fideswiss.oracleit;

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
    JAVAFO("JaVaFo 2.2", "javafo", OracleDialect.JAVAFO, List.of());

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
        var command = path.endsWith(".jar") ? List.of(java(), "-jar", path) : List.of(path);
        return new ExternalPairingProgram(label, command, flags, dialect);
    }

    private static String java() {
        return ProcessHandle.current().info().command().orElse("java");
    }
}
