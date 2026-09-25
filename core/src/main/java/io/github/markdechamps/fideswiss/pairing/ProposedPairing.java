package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Round;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A pairing for the next round that did not come from the library: an arbiter's manual pairing, another program's
 * output, or a round read from a file, to be checked against the rules and the system.
 */
public record ProposedPairing(List<ProposedBoard> boards, Optional<ParticipantId> pairingAllocatedBye) {

    public ProposedPairing {
        boards = List.copyOf(boards);
        Objects.requireNonNull(pairingAllocatedBye, "pairingAllocatedBye");
    }

    public record ProposedBoard(ParticipantId white, ParticipantId black) {
        public ProposedBoard {
            Objects.requireNonNull(white, "white");
            Objects.requireNonNull(black, "black");
        }
    }

    public static ProposedPairing of(List<ProposedBoard> boards, Optional<ParticipantId> pairingAllocatedBye) {
        return new ProposedPairing(boards, pairingAllocatedBye);
    }

    public static ProposedPairing of(RoundPairing pairing) {
        return new ProposedPairing(
                pairing.boards().stream()
                        .map(board -> new ProposedBoard(board.white(), board.black()))
                        .toList(),
                pairing.pairingAllocatedBye());
    }

    /** The pairing a recorded round was played with, whatever its outcomes. */
    public static ProposedPairing of(Round round) {
        var bye = round.byes().entrySet().stream()
                .filter(entry -> entry.getValue() == Bye.PAIRING_ALLOCATED)
                .map(java.util.Map.Entry::getKey)
                .findFirst();
        return new ProposedPairing(
                round.boards().stream()
                        .map(board -> new ProposedBoard(board.white(), board.black()))
                        .toList(),
                bye);
    }
}
