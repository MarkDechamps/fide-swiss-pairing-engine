package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.standings.DecidingTieBreak;
import io.github.markdechamps.fideswiss.standings.Standing;
import io.github.markdechamps.fideswiss.standings.Standings;
import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code fide-swiss standings}: rank, id, name, score, one column per tie-break in list order, and for a row tied on
 * score with the row above, what separates them ({@code decided by}); {@code --why} prints the full breakdown.
 */
final class StandingsTable {

    private StandingsTable() {}

    static String of(Standings standings) {
        var rows = new ArrayList<List<String>>();
        var header = new ArrayList<>(List.of("rank", "id", "name", "score"));
        standings.tieBreakList().codes().stream().map(TieBreakCode::toString).forEach(header::add);
        header.add("decided by");
        rows.add(header);
        for (var standing : standings.ranked()) {
            rows.add(row(standing));
        }
        return aligned(rows);
    }

    static String why(Standings standings, ParticipantId first, ParticipantId second) {
        var comparison = standings.compare(first, second);
        var text = new StringBuilder();
        text.append(
                        comparison.decidedBy() instanceof DecidingTieBreak.SharedRank
                                ? first + " and " + second + " share a rank: every tie-break is equal"
                                : comparison.higher() + " ranks above " + comparison.lower() + ": "
                                        + comparison.decidedBy())
                .append('\n');
        if (comparison.decidedBy() instanceof DecidingTieBreak.ByTieBreak byTieBreak) {
            breakdown(text, comparison.higher(), byTieBreak.higher());
            breakdown(text, comparison.lower(), byTieBreak.lower());
        }
        return text.toString();
    }

    private static void breakdown(StringBuilder text, ParticipantId participant, TieBreakValue value) {
        text.append("  ")
                .append(participant)
                .append(' ')
                .append(value.code())
                .append(' ')
                .append(value)
                .append(" [")
                .append(value.explanation())
                .append("]\n");
        value.contributions()
                .forEach(
                        contribution -> text.append("    ").append(contribution).append('\n'));
    }

    private static List<String> row(Standing standing) {
        var row = new ArrayList<>(List.of(
                standing.rank().toString(),
                standing.participant().id().toString(),
                standing.participant().name().value(),
                standing.score().toString()));
        standing.tieBreakValues().values().stream().map(TieBreakValue::toString).forEach(row::add);
        row.add(standing.decidedBy().map(Object::toString).orElse(""));
        return row;
    }

    private static String aligned(List<List<String>> rows) {
        var widths = new int[rows.getFirst().size()];
        for (var row : rows) {
            for (var column = 0; column < row.size(); column++) {
                widths[column] = Math.max(widths[column], row.get(column).length());
            }
        }
        var text = new StringBuilder();
        for (var row : rows) {
            var line = new StringBuilder();
            for (var column = 0; column < row.size(); column++) {
                var cell = row.get(column);
                line.append(column == row.size() - 1 ? cell : String.format("%-" + widths[column] + "s  ", cell));
            }
            text.append(line.toString().stripTrailing()).append('\n');
        }
        return text.toString();
    }
}
