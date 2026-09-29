package io.github.markdechamps.fideswiss.olympiad;

import java.util.ArrayList;
import java.util.List;

/**
 * 9.3: with 2N teams arranged in priority order, the first tries N + 1, N + 2, ... 2N, and then N, N - 1, ... 2 (the
 * text says "N - 1, N - 2", but its six-team example tries 3 before 2). Once it has an opponent the subgroup of
 * 2N - 2 teams is treated the same way. It is the Lim System's generalised Article 4, word for word.
 */
final class ExchangeOrder {

    private ExchangeOrder() {}

    /** The opponents the first of {@code arranged} tries, in order. */
    static List<Team> forFirstOf(List<Team> arranged) {
        var half = arranged.size() / 2;
        var order = new ArrayList<>(arranged.subList(half, arranged.size()));
        for (var index = half - 1; index >= 1; index--) {
            order.add(arranged.get(index));
        }
        return order;
    }
}
