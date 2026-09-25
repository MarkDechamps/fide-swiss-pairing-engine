package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

/** The colour drawn by lot before round 1 for the top participant (C.04.3 5.1, TRF 152). */
public record InitialColour(Colour colour) {

    public InitialColour {
        Objects.requireNonNull(colour, "colour");
    }

    public static InitialColour white() {
        return new InitialColour(Colour.WHITE);
    }

    public static InitialColour black() {
        return new InitialColour(Colour.BLACK);
    }

    /** The colour for a participant of this Pairing Number: the initial colour when it is odd. */
    public Colour forPairingNumber(PairingNumber number) {
        return number.isOdd() ? colour : colour.opposite();
    }
}
