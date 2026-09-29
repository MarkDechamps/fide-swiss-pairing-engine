package io.github.markdechamps.fideswiss.olympiad;

import java.math.BigDecimal;
import java.util.Comparator;

/**
 * 11.1: the matches are published by the matchpoints of the higher-ranked team, then the sum of both teams'
 * matchpoints, then the average rating of the higher-ranked team (3.1.1), each highest first. The initial pairing
 * number of the higher-ranked team is our last key. 11.2 and 11.3 are the arbiter's changes.
 */
final class PublicationOrder {

    static final Comparator<Game> ORDER = Comparator.comparing(
                    (Game game) -> game.higherRanked().matchPoints())
            .reversed()
            .thenComparing(
                    Comparator.comparing(PublicationOrder::sumOfMatchPoints).reversed())
            .thenComparing(
                    Comparator.comparing((Game game) -> game.higherRanked().rating())
                            .reversed())
            .thenComparing(game -> game.higherRanked().pairingNumber());

    private PublicationOrder() {}

    private static BigDecimal sumOfMatchPoints(Game game) {
        return game.white().matchPoints().plus(game.black().matchPoints()).toBigDecimal();
    }
}
