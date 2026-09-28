package io.github.markdechamps.fideswiss.lim;

import java.util.Comparator;

/**
 * A player transferred into the scoregroup being paired (3.1).
 *
 * @param fromAhead true when it comes from the side the pairing started at: a downfloater in a group paired
 *     downward, an upfloater in a group paired upward. Only the Median Scoregroup also receives floaters from behind
 *     (3.6.3, 3.7.3).
 */
record Floater(Player player, boolean fromAhead) {

    /**
     * 3.6, 3.7: the floaters from ahead first, and among them the highest (lowest) score first, then the highest
     * (lowest) numbered; the floaters from behind after them, in the mirror order.
     */
    static Comparator<Floater> pairingOrder(Direction direction) {
        Comparator<Floater> aheadFirst = Comparator.comparing(floater -> !floater.fromAhead());
        return aheadFirst.thenComparing((first, second) -> first.fromAhead()
                ? byScoreThenNumber(direction).compare(first.player(), second.player())
                : byScoreThenNumber(direction.mirrored()).compare(first.player(), second.player()));
    }

    static Comparator<Player> byScoreThenNumber(Direction direction) {
        return Comparator.comparing(Player::pairingScore, direction.scoresFromAhead())
                .thenComparing(direction.order());
    }
}
