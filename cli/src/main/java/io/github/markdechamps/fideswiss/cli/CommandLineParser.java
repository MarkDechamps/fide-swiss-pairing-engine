package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.generator.FlatDrawModel;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * The two grammars of TRF CLI surface: a subcommand name first gives the canonical one ({@code pair in.trf -o
 * reply}); anything else the JaVaFo/bbpPairings short form ({@code --dutch in.trf -p reply}).
 */
final class CommandLineParser {

    private static final Set<String> SYSTEMS = Set.of("dutch");

    private CommandLineParser() {}

    static Command parse(List<String> arguments) {
        if (arguments.isEmpty()) {
            return new Command.Help();
        }
        var queue = new ArrayDeque<>(arguments);
        return switch (queue.peekFirst()) {
            case "pair" -> {
                queue.removeFirst();
                yield canonicalPair(queue);
            }
            case "standings" -> {
                queue.removeFirst();
                yield canonicalStandings(queue);
            }
            case "check" -> {
                queue.removeFirst();
                yield canonicalCheck(queue);
            }
            case "-check" -> {
                queue.removeFirst();
                yield new Command.Check(required(queue.pollFirst()), Optional.empty(), SettingsOverrides.NONE);
            }
            case "generate" -> {
                queue.removeFirst();
                yield canonicalGenerate(queue);
            }
            case "-g" -> {
                queue.removeFirst();
                yield compatibleGenerate(queue);
            }
            case "version", "-r" -> new Command.Version();
            case "help", "--help", "-h" -> new Command.Help();
            default -> compatible(queue);
        };
    }

