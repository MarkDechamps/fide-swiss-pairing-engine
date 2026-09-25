package io.github.markdechamps.fideswiss.matching;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OrderedSetSearchTest {

    @Test
    void triesTheSetsInSmallestDifferingSequenceNumberOrder() {
        // C.04.4.1 4.1.3: {1,2} < {1,3} < {1,4} < {2,3} < {2,4} < {3,4}.
        var tried = new ArrayList<Set<Integer>>();

        OrderedSetSearch.firstReachable(
                List.of(new OrderedSetSearch.Level<>(List.of(1, 2, 3, 4), 2)), (chosen, excluded) -> {
                    if (chosen.size() == 2 && excluded.size() == 2) {
                        tried.add(Set.copyOf(chosen));
                        return false;
                    }
                    return true;
                });

        assertThat(tried)
                .containsExactly(Set.of(1, 2), Set.of(1, 3), Set.of(1, 4), Set.of(2, 3), Set.of(2, 4), Set.of(3, 4));
    }

    @Test
    void returnsTheFirstSetWhoseBranchesStayReachable() {
        var found = OrderedSetSearch.firstReachable(
                List.of(new OrderedSetSearch.Level<>(List.of(1, 2, 3, 4), 2)),
                (chosen, excluded) -> !chosen.contains(1) && !excluded.contains(4));

        assertThat(found).contains(List.of(2, 4));
    }

    @Test
    void choosesLevelByLevelAndSeesTheEarlierChoices() {
        var found = OrderedSetSearch.firstReachable(
                List.of(new OrderedSetSearch.Level<>(List.of(1, 2), 1), new OrderedSetSearch.Level<>(List.of(3, 4), 1)),
                (chosen, excluded) -> !(chosen.contains(1) && chosen.contains(3)) && !chosen.contains(4));

        assertThat(found).contains(List.of(2, 3));
    }

    @Test
    void isEmptyWhenNoSetIsReachable() {
        var found = OrderedSetSearch.firstReachable(
                List.of(new OrderedSetSearch.Level<>(List.of(1, 2), 1)), (chosen, excluded) -> chosen.isEmpty());

        assertThat(found).isEmpty();
    }
}
