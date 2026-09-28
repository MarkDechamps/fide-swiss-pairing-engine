package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Name;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.Title;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** A {@code 001} record: a player's static data (columns 5–89) and its round blocks from column 92 on. */
record PlayerRecord(
        int startRank,
        Participant participant,
        Optional<Points> declaredPoints,
        Optional<Integer> declaredRank,
        List<RoundCell> rounds) {

    private static final int FIRST_ROUND_COLUMN = 92;
    private static final int ROUND_WIDTH = 10;

    static PlayerRecord parse(String line) {
        var startRank = Integer.parseInt(columns(line, 5, 8).trim());
        var participant = new Participant(
                ParticipantId.of(String.valueOf(startRank)),
                Name.of(columns(line, 15, 47).trim()),
                rating(columns(line, 49, 52).trim()),
                title(columns(line, 11, 13).trim()));
        return new PlayerRecord(
                startRank,
                participant,
                points(columns(line, 81, 84).trim()),
                rank(columns(line, 86, 89).trim()),
                roundCells(line));
    }

    RoundCell cellOf(int round) {
        return round <= rounds.size() ? rounds.get(round - 1) : RoundCell.EMPTY;
    }

    private static List<RoundCell> roundCells(String line) {
        var cells = new ArrayList<RoundCell>();
        for (var start = FIRST_ROUND_COLUMN; start <= line.length(); start += ROUND_WIDTH) {
            cells.add(RoundCell.parse(columns(line, start, start + 7)));
        }
        while (!cells.isEmpty() && cells.getLast().equals(RoundCell.EMPTY)) {
            cells.removeLast();
        }
        return List.copyOf(cells);
    }

    private static Optional<Points> points(String field) {
        try {
            return field.isEmpty() ? Optional.empty() : Optional.of(Points.of(field));
        } catch (NumberFormatException e) {
            throw new InvalidTrfException("Points must be a number like 11.5, not '" + field + "'");
        }
    }

    private static Optional<Integer> rank(String field) {
        try {
            return field.isEmpty() ? Optional.empty() : Optional.of(Integer.parseInt(field));
        } catch (NumberFormatException e) {
            throw new InvalidTrfException("A rank must be a whole number, not '" + field + "'");
        }
    }

    private static Rating rating(String field) {
        return field.isEmpty() || Integer.parseInt(field) == 0 ? Rating.unrated() : Rating.of(Integer.parseInt(field));
    }

    private static Optional<Title> title(String field) {
        return java.util.Arrays.stream(Title.values())
                .filter(title -> title.name().equalsIgnoreCase(field))
                .findFirst();
    }

    /** The 1-based, inclusive columns of the FIDE layout, blank where the line is shorter. */
    static String columns(String line, int first, int last) {
        if (line.length() < first) {
            return "";
        }
        return line.substring(first - 1, Math.min(last, line.length()));
    }
}
