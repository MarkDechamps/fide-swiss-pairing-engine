package io.github.markdechamps.fideswiss.matching;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * The one question behind "can these all be paired" and "what is the best any pairing can do": the cheapest
 * perfect matching, given a cost per pair (empty when the pair is not allowed).
 */
public final class CheapestPerfectMatching {

    /** The pairs, each in the order of the list given, and their total cost. */
    public record Result<T>(BigInteger cost, List<List<T>> pairs) {}

    private CheapestPerfectMatching() {}

    public static <T> Optional<Result<T>> of(List<T> members, BiFunction<T, T, Optional<BigInteger>> cost) {
        if (members.size() % 2 == 1) {
            return Optional.empty();
        }
        var costs = new ArrayList<PairCost>();
        var highest = BigInteger.ZERO;
        for (var a = 0; a < members.size(); a++) {
            for (var b = a + 1; b < members.size(); b++) {
                var pairCost = cost.apply(members.get(a), members.get(b));
                if (pairCost.isPresent()) {
                    costs.add(new PairCost(a, b, pairCost.get()));
                    highest = highest.max(pairCost.get());
                }
            }
        }
        var ceiling = highest.add(BigInteger.ONE);
        var edges = costs.stream()
                .map(pair -> new MaximumWeightMatching.Edge(pair.a(), pair.b(), ceiling.subtract(pair.cost())))
                .toList();
        var matching = MaximumWeightMatching.of(members.size(), edges);
        if (!matching.isPerfect()) {
            return Optional.empty();
        }
        var mates = matching.mates();
        var pairs = new ArrayList<List<T>>();
        var total = BigInteger.ZERO;
        for (var pair : costs) {
            if (mates[pair.a()] == pair.b()) {
                pairs.add(List.of(members.get(pair.a()), members.get(pair.b())));
                total = total.add(pair.cost());
            }
        }
        return Optional.of(new Result<>(total, List.copyOf(pairs)));
    }

    public static <T> boolean canPairAll(List<T> members, BiPredicate<T, T> allowed) {
        return of(members, (a, b) -> allowed.test(a, b) ? Optional.of(BigInteger.ZERO) : Optional.empty())
                .isPresent();
    }

    private record PairCost(int a, int b, BigInteger cost) {}
}
