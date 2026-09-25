package io.github.markdechamps.fideswiss.matching;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/**
 * A maximum-cardinality matching of greatest total weight in a general graph: Edmonds' weighted blossom
 * algorithm in Galil's O(n³) primal-dual form, after the structure of NetworkX's {@code max_weight_matching}
 * (BSD-3-Clause, see NOTICE).
 *
 * <p>This is mechanism, not rule code: the pairing systems ask it only "which is the best of all complete
 * pairings" and "can these all be paired". With integer weights every dual variable stays an integer.
 */
public final class MaximumWeightMatching {

    private static final int FREE = 0;
    private static final int OUTER = 1;
    private static final int INNER = 2;
    private static final int BREADCRUMB = 4;
    private static final int NONE = -1;

    /** An undirected edge between vertices {@code u} and {@code v}. */
    public record Edge(int u, int v, BigInteger weight) {
        public Edge {
            Objects.requireNonNull(weight, "weight");
            if (u == v) {
                throw new IllegalArgumentException("No loops: " + u);
            }
        }
    }

    private final int vertexCount;
    private final List<Edge> edges;
    private final int[] endpoint;
    private final List<List<Integer>> neighbourEndpoints;
    private final int[] mate;
    private final int[] label;
    private final int[] labelEnd;
    private final int[] inBlossom;
    private final int[] blossomParent;
    private final int[][] blossomChildren;
    private final int[] blossomBase;
    private final int[][] blossomEndpoints;
    private final int[] bestEdge;
    private final int[][] blossomBestEdges;
    private final Deque<Integer> unusedBlossoms = new ArrayDeque<>();
    private final BigInteger[] dual;
    private final boolean[] allowEdge;
    private final List<Integer> queue = new ArrayList<>();

    private MaximumWeightMatching(int vertexCount, List<Edge> edges) {
        this.vertexCount = vertexCount;
        this.edges = List.copyOf(edges);
        var blossoms = 2 * vertexCount;
        endpoint = new int[2 * edges.size()];
        neighbourEndpoints = new ArrayList<>();
        for (var vertex = 0; vertex < vertexCount; vertex++) {
            neighbourEndpoints.add(new ArrayList<>());
        }
        for (var k = 0; k < edges.size(); k++) {
            var edge = edges.get(k);
            endpoint[2 * k] = edge.u();
            endpoint[2 * k + 1] = edge.v();
            neighbourEndpoints.get(edge.u()).add(2 * k + 1);
            neighbourEndpoints.get(edge.v()).add(2 * k);
        }
        mate = filled(vertexCount, NONE);
        label = new int[blossoms];
        labelEnd = filled(blossoms, NONE);
        inBlossom = new int[vertexCount];
        Arrays.setAll(inBlossom, vertex -> vertex);
        blossomParent = filled(blossoms, NONE);
        blossomChildren = new int[blossoms][];
        blossomBase = filled(blossoms, NONE);
        for (var vertex = 0; vertex < vertexCount; vertex++) {
            blossomBase[vertex] = vertex;
        }
        blossomEndpoints = new int[blossoms][];
        bestEdge = filled(blossoms, NONE);
        blossomBestEdges = new int[blossoms][];
        for (var blossom = vertexCount; blossom < blossoms; blossom++) {
            unusedBlossoms.add(blossom);
        }
        var maxWeight = edges.stream().map(Edge::weight).reduce(BigInteger.ZERO, BigInteger::max);
        dual = new BigInteger[blossoms];
        Arrays.fill(dual, 0, vertexCount, maxWeight);
        Arrays.fill(dual, vertexCount, blossoms, BigInteger.ZERO);
        allowEdge = new boolean[edges.size()];
    }

    /** The matching of most edges, and among those of greatest weight. */
    public static MaximumWeightMatching of(int vertexCount, List<Edge> edges) {
        var matching = new MaximumWeightMatching(vertexCount, edges);
        if (!edges.isEmpty()) {
            matching.solve();
        }
        return matching;
    }

