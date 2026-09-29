package io.github.markdechamps.fideswiss.olympiad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * The Literal Enumerator for the Olympiad Pairing Rules: every question the procedure asks a matching answered by
 * trying every pairing (the first team unpaired, or paired with each allowed team in turn), with no matching theory.
 * Memoised on the set of teams left, so fields of up to about 24 teams stay quick.
 */
final class LiteralEnumeration implements Matchings {

    private static final int NONE = Integer.MIN_VALUE;

    @Override
    public int maximumPairs(List<Team> teams, BiPredicate<Team, Team> allowed) {
        var edge = edges(teams, allowed);
        return most(edge, all(teams), new HashMap<>());
    }

    @Override
    public Optional<Map<Team, Team>> mostKept(
            List<Team> teams, BiPredicate<Team, Team> allowed, BiPredicate<Team, Team> kept) {
        var size = teams.size();
        var weight = new int[size][size];
        for (var one = 0; one < size; one++) {
            for (var other = 0; other < size; other++) {
                var a = teams.get(one);
                var b = teams.get(other);
                weight[one][other] = one == other || !allowed.test(a, b) ? -1 : kept.test(a, b) ? 1 : 0;
            }
        }
        var memo = new HashMap<Long, Integer>();
        var left = all(teams);
        if (heaviest(weight, left, memo) == NONE) {
            return Optional.empty();
        }
        var partner = new HashMap<Team, Team>();
        while (left != 0) {
            var first = Long.numberOfTrailingZeros(left);
            var rest = left & ~(1L << first);
            var target = heaviest(weight, left, memo);
            for (var other = 0; other < size; other++) {
                if ((rest & (1L << other)) != 0 && weight[first][other] >= 0) {
                    var after = heaviest(weight, rest & ~(1L << other), memo);
                    if (after != NONE && after + weight[first][other] == target) {
                        partner.put(teams.get(first), teams.get(other));
                        partner.put(teams.get(other), teams.get(first));
                        left = rest & ~(1L << other);
                        break;
                    }
                }
            }
        }
        return Optional.of(partner);
    }

    private static int most(boolean[][] edge, long left, Map<Long, Integer> memo) {
        if (Long.bitCount(left) < 2) {
            return 0;
        }
        var known = memo.get(left);
        if (known != null) {
            return known;
        }
        var first = Long.numberOfTrailingZeros(left);
        var rest = left & ~(1L << first);
        var best = most(edge, rest, memo);
        for (var other = 0; other < edge.length; other++) {
            if ((rest & (1L << other)) != 0 && edge[first][other]) {
                best = Math.max(best, 1 + most(edge, rest & ~(1L << other), memo));
            }
        }
        memo.put(left, best);
        return best;
    }

    private static int heaviest(int[][] weight, long left, Map<Long, Integer> memo) {
        if (left == 0) {
            return 0;
        }
        var known = memo.get(left);
        if (known != null) {
            return known;
        }
        var first = Long.numberOfTrailingZeros(left);
        var rest = left & ~(1L << first);
        var best = NONE;
        for (var other = 0; other < weight.length; other++) {
            if ((rest & (1L << other)) != 0 && weight[first][other] >= 0) {
                var after = heaviest(weight, rest & ~(1L << other), memo);
                if (after != NONE) {
                    best = Math.max(best, after + weight[first][other]);
                }
            }
        }
        memo.put(left, best);
        return best;
    }

    private static boolean[][] edges(List<Team> teams, BiPredicate<Team, Team> allowed) {
        var size = teams.size();
        var edge = new boolean[size][size];
        for (var one = 0; one < size; one++) {
            for (var other = one + 1; other < size; other++) {
                edge[one][other] = edge[other][one] = allowed.test(teams.get(one), teams.get(other));
            }
        }
        return edge;
    }

    private static long all(List<Team> teams) {
        if (teams.size() > 62) {
            throw new IllegalArgumentException("Too many teams to enumerate: " + teams.size());
        }
        return teams.isEmpty() ? 0 : (1L << teams.size()) - 1;
    }
}
