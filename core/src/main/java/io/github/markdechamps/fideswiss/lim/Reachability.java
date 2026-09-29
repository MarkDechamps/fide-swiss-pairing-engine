package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.matching.MaximumCardinalityMatching;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * The only question Lim asks a matching: how many pairings can these players form, and so whether they can all
 * still be paired (4.4, 3.10.2). There are no weights, because Lim has no criteria to weigh.
 */
interface Reachability {

    Reachability MATCHING = MaximumCardinalityMatching::maximumPairs;

    int maximumPairs(List<Player> players, BiPredicate<Player, Player> allowed);

    default boolean canPairAll(List<Player> players, BiPredicate<Player, Player> allowed) {
        return players.size() % 2 == 0 && maximumPairs(players, allowed) == players.size() / 2;
    }

    /** How many of {@code floaters} can each get a distinct opponent among {@code opponents}. */
    default int opponentsFound(List<Player> floaters, List<Player> opponents, BiPredicate<Player, Player> allowed) {
        var all = new ArrayList<>(floaters);
        all.addAll(opponents);
        return maximumPairs(all, (a, b) -> floaters.contains(a) != floaters.contains(b) && allowed.test(a, b));
    }
}