    /** For each vertex, the vertex it is matched with, or -1. */
    public int[] mates() {
        var mates = new int[vertexCount];
        for (var vertex = 0; vertex < vertexCount; vertex++) {
            mates[vertex] = mate[vertex] == NONE ? NONE : endpoint[mate[vertex]];
        }
        return mates;
    }

    public boolean isPerfect() {
        return Arrays.stream(mate).noneMatch(partner -> partner == NONE);
    }

    private void solve() {
        for (var stage = 0; stage < vertexCount; stage++) {
            if (!augmentOnce()) {
                return;
            }
            expandOuterBlossomsWithZeroDual();
        }
    }

    /** One stage: grow alternating trees from every free vertex until an augmenting path is found. */
    private boolean augmentOnce() {
        Arrays.fill(label, FREE);
        Arrays.fill(bestEdge, NONE);
        Arrays.fill(blossomBestEdges, vertexCount, 2 * vertexCount, null);
        Arrays.fill(allowEdge, false);
        queue.clear();
        for (var vertex = 0; vertex < vertexCount; vertex++) {
            if (mate[vertex] == NONE && label[inBlossom[vertex]] == FREE) {
                assignLabel(vertex, OUTER, NONE);
            }
        }
        while (true) {
            if (scanQueue()) {
                return true;
            }
            if (!adjustDuals()) {
                return false;
            }
        }
    }

    private boolean scanQueue() {
        while (!queue.isEmpty()) {
            var v = queue.removeLast();
            for (var p : neighbourEndpoints.get(v)) {
                var k = p / 2;
                var w = endpoint[p];
                if (inBlossom[v] == inBlossom[w]) {
                    continue;
                }
                var kSlack = BigInteger.ZERO;
                if (!allowEdge[k]) {
                    kSlack = slack(k);
                    if (kSlack.signum() <= 0) {
                        allowEdge[k] = true;
                    }
                }
                if (allowEdge[k]) {
                    if (label[inBlossom[w]] == FREE) {
                        assignLabel(w, INNER, p ^ 1);
                    } else if (label[inBlossom[w]] == OUTER) {
                        var base = scanBlossom(v, w);
                        if (base >= 0) {
                            addBlossom(base, k);
                        } else {
                            augmentMatching(k);
                            return true;
                        }
                    } else if (label[w] == FREE) {
                        label[w] = INNER;
                        labelEnd[w] = p ^ 1;
                    }
                } else if (label[inBlossom[w]] == OUTER) {
                    var b = inBlossom[v];
                    if (bestEdge[b] == NONE || kSlack.compareTo(slack(bestEdge[b])) < 0) {
                        bestEdge[b] = k;
                    }
                } else if (label[w] == FREE) {
                    if (bestEdge[w] == NONE || kSlack.compareTo(slack(bestEdge[w])) < 0) {
                        bestEdge[w] = k;
                    }
                }
            }
        }
        return false;
    }

    /** Changes the duals by the largest safe amount; false when no augmenting path can exist any more. */
    private boolean adjustDuals() {
        var deltaType = NONE;
        var delta = BigInteger.ZERO;
        var deltaEdge = NONE;
        var deltaBlossom = NONE;
        for (var v = 0; v < vertexCount; v++) {
            if (label[inBlossom[v]] == FREE && bestEdge[v] != NONE) {
                var d = slack(bestEdge[v]);
                if (deltaType == NONE || d.compareTo(delta) < 0) {
                    delta = d;
                    deltaType = 2;
                    deltaEdge = bestEdge[v];
                }
            }
        }
        for (var b = 0; b < 2 * vertexCount; b++) {
            if (blossomParent[b] == NONE && label[b] == OUTER && bestEdge[b] != NONE) {
                var d = slack(bestEdge[b]).shiftRight(1);
                if (deltaType == NONE || d.compareTo(delta) < 0) {
                    delta = d;
                    deltaType = 3;
                    deltaEdge = bestEdge[b];
                }
            }
        }
        for (var b = vertexCount; b < 2 * vertexCount; b++) {
            if (blossomBase[b] >= 0
                    && blossomParent[b] == NONE
                    && label[b] == INNER
                    && (deltaType == NONE || dual[b].compareTo(delta) < 0)) {
                delta = dual[b];
                deltaType = 4;
                deltaBlossom = b;
            }
        }
        if (deltaType == NONE) {
            deltaType = 1;
            delta = BigInteger.ZERO.max(minimumVertexDual());
        }
        for (var v = 0; v < vertexCount; v++) {
            if (label[inBlossom[v]] == OUTER) {
                dual[v] = dual[v].subtract(delta);
            } else if (label[inBlossom[v]] == INNER) {
                dual[v] = dual[v].add(delta);
            }
        }
        for (var b = vertexCount; b < 2 * vertexCount; b++) {
            if (blossomBase[b] >= 0 && blossomParent[b] == NONE) {
                if (label[b] == OUTER) {
                    dual[b] = dual[b].add(delta);
                } else if (label[b] == INNER) {
                    dual[b] = dual[b].subtract(delta);
                }
            }
        }
        switch (deltaType) {
            case 1 -> {
                return false;
            }
            case 2 -> {
                allowEdge[deltaEdge] = true;
                var edge = edges.get(deltaEdge);
                queue.add(label[inBlossom[edge.u()]] == FREE ? edge.v() : edge.u());
            }
            case 3 -> {
                allowEdge[deltaEdge] = true;
                queue.add(edges.get(deltaEdge).u());
            }
            default -> expandBlossom(deltaBlossom, false);
        }
        return true;
    }

