package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.matching.MaximumWeightMatching;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Answers "can these players all be paired?" for any group of a round's players: the legality question behind
 * [C4]. It never chooses a pairing.
 */
final class CompletionOracle {

    private final boolean[][] mayMeet;
    private final Map<BitSet, Boolean> answers = new HashMap<>();

    CompletionOracle(PlayerSet players, BiPredicate<Player, Player> mayMeet) {
        this.mayMeet = new boolean[players.size()][players.size()];
        for (var a = 0; a < players.size(); a++) {
            for (var b = 0; b < players.size(); b++) {
                this.mayMeet[a][b] = a != b && mayMeet.test(players.at(a), players.at(b));
            }
        }
    }

    boolean canPairAll(BitSet group) {
        if (group.cardinality() % 2 == 1) {
            return false;
        }
        return answers.computeIfAbsent(group, this::hasPerfectMatching);
    }

    private boolean hasPerfectMatching(BitSet group) {
        var members = group.stream().boxed().toList();
        var edges = new ArrayList<MaximumWeightMatching.Edge>();
        for (var a = 0; a < members.size(); a++) {
            for (var b = a + 1; b < members.size(); b++) {
                if (mayMeet[members.get(a)][members.get(b)]) {
                    edges.add(new MaximumWeightMatching.Edge(a, b, BigInteger.ONE));
                }
            }
        }
        return MaximumWeightMatching.of(members.size(), edges).isPerfect();
    }
}
