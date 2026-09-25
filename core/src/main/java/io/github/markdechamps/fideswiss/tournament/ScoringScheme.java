package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;
import java.util.Optional;

/**
 * The points each result is worth (C.04.1 Art. 3–4 are written in terms of these values, so they are
 * configuration). The Pairing-Allocated Bye's value is the same for every PAB of an event; left unset, it is
 * whatever the Pairing System defines.
 */
public record ScoringScheme(Points win, Points draw, Points loss, Optional<Points> pairingAllocatedBye) {

    public ScoringScheme {
        Objects.requireNonNull(win, "win");
        Objects.requireNonNull(draw, "draw");
        Objects.requireNonNull(loss, "loss");
        Objects.requireNonNull(pairingAllocatedBye, "pairingAllocatedBye");
    }

    /** 1 / ½ / 0, with the PAB as the system defines. */
    public static ScoringScheme standard() {
        return new ScoringScheme(Points.of(1), Points.of("0.5"), Points.ZERO, Optional.empty());
    }

    public ScoringScheme withPairingAllocatedBye(Points value) {
        return new ScoringScheme(win, draw, loss, Optional.of(value));
    }

    public Points pointsFor(GameResult result) {
        return switch (result) {
            case WIN -> win;
            case DRAW -> draw;
            case LOSS -> loss;
        };
    }

    /** What a round without a game is worth, given the PAB value the system resolved. */
    public Points pointsFor(Bye bye, Points pairingAllocatedByeValue) {
        return switch (bye) {
            case PAIRING_ALLOCATED -> pairingAllocatedByeValue;
            case FULL_POINT -> win;
            case HALF_POINT -> draw;
            case ZERO_POINT, WITHDRAWN, NOT_YET_ENTERED -> Points.ZERO;
        };
    }
}
