package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Comparator;
import java.util.Optional;

/** Article 5.2: which player of a pair has White; the rules are tried in priority order. */
final class ColourAllocation {

    /** 5.2.1: the player with more points or, when equal, the smaller Pairing Number ranks higher. */
    static final Comparator<Player> RANKING =
            Comparator.comparing(Player::pairingScore).reversed().thenComparingInt(Player::tpn);

    private final RoundToPair round;

    ColourAllocation(RoundToPair round) {
        this.round = round;
    }

    Game allocate(Pair pair) {
        var higher = RANKING.compare(pair.first(), pair.second()) <= 0 ? pair.first() : pair.second();
        var lower = pair.other(higher);
        var decision = firstRound(higher, lower)
                .map(colour -> new Decision(colour, "C.04.4.1 5.2.1"))
                .or(() -> bothPreferences(higher, lower).map(colour -> new Decision(colour, "C.04.4.1 5.2.2")))
                .or(() -> strongerPreference(higher, lower).map(colour -> new Decision(colour, "C.04.4.1 5.2.3")))
                .or(() -> alternation(higher, lower).map(colour -> new Decision(colour, "C.04.4.1 5.2.4")))
                .orElseGet(() -> new Decision(higher.preference().colour(), "C.04.4.1 5.2.5"));
        return decision.colour() == Colour.WHITE
                ? new Game(higher, lower, decision.article())
                : new Game(lower, higher, decision.article());
    }

    private record Decision(Colour colour, String article) {}

    /** 5.2.1: neither has played, so the higher-ranked player's Pairing Number decides. */
    private Optional<Colour> firstRound(Player higher, Player lower) {
        if (higher.hasPlayed() || lower.hasPlayed()) {
            return Optional.empty();
        }
        return Optional.of(round.initialColour().forPairingNumber(higher.pairingNumber()));
    }

    /** 5.2.2. */
    private static Optional<Colour> bothPreferences(Player higher, Player lower) {
        var wanted = higher.preference().colour();
        return wanted != lower.preference().colour() ? Optional.of(wanted) : Optional.empty();
    }

    /** 5.2.3. */
    private static Optional<Colour> strongerPreference(Player higher, Player lower) {
        var mine = higher.preference();
        var theirs = lower.preference();
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
