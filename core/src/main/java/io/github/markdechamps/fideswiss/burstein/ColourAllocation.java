package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Optional;

/** Article 5.2: which player of a pair has White; the rules are tried in priority order. */
final class ColourAllocation {

    private final RoundToPair round;

    ColourAllocation(RoundToPair round) {
        this.round = round;
    }

    Game allocate(Pair pair) {
        var higher = pair.first().ranksAbove(pair.second()) ? pair.first() : pair.second();
        var lower = pair.other(higher);
        var decision = firstGame(higher, lower)
                .map(colour -> new Decision(colour, "C.04.4.2 5.2.1"))
                .or(() -> bothPreferences(higher, lower).map(colour -> new Decision(colour, "C.04.4.2 5.2.2")))
                .or(() -> strongerPreference(higher, lower).map(colour -> new Decision(colour, "C.04.4.2 5.2.3")))
                .or(() -> alternation(higher, lower).map(colour -> new Decision(colour, "C.04.4.2 5.2.4")))
                .orElseGet(() -> new Decision(higher.preference().orElseThrow().colour(), "C.04.4.2 5.2.5"));
        return decision.colour() == Colour.WHITE
                ? new Game(higher, lower, decision.article())
                : new Game(lower, higher, decision.article());
    }

    private record Decision(Colour colour, String article) {}

    /** 5.2.1: both have yet to play, so the higher-ranked player's TPN decides. */
    private Optional<Colour> firstGame(Player higher, Player lower) {
        if (higher.hasPlayed() || lower.hasPlayed()) {
            return Optional.empty();
        }
        return Optional.of(round.initialColour().forPairingNumber(higher.pairingNumber()));
    }

    /** 5.2.2; a player with no preference grants the opponent's (1.5.4). */
    private static Optional<Colour> bothPreferences(Player higher, Player lower) {
        var mine = higher.preference();
        var theirs = lower.preference();
        if (mine.isEmpty()) {
            return theirs.map(preference -> preference.colour().opposite());
        }
        if (theirs.isEmpty() || mine.get().colour() != theirs.get().colour()) {
            return Optional.of(mine.get().colour());
        }
        return Optional.empty();
    }

    /** 5.2.3; reached only when both want the same colour. */
    private static Optional<Colour> strongerPreference(Player higher, Player lower) {
        var mine = higher.preference().orElseThrow();
        var theirs = lower.preference().orElseThrow();
        if (mine.strength().isStrongerThan(theirs.strength())) {
            return Optional.of(mine.colour());
        }
        if (theirs.strength().isStrongerThan(mine.strength())) {
            return Optional.of(theirs.colour().opposite());
        }
        return Optional.empty();
    }

    /** 5.2.4, on the colours of the games played, compared from the latest back (GHR 3.4). */
    private static Optional<Colour> alternation(Player higher, Player lower) {
        var mine = higher.playedColours();
        var theirs = lower.playedColours();
        for (var back = 1; back <= Math.min(mine.size(), theirs.size()); back++) {
            var myColour = mine.get(mine.size() - back);
            if (myColour != theirs.get(theirs.size() - back)) {
                return Optional.of(myColour.opposite());
            }
        }
        return Optional.empty();
    }
}
