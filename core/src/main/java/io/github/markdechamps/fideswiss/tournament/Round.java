package io.github.markdechamps.fideswiss.tournament;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The recorded facts of one round: its boards and the participants who were not on a board, with why. A round
 * records what was played, legal or not (ADR 0004); a participant missing from both is not part of the round.
 */
public record Round(RoundNumber number, List<Board> boards, Map<ParticipantId, Bye> byes) {

    public Round {
        Objects.requireNonNull(number, "number");
        boards = List.copyOf(boards);
        byes = Map.copyOf(byes);
    }

    public static Round of(RoundNumber number, List<Board> boards, Map<ParticipantId, Bye> byes) {
        return new Round(number, boards, byes);
    }

    public Optional<Board> boardOf(ParticipantId participant) {
        return boards.stream().filter(board -> board.seats(participant)).findFirst();
    }

    public Optional<Bye> byeOf(ParticipantId participant) {
        return Optional.ofNullable(byes.get(participant));
    }

    public boolean includes(ParticipantId participant) {
        return byes.containsKey(participant) || boardOf(participant).isPresent();
    }

    /** Whether the participant was taken into account for this round's pairing: on a board or given the PAB. */
    public boolean pairs(ParticipantId participant) {
        return boardOf(participant).isPresent()
                || byeOf(participant).filter(Bye.PAIRING_ALLOCATED::equals).isPresent();
    }

    public Optional<Board> board(BoardNumber board) {
        return boards.stream()
                .filter(candidate -> candidate.number().equals(board))
                .findFirst();
    }

    /** Every participant this round mentions, once per mention, so a repetition shows up as a duplicate. */
    Stream<ParticipantId> mentions() {
        var mentioned = new ArrayList<ParticipantId>();
        boards.forEach(board -> board.participants().forEach(mentioned::add));
        mentioned.addAll(byes.keySet());
        return mentioned.stream();
    }
}
