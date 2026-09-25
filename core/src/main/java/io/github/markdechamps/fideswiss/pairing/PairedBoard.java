package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.Objects;

/** One board of a round's pairing, before it is played. */
public record PairedBoard(BoardNumber number, ParticipantId white, ParticipantId black) {

    public PairedBoard {
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(white, "white");
        Objects.requireNonNull(black, "black");
    }
}
