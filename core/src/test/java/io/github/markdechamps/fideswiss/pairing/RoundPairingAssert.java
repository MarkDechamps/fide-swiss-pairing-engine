package io.github.markdechamps.fideswiss.pairing;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;

/** Reads a round's boards as "white-black" strings of participant ids, the way the Handbook writes them. */
public final class RoundPairingAssert {

    private final RoundPairing pairing;

    private RoundPairingAssert(RoundPairing pairing) {
        this.pairing = pairing;
    }

    public static RoundPairingAssert assertThatPairing(RoundPairing pairing) {
        return new RoundPairingAssert(pairing);
    }

    public RoundPairingAssert hasBoards(String... whiteBlack) {
        assertThat(boards()).containsExactly(whiteBlack);
        return this;
    }

    public RoundPairingAssert hasBoardsInAnyOrder(String... whiteBlack) {
        assertThat(boards()).containsExactlyInAnyOrder(whiteBlack);
        return this;
    }

    public RoundPairingAssert givesPairingAllocatedByeTo(String participant) {
        assertThat(pairing.pairingAllocatedBye()).contains(ParticipantId.of(participant));
        return this;
    }

    public RoundPairingAssert givesNoPairingAllocatedBye() {
        assertThat(pairing.pairingAllocatedBye()).isEmpty();
        return this;
    }

    private List<String> boards() {
        return pairing.boards().stream()
                .map(board -> board.white().value() + "-" + board.black().value())
                .toList();
    }
}
