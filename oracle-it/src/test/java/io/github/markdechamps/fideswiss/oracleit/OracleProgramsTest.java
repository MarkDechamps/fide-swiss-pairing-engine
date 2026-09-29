package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class OracleProgramsTest {

    @Test
    void aProgramThatIsNotConfiguredIsAbsentAndSaysHowToConfigureIt() {
        assertThat(OraclePrograms.BBP_V6.locate(key -> null, key -> null)).isEmpty();
        assertThat(OraclePrograms.BBP_V6.absence())
                .contains("-Dfideswiss.oracle.bbp6=<path>")
                .contains("FIDESWISS_ORACLE_BBP6=<path>");
    }

    @Test
    void theSystemPropertyWinsOverTheEnvironmentVariable() {
        var properties = Map.of("fideswiss.oracle.javafo", "/x/javafo.jar");
        var environment = Map.of("FIDESWISS_ORACLE_JAVAFO", "/y/other.jar");

        assertThat(OraclePrograms.JAVAFO.locate(properties::get, environment::get))
                .isPresent();
    }

    @Test
    void theEnvironmentVariableIsUsedWhenNoPropertyIsSet() {
        var environment = Map.of("FIDESWISS_ORACLE_BBP5", "/opt/bbp5");

        assertThat(OraclePrograms.BBP_V5.locate(key -> null, environment::get)).isPresent();
    }

    @Test
    void aBlankPathIsAbsent() {
        assertThat(OraclePrograms.BBP_V5.locate(key -> " ", key -> null)).isEmpty();
    }
}
