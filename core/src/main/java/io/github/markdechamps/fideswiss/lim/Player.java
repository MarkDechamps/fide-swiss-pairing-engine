package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Rating;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A participant as the Lim System sees it when pairing one round. Players are compared by identity.
 *
 * @param pairingNumber #1 is the "highest numbered" player (4.2)
 * @param playedColours the colours of the games played over the board, in order (GHR 3.4)
 * @param floatedPreviousRound 3.10: played the previous round against an opponent with another score
 */
record Player(
        ParticipantId id,
        PairingNumber pairingNumber,
        Rating rating,
        PairingScore pairingScore,
        List<Colour> playedColours,
        Set<ParticipantId> met,
        boolean mayReceivePairingAllocatedBye,
        boolean floatedPreviousRound) {

    Player {
        playedColours = List.copyOf(playedColours);
        met = Set.copyOf(met);
    }

    int tpn() {
        return pairingNumber.value();
    }

    boolean hasPlayed() {
        return !playedColours.isEmpty();
    }

    boolean hasMet(Player other) {
        return met.contains(other.id);
    }

    int colourDifference() {
        var whites = playedColours.stream().filter(Colour.WHITE::equals).count();
        return (int) (2 * whites - playedColours.size());
    }

    /** 5.1.1 and 5.1.2: the colour would be neither the third in a row nor three more of one colour. */
    boolean mayReceive(Colour colour) {
        var games = playedColours.size();
        var thirdInARow =
                games >= 2 && playedColours.get(games - 1) == colour && playedColours.get(games - 2) == colour;
        var difference = colourDifference() + (colour == Colour.WHITE ? 1 : -1);
        return !thirdInARow && Math.abs(difference) < 3;
    }

    /** 5.1, 5.5, 5.6: the equalising colour when the colours are unequal, otherwise the alternating one. */
    Optional<Colour> dueColour() {
        if (colourDifference() > 0) {
            return Optional.of(Colour.BLACK);
        }
        if (colourDifference() < 0) {
            return Optional.of(Colour.WHITE);
        }
        return hasPlayed() ? Optional.of(playedColours.getLast().opposite()) : Optional.empty();
    }

    @Override
    public boolean equals(Object other) {
        return this == other;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return id + "(#" + pairingNumber + ", " + pairingScore + ")";
    }
}
