package io.github.markdechamps.fideswiss.dutch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * 2017 D.3: MDP-exchanges between the original S1 and the original Limbo, in the order of the S1 they yield: (a)
 * the highest different score first, then (b) the lowest lexicographic value of the BSNs. Every set of M1 MDPs is
 * an S1 some exchange yields, and the optimum finder prunes those that cannot reach the target (reading R11).
 */
final class MdpExchanges {

    /** D.3.a: compare the S1 scores highest first; the higher score at the first difference comes first. */
    private static final Comparator<List<Player>> HIGHEST_DIFFERENT_SCORE = (a, b) -> {
        for (var index = 0; index < a.size(); index++) {
            var difference = b.get(index).score().compareTo(a.get(index).score());
            if (difference != 0) {
                return difference;
            }
        }
        return 0;
    };

    private MdpExchanges() {}

    static Stream<List<Player>> inOrder(List<Player> movedDown, List<Player> bestAchievableLimbo) {
        var m1 = movedDown.size() - bestAchievableLimbo.size();
        // Combinations come in ascending lexicographic BSN order (D.3.b); a stable sort puts D.3.a first.
        return Combinations.of(movedDown, m1).sorted(HIGHEST_DIFFERENT_SCORE);
    }
}
