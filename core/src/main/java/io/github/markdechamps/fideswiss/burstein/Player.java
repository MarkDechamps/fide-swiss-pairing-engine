package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A participant as the Burstein System sees it when pairing one round after the seeding rounds. Players are
 * compared by identity.
 *
 * @param index the Opposition Evaluation Index of the tournament so far (1.7, 1.8.1)
 */
record Player(
        ParticipantId id,
        PairingNumber pairingNumber,
        PairingScore pairingScore,
        List<Colour> playedColours,
        Set<ParticipantId> met,
        boolean mayReceivePairingAllocatedBye,
        OppositionIndex index) {

    /** 1.8: Index (1.8.1), then TPN ascending (1.8.2). Players' scores are not used. */
    static final Comparator<Player> RANKING =
            Comparator.comparing(Player::index, OppositionIndex.HIGHEST_FIRST).thenComparingInt(Player::tpn);

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

    int gamesPlayed() {
        return playedColours.size();
    }

    boolean hasMet(Player other) {
        return met.contains(other.id);
    }

    boolean ranksAbove(Player other) {
        return RANKING.compare(this, other) < 0;
    }

    /** 1.4: games with White minus games with Black. */
    int colourDifference() {
        var whites = playedColours.stream().filter(Colour.WHITE::equals).count();
        return (int) (2 * whites - playedColours.size());
    }

    /** 1.5; none before the first game (1.5.4). */
    Optional<ColourPreference> preference() {
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
        return id + "(#" + pairingNumber + ", " + pairingScore + ", " + index + ")";
    }
}
