package io.github.markdechamps.fideswiss.history;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Every participant's history, derived once from a tournament's recorded rounds and shared by all systems. */
public final class TournamentHistory {

    private final Map<ParticipantId, ParticipantHistory> histories;

    private TournamentHistory(Map<ParticipantId, ParticipantHistory> histories) {
        this.histories = Map.copyOf(histories);
    }

    public static TournamentHistory of(Tournament tournament) {
        var histories = new HashMap<ParticipantId, ParticipantHistory>();
        for (var participant : tournament.participants()) {
            histories.put(participant.id(), historyOf(tournament, participant));
        }
        return new TournamentHistory(histories);
    }

    public ParticipantHistory of(ParticipantId participant) {
        var history = histories.get(participant);
        if (history == null) {
            throw new IllegalArgumentException("No history for " + participant);
        }
        return history;
    }

    private static ParticipantHistory historyOf(Tournament tournament, Participant participant) {
        var records = new ArrayList<RoundRecord>();
        var id = participant.id();
        for (var round : tournament.rounds()) {
            records.add(recordOf(tournament, round, id, records));
        }
        return new ParticipantHistory(
                id, records, tournament.settings().scoring().win());
    }

    private static RoundRecord recordOf(
            Tournament tournament, Round round, ParticipantId participant, List<RoundRecord> earlier) {
        var scoring = tournament.settings().scoring();
        return round.boardOf(participant)
                .map(board -> boardRecord(tournament, board, participant))
                .orElseGet(() -> {
                    var bye = round.byeOf(participant)
                            .or(() -> tournament.absenceIn(participant, round.number()))
                            .orElseGet(() -> absence(earlier));
                    return new RoundRecord.NoBoard(
                            bye, scoring.pointsFor(bye, tournament.settings().pairingAllocatedByeValue()));
                });
    }

    private static RoundRecord boardRecord(Tournament tournament, Board board, ParticipantId participant) {
        var colour = board.colourOf(participant);
        var result = board.outcome().resultOf(colour);
        var points = tournament.settings().scoring().pointsFor(result);
        var opponent = board.opponentOf(participant);
        return board.outcome().isPlayed()
                ? new RoundRecord.Game(opponent, colour, result, points)
                : new RoundRecord.Forfeit(opponent, result, points);
    }

    /** A participant a round does not mention had not arrived yet, or had left. */
    private static Bye absence(List<RoundRecord> earlier) {
        var everOnBoard = earlier.stream()
                .anyMatch(record ->
                        !(record instanceof RoundRecord.NoBoard noBoard) || noBoard.bye() != Bye.NOT_YET_ENTERED);
        return everOnBoard ? Bye.WITHDRAWN : Bye.NOT_YET_ENTERED;
    }
}
