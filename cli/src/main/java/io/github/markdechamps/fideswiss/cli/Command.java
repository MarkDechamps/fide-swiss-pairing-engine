package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/** What the command line asks for; both grammars parse into these values. */
sealed interface Command {

    /** Pair the next round of the tournament in {@code input} ("-" is standard input). */
    record Pair(String input, Optional<String> reply, Optional<String> trace, SettingsOverrides overrides)
            implements Command {}

    /** Check every recorded round (or only {@code round}) against the rules and the system (the PTC). */
    record Check(String input, Optional<Integer> round, SettingsOverrides overrides) implements Command {}

    /**
     * Generate {@code count} tournaments from a corpus seed (a fresh one, printed, when none is given) into
     * {@code output}, where {@code %d} stands for the index; a configuration file, then the flags, override the
     * generator's defaults.
     */
    record Generate(
            Optional<Long> seed,
            int count,
            String output,
            Optional<String> configuration,
            List<UnaryOperator<GeneratorSettings>> overrides)
            implements Command {

        public Generate {
            overrides = List.copyOf(overrides);
        }
    }

    record Version() implements Command {}

    record Help() implements Command {}
}
