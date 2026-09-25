package io.github.markdechamps.fideswiss.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SystemFlagTest {

    @ParameterizedTest
    @CsvSource({
        "--dutch, C.04.3 Dutch System 2026",
        "--dubov, C.04.4.1 Dubov System 2026",
    })
    void selectsTheSystemWithTheBbpStyleAlias(String flag, String system) {
        var command = (Command.Pair) CommandLineParser.parse(List.of(flag, "in.trf", "-p"));

        assertThat(command.overrides().system()).map(PairingSystem::name).contains(system);
    }

    @ParameterizedTest
    @CsvSource({"dutch, C.04.3 Dutch System 2026", "dubov, C.04.4.1 Dubov System 2026"})
    void selectsTheSystemWithTheSystemFlag(String name, String system) {
        var command = (Command.Pair) CommandLineParser.parse(List.of("pair", "in.trf", "--system", name));

        assertThat(command.overrides().system()).map(PairingSystem::name).contains(system);
    }

    @ParameterizedTest
    @CsvSource({"burstein", "swiss-team"})
    void rejectsASystemNotYetBuilt(String name) {
        assertThatThrownBy(() -> CommandLineParser.parse(List.of("pair", "in.trf", "--system", name)))
                .isInstanceOf(UsageException.class);
    }
}
