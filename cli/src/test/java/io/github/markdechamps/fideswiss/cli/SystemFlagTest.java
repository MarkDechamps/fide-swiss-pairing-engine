package io.github.markdechamps.fideswiss.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SystemFlagTest {

    @ParameterizedTest
    @CsvSource({
        "--dutch, C.04.3 Dutch System 2026",
        "--dubov, C.04.4.1 Dubov System 2026",
        "--lim, C.04.4.3 Lim System 2026",
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

    @Test
    void declaresAMaxiTournamentForLim() {
        var command = (Command.Pair) CommandLineParser.parse(List.of("pair", "in.trf", "--lim", "--maxi-tournament"));

        var settings = command.overrides().applyTo(Profiles.individualSwiss(NumberOfRounds.of(5)));

        assertThat(settings.pairingSystem().name()).isEqualTo("C.04.4.3 Lim System 2026 (Maxi-tournament)");
    }

    @Test
    void rejectsAMaxiTournamentForAnotherSystem() {
        var command = (Command.Pair) CommandLineParser.parse(List.of("pair", "in.trf", "--dutch", "--maxi-tournament"));

        assertThatThrownBy(() -> command.overrides().applyTo(Profiles.individualSwiss(NumberOfRounds.of(5))))
                .isInstanceOf(InvalidSettingsException.class);
    }

    @ParameterizedTest
    @CsvSource({"burstein", "double-swiss"})
    void rejectsASystemNotYetBuilt(String name) {
        assertThatThrownBy(() -> CommandLineParser.parse(List.of("pair", "in.trf", "--system", name)))
                .isInstanceOf(UsageException.class);
    }
}
