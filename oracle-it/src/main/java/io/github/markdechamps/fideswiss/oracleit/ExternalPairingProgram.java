package io.github.markdechamps.fideswiss.oracleit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * An Oracle run as a child process with the JaVaFo/bbpPairings protocol: {@code <command> [flags] <in.trf> -p <out>}.
 * Exit code 0 is a pairing; anything else is a refusal. The same program is also run as its own checker ({@code <in.trf> -c})
 * and as its own random tournament generator ({@code -g <config> -o <out> [-s seed]}).
 */
public final class ExternalPairingProgram implements PairingOracle, OracleChecker, OracleGenerator {

    private static final long TIMEOUT_SECONDS = 120;
    private static final Pattern ROUND_LINE = Pattern.compile(".+: Round #\\d+");

    private final String name;
    private final List<String> command;
    private final List<String> flags;
    private final OracleDialect dialect;

    public ExternalPairingProgram(String name, List<String> command, List<String> flags, OracleDialect dialect) {
        this.name = name;
        this.command = List.copyOf(command);
        this.flags = List.copyOf(flags);
        this.dialect = dialect;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public OracleDialect dialect() {
        return dialect;
    }

    @Override
    public OracleAnswer pair(String trf) {
        try {
            var input = Files.createTempFile("oracle-in", ".trf");
            var reply = Files.createTempFile("oracle-out", ".txt");
            try {
                Files.writeString(input, trf, StandardCharsets.UTF_8);
                return run(input, reply);
            } finally {
                Files.deleteIfExists(input);
                Files.deleteIfExists(reply);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(name + " was interrupted", e);
        }
    }

    private OracleAnswer run(Path input, Path reply) throws IOException, InterruptedException {
        var result = execute(List.of(input.toString(), "-p", reply.toString()));
        if (result.exitCode() != 0) {
            return new OracleAnswer.Refused(result.exitCode(), result.output());
        }
        return new OracleAnswer.Paired(OraclePairing.fromReply(Files.readString(reply, StandardCharsets.UTF_8)));
    }

    /**
     * Both programs check with {@code <in.trf> -c}, print {@code name: Round #n} for each round they accept, and exit 0
     * even when a round differs: then they print the two pairings. So the file is accepted only when the exit code is
     * 0 and the output is nothing but those round lines.
     */
    @Override
    public OracleVerdict check(String trf) {
        try {
            var input = Files.createTempFile("oracle-check", ".trf");
            try {
                Files.writeString(input, trf, StandardCharsets.UTF_8);
                var result = execute(List.of(input.toString(), "-c"));
                var onlyRounds = result.output()
                        .lines()
                        .map(String::strip)
                        .filter(line -> !line.isEmpty())
                        .allMatch(line -> ROUND_LINE.matcher(line).matches());
                return result.exitCode() == 0 && onlyRounds
                        ? new OracleVerdict.Accepted()
                        : new OracleVerdict.Rejected(
                                result.exitCode(), result.output().strip());
            } finally {
                Files.deleteIfExists(input);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(name + " was interrupted", e);
        }
    }

    @Override
    public boolean isReproducible() {
        return dialect == OracleDialect.BBP;
    }

    @Override
    public String generate(long seed, String configuration) {
        try {
            var config = Files.createTempFile("oracle-config", ".txt");
            var output = Files.createTempFile("oracle-generated", ".trf");
            try {
                Files.writeString(config, configuration, StandardCharsets.UTF_8);
                var arguments = new ArrayList<>(List.of("-g", config.toString(), "-o", output.toString()));
                if (isReproducible()) {
                    arguments.addAll(List.of("-s", Long.toString(seed)));
                }
                var result = execute(arguments);
                if (result.exitCode() != 0) {
                    throw new GenerationFailed(name + " could not generate a tournament: " + result.output());
                }
                return Files.readString(output, StandardCharsets.UTF_8);
            } finally {
                Files.deleteIfExists(config);
                Files.deleteIfExists(output);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(name + " was interrupted", e);
        }
    }

    private record Result(int exitCode, String output) {}

    private Result execute(List<String> arguments) throws IOException, InterruptedException {
        var line = new ArrayList<>(command);
        line.addAll(flags);
        line.addAll(arguments);
        var process = new ProcessBuilder(line).redirectErrorStream(true).start();
        var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(name + " did not answer within " + TIMEOUT_SECONDS + " seconds");
        }
        return new Result(process.exitValue(), output);
    }
}
