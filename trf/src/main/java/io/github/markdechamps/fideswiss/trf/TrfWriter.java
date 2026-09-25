package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Writes a tournament as TRF26: the header records the library's settings can fill ({@code 142}, {@code 152},
 * {@code 162} when the scoring is not the default, {@code 192}), then one {@code 001} record per participant in
 * registration order, whose position is its starting rank. Absences already known for the next round go into
 * one more round column. Lines end with CR LF (TRF CLI surface).
 */
public final class TrfWriter {

    private static final String LINE_END = "\r\n";
    private static final int ROUND_COLUMN = 92;
    private static final int ROUND_WIDTH = 10;

    /** The tournament name for {@code 012}, and whether to add JaVaFo's {@code XXR}/{@code XXC} lines. */
    public record Options(String name, boolean jaVaFoLines) {

        public Options {
            Objects.requireNonNull(name, "name");
        }

        public static Options named(String name) {
            return new Options(name, false);
        }

        /** Also write {@code XXR} and {@code XXC}, which JaVaFo needs to pair (TRF file format research). */
        public Options withJaVaFoLines() {
            return new Options(name, true);
        }
    }

    private final Tournament tournament;
    private final Map<ParticipantId, Integer> startRanks = new HashMap<>();

    private TrfWriter(Tournament tournament) {
        this.tournament = tournament;
        var participants = tournament.participants();
        for (var index = 0; index < participants.size(); index++) {
            startRanks.put(participants.get(index).id(), index + 1);
        }
    }

    public static String write(Tournament tournament, Options options) {
        return new TrfWriter(tournament).text(options);
    }

    private String text(Options options) {
        var lines = new ArrayList<String>();
        var settings = tournament.settings();
        lines.add("012 " + options.name());
        lines.add("142 " + settings.numberOfRounds().value());
        lines.add("152 " + (settings.initialColour().colour() == Colour.WHITE ? "W" : "B"));
        scoringRecord(settings.scoring()).ifPresent(lines::add);
        lines.add("192 " + systemCode(settings.swissRulesEdition()));
        if (options.jaVaFoLines()) {
            lines.add("XXR " + settings.numberOfRounds().value());
            lines.add("XXC " + (settings.initialColour().colour() == Colour.WHITE ? "white1" : "black1"));
        }
        tournament.participants().forEach(participant -> lines.add(playerRecord(participant)));
        return String.join(LINE_END, lines) + LINE_END;
    }

    /** Only the Dutch System is implemented, so the code follows the Swiss Rules Edition alone. */
    private static String systemCode(SwissRulesEdition edition) {
        return edition == SwissRulesEdition.EDITION_2026 ? "FIDE_DUTCH_2026" : "FIDE_DUTCH_2017";
    }

    /** {@code 162}: symbol at column 6 with points at 7–10, each next pair 9 columns on; defaults left out. */
    private static Optional<String> scoringRecord(ScoringScheme scoring) {
        var standard = ScoringScheme.standard();
        var values = new LinkedHashMap<Character, Points>();
        putIfDifferent(values, 'W', scoring.win(), standard.win());
        putIfDifferent(values, 'D', scoring.draw(), standard.draw());
        putIfDifferent(values, 'L', scoring.loss(), standard.loss());
        scoring.pairingAllocatedBye().ifPresent(value -> values.put('P', value));
        if (values.isEmpty()) {
            return Optional.empty();
        }
        var line = new StringBuilder("162");
        var column = 6;
        for (var value : values.entrySet()) {
            put(line, column, value.getKey() + String.format("%4s", decimal(value.getValue())));
            column += 9;
        }
        return Optional.of(line.toString());
    }

    private static void putIfDifferent(Map<Character, Points> values, char symbol, Points value, Points standard) {
        if (!value.equals(standard)) {
            values.put(symbol, value);
        }
    }

    private String playerRecord(Participant participant) {
        var line = new StringBuilder();
        put(line, 1, "001");
        put(line, 5, String.format("%4d", startRanks.get(participant.id())));
        put(line, 11, String.format("%3s", participant.title().map(Enum::name).orElse("")));
        put(line, 15, truncated(participant.name().value(), 33));
        if (participant.rating().isRated()) {
            put(line, 49, String.format("%4d", participant.rating().valueOrZero()));
        }
        put(line, 81, String.format("%4s", decimal(pointsOf(participant.id()))));
        var column = ROUND_COLUMN;
        for (var round : tournament.rounds()) {
            put(line, column, cellOf(round, participant.id()));
            column += ROUND_WIDTH;
        }
        var absence = tournament.absencesInNextRound().get(participant.id());
        if (absence != null && tournament.settings().numberOfRounds().includes(tournament.nextRound())) {
            put(line, column, "0000 - " + byeCode(absence));
        }
        return line.toString().stripTrailing();
    }

    private String cellOf(Round round, ParticipantId participant) {
        return round.boardOf(participant)
                .map(board -> boardCell(board, participant))
                .orElseGet(() -> "0000 - " + byeCode(round.byeOf(participant).orElse(Bye.ZERO_POINT)));
    }

    private String boardCell(Board board, ParticipantId participant) {
        var colour = board.colourOf(participant);
        var opponent = startRanks.get(board.opponentOf(participant));
        var result = board.outcome().resultOf(colour);
        var code = board.outcome().isPlayed() ? playedCode(result) : (result == GameResult.WIN ? '+' : '-');
        return String.format("%4d %c %c", opponent, colour == Colour.WHITE ? 'w' : 'b', code);
    }

    private static char playedCode(GameResult result) {
        return switch (result) {
            case WIN -> '1';
            case DRAW -> '=';
            case LOSS -> '0';
        };
    }

    private static char byeCode(Bye bye) {
        return switch (bye) {
            case PAIRING_ALLOCATED -> 'U';
            case FULL_POINT -> 'F';
            case HALF_POINT -> 'H';
            case ZERO_POINT, WITHDRAWN, NOT_YET_ENTERED -> 'Z';
        };
    }

    /** Points 81–84: the standings score under the tournament's scoring. */
    private Points pointsOf(ParticipantId participant) {
        var scoring = tournament.settings().scoring();
        var pabValue = tournament.settings().pairingAllocatedByeValue();
        var total = Points.ZERO;
        for (var round : tournament.rounds()) {
            var points = round.boardOf(participant)
                    .map(board -> scoring.pointsFor(board.outcome().resultOf(board.colourOf(participant))))
                    .orElseGet(() -> scoring.pointsFor(round.byeOf(participant).orElse(Bye.ZERO_POINT), pabValue));
            total = total.plus(points);
        }
        return total;
    }

    private static String decimal(Points points) {
        return points.toBigDecimal().setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    private static String truncated(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width);
    }

    /** Writes {@code value} from the 1-based column {@code column} on. */
    private static void put(StringBuilder line, int column, String value) {
        pad(line, column - 1);
        line.replace(column - 1, column - 1 + value.length(), value);
    }

    private static void pad(StringBuilder line, int length) {
        while (line.length() < length) {
            line.append(' ');
        }
    }
}
