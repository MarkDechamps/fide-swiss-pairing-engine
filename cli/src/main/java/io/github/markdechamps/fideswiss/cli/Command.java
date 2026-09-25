package io.github.markdechamps.fideswiss.cli;

import java.util.Optional;

/** What the command line asks for; both grammars parse into these values. */
sealed interface Command {

    /** Pair the next round of the tournament in {@code input} ("-" is standard input). */
    record Pair(String input, Optional<String> reply, Optional<String> trace, SettingsOverrides overrides)
            implements Command {}

    record Version() implements Command {}

    record Help() implements Command {}
}
