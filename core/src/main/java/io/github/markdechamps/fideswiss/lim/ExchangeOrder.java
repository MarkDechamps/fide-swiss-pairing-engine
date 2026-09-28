package io.github.markdechamps.fideswiss.lim;

import java.util.ArrayList;
import java.util.List;

/**
 * Article 4.2–4.3, generalised beyond six players. The players still to pair are arranged in the direction's order
 * and proposed top half against bottom half (2.4); the first of them is scrutinised (4.1) and tries, in turn, the
 * bottom half in order and then the top half from the bottom up: for six players, #1 tries 4, 5, 6, 3, 2 (the
 * columns of 4.2). Once it has an opponent the rest is proposed afresh the same way, which gives every column of
 * 4.2 and 4.3.
 */
final class ExchangeOrder {

    private ExchangeOrder() {}

    /** The opponents the first of {@code arranged} tries, in order. */
    static List<Player> forFirstOf(List<Player> arranged) {
        var half = arranged.size() / 2;
        var order = new ArrayList<>(arranged.subList(half, arranged.size()));
        for (var index = half - 1; index >= 1; index--) {
            order.add(arranged.get(index));
        }
        return order;
    }

    /** 2.4: 1 v (n/2 + 1), 2 v (n/2 + 2), ..., n/2 v n. */
    static List<Pair> proposed(List<Player> arranged) {
        var half = arranged.size() / 2;
        var pairs = new ArrayList<Pair>();
        for (var index = 0; index < half; index++) {
            pairs.add(new Pair(arranged.get(index), arranged.get(index + half)));
        }
        return pairs;
    }
}
