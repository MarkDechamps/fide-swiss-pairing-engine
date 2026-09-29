package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.matching.CheapestPerfectMatching;
import io.github.markdechamps.fideswiss.matching.MaximumCardinalityMatching;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * The only two questions the Olympiad procedure asks a matching: how many pairings can these teams form (Article
 * 4, 9.3's "subgroup unsolvable"), and, of the ways to pair them all, which keeps the most pairings of one kind
 * (Kept Pairings). Neither knows about chess; the Literal Enumerator answers both by trying every pairing.
 */
interface Matchings {

    Matchings BLOSSOM = new Blossom();

    int maximumPairs(List<Team> teams, BiPredicate<Team, Team> allowed);

    /**
     * A perfect pairing of {@code teams} over the pairs {@code allowed}, with as many pairs {@code kept} as any has:
     * each team's partner. Empty when the teams cannot all be paired.
     */
    Optional<Map<Team, Team>> mostKept(List<Team> teams, BiPredicate<Team, Team> allowed, BiPredicate<Team, Team> kept);

    default boolean canPairAll(List<Team> teams, BiPredicate<Team, Team> allowed) {
        return teams.size() % 2 == 0 && maximumPairs(teams, allowed) == teams.size() / 2;
    }

    /** The core matchings: Edmonds' blossom algorithm, and the cheapest perfect matching (a kept pair costs 0). */
    final class Blossom implements Matchings {

        @Override
        public int maximumPairs(List<Team> teams, BiPredicate<Team, Team> allowed) {
            return MaximumCardinalityMatching.maximumPairs(teams, allowed);
        }

        @Override
        public Optional<Map<Team, Team>> mostKept(
                List<Team> teams, BiPredicate<Team, Team> allowed, BiPredicate<Team, Team> kept) {
            return CheapestPerfectMatching.of(
                            teams,
                            (one, other) -> allowed.test(one, other)
                                    ? Optional.of(kept.test(one, other) ? BigInteger.ZERO : BigInteger.ONE)
                                    : Optional.empty())
                    .map(result -> {
                        var partner = new HashMap<Team, Team>();
                        result.pairs().forEach(pair -> {
                            partner.put(pair.get(0), pair.get(1));
                            partner.put(pair.get(1), pair.get(0));
                        });
                        return partner;
                    });
        }
    }
}
