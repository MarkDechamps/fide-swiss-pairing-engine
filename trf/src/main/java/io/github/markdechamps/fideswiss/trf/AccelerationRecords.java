package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.VirtualPoints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The acceleration a file declares: explicit Virtual Points from {@code 250} (TRF26) or {@code XXA} (JaVaFo),
 * which override a {@code _BAKU} code in {@code 192}; the code alone means Baku (TRF26, Acceleration readings).
 */
final class AccelerationRecords {

    private AccelerationRecords() {}

    static Acceleration read(Map<String, List<String>> records, int numberOfRounds) {
        return read(records, numberOfRounds, false);
    }

    /** For a team event whose primary score is match points, {@code 250}'s match points are the Virtual Points. */
    static Acceleration read(Map<String, List<String>> records, int numberOfRounds, boolean inMatchPoints) {
        var points = new HashMap<ParticipantId, Map<RoundNumber, Points>>();
        records.getOrDefault("XXA", List.of()).forEach(line -> readXxa(line, points));
        records.getOrDefault("250", List.of()).forEach(line -> read250(line, points, numberOfRounds, inMatchPoints));
        if (!points.isEmpty()) {
            return Acceleration.explicit(VirtualPoints.of(points));
        }
        var system = records.getOrDefault("192", List.of()).stream()
                .map(TrfReader::valueOf)
                .findFirst();
        return system.filter(code -> code.toUpperCase().endsWith("_BAKU")).isPresent()
                ? Acceleration.baku()
                : Acceleration.none();
    }

    /** {@code XXA NNNN pp.p pp.p …}: the id at columns 5–8, round r's points at 10+5(r−1). */
    private static void readXxa(String line, Map<ParticipantId, Map<RoundNumber, Points>> points) {
        var id = participant(PlayerRecord.columns(line, 5, 8));
        for (var round = 1; 10 + 5 * (round - 1) <= line.length(); round++) {
            var start = 10 + 5 * (round - 1);
            var value = PlayerRecord.columns(line, start, start + 3).trim();
            if (!value.isEmpty()) {
                add(points, id, RoundNumber.of(round), Points.of(value));
            }
        }
    }

    /**
     * {@code 250}: match points at 5–8 (teams), game points at 10–13, rounds 15–17 to 19–21, ids 23–26 to
     * 28–31. For individuals the game points are the Virtual Points, and for teams on match points the match
     * points, unless the record leaves them blank (TRF26's own team example).
     */
    private static void read250(
            String line,
            Map<ParticipantId, Map<RoundNumber, Points>> points,
            int numberOfRounds,
            boolean inMatchPoints) {
        var matchPoints = PlayerRecord.columns(line, 5, 8).trim();
        var value = Points.of(
                inMatchPoints && !matchPoints.isEmpty()
                        ? matchPoints
                        : PlayerRecord.columns(line, 10, 13).trim());
        var firstRound = number(PlayerRecord.columns(line, 15, 17), 1);
        var lastRound = number(PlayerRecord.columns(line, 19, 21), numberOfRounds);
        var firstId = number(PlayerRecord.columns(line, 23, 26), 1);
        var lastId = number(PlayerRecord.columns(line, 28, 31), firstId);
        for (var id = firstId; id <= lastId; id++) {
            for (var round = firstRound; round <= lastRound; round++) {
                add(points, ParticipantId.of(String.valueOf(id)), RoundNumber.of(round), value);
            }
        }
    }

    private static void add(
            Map<ParticipantId, Map<RoundNumber, Points>> points, ParticipantId id, RoundNumber round, Points value) {
        points.computeIfAbsent(id, key -> new HashMap<>()).merge(round, value, Points::plus);
    }

    private static ParticipantId participant(String field) {
        return ParticipantId.of(String.valueOf(Integer.parseInt(field.trim())));
    }

    private static int number(String field, int absent) {
        var trimmed = field.trim();
        return trimmed.isEmpty() ? absent : Integer.parseInt(trimmed);
    }
}
