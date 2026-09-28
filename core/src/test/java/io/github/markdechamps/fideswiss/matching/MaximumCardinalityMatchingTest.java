package io.github.markdechamps.fideswiss.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class MaximumCardinalityMatchingTest {

    @Test
    void findsAsManyPairsAsTheWeightedMatchingOnRandomGraphs() {
        var random = new Random(24L);
        for (var graph = 0; graph < 3_000; graph++) {
            var vertices = 1 + random.nextInt(14);
            var members = new ArrayList<Integer>();
            for (var vertex = 0; vertex < vertices; vertex++) {
                members.add(vertex);
            }
            var edge = new boolean[vertices][vertices];
            var edges = new ArrayList<MaximumWeightMatching.Edge>();
            var density = random.nextDouble();
            for (var a = 0; a < vertices; a++) {
                for (var b = a + 1; b < vertices; b++) {
                    if (random.nextDouble() < density) {
                        edge[a][b] = edge[b][a] = true;
                        edges.add(new MaximumWeightMatching.Edge(a, b, BigInteger.ONE));
                    }
                }
            }
            var expected = (int) java.util.Arrays.stream(
                                    MaximumWeightMatching.of(vertices, edges).mates())
                            .filter(mate -> mate != -1)
                            .count()
                    / 2;

            var pairs = MaximumCardinalityMatching.of(members, (a, b) -> edge[a][b]);

            assertThat(pairs.size()).as("graph %d", graph).isEqualTo(expected);
            assertThat(pairs)
                    .allSatisfy(
                            pair -> assertThat(edge[pair.get(0)][pair.get(1)]).isTrue());
            assertThat(pairs.stream().flatMap(List::stream).distinct().count()).isEqualTo(2L * pairs.size());
        }
    }
}
