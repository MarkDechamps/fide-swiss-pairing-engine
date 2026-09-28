package io.github.markdechamps.fideswiss.topscoregroup;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.List;
import java.util.Set;

/**
 * A team (Swiss Team) or a player (Double-Swiss) as the Top-Scoregroup Procedure sees it when pairing one round.
 *
 * @param score the Pairing Score
 * @param secondaryScore the score used only to choose the first-team for colours (C.04.6 4.2.2)
 * @param matchesPlayed matches not forfeited, byes excluded (3.4.3, ruling A9)
 * @param colours the colour of each match that gave one, oldest first: the played-only history (GHR 3.4)
 * @param met the participants met in a match that was not forfeited ([C1], GHR 3.5)
 * @param pairingAllocatedByeBarred [C2]: had a PAB, won a match by forfeit or had a full-point bye
 * @param floatedInPreviousRound a floater of the previous round (1.5)
 */
public record Contender(
        ParticipantId id,
        int tpn,
        Points score,
        Points secondaryScore,
        int matchesPlayed,
        List<Colour> colours,
        Set<ParticipantId> met,
        boolean pairingAllocatedByeBarred,
        boolean floatedInPreviousRound) {

    public Contender {
        colours = List.copyOf(colours);
        met = Set.copyOf(met);
    }

    public boolean hasPlayedAMatch() {
        return !colours.isEmpty();
    }

    public int colourDifference() {
        var whites = colours.stream().filter(Colour.WHITE::equals).count();
        return (int) (2 * whites - colours.size());
    }

    public long whites() {
        return colours.stream().filter(Colour.WHITE::equals).count();
    }

    /** Whether the last {@code count} played matches all had this colour. */
    public boolean hadInLastPlayedMatches(Colour colour, int count) {
        return colours.size() >= count
                && colours.subList(colours.size() - count, colours.size()).stream()
                        .allMatch(colour::equals);
    }

    boolean mayMeet(Contender other) {
        return !id.equals(other.id) && !met.contains(other.id);
    }

    boolean hasLowerScoreThan(Contender other) {
        return score.isLessThan(other.score);
    }

    @Override
    public String toString() {
        return id + "(#" + tpn + ", " + score + ")";
    }
}
