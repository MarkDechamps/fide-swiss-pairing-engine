package io.github.markdechamps.fideswiss.matching;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * The most pairs that can be formed among some members, when only reachability matters and nothing is weighed:
 * Edmonds' blossom algorithm in its textbook O(n³) form (augmenting paths grown breadth-first, blossoms
 * contracted onto their base).
 */
public final class MaximumCardinalityMatching {

    private final int n;
    private final boolean[][] edge;
    private final int[] mate;
    private final int[] parent;
    private final int[] base;
    private final boolean[] used;
    private final boolean[] blossom;
    private final ArrayDeque<Integer> queue = new ArrayDeque<>();

    private MaximumCardinalityMatching(boolean[][] edge) {
        this.n = edge.length;
        this.edge = edge;
        this.mate = new int[n];
        Arrays.fill(mate, -1);
        this.parent = new int[n];
        this.base = new int[n];
        this.used = new boolean[n];
        this.blossom = new boolean[n];
    }

    /** The pairs of a maximum matching, each in the order of the members given. */
    public static <T> List<List<T>> of(List<T> members, BiPredicate<T, T> allowed) {
        var size = members.size();
        var edge = new boolean[size][size];
        for (var a = 0; a < size; a++) {
            for (var b = a + 1; b < size; b++) {
                edge[a][b] = edge[b][a] = allowed.test(members.get(a), members.get(b));
            }
        }
        var matching = new MaximumCardinalityMatching(edge);
        matching.solve();
        var pairs = new ArrayList<List<T>>();
        for (var vertex = 0; vertex < size; vertex++) {
            if (matching.mate[vertex] > vertex) {
                pairs.add(List.of(members.get(vertex), members.get(matching.mate[vertex])));
            }
        }
        return List.copyOf(pairs);
    }

    public static <T> int maximumPairs(List<T> members, BiPredicate<T, T> allowed) {
        return of(members, allowed).size();
    }

    public static <T> boolean canPairAll(List<T> members, BiPredicate<T, T> allowed) {
        return members.size() % 2 == 0 && maximumPairs(members, allowed) == members.size() / 2;
    }

    private void solve() {
        for (var vertex = 0; vertex < n; vertex++) {
            if (mate[vertex] == -1) {
                augmentFrom(vertex);
            }
        }
    }

    private void augmentFrom(int root) {
        var v = findPath(root);
        while (v != -1) {
            var pv = parent[v];
            var ppv = mate[pv];
            mate[v] = pv;
            mate[pv] = v;
            v = ppv;
        }
    }

    private int findPath(int root) {
        Arrays.fill(used, false);
        Arrays.fill(parent, -1);
        for (var i = 0; i < n; i++) {
            base[i] = i;
        }
        used[root] = true;
        queue.clear();
        queue.add(root);
        while (!queue.isEmpty()) {
            var v = queue.poll();
            for (var to = 0; to < n; to++) {
                if (!edge[v][to] || base[v] == base[to] || mate[v] == to) {
                    continue;
                }
                if (to == root || mate[to] != -1 && parent[mate[to]] != -1) {
                    contract(v, to);
                } else if (parent[to] == -1) {
                    parent[to] = v;
                    if (mate[to] == -1) {
                        return to;
                    }
                    used[mate[to]] = true;
                    queue.add(mate[to]);
                }
            }
        }
        return -1;
    }

    private void contract(int v, int to) {
        var lca = lowestCommonAncestor(v, to);
        Arrays.fill(blossom, false);
        markPath(v, lca, to);
        markPath(to, lca, v);
        for (var i = 0; i < n; i++) {
            if (blossom[base[i]]) {
                base[i] = lca;
                if (!used[i]) {
                    used[i] = true;
                    queue.add(i);
                }
            }
        }
    }

    private int lowestCommonAncestor(int first, int second) {
        var seen = new boolean[n];
        var a = first;
        while (true) {
            a = base[a];
            seen[a] = true;
            if (mate[a] == -1) {
                break;
            }
            a = parent[mate[a]];
        }
        var b = second;
        while (true) {
            b = base[b];
            if (seen[b]) {
                return b;
            }
            b = parent[mate[b]];
        }
    }

    private void markPath(int start, int blossomBase, int firstChild) {
        var v = start;
        var child = firstChild;
        while (base[v] != blossomBase) {
            blossom[base[v]] = true;
            blossom[base[mate[v]]] = true;
            parent[v] = child;
            child = mate[v];
            v = parent[mate[v]];
        }
    }
}
