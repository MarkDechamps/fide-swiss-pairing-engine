package io.github.markdechamps.fideswiss.matching;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

/**
 * Chooses sets from pools in "smallest differing sequence number" order (Dubov 4.1.3, Swiss Team 3.5.4), one
 * level after another, and returns the first whose every branch stays reachable. Each branch is asked with the
 * members chosen so far and the members passed over; the systems answer it with a matching.
 */
public final class OrderedSetSearch {

    /** Choose {@code size} members of {@code poolInOrder}, which is sorted by sequence number. */
    public record Level<T>(List<T> poolInOrder, int size) {
        public Level {
            poolInOrder = List.copyOf(poolInOrder);
        }
    }

    private OrderedSetSearch() {}

    public static <T> Optional<List<T>> firstReachable(List<Level<T>> levels, BiPredicate<Set<T>, Set<T>> reachable) {
        var search = new Search<>(levels, reachable);
        return search.chooseLevel(0) ? Optional.of(List.copyOf(search.chosen)) : Optional.empty();
    }

    private static final class Search<T> {

        private final List<Level<T>> levels;
        private final BiPredicate<Set<T>, Set<T>> reachable;
        private final Set<T> chosen = new LinkedHashSet<>();
        private final Set<T> excluded = new LinkedHashSet<>();

        Search(List<Level<T>> levels, BiPredicate<Set<T>, Set<T>> reachable) {
            this.levels = levels;
            this.reachable = reachable;
        }

        boolean chooseLevel(int level) {
            if (level == levels.size()) {
                return reachable.test(chosen, excluded);
            }
            var current = levels.get(level);
            return choose(level, current.poolInOrder(), 0, current.size());
        }

        private boolean choose(int level, List<T> pool, int from, int still) {
            if (still == 0) {
                return withExcluded(pool.subList(from, pool.size()), () -> chooseLevel(level + 1));
            }
            for (var index = from; index <= pool.size() - still; index++) {
                var member = pool.get(index);
                var next = index + 1;
                var found = withExcluded(pool.subList(from, index), () -> {
                    chosen.add(member);
                    if (reachable.test(chosen, excluded) && choose(level, pool, next, still - 1)) {
                        return true;
                    }
                    chosen.remove(member);
                    return false;
                });
                if (found) {
                    return true;
                }
            }
            return false;
        }

        /** Runs the body with these members passed over, and restores them unless it succeeds. */
        private boolean withExcluded(List<T> members, BooleanSupplier body) {
            var added = new ArrayList<T>();
            for (var member : members) {
                if (excluded.add(member)) {
                    added.add(member);
                }
            }
            var result = body.getAsBoolean();
            if (!result) {
                added.forEach(excluded::remove);
            }
            return result;
        }
    }
}
