package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.PairingAllocatedByeBar;
import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.List;

/** A participant as the Dutch System sees it when pairing one round. Players are compared by identity. */
final class Player {

    private final ParticipantId id;
    private final PairingNumber pairingNumber;
    private final Score score;
    private final ParticipantHistory history;
    private final List<FloatDirection> floats;
    private final List<Colour> playedColours;
    private final ColourPreference colourPreference;

    Player(
            ParticipantId id,
            PairingNumber pairingNumber,
            Score score,
            ParticipantHistory history,
            List<FloatDirection> floats) {
        this.id = id;
        this.pairingNumber = pairingNumber;
        this.score = score;
        this.history = history;
        this.floats = List.copyOf(floats);
        this.playedColours = history.playedColours();
        this.colourPreference = ColourPreference.from(playedColours);
    }

    ParticipantId id() {
        return id;
    }

    PairingNumber pairingNumber() {
        return pairingNumber;
    }

    /** The score the Dutch System pairs on. */
    Score score() {
        return score;
    }

    boolean hasPlayed(Player other) {
        return history.hasPlayedAgainst(other.id);
    }

    boolean mayReceivePairingAllocatedBye(PairingAllocatedByeBar bar) {
        return history.mayReceivePairingAllocatedBye(bar);
    }

    int unplayedRounds() {
        return history.unplayedRounds();
    }

    List<Colour> playedColours() {
        return playedColours;
    }

    int colourDifference() {
        return ColourPreference.colourDifference(playedColours);
    }

    ColourPreference colourPreference() {
        return colourPreference;
    }

    /** The float of the round {@code roundsAgo} before the one being paired (1 = the previous round). */
    FloatDirection floatRoundsAgo(int roundsAgo) {
        var index = floats.size() - roundsAgo;
        return index >= 0 ? floats.get(index) : FloatDirection.NONE;
    }

    @Override
    public String toString() {
        return id + "(#" + pairingNumber + ", " + score + ")";
    }
}
