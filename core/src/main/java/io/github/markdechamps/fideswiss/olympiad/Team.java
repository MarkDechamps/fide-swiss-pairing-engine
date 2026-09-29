package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Rating;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A team as the Olympiad Pairing Rules see it when pairing one round. Teams are compared by identity.
 *
 * @param pairingNumber the initial pairing number of 3.1
 * @param matchPoints the matchpoints before the round (3.2.1, 6.3)
 * @param board1 the colour of board 1 in each earlier round; empty for the bye, an unplayed match or a round the
 *     team was not paired in (7.1, 7.7)
 * @param met the opponents met in a played match (6.1)
 * @param mayReceiveBye 4.2: has not had the bye, won no match by default, and was paired from round 1
 * @param rating the average rating of 3.1.1, which 11.1.3 publishes by
 */
record Team(
        ParticipantId id,
        PairingNumber pairingNumber,
        Points matchPoints,
        List<Optional<Colour>> board1,
        Set<ParticipantId> met,
        boolean mayReceiveBye,
        Rating rating) {

    /** 3.2: matchpoints, then the initial pairing number. The first in this order is the highest ranked. */
    static final Comparator<Team> RANKING =
            Comparator.comparing(Team::matchPoints).reversed().thenComparing(Team::pairingNumber);

    Team {
        board1 = List.copyOf(board1);
        met = Set.copyOf(met);
    }

    boolean hasMet(Team other) {
        return met.contains(other.id);
    }

    /** 6.1: two other teams that have not met in a played match. */
    boolean mayMeet(Team other) {
        return this != other && !hasMet(other);
    }

    boolean isRankedAbove(Team other) {
        return RANKING.compare(this, other) < 0;
    }

    List<Colour> playedColours() {
        return board1.stream().flatMap(Optional::stream).toList();
    }

    int colourDifference() {
        var played = playedColours();
        var whites = played.stream().filter(Colour.WHITE::equals).count();
        return (int) (2 * whites - played.size());
    }

    Optional<Colour> lastColour() {
        var played = playedColours();
        return played.isEmpty() ? Optional.empty() : Optional.of(played.getLast());
    }

    /** Board 1's colour in the given earlier round (1-based), if the team played a match then. */
    Optional<Colour> colourIn(int round) {
        return round <= board1.size() ? board1.get(round - 1) : Optional.empty();
    }

    /** 7.3: the colour takes the difference beyond neither +2 nor -2, and is not the same colour three times running. */
    boolean mayReceive(Colour colour) {
        var played = playedColours();
        var matches = played.size();
        var thirdInARow = matches >= 2 && played.get(matches - 1) == colour && played.get(matches - 2) == colour;
        var difference = colourDifference() + (colour == Colour.WHITE ? 1 : -1);
        return !thirdInARow && Math.abs(difference) <= 2;
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
        return id.toString();
    }
}
