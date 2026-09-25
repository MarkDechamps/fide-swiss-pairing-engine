package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.InvalidTournamentException;
import io.github.markdechamps.fideswiss.tournament.Outcome;
import io.github.markdechamps.fideswiss.tournament.PairingNumbers;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * The pairing of one round: its boards in GHR 3.6 order, the Pairing-Allocated Bye, everyone else left out with
 * the reason, the Pairing Numbers it was made with, and the trace of how it was reached.
 */
public record RoundPairing(
        RoundNumber roundNumber,
        List<PairedBoard> boards,
        Optional<ParticipantId> pairingAllocatedBye,
        Map<ParticipantId, Bye> unpaired,
        PairingNumbers pairingNumbers,
        PairingTrace trace) {

    public RoundPairing {
        Objects.requireNonNull(roundNumber, "roundNumber");
        boards = List.copyOf(boards);
        Objects.requireNonNull(pairingAllocatedBye, "pairingAllocatedBye");
        unpaired = Map.copyOf(unpaired);
        Objects.requireNonNull(pairingNumbers, "pairingNumbers");
        Objects.requireNonNull(trace, "trace");
    }

    /** The round as played, given every board's outcome. */
    public Round completedWith(Map<BoardNumber, ? extends Outcome> outcomes) {
        var played = boards.stream()
                .map(board -> new Board(board.number(), board.white(), board.black(), outcomeOf(board, outcomes)))
                .toList();
        var byes = new HashMap<>(unpaired);
        pairingAllocatedBye.ifPresent(participant -> byes.put(participant, Bye.PAIRING_ALLOCATED));
        return Round.of(roundNumber, played, byes);
    }

    /** The same boards, colours and PAB, whatever the board order or trace: the equality Oracle runs use. */
    public boolean samePairingAs(RoundPairing other) {
        return boardsAsSet().equals(other.boardsAsSet()) && pairingAllocatedBye.equals(other.pairingAllocatedBye);
    }

    private java.util.Set<List<ParticipantId>> boardsAsSet() {
        return boards.stream()
                .map(board -> List.of(board.white(), board.black()))
                .collect(java.util.stream.Collectors.toSet());
    }

    private static Outcome outcomeOf(PairedBoard board, Map<BoardNumber, ? extends Outcome> outcomes) {
        var outcome = outcomes.get(board.number());
        if (outcome == null) {
            throw new InvalidTournamentException(Problem.of("No outcome for board " + board.number()));
        }
        return outcome;
    }
}
