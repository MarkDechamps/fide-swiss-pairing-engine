package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;
import java.util.Set;

/**
 * A participant as the Dubov System sees it when pairing one round. Players are compared by identity.
 *
 * @param opponentRatings the ratings of the opponents met over the board, one per game (1.7)
 * @param upfloats the rounds in which the player was paired with an opponent who had a higher score (1.8)
 */
record Player(
        ParticipantId id,
        PairingNumber pairingNumber,
        int rating,
        PairingScore pairingScore,
        List<Colour> playedColours,
        Set<ParticipantId> met,
        List<Integer> opponentRatings,
        boolean mayReceivePairingAllocatedBye,
        int upfloats,
        boolean upfloatedPreviousRound) {

    Player {
        playedColours = List.copyOf(playedColours);
        met = Set.copyOf(met);
        opponentRatings = List.copyOf(opponentRatings);
    }

    int tpn() {
        return pairingNumber.value();
    }

    boolean hasPlayed() {
        return !playedColours.isEmpty();
    }

    int gamesPlayed() {
        return playedColours.size();
    }

    boolean hasMet(Player other) {
        return met.contains(other.id);
    }

    /** 1.5: games with White minus games with Black. */
    int colourDifference() {
        var whites = playedColours.stream().filter(Colour.WHITE::equals).count();
        return (int) (2 * whites - playedColours.size());
    }

    /** 1.7: the mean rating of the opponents met over the board, rounded half up; zero before the first game. */
    int aro() {
        if (opponentRatings.isEmpty()) {
            return 0;
        }
        var sum = opponentRatings.stream().mapToLong(Integer::longValue).sum();
        var games = opponentRatings.size();
        return (int) ((2 * sum + games) / (2L * games));
    }

    ColourPreference preference() {
        return ColourPreference.of(this);
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
