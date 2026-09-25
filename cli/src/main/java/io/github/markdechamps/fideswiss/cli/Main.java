package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.InvalidTournamentException;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.trf.InvalidTrfException;
import io.github.markdechamps.fideswiss.trf.TrfReader;
import io.github.markdechamps.fideswiss.trf.TrfTournament;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** The {@code fide-swiss} command line: pairs TRF files and speaks the JaVaFo/bbpPairings protocol (ADR 0005). */
public final class Main {

    static final int SUCCESS = 0;
    static final int NO_LEGAL_PAIRING = 1;
    static final int INTERNAL_ERROR = 2;
    static final int INVALID_REQUEST = 3;
    static final int FILE_ACCESS_ERROR = 5;
    static final int INCONSISTENT = 6;

    private final InputStream in;
    private final PrintStream out;
    private final PrintStream err;

    Main(InputStream in, PrintStream out, PrintStream err) {
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public static void main(String[] arguments) {
        System.exit(new Main(System.in, System.out, System.err).run(arguments));
    }

    int run(String... arguments) {
        try {
            return execute(CommandLineParser.parse(List.of(arguments)));
        } catch (UsageException | InvalidTrfException e) {
            return fail(INVALID_REQUEST, "error: " + e.getMessage());
        } catch (InvalidSettingsException | InvalidTournamentException e) {
            return fail(INVALID_REQUEST, problems(e.problems()));
        } catch (NoLegalPairingException e) {
            return fail(
                    NO_LEGAL_PAIRING,
                    problems(e.problems()) + System.lineSeparator() + e.trace().describe());
        } catch (UncheckedIOException e) {
            return fail(FILE_ACCESS_ERROR, "error: " + e.getCause().getMessage());
        } catch (RuntimeException e) {
            return fail(INTERNAL_ERROR, "error: internal: " + e);
        }
    }

    private int execute(Command command) {
        return switch (command) {
            case Command.Pair pair -> pair(pair);
            case Command.Check check ->
                new PairingsChecker(out).check(read(check.input(), check.overrides()), check.round());
            case Command.Version version -> print(Version.describe());
            case Command.Help help -> print(Help.TEXT);
        };
    }

    private int pair(Command.Pair command) {
        var tournament = read(command.input(), command.overrides()).tournament();
        var pairing = tournament.pairNextRound();
        command.trace().ifPresent(trace -> write(trace, pairing.trace().describe() + "\n"));
        var reply = PairingReply.of(pairing);
        command.reply().ifPresentOrElse(replyFile -> write(replyFile, reply), () -> out.print(reply));
        return SUCCESS;
    }

    private TrfTournament read(String input, SettingsOverrides overrides) {
        var file = TrfReader.read(readInput(input));
        return file.with(overrides.applyTo(file.settings()));
    }

    private int print(String text) {
        out.print(text);
        return SUCCESS;
    }

    private String readInput(String input) {
        try {
            var bytes = input.equals("-") ? in.readAllBytes() : Files.readAllBytes(Path.of(input));
            return Encoding.decode(bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(String file, String text) {
        try {
            Files.writeString(Path.of(file), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int fail(int exitCode, String message) {
        err.println(message);
        return exitCode;
    }

    private static String problems(List<Problem> problems) {
        var lines = new ArrayList<String>();
        for (var problem : problems) {
            var article = problem.article().map(value -> "[" + value + "] ").orElse("");
            var involved = problem.participants().isEmpty()
                    ? ""
                    : " ("
                            + String.join(
                                    ", ",
                                    problem.participants().stream()
                                            .map(Object::toString)
                                            .toList()) + ")";
            lines.add("error: " + article + problem.message() + involved);
        }
        return String.join(System.lineSeparator(), lines);
    }
}
