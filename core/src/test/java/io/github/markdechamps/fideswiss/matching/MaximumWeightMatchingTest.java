package io.github.markdechamps.fideswiss.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MaximumWeightMatchingTest {

    @Nested
    class GivenSmallGraphs {

        @Test
        void matchesNothingWithoutEdges() {
            assertThat(MaximumWeightMatching.of(3, List.of()).mates()).containsExactly(-1, -1, -1);
        }

        @Test
        void prefersMoreEdgesOverMoreWeight() {
            // Path 0-1-2-3: the heavy middle edge alone weighs more, but two edges are more.
            var edges = List.of(edge(0, 1, 1), edge(1, 2, 10), edge(2, 3, 1));

            assertThat(MaximumWeightMatching.of(4, edges).mates()).containsExactly(1, 0, 3, 2);
        }

        @Test
        void prefersTheHeavierOfTwoPerfectMatchings() {
            var edges = List.of(edge(0, 1, 5), edge(2, 3, 5), edge(0, 2, 6), edge(1, 3, 6));

            assertThat(MaximumWeightMatching.of(4, edges).mates()).containsExactly(2, 3, 0, 1);
        }

        @Test
        void findsThePerfectMatchingThroughAnOddCycle() {
            // A triangle 0-1-2 with a tail 2-3 and 0-4, 5-1: needs a blossom to see 0-4, 1-5, 2-3.
            var edges =
                    List.of(edge(0, 1, 9), edge(1, 2, 9), edge(2, 0, 9), edge(2, 3, 1), edge(0, 4, 1), edge(1, 5, 1));

            assertThat(MaximumWeightMatching.of(6, edges).mates()).containsExactly(4, 5, 3, 2, 0, 1);
        }
    }

    @Nested
    class GivenRandomGraphs {

        @Test
        void agreesWithExhaustiveSearchOnCardinalityAndWeight() {
            var random = new Random(20260925L);
            for (var graph = 0; graph < 3_000; graph++) {
                var vertices = 1 + random.nextInt(10);
                var edges = randomEdges(random, vertices);

                var mates = MaximumWeightMatching.of(vertices, edges).mates();

                var best = exhaustiveBest(vertices, edges);
                assertThat(isMatching(mates)).isTrue();
                assertThat(new long[] {cardinality(mates), weight(mates, edges)})
                        .as("graph %d: %s", graph, edges)
                        .containsExactly(best);
            }
        }
    }

    private static MaximumWeightMatching.Edge edge(int u, int v, long weight) {
        return new MaximumWeightMatching.Edge(u, v, BigInteger.valueOf(weight));
    }

    private static List<MaximumWeightMatching.Edge> randomEdges(Random random, int vertices) {
        var edges = new ArrayList<MaximumWeightMatching.Edge>();
        var density = random.nextDouble();
        for (var u = 0; u < vertices; u++) {
            for (var v = u + 1; v < vertices; v++) {
                if (random.nextDouble() < density) {
                    edges.add(edge(u, v, 1 + random.nextInt(random.nextBoolean() ? 4 : 1_000)));
                }
            }
        }
        return edges;
    }

    /** The best (cardinality, weight) of any matching, by trying every one. */
    private static long[] exhaustiveBest(int vertices, List<MaximumWeightMatching.Edge> edges) {
        var best = new long[] {0, 0};
        search(0, new boolean[vertices], 0, 0, edges, best);
        return best;
    }

    private static void search(
            int from,
            boolean[] used,
            long cardinality,
            long weight,
            List<MaximumWeightMatching.Edge> edges,
            long[] best) {
        if (cardinality > best[0] || (cardinality == best[0] && weight > best[1])) {
            best[0] = cardinality;
            best[1] = weight;
        }
        for (var index = from; index < edges.size(); index++) {
            var edge = edges.get(index);
            if (!used[edge.u()] && !used[edge.v()]) {
                used[edge.u()] = used[edge.v()] = true;
                search(index + 1, used, cardinality + 1, weight + edge.weight().longValueExact(), edges, best);
                used[edge.u()] = used[edge.v()] = false;
            }
        }
    }

    private static boolean isMatching(int[] mates) {
        for (var vertex = 0; vertex < mates.length; vertex++) {
            if (mates[vertex] != -1 && mates[mates[vertex]] != vertex) {
                return false;
            }
        }
        return true;
    }

    private static long cardinality(int[] mates) {
        return java.util.Arrays.stream(mates).filter(mate -> mate != -1).count() / 2;
    }

    private static long weight(int[] mates, List<MaximumWeightMatching.Edge> edges) {
        return edges.stream()
                .filter(edge -> mates[edge.u()] == edge.v())
                .mapToLong(edge -> edge.weight().longValueExact())
                .sum();
    }
}