    private BigInteger minimumVertexDual() {
        var minimum = dual[0];
        for (var v = 1; v < vertexCount; v++) {
            minimum = minimum.min(dual[v]);
        }
        return minimum;
    }

    private void expandOuterBlossomsWithZeroDual() {
        for (var b = vertexCount; b < 2 * vertexCount; b++) {
            if (blossomParent[b] == NONE && blossomBase[b] >= 0 && label[b] == OUTER && dual[b].signum() == 0) {
                expandBlossom(b, true);
            }
        }
    }

    private BigInteger slack(int k) {
        var edge = edges.get(k);
        return dual[edge.u()].add(dual[edge.v()]).subtract(edge.weight().shiftLeft(1));
    }

    private List<Integer> leaves(int blossom) {
        var leaves = new ArrayList<Integer>();
        collectLeaves(blossom, leaves);
        return leaves;
    }

    private void collectLeaves(int blossom, List<Integer> leaves) {
        if (blossom < vertexCount) {
            leaves.add(blossom);
            return;
        }
        for (var child : blossomChildren[blossom]) {
            collectLeaves(child, leaves);
        }
    }

    private void assignLabel(int w, int type, int p) {
        var b = inBlossom[w];
        label[w] = label[b] = type;
        labelEnd[w] = labelEnd[b] = p;
        bestEdge[w] = bestEdge[b] = NONE;
        if (type == OUTER) {
            queue.addAll(leaves(b));
        } else {
            var base = blossomBase[b];
            assignLabel(endpoint[mate[base]], OUTER, mate[base] ^ 1);
        }
    }

    /** Traces back from v and w; the base of the new blossom, or -1 when they lie in different trees. */
    private int scanBlossom(int v, int w) {
        var path = new ArrayList<Integer>();
        var base = NONE;
        while (v != NONE || w != NONE) {
            var b = inBlossom[v];
            if ((label[b] & BREADCRUMB) != 0) {
                base = blossomBase[b];
                break;
            }
            path.add(b);
            label[b] = OUTER | BREADCRUMB;
            if (labelEnd[b] == NONE) {
                v = NONE;
            } else {
                v = endpoint[labelEnd[b]];
                b = inBlossom[v];
                v = endpoint[labelEnd[b]];
            }
            if (w != NONE) {
                var swap = v;
                v = w;
                w = swap;
            }
        }
        for (var b : path) {
            label[b] = OUTER;
        }
        return base;
    }

