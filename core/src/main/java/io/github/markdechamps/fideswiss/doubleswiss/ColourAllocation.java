package io.github.markdechamps.fideswiss.doubleswiss;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import java.util.Comparator;
import java.util.Optional;

/**
 * Article 4: the colours of a pair, once the whole round is paired (3.3.2). White plays game 1 with White and game 2
 * with Black (4.4). The rules always decide, since 4.3.4 or 4.3.5 applies whenever 4.3.1 does not.
 */
final class ColourAllocation {

    /** A pair with its colours, and the rule of 4.3 that decided them. */
    record ColouredPair(Contender white, Contender black, String article) {}

    /** 4.2: the higher score, then the smaller TPN. */
    static final Comparator<Contender> RANKING =
            Comparator.comparing(Contender::score).reversed().thenComparingInt(Contender::tpn);

    private final InitialColour initialColour;

    ColourAllocation(InitialColour initialColour) {
        this.initialColour = initialColour;
    }

    ColouredPair allocate(ContenderPair pair) {
        var higher = RANKING.compare(pair.top(), pair.bottom()) <= 0 ? pair.top() : pair.bottom();
        var players = new Sides(higher, pair.other(higher));
        return bothYetToPlay(players)
                .or(() -> fewerWhites(players))
                .or(() -> mostRecentDifference(players))
                .or(() -> alternateTheHigherRanked(players))
                .or(() -> alternateTheOpponent(players))
                .orElseThrow(() -> new IllegalStateException("Article 4.3 left " + pair + " without colours"));
    }

    private record Sides(Contender higherRanked, Contender opponent) {

        ColouredPair higherRankedGets(Colour colour, String article) {
            return colour == Colour.WHITE
                    ? new ColouredPair(higherRanked, opponent, article)
                    : new ColouredPair(opponent, higherRanked, article);
        }
    }

    /** 4.3.1: the initial-colour to an HRP of odd TPN, the opposite to an even one. */
    private Optional<ColouredPair> bothYetToPlay(Sides players) {
        if (players.higherRanked().hasPlayedAMatch() || players.opponent().hasPlayedAMatch()) {
            return Optional.empty();
        }
        var colour = players.higherRanked().tpn() % 2 == 1
                ? initialColour.colour()
                : initialColour.colour().opposite();
        return Optional.of(players.higherRankedGets(colour, "C.04.5 4.3.1"));
    }

    /** 4.3.2: White to the player with fewer Whites (a count, not the colour difference). */
    private static Optional<ColouredPair> fewerWhites(Sides players) {
        var higher = players.higherRanked().whites();
        var other = players.opponent().whites();
        if (higher == other) {
            return Optional.empty();
        }
        return Optional.of(players.higherRankedGets(higher < other ? Colour.WHITE : Colour.BLACK, "C.04.5 4.3.2"));
    }

    /** 4.3.3 with GHR 3.4: the played-only histories compared from the end, back to the latest difference. */
    private static Optional<ColouredPair> mostRecentDifference(Sides players) {
        var higher = players.higherRanked().colours();
        var other = players.opponent().colours();
        for (var back = 1; back <= Math.min(higher.size(), other.size()); back++) {
            var higherColour = higher.get(higher.size() - back);
            if (higherColour != other.get(other.size() - back)) {
                return Optional.of(players.higherRankedGets(higherColour.opposite(), "C.04.5 4.3.3"));
            }
        }
        return Optional.empty();
    }

    /** 4.3.4: the HRP's colour alternated from its last played match. */
    private static Optional<ColouredPair> alternateTheHigherRanked(Sides players) {
        return lastColour(players.higherRanked())
                .map(colour -> players.higherRankedGets(colour.opposite(), "C.04.5 4.3.4"));
    }

    /** 4.3.5: the opponent's colour alternated from its last played match. */
    private static Optional<ColouredPair> alternateTheOpponent(Sides players) {
        return lastColour(players.opponent()).map(colour -> players.higherRankedGets(colour, "C.04.5 4.3.5"));
    }

    private static Optional<Colour> lastColour(Contender player) {
        return player.colours().isEmpty()
                ? Optional.empty()
                : Optional.of(player.colours().getLast());
    }
}
