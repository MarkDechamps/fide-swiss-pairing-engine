package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The two grammars of TRF CLI surface: a subcommand name first gives the canonical one ({@code pair in.trf -o
 * reply}); anything else the JaVaFo/bbpPairings short form ({@code --dutch in.trf -p reply}).
 */
final class CommandLineParser {

    private static final Map<String, Supplier<PairingSystem>> SYSTEMS =
            Map.of("dutch", PairingSystems::dutch, "dubov", PairingSystems::dubov);

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
            case "check" -> {
                queue.removeFirst();
                yield canonicalCheck(queue);
            }
            case "-check" -> {
                queue.removeFirst();
                yield new Command.Check(required(queue.pollFirst()), Optional.empty(), SettingsOverrides.NONE);
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
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
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
        return pairCommand(input, reply, trace, overrides);
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

    private static Command compatible(Deque<String> queue) {
        String input = null;
        var pair = false;
        var check = false;
        Optional<Integer> round = Optional.empty();
        Optional<String> reply = Optional.empty();
        Optional<String> trace = Optional.empty();
        var overrides = SettingsOverrides.NONE;
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
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
        return pairCommand(input, reply, trace, overrides);
    }

    /** A bare {@code -l} writes the trace beside the input as {@code <input>.trace.txt}. */
    private static Command pairCommand(
            String input, Optional<String> reply, Optional<String> trace, SettingsOverrides overrides) {
        var file = required(input);
        var traceFile = trace.map(given -> given.isEmpty() ? file + ".trace.txt" : given);
        return new Command.Pair(file, reply, traceFile, overrides);
    }

    private static Optional<SettingsOverrides> settingsFlag(
            String argument, Deque<String> queue, SettingsOverrides overrides) {
        if (argument.startsWith("--") && SYSTEMS.containsKey(argument.substring(2))) {
            return Optional.of(overrides.withSystem(system(argument.substring(2))));
        }
        return switch (argument) {
            case "--system" -> Optional.of(overrides.withSystem(system(value(queue, argument))));
            case "--rounds" -> Optional.of(overrides.withRounds(NumberOfRounds.of(number(value(queue, argument)))));
            case "--initial-colour" -> Optional.of(overrides.withInitialColour(colour(value(queue, argument))));
            default -> Optional.empty();
        };
    }

    private static PairingSystem system(String name) {
        var system = SYSTEMS.get(name);
        if (system == null) {
            throw new UsageException("unsupported pairing system " + name);
        }
        return system.get();
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