    private void addBlossom(int base, int k) {
        var edge = edges.get(k);
        var bb = inBlossom[base];
        var bv = inBlossom[edge.u()];
        var bw = inBlossom[edge.v()];
        var b = unusedBlossoms.removeFirst();
        blossomBase[b] = base;
        blossomParent[b] = NONE;
        blossomParent[bb] = b;
        var path = new ArrayList<Integer>();
        var endpoints = new ArrayList<Integer>();
        while (bv != bb) {
            blossomParent[bv] = b;
            path.add(bv);
            endpoints.add(labelEnd[bv]);
            bv = inBlossom[endpoint[labelEnd[bv]]];
        }
        path.add(bb);
        java.util.Collections.reverse(path);
        java.util.Collections.reverse(endpoints);
        endpoints.add(2 * k);
        while (bw != bb) {
            blossomParent[bw] = b;
            path.add(bw);
            endpoints.add(labelEnd[bw] ^ 1);
            bw = inBlossom[endpoint[labelEnd[bw]]];
        }
        blossomChildren[b] = toArray(path);
        blossomEndpoints[b] = toArray(endpoints);
        label[b] = OUTER;
        labelEnd[b] = labelEnd[bb];
        dual[b] = BigInteger.ZERO;
        for (var v : leaves(b)) {
            if (label[inBlossom[v]] == INNER) {
                queue.add(v);
            }
            inBlossom[v] = b;
        }
        computeBestEdges(b, path);
    }

    private void computeBestEdges(int b, List<Integer> children) {
        var bestEdgeTo = filled(2 * vertexCount, NONE);
        for (var child : children) {
            for (var k : edgesLeaving(child)) {
                var edge = edges.get(k);
                var j = inBlossom[edge.v()] == b ? edge.u() : edge.v();
                var bj = inBlossom[j];
                if (bj != b
                        && label[bj] == OUTER
                        && (bestEdgeTo[bj] == NONE || slack(k).compareTo(slack(bestEdgeTo[bj])) < 0)) {
                    bestEdgeTo[bj] = k;
                }
            }
            blossomBestEdges[child] = null;
            bestEdge[child] = NONE;
        }
        blossomBestEdges[b] = Arrays.stream(bestEdgeTo).filter(k -> k != NONE).toArray();
        bestEdge[b] = NONE;
        for (var k : blossomBestEdges[b]) {
            if (bestEdge[b] == NONE || slack(k).compareTo(slack(bestEdge[b])) < 0) {
                bestEdge[b] = k;
            }
        }
    }

    private List<Integer> edgesLeaving(int blossom) {
        if (blossomBestEdges[blossom] != null) {
            return Arrays.stream(blossomBestEdges[blossom]).boxed().toList();
        }
        var leaving = new ArrayList<Integer>();
        for (var v : leaves(blossom)) {
            for (var p : neighbourEndpoints.get(v)) {
                leaving.add(p / 2);
            }
        }
        return leaving;
    }

    private void expandBlossom(int b, boolean endStage) {
        for (var s : blossomChildren[b]) {
            blossomParent[s] = NONE;
            if (s < vertexCount) {
                inBlossom[s] = s;
            } else if (endStage && dual[s].signum() == 0) {
                expandBlossom(s, true);
            } else {
                for (var v : leaves(s)) {
                    inBlossom[v] = s;
                }
            }
        }
        if (!endStage && label[b] == INNER) {
            relabelExpandedInnerBlossom(b);
        }
        label[b] = labelEnd[b] = NONE;
        blossomChildren[b] = null;
        blossomEndpoints[b] = null;
        blossomBase[b] = NONE;
        blossomBestEdges[b] = null;
        bestEdge[b] = NONE;
        unusedBlossoms.add(b);
    }

