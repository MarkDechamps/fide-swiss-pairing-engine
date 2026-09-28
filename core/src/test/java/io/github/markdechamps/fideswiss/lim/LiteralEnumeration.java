package io.github.markdechamps.fideswiss.lim;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * The Literal Enumerator for the Lim System: every reachability question answered by trying every pairing (the
 * first player unpaired, or paired with each allowed player in turn), with no matching theory. Memoised on the set
 * of players left, so fields of up to about 24 players stay quick.
 */
final class LiteralEnumeration implements Reachability {

    @Override
    public int maximumPairs(List<Player> players, BiPredicate<Player, Player> allowed) {
        var size = players.size();
        if (size > 62) {
            throw new IllegalArgumentException("Too many players to enumerate: " + size);
        }
        var edge = new boolean[size][size];
        for (var a = 0; a < size; a++) {
            for (var b = a + 1; b < size; b++) {
                edge[a][b] = edge[b][a] = allowed.test(players.get(a), players.get(b));
            }
        }
        return best(edge, size == 0 ? 0 : (1L << size) - 1, new HashMap<>());
    }

    private static int best(boolean[][] edge, long left, Map<Long, Integer> memo) {
        if (Long.bitCount(left) < 2) {
            return 0;
        }
        var known = memo.get(left);
        if (known != null) {
            return known;
        }
        var first = Long.numberOfTrailingZeros(left);
        var rest = left & ~(1L << first);
        var most = best(edge, rest, memo);
        for (var other = 0; other < edge.length; other++) {
            if ((rest & (1L << other)) != 0 && edge[first][other]) {
                most = Math.max(most, 1 + best(edge, rest & ~(1L << other), memo));
            }
        }
        memo.put(left, most);
        return most;
    }
}
