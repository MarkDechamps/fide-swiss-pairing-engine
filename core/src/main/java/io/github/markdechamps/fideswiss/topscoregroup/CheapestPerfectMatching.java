package io.github.markdechamps.fideswiss.topscoregroup;

import io.github.markdechamps.fideswiss.matching.MaximumWeightMatching;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * The one mechanism behind every "can these all be paired" and "what is the best any pairing can do" question: a
 * minimum-cost perfect matching. Rule code gives the contenders and a cost per pair (empty: not allowed) and gets
 * back the cheapest perfect matching, if any.
 */
final class CheapestPerfectMatching {

    record Result(BigInteger cost, List<ContenderPair> pairs) {}

    private CheapestPerfectMatching() {}

    static Optional<Result> of(
            List<Contender> contenders, BiFunction<Contender, Contender, Optional<BigInteger>> cost) {
        return of(contenders, cost, Comparator.comparingInt(Contender::tpn));
    }

    /** The pairs come out with the member first in {@code seatOrder} on top. */
    static Optional<Result> of(
            List<Contender> contenders,
            BiFunction<Contender, Contender, Optional<BigInteger>> cost,
            Comparator<Contender> seatOrder) {
        if (contenders.isEmpty()) {
            return Optional.of(new Result(BigInteger.ZERO, List.of()));
        }
        if (contenders.size() % 2 == 1) {
            return Optional.empty();
        }
        var costs = new ArrayList<Cost>();
        var highest = BigInteger.ZERO;
        for (var i = 0; i < contenders.size(); i++) {
            for (var j = i + 1; j < contenders.size(); j++) {
                var pairCost = cost.apply(contenders.get(i), contenders.get(j));
                if (pairCost.isPresent()) {
                    costs.add(new Cost(i, j, pairCost.get()));
                    highest = highest.max(pairCost.get());
                }
            }
        }
        var ceiling = highest.add(BigInteger.ONE);
        var edges = costs.stream()
                .map(c -> new MaximumWeightMatching.Edge(c.from(), c.to(), ceiling.subtract(c.cost())))
                .toList();
        var matching = MaximumWeightMatching.of(contenders.size(), edges);
        if (!matching.isPerfect()) {
            return Optional.empty();
        }
        var mates = matching.mates();
        var pairs = new ArrayList<ContenderPair>();
        var total = BigInteger.ZERO;
        for (var c : costs) {
            if (mates[c.from()] == c.to()) {
                pairs.add(ContenderPair.of(contenders.get(c.from()), contenders.get(c.to()), seatOrder));
                total = total.add(c.cost());
            }
        }
        return Optional.of(new Result(total, List.copyOf(pairs)));
    }

    static boolean canPairAll(List<Contender> contenders) {
        return of(contenders, (a, b) -> a.mayMeet(b) ? Optional.of(BigInteger.ZERO) : Optional.empty())
                .isPresent();
    }

    private record Cost(int from, int to, BigInteger cost) {}
}
