package io.github.markdechamps.fideswiss.dutch;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Article 4.2: the orders of S2, sorted by the lexicographic value of their first N1 BSNs. The rest of S2 (the
 * remainder, or the players bound to float) follows in BSN order, so it never adds an order.
 *
 * <p>Orders in which an S1 player would face an S2 player it may not meet are skipped as a whole subtree, and so
 * is every subtree whose partial order the caller rejects (the optimum finder saying the target is out of
 * reach). Skipping keeps the order of what is left.
 */
final class Transpositions {

    private Transpositions() {}

    /** A partial order: the S2 players placed against the first S1 players, and the S2 players left. */
    interface BranchTest {
        boolean worthExploring(List<Player> placed, List<Player> available);

        BranchTest EVERY_BRANCH = (placed, available) -> true;
    }

    static Stream<List<Player>> of(
            List<Player> s1, List<Player> s2, BiPredicate<Player, Player> mayMeet, BranchTest branchTest) {
        return arrange(s1, List.of(), s2, mayMeet, branchTest);
    }

    private static Stream<List<Player>> arrange(
            List<Player> s1,
            List<Player> placed,
            List<Player> available,
            BiPredicate<Player, Player> mayMeet,
            BranchTest branchTest) {
        if (!branchTest.worthExploring(placed, available)) {
            return Stream.empty();
        }
        if (placed.size() == s1.size()) {
            return Stream.of(concatenated(placed, available));
        }
        var s1Player = s1.get(placed.size());
        return available.stream()
                .filter(s2Player -> mayMeet.test(s1Player, s2Player))
                .flatMap(s2Player -> arrange(
                        s1,
                        Combinations.appended(placed, s2Player),
                        without(available, s2Player),
                        mayMeet,
                        branchTest));
    }

    private static List<Player> concatenated(List<Player> first, List<Player> second) {
        var all = new ArrayList<>(first);
        all.addAll(second);
        return List.copyOf(all);
    }

    private static List<Player> without(List<Player> list, Player removed) {
        return list.stream().filter(player -> player != removed).toList();
    }
}
