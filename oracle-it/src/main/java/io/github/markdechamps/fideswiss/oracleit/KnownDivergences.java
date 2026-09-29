package io.github.markdechamps.fideswiss.oracleit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The machine-readable companion of {@code docs/verification/known-divergences.md}: for each Known Divergence that
 * shows in an Oracle's input, the Oracle it concerns and a pattern its input matches. A difference the register
 * recognises is reported apart; any other is a failure of the gate.
 */
public final class KnownDivergences {

    /** One registered divergence: {@code id} (KD-n), the start of the Oracle's label, and the input it shows in. */
    public record Entry(String id, String oracle, Pattern input) {

        boolean covers(String oracleName, String oracleInput) {
            return oracleName.startsWith(oracle) && input.matcher(oracleInput).find();
        }
    }

    private static final KnownDivergences NONE = new KnownDivergences(List.of());

    private final List<Entry> entries;

    public KnownDivergences(List<Entry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static KnownDivergences none() {
        return NONE;
    }

    /** The register shipped with the module ({@code known-divergences.tsv}). */
    public static KnownDivergences register() {
        try (var in = KnownDivergences.class.getResourceAsStream("/known-divergences.tsv")) {
            if (in == null) {
                throw new IllegalStateException("known-divergences.tsv is missing");
            }
            return parse(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Tab-separated lines {@code id, oracle, regex}; blank lines and lines starting with {@code #} are skipped. */
    public static KnownDivergences parse(String text) {
        var entries = new ArrayList<Entry>();
        for (var line : text.split("\r?\n")) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            var cells = line.split("\t", 3);
            if (cells.length != 3) {
                throw new IllegalArgumentException("a register line has id, oracle and pattern: " + line);
            }
            entries.add(new Entry(cells[0], cells[1], Pattern.compile(cells[2])));
        }
        return new KnownDivergences(entries);
    }

    /** The id of the first registered divergence the difference falls under. */
    public Optional<String> classify(String oracleName, String oracleInput) {
        return entries.stream()
                .filter(entry -> entry.covers(oracleName, oracleInput))
                .map(Entry::id)
                .findFirst();
    }
}
