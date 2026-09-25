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

    /** A Correction of the outcome on the participant's board (GHR 4.3). */
    public Round withOutcome(ParticipantId participant, Outcome corrected) {
        var correctedBoards = boards.stream()
                .map(board -> board.seats(participant)
                        ? new Board(board.number(), board.white(), board.black(), corrected)
                        : board)
                .toList();
        return new Round(number, correctedBoards, byes);
    }

    /** A Correction of a board's colours (GHR 4.3): the players swap colours, the same player still wins. */
    public Round withColoursSwapped(BoardNumber swapped) {
        var corrected = boards.stream()
                .map(board -> board.number().equals(swapped)
                        ? new Board(
                                board.number(),
                                board.black(),
                                board.white(),
                                board.outcome().mirrored())
                        : board)
                .toList();
        return new Round(number, corrected, byes);
    }
}
