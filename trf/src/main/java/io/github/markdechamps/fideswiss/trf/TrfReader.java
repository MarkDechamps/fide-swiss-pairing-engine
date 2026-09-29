package io.github.markdechamps.fideswiss.trf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/** Reads a Tournament Report File (TRF26, TRF16 and the JaVaFo {@code XX?} lines) into the library's model. */
public final class TrfReader {

    private TrfReader() {}

    /** Lines may end with CR, LF or CRLF. Unknown records are kept aside, never an error. */
    public static TrfTournament read(String text) {
        return read(text, Optional.empty());
    }

    /**
     * As {@link #read(String)}, with the Double-Swiss encoding (two TRF rounds per match, ADR 0007) switched on or
     * off whatever {@code 192} says: for a pairing system chosen on the command line.
     */
    public static TrfTournament read(String text, boolean doubleSwiss) {
        return read(text, Optional.of(doubleSwiss));
    }

    private static TrfTournament read(String text, Optional<Boolean> doubleSwiss) {
        var players = new ArrayList<PlayerRecord>();
        var records = new LinkedHashMap<String, List<String>>();
        // Records keep their whole line: some, like XXA and 250, are read by column.
        for (var line : text.split("\r\n|\r|\n")) {
            if (line.length() < 3 || line.startsWith("###")) {
                continue;
            }
            var code = line.substring(0, 3);
            if (code.equals("001")) {
                players.add(PlayerRecord.parse(line));
            } else {
                records.computeIfAbsent(code, key -> new ArrayList<>()).add(line);
            }
        }
        players.sort(Comparator.comparingInt(PlayerRecord::startRank));
        return TrfTournament.of(players, records, doubleSwiss);
    }

    /** A header record's value: everything from column 5 on. */
    static String valueOf(String line) {
        return line.length() > 4 ? line.substring(4).strip() : "";
    }

    static List<String> words(String value) {
        return Arrays.stream(value.trim().split("\\s+"))
                .filter(word -> !word.isEmpty())
                .toList();
    }
}
