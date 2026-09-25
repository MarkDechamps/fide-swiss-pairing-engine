package io.github.markdechamps.fideswiss.tournament;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

/** Tournaments of participants "1", "2", … whose ratings fall with their number, so the id is the start rank. */
public final class TournamentMother {

    private static final int TOP_RATING = 2500;

    private TournamentMother() {}

    public static Tournament individualSwiss(int participants, int rounds) {
        return Tournament.of(participants(participants), NumberOfRounds.of(rounds));
    }

    public static List<Participant> participants(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(TournamentMother::participant)
                .toList();
    }

    public static Participant participant(int startRank) {
        return Participant.of(
                ParticipantId.of(String.valueOf(startRank)),
                Name.of("Player " + startRank),
                Rating.of(TOP_RATING - 10 * startRank));
    }

    /** Pairs the next round and records it with these outcomes, board by board. */
    public static Tournament playNextRound(Tournament tournament, Outcome... outcomes) {
        var pairing = tournament.pairNextRound();
        var byBoard = new HashMap<BoardNumber, Outcome>();
        for (var board = 0; board < outcomes.length; board++) {
            byBoard.put(BoardNumber.of(board + 1), outcomes[board]);
        }
        return tournament.withRound(pairing.completedWith(byBoard));
    }

    public static ParticipantId id(int startRank) {
        return ParticipantId.of(String.valueOf(startRank));
    }
}
