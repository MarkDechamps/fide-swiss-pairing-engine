package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;
import java.util.stream.Stream;

/** A recorded board: who had which colour and what happened. A forfeit keeps its assigned colours. */
public record Board(BoardNumber number, ParticipantId white, ParticipantId black, Outcome outcome) {

    public Board {
        Objects.requireNonNull(number, "number");
        Objects.requireNonNull(white, "white");
        Objects.requireNonNull(black, "black");
        Objects.requireNonNull(outcome, "outcome");
        if (white.equals(black)) {
            throw new IllegalArgumentException("A participant cannot meet itself on board " + number);
        }
    }

    public static Board of(int number, String white, String black, Outcome outcome) {
        return new Board(BoardNumber.of(number), ParticipantId.of(white), ParticipantId.of(black), outcome);
    }

    public boolean seats(ParticipantId participant) {
        return white.equals(participant) || black.equals(participant);
    }

    public Colour colourOf(ParticipantId participant) {
        return white.equals(participant) ? Colour.WHITE : Colour.BLACK;
    }

    public ParticipantId opponentOf(ParticipantId participant) {
        return white.equals(participant) ? black : white;
    }

    public Stream<ParticipantId> participants() {
        return Stream.of(white, black);
    }
}
