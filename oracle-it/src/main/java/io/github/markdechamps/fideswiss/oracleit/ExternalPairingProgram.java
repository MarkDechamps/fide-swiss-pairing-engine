package io.github.markdechamps.fideswiss.oracleit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An Oracle run as a child process with the JaVaFo/bbpPairings protocol: {@code <command> [flags] <in.trf> -p <out>}.
 * Exit code 0 is a pairing; anything else is a refusal.
 */
public final class ExternalPairingProgram implements PairingOracle {

    private static final long TIMEOUT_SECONDS = 120;

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
        var line = new ArrayList<>(command);
        line.addAll(flags);
        line.add(input.toString());
        line.add("-p");
        line.add(reply.toString());
        var process = new ProcessBuilder(line).redirectErrorStream(true).start();
        var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(name + " did not answer within " + TIMEOUT_SECONDS + " seconds");
        }
        if (process.exitValue() != 0) {
            return new OracleAnswer.Refused(process.exitValue(), output.strip());
        }
        return new OracleAnswer.Paired(OraclePairing.fromReply(Files.readString(reply, StandardCharsets.UTF_8)));
    }
}
