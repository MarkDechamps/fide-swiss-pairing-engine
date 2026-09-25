package io.github.markdechamps.fideswiss.cli;

import java.util.List;
import java.util.Optional;

/** What the command line asks for; both grammars parse into these values. */
sealed interface Command {

    /** Pair the next round of the tournament in {@code input} ("-" is standard input). */
    record Pair(
            String input,
            Optional<String> reply,
            Optional<String> trace,
            List<String> explain,
            boolean quiet,
            SettingsOverrides overrides)
            implements Command {}

    /** Check every recorded round (or only {@code round}) against the rules and the system (the PTC). */
    record Check(String input, Optional<Integer> round, SettingsOverrides overrides) implements Command {}

    /**
     * Print the Standings after the last recorded round (or after {@code after}); with {@code why}, explain why the
     * first of the two ranks above the second.
     */
    record Standings(
            String input, Optional<Integer> after, Optional<java.util.List<String>> why, SettingsOverrides overrides)
            implements Command {}

    record Version() implements Command {}

    record Help() implements Command {}
}