    private static Command canonicalPair(Deque<String> queue) {
        String input = null;
        Optional<String> reply = Optional.empty();
        Optional<String> trace = Optional.empty();
        var overrides = SettingsOverrides.NONE;
        var extras = new PairExtras();
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            if (extras.accepts(argument, queue)) {
                continue;
            }
            switch (argument) {
                case "-o" -> reply = Optional.of(value(queue, "-o"));
                case "-l" -> trace = Optional.of(optionalValue(queue).orElse(""));
                default -> {
                    var parsed = settingsFlag(argument, queue, overrides);
                    if (parsed.isPresent()) {
                        overrides = parsed.get();
                    } else if (input == null && !argument.startsWith("--")) {
                        input = argument;
                    } else {
                        throw new UsageException("unexpected argument " + argument);
                    }
                }
            }
        }
        return pairCommand(input, reply, trace, extras, overrides);
    }

    private static Command canonicalStandings(Deque<String> queue) {
        String input = null;
        Optional<Integer> after = Optional.empty();
        Optional<List<String>> why = Optional.empty();
        var overrides = SettingsOverrides.NONE;
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            if (argument.equals("--after")) {
                after = Optional.of(number(value(queue, argument)));
                continue;
            }
            if (argument.equals("--why")) {
                why = Optional.of(List.of(value(queue, argument), value(queue, argument)));
                continue;
            }
            var parsed = settingsFlag(argument, queue, overrides);
            if (parsed.isPresent()) {
                overrides = parsed.get();
            } else if (input == null && !argument.startsWith("--")) {
                input = argument;
            } else {
                throw new UsageException("unexpected argument " + argument);
            }
        }
        return new Command.Standings(required(input), after, why, overrides);
    }

    private static Command canonicalCheck(Deque<String> queue) {
        String input = null;
        Optional<Integer> round = Optional.empty();
        var overrides = SettingsOverrides.NONE;
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            if (argument.equals("--round")) {
                round = Optional.of(number(value(queue, argument)));
                continue;
            }
            var parsed = settingsFlag(argument, queue, overrides);
            if (parsed.isPresent()) {
                overrides = parsed.get();
            } else if (input == null && !argument.startsWith("--")) {
                input = argument;
            } else {
                throw new UsageException("unexpected argument " + argument);
            }
        }
        return new Command.Check(required(input), round, overrides);
    }

    private static Command canonicalGenerate(Deque<String> queue) {
        Optional<Long> seed = Optional.empty();
        var count = 1;
        String output = null;
        Optional<String> configuration = Optional.empty();
        var overrides = new ArrayList<UnaryOperator<GeneratorSettings>>();
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            switch (argument) {
                case "--seed" -> seed = Optional.of(longNumber(value(queue, argument)));
                case "--count" -> count = number(value(queue, argument));
                case "-o" -> output = value(queue, argument);
                case "--config" -> configuration = Optional.of(value(queue, argument));
                case "--system" -> requireSupportedSystem(value(queue, argument));
                default -> overrides.add(generatorFlag(argument, queue));
            }
        }
        if (output == null) {
            throw new UsageException("generate needs -o <file or pattern with %d>");
        }
        return new Command.Generate(seed, count, output, configuration, overrides);
    }

    /** JaVaFo/bbp: {@code -g [<cfg>|<seed>] -o <out> [-s <seed>]}; one tournament. */
    private static Command compatibleGenerate(Deque<String> queue) {
        Optional<Long> seed = Optional.empty();
        Optional<String> configuration = Optional.empty();
        String output = null;
        var given = optionalValue(queue);
        if (given.isPresent()) {
            if (given.get().matches("\\d+")) {
                seed = Optional.of(longNumber(given.get()));
            } else {
                configuration = given;
            }
        }
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            switch (argument) {
                case "-o" -> output = value(queue, argument);
                case "-s" -> seed = Optional.of(longNumber(value(queue, argument)));
                default -> {
                    if (!(argument.startsWith("--") && SYSTEMS.contains(argument.substring(2)))) {
                        throw new UsageException("unexpected argument " + argument);
                    }
                }
            }
        }
        if (output == null) {
            throw new UsageException("-g needs -o <file>");
        }
        return new Command.Generate(seed, 1, output, configuration, List.of());
    }

    private static UnaryOperator<GeneratorSettings> generatorFlag(String flag, Deque<String> queue) {
        if (flag.startsWith("--") && SYSTEMS.contains(flag.substring(2))) {
            return settings -> settings;
        }
        return switch (flag) {
            case "--players" -> withRange(flag, queue, GeneratorSettings::withPlayers);
            case "--rounds" -> withRange(flag, queue, GeneratorSettings::withRounds);
            case "--highest-rating" -> withRange(flag, queue, GeneratorSettings::withHighestRating);
            case "--lowest-rating" -> withRange(flag, queue, GeneratorSettings::withLowestRating);
            case "--unrated" -> withRange(flag, queue, GeneratorSettings::withUnratedPercentage);
            case "--forfeit-rate" -> withRange(flag, queue, GeneratorSettings::withForfeitRate);
            case "--hpb-rate" -> withRange(flag, queue, GeneratorSettings::withHalfPointByeRate);
            case "--zpb-rate" -> withRange(flag, queue, GeneratorSettings::withZeroPointByeRate);
            case "--fpb-rate" -> withRange(flag, queue, GeneratorSettings::withFullPointByeRate);
            case "--withdrawals" -> withRange(flag, queue, GeneratorSettings::withWithdrawalPercentage);
            case "--draw-percentage" -> {
                var model = new FlatDrawModel(number(value(queue, flag)));
                yield settings -> settings.with(model);
            }
            default -> throw new UsageException("unexpected argument " + flag);
        };
    }

    private static UnaryOperator<GeneratorSettings> withRange(
            String flag, Deque<String> queue, BiFunction<GeneratorSettings, Range, GeneratorSettings> setter) {
        var text = value(queue, flag);
        try {
            var range = Range.parse(text);
            return settings -> setter.apply(settings, range);
        } catch (IllegalArgumentException e) {
            throw new UsageException(flag + " takes a number or a range A..B, not " + text);
        }
    }

    private static void requireSupportedSystem(String system) {
        if (!SYSTEMS.contains(system)) {
            throw new UsageException("unsupported pairing system " + system);
        }
    }

    private static long longNumber(String value) {
        try {
            return Long.parseUnsignedLong(value);
        } catch (NumberFormatException e) {
            throw new UsageException("not a seed: " + value);
        }
    }

    private static Command compatible(Deque<String> queue) {
        String input = null;
        var pair = false;
        var check = false;
        Optional<Integer> round = Optional.empty();
        Optional<String> reply = Optional.empty();
        Optional<String> trace = Optional.empty();
        var overrides = SettingsOverrides.NONE;
        var extras = new PairExtras();
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            if (extras.accepts(argument, queue)) {
                continue;
            }
            switch (argument) {
                case "-p" -> {
                    pair = true;
                    reply = optionalValue(queue);
                }
                case "-c" -> {
                    check = true;
                    round = optionalValue(queue).map(CommandLineParser::number);
                }
                case "-l" -> trace = Optional.of(optionalValue(queue).orElse(""));
                default -> {
                    var parsed = settingsFlag(argument, queue, overrides);
                    if (parsed.isPresent()) {
                        overrides = parsed.get();
                    } else if (input == null && !argument.startsWith("-") || "-".equals(argument)) {
                        input = argument;
                    } else {
                        throw new UsageException("unexpected argument " + argument);
                    }
                }
            }
        }
        if (check) {
            return new Command.Check(required(input), round, overrides);
        }
        if (!pair) {
            throw new UsageException("nothing to do: give -p to pair the next round or -c to check");
        }
        return pairCommand(input, reply, trace, extras, overrides);
    }

    /** A bare {@code -l} writes the trace beside the input as {@code <input>.trace.txt}. */
    private static Command pairCommand(
            String input,
            Optional<String> reply,
            Optional<String> trace,
            PairExtras extras,
            SettingsOverrides overrides) {
        var file = required(input);
        var traceFile = trace.map(given -> given.isEmpty() ? file + ".trace.txt" : given);
        return new Command.Pair(file, reply, traceFile, List.copyOf(extras.explain), extras.quiet, overrides);
    }

    /** The pairing flags both grammars share: {@code --explain <id>} (repeatable) and {@code --quiet}. */
    private static final class PairExtras {
        private final List<String> explain = new java.util.ArrayList<>();
        private boolean quiet;

        boolean accepts(String argument, Deque<String> queue) {
            switch (argument) {
                case "--explain" -> explain.add(value(queue, argument));
                case "--quiet" -> quiet = true;
                default -> {
                    return false;
                }
            }
            return true;
        }
    }

    private static Optional<SettingsOverrides> settingsFlag(
            String argument, Deque<String> queue, SettingsOverrides overrides) {
        if (argument.startsWith("--") && SYSTEMS.contains(argument.substring(2))) {
            return Optional.of(overrides);
        }
        return switch (argument) {
            case "--system" -> {
                var system = value(queue, argument);
                if (!SYSTEMS.contains(system)) {
                    throw new UsageException("unsupported pairing system " + system);
                }
                yield Optional.of(overrides);
            }
            case "--rounds" -> Optional.of(overrides.withRounds(NumberOfRounds.of(number(value(queue, argument)))));
            case "--tiebreaks" -> Optional.of(overrides.withTieBreaks(TieBreakList.parse(value(queue, argument))));
            case "--tiebreak-edition" ->
                Optional.of(overrides.withTieBreakEdition(tieBreakEdition(value(queue, argument))));
            case "--initial-colour" -> Optional.of(overrides.withInitialColour(colour(value(queue, argument))));
            case "--edition" -> Optional.of(overrides.withEdition(edition(value(queue, argument))));
            default -> Optional.empty();
        };
    }

    private static SwissRulesEdition edition(String value) {
        return switch (value) {
            case "2026" -> SwissRulesEdition.EDITION_2026;
            case "pre-2026" -> SwissRulesEdition.PRE_2026;
            default -> throw new UsageException("--edition takes 2026 or pre-2026, not " + value);
        };
    }

    private static TieBreakEdition tieBreakEdition(String value) {
        return switch (value) {
            case "2026-03" -> TieBreakEdition.EDITION_2026_03;
            case "2024-08" -> TieBreakEdition.EDITION_2024_08;
            default -> throw new UsageException("--tiebreak-edition takes 2026-03 or 2024-08, not " + value);
        };
    }

    private static InitialColour colour(String value) {
        return switch (value) {
            case "white" -> InitialColour.white();
            case "black" -> InitialColour.black();
            default -> throw new UsageException("--initial-colour takes white or black, not " + value);
        };
    }

    private static int number(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new UsageException("not a number: " + value);
        }
    }

    private static String value(Deque<String> queue, String flag) {
        if (queue.isEmpty()) {
            throw new UsageException(flag + " needs a value");
        }
        return queue.removeFirst();
    }

    /** The next argument, unless it is a flag or there is none. */
    private static Optional<String> optionalValue(Deque<String> queue) {
        var next = queue.peekFirst();
        if (next == null || (next.startsWith("-") && !next.equals("-"))) {
            return Optional.empty();
        }
        return Optional.of(queue.removeFirst());
    }

    private static String required(String input) {
        if (input == null) {
            throw new UsageException("no input file given");
        }
        return input;
    }
}
