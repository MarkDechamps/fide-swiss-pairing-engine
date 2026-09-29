package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Writes a tournament as TRF26: the header records the library's settings can fill ({@code 142}, {@code 152},
 * {@code 162} when the scoring is not the default, {@code 192} with {@code _BAKU} for Baku acceleration, {@code 212}
 * for the Tie-break List), then one {@code 001} record per participant in
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
        if (tournament.settings().pairingSystem().competitionType() == CompetitionType.TEAM) {
            return TeamTrfWriter.write(tournament, options);
        }
        return new TrfWriter(tournament).text(options);
    }

    private String text(Options options) {
        var lines = new ArrayList<String>();
        var settings = tournament.settings();
        lines.add("012 " + options.name());
        lines.add("142 " + settings.numberOfRounds().value() * gamesInSuccession());
        lines.add("152 " + (settings.initialColour().colour() == Colour.WHITE ? "W" : "B"));
        scoringRecord(settings).ifPresent(lines::add);
        lines.add("192 " + PairingSystemCode.of(settings) + accelerationSuffix(settings.acceleration()));
        if (!settings.tieBreakList().isEmpty()) {
            lines.add("212 " + tieBreakRecord(settings.tieBreakList()));
        }
        if (options.jaVaFoLines()) {
            lines.add("XXR " + settings.numberOfRounds().value());
            lines.add("XXC " + (settings.initialColour().colour() == Colour.WHITE ? "white1" : "black1"));
        }
        tournament.participants().forEach(participant -> lines.add(playerRecord(participant)));
        return String.join(LINE_END, lines) + LINE_END;
    }

    /** Baku acceleration travels as the {@code _BAKU} suffix of the system code (Acceleration readings). */
    static String accelerationSuffix(Acceleration acceleration) {
        return acceleration instanceof Acceleration.Baku ? "_BAKU" : "";
    }

    /** {@code 212}: the Tie-break List after {@code PTS}, the score, comma-separated as TRF26 lists it. */
    static String tieBreakRecord(TieBreakList list) {
        var codes = new ArrayList<String>();
        codes.add("PTS");
        list.codes().forEach(code -> codes.add(code.toString()));
        return String.join(",", codes);
    }

    /**
     * {@code 162}: symbol at column 6 with points at 7–10, each next pair 9 columns on; defaults left out. A
     * Double-Swiss file always has the per-match bye values {@code P}, {@code F} and {@code H} (ADR 0007).
     */
    private static Optional<String> scoringRecord(TournamentSettings settings) {
        var scoring = settings.scoring();
        var standard = ScoringScheme.standard();
        var values = new LinkedHashMap<Character, Points>();
        putIfDifferent(values, 'W', scoring.win(), standard.win());
        putIfDifferent(values, 'D', scoring.draw(), standard.draw());
        putIfDifferent(values, 'L', scoring.loss(), standard.loss());
        scoring.pairingAllocatedBye().ifPresent(value -> values.put('P', value));
        if (settings.pairingSystem().gamesInSuccession() == 2) {
            var pab = settings.pairingAllocatedByeValue();
            values.put('P', pab);
            values.put('F', scoring.pointsFor(Bye.FULL_POINT, pab));
            values.put('H', scoring.pointsFor(Bye.HALF_POINT, pab));
        }
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
            for (var game = 0; game < gamesInSuccession(); game++) {
                put(line, column, cellOf(round, participant.id(), game));
                column += ROUND_WIDTH;
            }
        }
        var absence = tournament.absencesInNextRound().get(participant.id());
        if (absence != null && tournament.settings().numberOfRounds().includes(tournament.nextRound())) {
            for (var game = 0; game < gamesInSuccession(); game++) {
                put(line, column, "0000 - " + byeCode(absence));
                column += ROUND_WIDTH;
            }
        }
        return line.toString().stripTrailing();
    }

    /** Two games in succession take one column each, the colours reversed in game 2 (ADR 0007). */
    private int gamesInSuccession() {
        return tournament.settings().pairingSystem().gamesInSuccession();
    }

    private String cellOf(Round round, ParticipantId participant, int game) {
        return round.boardOf(participant)
                .map(board -> boardCell(board, participant, game))
                .orElseGet(() -> "0000 - " + byeCode(round.byeOf(participant).orElse(Bye.ZERO_POINT)));
    }

    private String boardCell(Board board, ParticipantId participant, int game) {
        var side = board.colourOf(participant);
        var outcome = gamesInSuccession() == 1
                ? board.outcome()
                : ((MatchOutcome) board.outcome()).games().get(game);
        var colour = game % 2 == 0 ? side : side.opposite();
        var opponent = startRanks.get(board.opponentOf(participant));
        var result = outcome.resultOf(side);
        var code = outcome.isPlayed() ? playedCode(result) : (result == GameResult.WIN ? '+' : '-');
        return String.format("%4d %c %c", opponent, colour == Colour.WHITE ? 'w' : 'b', code);
    }

    static char playedCode(GameResult result) {
        return switch (result) {
            case WIN -> '1';
            case DRAW -> '=';
            case LOSS -> '0';
        };
    }

    static char byeCode(Bye bye) {
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
                    .map(board -> scoring.pointsFor(board.outcome(), board.colourOf(participant)))
                    .orElseGet(() -> scoring.pointsFor(round.byeOf(participant).orElse(Bye.ZERO_POINT), pabValue));
            total = total.plus(points);
        }
        return total;
    }

    private static String decimal(Points points) {
        return points.toBigDecimal().setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    static String truncated(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width);
    }

    /** Writes {@code value} from the 1-based column {@code column} on. */
    static void put(StringBuilder line, int column, String value) {
        pad(line, column - 1);
        line.replace(column - 1, column - 1 + value.length(), value);
    }

    private static void pad(StringBuilder line, int length) {
        while (line.length() < length) {
            line.append(' ');
        }
    }
}
