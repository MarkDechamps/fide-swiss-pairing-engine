package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Records a hand-written round, the way the Handbook writes a crosstable line: {@code "1-3 1-0"} (white-black and
 * the result: {@code 1-0}, {@code ½}, {@code 0-1}, {@code +-}, {@code -+} for forfeits), {@code "5 PAB"} for the
 * Pairing-Allocated Bye and {@code "5 ½bye"} for a requested half-point bye.
 */
final class RecordedRoundMother {

    private static final Map<String, GameOutcome> RESULTS = Map.of(
            "1-0", GameOutcome.WHITE_WINS,
            "½", GameOutcome.DRAW,
            "0-1", GameOutcome.BLACK_WINS,
            "+-", GameOutcome.WHITE_WINS_BY_FORFEIT,
            "-+", GameOutcome.BLACK_WINS_BY_FORFEIT);

    private static final Map<String, Bye> BYES = Map.of("PAB", Bye.PAIRING_ALLOCATED, "½bye", Bye.HALF_POINT);

    private RecordedRoundMother() {}

    static Tournament withRound(Tournament tournament, String... boardsAndByes) {
        var boards = new ArrayList<Board>();
        var byes = new HashMap<ParticipantId, Bye>();
        for (var entry : boardsAndByes) {
            var parts = entry.split(" ");
            if (BYES.containsKey(parts[1])) {
                byes.put(ParticipantId.of(parts[0]), BYES.get(parts[1]));
            } else {
                var players = parts[0].split("-");
                boards.add(Board.of(boards.size() + 1, players[0], players[1], RESULTS.get(parts[1])));
            }
        }
        return tournament.withRound(Round.of(tournament.nextRound(), boards, byes));
    }
}
