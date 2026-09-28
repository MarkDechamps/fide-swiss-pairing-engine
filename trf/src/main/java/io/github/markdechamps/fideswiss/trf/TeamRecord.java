package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Name;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Rating;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@code 310} record (TRF26): the team's number (columns 5–7), name (9–40), strength factor (48–53), match and
 * game points (55–60, 62–67), rank (69–71) and its members' starting ranks from column 74 in steps of 5.
 */
record TeamRecord(
        int number,
        Participant participant,
        Optional<Points> declaredMatchPoints,
        Optional<Points> declaredGamePoints,
        Optional<Integer> declaredRank,
        List<Integer> members) {

    private static final int FIRST_MEMBER_COLUMN = 74;
    private static final int MEMBER_WIDTH = 5;

    static TeamRecord parse(String line) {
        var number = Integer.parseInt(PlayerRecord.columns(line, 5, 7).trim());
        var strength = PlayerRecord.columns(line, 48, 53).trim();
        var participant = Participant.of(
                ParticipantId.of(String.valueOf(number)),
                Name.of(PlayerRecord.columns(line, 9, 40).trim()),
                strength.isEmpty() || Double.parseDouble(strength) == 0
                        ? Rating.unrated()
                        : Rating.of((int) Math.round(Double.parseDouble(strength))));
        return new TeamRecord(
                number,
                participant,
                points(PlayerRecord.columns(line, 55, 60)),
                points(PlayerRecord.columns(line, 62, 67)),
                whole(PlayerRecord.columns(line, 69, 71)),
                members(line));
    }

    ParticipantId id() {
        return participant.id();
    }

    private static List<Integer> members(String line) {
        var members = new ArrayList<Integer>();
        for (var start = FIRST_MEMBER_COLUMN; start <= line.length(); start += MEMBER_WIDTH) {
            whole(PlayerRecord.columns(line, start, start + 3))
                    .filter(rank -> rank > 0)
                    .ifPresent(members::add);
        }
        return List.copyOf(members);
    }

    private static Optional<Points> points(String field) {
        var trimmed = field.trim();
        try {
            return trimmed.isEmpty() ? Optional.empty() : Optional.of(Points.of(trimmed));
        } catch (NumberFormatException e) {
            throw new InvalidTrfException("Team points must be a number like 11.5, not '" + trimmed + "'");
        }
    }

    private static Optional<Integer> whole(String field) {
        var trimmed = field.trim();
        try {
            return trimmed.isEmpty() ? Optional.empty() : Optional.of(Integer.parseInt(trimmed));
        } catch (NumberFormatException e) {
            throw new InvalidTrfException("Expected a whole number in a 310 record, not '" + trimmed + "'");
        }
    }
}
