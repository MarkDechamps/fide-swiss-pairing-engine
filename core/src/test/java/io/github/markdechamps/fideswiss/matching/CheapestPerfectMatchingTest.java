package io.github.markdechamps.fideswiss.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CheapestPerfectMatchingTest {

    @Test
    void findsTheCheapestWayToPairEveryone() {
        var result = CheapestPerfectMatching.of(List.of("a", "b", "c", "d"), (x, y) -> Optional.of(cost(x, y)));

        assertThat(result).hasValueSatisfying(matching -> {
            assertThat(matching.cost()).isEqualTo(BigInteger.valueOf(2));
            assertThat(matching.pairs()).containsExactlyInAnyOrder(List.of("a", "b"), List.of("c", "d"));
        });
    }

    @Test
    void isEmptyWhenSomeoneCannotBePaired() {
        var result = CheapestPerfectMatching.of(
                List.of("a", "b", "c", "d"),
                (x, y) -> x.equals("d") || y.equals("d") ? Optional.empty() : Optional.of(BigInteger.ONE));

        assertThat(result).isEmpty();
    }

    @Test
    void pairsNobodyAtNoCost() {
        assertThat(CheapestPerfectMatching.<String>of(List.of(), (x, y) -> Optional.of(BigInteger.ONE)))
                .hasValueSatisfying(matching -> assertThat(matching.cost()).isZero());
    }

    private static BigInteger cost(String x, String y) {
        var pair = x + y;
        return BigInteger.valueOf(pair.equals("ab") || pair.equals("cd") ? 1 : 5);
    }
}
