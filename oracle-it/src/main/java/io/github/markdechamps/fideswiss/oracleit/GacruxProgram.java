package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.trf.TrfReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Gacrux (FIDE's TieBreakServer, MIT) as the Oracle of the Swiss Team System (C.04.6), run as a Python child process
 * on a clone of its repository: {@code python -c <driver> <clone> <in.trf> <round>}. The driver is ours; it only
 * imports Gacrux's own modules. Unpatched, Gacrux is the reference app the engine agrees with by default (ADR 0009).
 * With {@code tpnOrder} the driver puts Ruling G1 in front of its bracket seating, so that within a bracket the top
 * member of a pair is the team with the smaller TPN whatever its score (C.04.6 3.6.1; Known Divergence KD-1), which
 * is the literal reading of the text ({@code bracket-seating=tpn}). Gacrux prints the pairing as a count line then
 * {@code white black} lines with 0 for the PAB, and leaves teams unpaired (a 0 opposite a second team) when the round
 * cannot be completed, which is a refusal. Nothing of Gacrux is linked or copied here.
 */
public final class GacruxProgram implements PairingOracle {

    private static final long TIMEOUT_SECONDS = 120;

    private static final String PATCH = """
            import crosstablefideteam as ctf
            original = ctf.crosstable_fideteam.update_bracket
            ctf.crosstable_fideteam.update_bracket = (
                lambda self, sl, nodes, edges: original(self, sl, sorted(nodes, key=lambda n: n["tpn"]), edges))
            """;

    /** Runs {@code pairingchecker.py -p -n ROUND -dT}, with {@code update_bracket} over the nodes in TPN order if patched. */
    private static final String DRIVER = """
            import os, runpy, sys
            clone, trf, number = sys.argv[1:4]
            sys.path.insert(0, os.path.join(clone, "gacrux"))
            %s
            sys.argv = ["pairingchecker.py", "-i", trf, "-p", "-n", number, "-dT"]
            os.chdir(os.path.join(clone, "gacrux"))
            runpy.run_path("pairingchecker.py", run_name="__main__")
            """;

    private final String name;
    private final Path clone;
    private final String python;
    private final boolean tpnOrder;

    public GacruxProgram(String name, Path clone, String python, boolean tpnOrder) {
        this.tpnOrder = tpnOrder;
        this.name = name;
        this.clone = clone;
        this.python = python;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public OracleDialect dialect() {
        return OracleDialect.GACRUX;
    }

    @Override
    public OracleAnswer pair(String trf) {
        try {
            var input = Files.createTempFile("gacrux-in", ".trf");
            try {
                Files.writeString(input, trf, StandardCharsets.UTF_8);
                return run(input, TrfReader.read(trf).tournament().nextRound().value());
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

    private OracleAnswer run(Path input, int round) throws IOException, InterruptedException {
        var line = new ArrayList<String>();
        line.add(python);
        line.add("-c");
        line.add(DRIVER.formatted(tpnOrder ? PATCH.strip() : "pass"));
        line.add(clone.toAbsolutePath().toString());
        line.add(input.toAbsolutePath().toString());
        line.add(String.valueOf(round));
        var process = new ProcessBuilder(line).redirectErrorStream(true).start();
        var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException(name + " did not answer within " + TIMEOUT_SECONDS + " seconds");
        }
        if (process.exitValue() != 0) {
            return new OracleAnswer.Refused(process.exitValue(), output.strip());
        }
        return answer(output);
    }

    /** A reply that is not a whole pairing (a second PAB, a team left alone) is a refusal, as in the Oracle driver. */
    private static OracleAnswer answer(String output) {
        var lines = output.lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .toList();
        if (lines.isEmpty() || !lines.getFirst().matches("\\d+")) {
            return new OracleAnswer.Refused(0, output.strip());
        }
        var count = Integer.parseInt(lines.getFirst());
        var byes = lines.stream()
                .skip(1)
                .limit(count)
                .filter(line -> line.split("\\s+").length == 2 && line.endsWith(" 0"))
                .count();
        if (lines.size() < count + 1 || byes > 1) {
            return new OracleAnswer.Refused(0, "not a whole pairing: " + output.strip());
        }
        var reply = String.join("\n", lines.subList(0, count + 1));
        return new OracleAnswer.Paired(OraclePairing.fromReply(reply));
    }
}
