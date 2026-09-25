package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Optional;

/** One round's block of a {@code 001} record: opponent (columns 92–95), colour (97) and result (99). */
record RoundCell(Optional<String> opponent, Optional<Colour> colour, char result) {

    static final RoundCell EMPTY = new RoundCell(Optional.empty(), Optional.empty(), ' ');

    static RoundCell parse(String block) {
        var padded = (block + "        ").substring(0, 8);
        var opponentField = padded.substring(0, 4).trim();
        var opponent = opponentField.isEmpty() || Integer.parseInt(opponentField) == 0
                ? Optional.<String>empty()
                : Optional.of(String.valueOf(Integer.parseInt(opponentField)));
        var colour =
                switch (Character.toLowerCase(padded.charAt(5))) {
                    case 'w' -> Optional.of(Colour.WHITE);
                    case 'b' -> Optional.of(Colour.BLACK);
                    default -> Optional.<Colour>empty();
                };
        return new RoundCell(opponent, colour, Character.toUpperCase(padded.charAt(7)));
    }

    boolean hasOpponent() {
        return opponent.isPresent();
    }

    boolean isPairingAllocatedBye() {
        return opponent.isEmpty() && result == 'U';
    }

    boolean isEmpty() {
        return opponent.isEmpty() && colour.isEmpty() && (result == ' ' || result == 'Z');
    }
}
