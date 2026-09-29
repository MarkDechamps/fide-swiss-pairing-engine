package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.generator.FlatDrawModel;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * The two grammars of {@code generate} (Random tournament generator): the canonical subcommand with its ranges and
 * tournament flags, and JaVaFo/bbp's {@code -g [<cfg>|<seed>] -o <out> [-s <seed>]}.
 */
final class GenerateArguments {

    /** The profiles' number of rounds is a placeholder: the generator draws the rounds of every tournament. */
    private static final NumberOfRounds DRAWN = NumberOfRounds.of(9);

    private GenerateArguments() {}

    static Command canonical(Deque<String> queue) {
        Optional<Long> seed = Optional.empty();
        var count = 1;
        String output = null;
        var baseline = GeneratorSettings.of(Profiles.individualSwiss(DRAWN));
        Optional<String> configuration = Optional.empty();
        Optional<String> model = Optional.empty();
        var maxiTournament = false;
        var overrides = new ArrayList<UnaryOperator<GeneratorSettings>>();
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            switch (argument) {
                case "--seed" -> seed = Optional.of(seed(CommandLineParser.value(queue, argument)));
                case "--count" -> count = CommandLineParser.number(CommandLineParser.value(queue, argument));
                case "-o" -> output = CommandLineParser.value(queue, argument);
                case "--profile" -> baseline = GeneratorSettings.of(profile(CommandLineParser.value(queue, argument)));
                case "--config" -> configuration = Optional.of(CommandLineParser.value(queue, argument));
                case "--model" -> model = Optional.of(CommandLineParser.value(queue, argument));
                case "--system" -> overrides.add(system(CommandLineParser.value(queue, argument)));
                case "--maxi-tournament" -> maxiTournament = true;
                default -> overrides.add(flag(argument, queue));
            }
        }
        if (maxiTournament) {
            overrides.add(inTournament(
                    tournament -> tournament.with(PairingSystems.asMaxiTournament(tournament.pairingSystem()))));
        }
        if (output == null) {
            throw new UsageException("generate needs -o <file or pattern with %d>");
        }
        return new Command.Generate(seed, count, output, baseline, configuration, model, overrides);
    }

    /** JaVaFo/bbp: {@code [--dutch|--dubov|--burstein|--lim|--double-swiss] -g [<cfg>|<seed>] -o <out> [-s <seed>]}; one tournament. */
    static Command compatible(Deque<String> queue) {
        Optional<Long> seed = Optional.empty();
        Optional<String> configuration = Optional.empty();
        String output = null;
        var overrides = new ArrayList<UnaryOperator<GeneratorSettings>>();
        var given = CommandLineParser.optionalValue(queue);
        if (given.isPresent()) {
            if (given.get().matches("\\d+")) {
                seed = Optional.of(seed(given.get()));
            } else {
                configuration = given;
            }
        }
        while (!queue.isEmpty()) {
            var argument = queue.removeFirst();
            switch (argument) {
                case "-o" -> output = CommandLineParser.value(queue, argument);
                case "-s" -> seed = Optional.of(seed(CommandLineParser.value(queue, argument)));
                default -> {
                    if (!isSystemFlag(argument)) {
                        throw new UsageException("unexpected argument " + argument);
                    }
                    overrides.add(system(argument.substring(2)));
                }
            }
        }
        if (output == null) {
            throw new UsageException("-g needs -o <file>");
        }
        return new Command.Generate(
                seed,
                1,
                output,
                GeneratorSettings.of(Profiles.individualSwiss(DRAWN)),
                configuration,
                Optional.empty(),
                overrides);
    }

    private static UnaryOperator<GeneratorSettings> flag(String flag, Deque<String> queue) {
        if (isSystemFlag(flag)) {
            return system(flag.substring(2));
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
            case "--late-entries" -> withRange(flag, queue, GeneratorSettings::withLateEntryPercentage);
            case "--draw-percentage" -> {
                var model = new FlatDrawModel(CommandLineParser.number(CommandLineParser.value(queue, flag)));
                yield settings -> settings.with(model);
            }
            case "--random-scoring" -> GeneratorSettings::withRandomScoring;
            case "--acceleration" -> acceleration(CommandLineParser.value(queue, flag));
            case "--tiebreaks" -> tieBreaks(CommandLineParser.value(queue, flag));
            case "--edition" -> {
                var edition = CommandLineParser.edition(CommandLineParser.value(queue, flag));
                yield inTournament(tournament -> tournament.with(edition));
            }
            case "--tiebreak-edition" -> {
                var edition = CommandLineParser.tieBreakEdition(CommandLineParser.value(queue, flag));
                yield inTournament(tournament -> tournament.with(edition));
            }
            default -> throw new UsageException("unexpected argument " + flag);
        };
    }

    private static TournamentSettings profile(String name) {
        return switch (name) {
            case "individual-swiss" -> Profiles.individualSwiss(DRAWN);
            case "accelerated-open" -> Profiles.acceleratedOpen(DRAWN);
            case "double-swiss" -> Profiles.doubleSwiss(DRAWN);
            case "team-swiss", "olympiad" ->
                throw new UsageException("the " + name + " profile's system is not implemented yet");
            default -> throw new UsageException("unknown profile " + name);
        };
    }

    private static UnaryOperator<GeneratorSettings> acceleration(String value) {
        return switch (value) {
            case "none" -> inTournament(tournament -> tournament.with(Acceleration.none()));
            case "baku" -> inTournament(tournament -> tournament.with(Acceleration.baku()));
            case "random" ->
                settings -> settings.with(settings.tournament().with(Acceleration.none()))
                        .withRandomAcceleration();
            default -> throw new UsageException("--acceleration takes none, baku or random, not " + value);
        };
    }

    private static UnaryOperator<GeneratorSettings> tieBreaks(String value) {
        if (value.equals("random")) {
            return GeneratorSettings::withRandomTieBreaks;
        }
        var list = TieBreakList.parse(value);
        return inTournament(tournament -> tournament.with(list));
    }

    private static UnaryOperator<GeneratorSettings> inTournament(UnaryOperator<TournamentSettings> change) {
        return settings -> settings.with(change.apply(settings.tournament()));
    }

    private static UnaryOperator<GeneratorSettings> withRange(
            String flag, Deque<String> queue, BiFunction<GeneratorSettings, Range, GeneratorSettings> setter) {
        var text = CommandLineParser.value(queue, flag);
        try {
            var range = Range.parse(text);
            return settings -> setter.apply(settings, range);
        } catch (IllegalArgumentException e) {
            throw new UsageException(flag + " takes a number or a range A..B, not " + text);
        }
    }

    private static boolean isSystemFlag(String argument) {
        return argument.startsWith("--") && CommandLineParser.SYSTEMS.containsKey(argument.substring(2));
    }

    private static UnaryOperator<GeneratorSettings> system(String name) {
        var system = CommandLineParser.system(name);
        return inTournament(tournament -> tournament.with(system));
    }

    private static long seed(String value) {
        try {
            return Long.parseUnsignedLong(value);
        } catch (NumberFormatException e) {
            throw new UsageException("not a seed: " + value);
        }
    }
}