    /** An inner blossom expanded mid-stage: its children on the path through it become inner or outer again. */
    private void relabelExpandedInnerBlossom(int b) {
        var children = blossomChildren[b];
        var endpoints = blossomEndpoints[b];
        var entryChild = inBlossom[endpoint[labelEnd[b] ^ 1]];
        var j = indexOf(children, entryChild);
        int step;
        int endpointTrick;
        if ((j & 1) != 0) {
            j -= children.length;
            step = 1;
            endpointTrick = 0;
        } else {
            step = -1;
            endpointTrick = 1;
        }
        var p = labelEnd[b];
        while (j != 0) {
            label[endpoint[p ^ 1]] = FREE;
            label[endpoint[at(endpoints, j - endpointTrick) ^ endpointTrick ^ 1]] = FREE;
            assignLabel(endpoint[p ^ 1], INNER, p);
            allowEdge[at(endpoints, j - endpointTrick) / 2] = true;
            j += step;
            p = at(endpoints, j - endpointTrick) ^ endpointTrick;
            allowEdge[p / 2] = true;
            j += step;
        }
        var bv = at(children, j);
        label[endpoint[p ^ 1]] = label[bv] = INNER;
        labelEnd[endpoint[p ^ 1]] = labelEnd[bv] = p;
        bestEdge[bv] = NONE;
        j += step;
        while (at(children, j) != entryChild) {
            bv = at(children, j);
            if (label[bv] == OUTER) {
                j += step;
                continue;
            }
            var labelled = leaves(bv).stream().filter(v -> label[v] != FREE).findFirst();
            if (labelled.isPresent()) {
                var v = labelled.get();
                label[v] = FREE;
                label[endpoint[mate[blossomBase[bv]]]] = FREE;
                assignLabel(v, INNER, labelEnd[v]);
            }
            j += step;
        }
    }

    /** Swaps matched and unmatched edges on the even path from vertex v to the base of blossom b. */
    private void augmentBlossom(int b, int v) {
        var t = v;
        while (blossomParent[t] != b) {
            t = blossomParent[t];
        }
        if (t >= vertexCount) {
            augmentBlossom(t, v);
        }
        var children = blossomChildren[b];
        var endpoints = blossomEndpoints[b];
        var i = indexOf(children, t);
        var j = i;
        int step;
        int endpointTrick;
        if ((i & 1) != 0) {
            j -= children.length;
            step = 1;
            endpointTrick = 0;
        } else {
            step = -1;
            endpointTrick = 1;
        }
        while (j != 0) {
            j += step;
            t = at(children, j);
            var p = at(endpoints, j - endpointTrick) ^ endpointTrick;
            if (t >= vertexCount) {
                augmentBlossom(t, endpoint[p]);
            }
            j += step;
            t = at(children, j);
            if (t >= vertexCount) {
                augmentBlossom(t, endpoint[p ^ 1]);
            }
            mate[endpoint[p]] = p ^ 1;
            mate[endpoint[p ^ 1]] = p;
        }
        blossomChildren[b] = rotated(children, i);
        blossomEndpoints[b] = rotated(endpoints, i);
        blossomBase[b] = blossomBase[blossomChildren[b][0]];
    }

    private void augmentMatching(int k) {
        var edge = edges.get(k);
        augmentFrom(edge.u(), 2 * k + 1);
        augmentFrom(edge.v(), 2 * k);
    }

    private void augmentFrom(int start, int startEndpoint) {
        var s = start;
        var p = startEndpoint;
        while (true) {
            var bs = inBlossom[s];
            if (bs >= vertexCount) {
                augmentBlossom(bs, s);
            }
            mate[s] = p;
            if (labelEnd[bs] == NONE) {
                return;
            }
            var t = endpoint[labelEnd[bs]];
            var bt = inBlossom[t];
            s = endpoint[labelEnd[bt]];
            var j = endpoint[labelEnd[bt] ^ 1];
            if (bt >= vertexCount) {
                augmentBlossom(bt, j);
            }
            mate[j] = labelEnd[bt];
            p = labelEnd[bt] ^ 1;
        }
    }

    /** Python-style indexing: a negative index counts from the end. */
    private static int at(int[] values, int index) {
        return values[Math.floorMod(index, values.length)];
    }

    private static int indexOf(int[] values, int value) {
        for (var index = 0; index < values.length; index++) {
            if (values[index] == value) {
                return index;
            }
        }
        throw new IllegalStateException("Not a child: " + value);
    }

    private static int[] rotated(int[] values, int start) {
        var rotated = new int[values.length];
        for (var index = 0; index < values.length; index++) {
            rotated[index] = values[(start + index) % values.length];
        }
        return rotated;
    }

    private static int[] toArray(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    private static int[] filled(int length, int value) {
        var array = new int[length];
        Arrays.fill(array, value);
        return array;
    }
}
