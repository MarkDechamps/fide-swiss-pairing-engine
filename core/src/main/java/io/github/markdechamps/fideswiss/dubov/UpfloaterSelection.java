package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.matching.CheapestPerfectMatching;
import io.github.markdechamps.fideswiss.matching.OrderedSetSearch;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * 3.2.1–3.2.2: the set of upfloaters for a scoregroup.
 *
 * <p>The sets are generated literally in the order of 4.2 (descending score, then ascending Pairing Number; sets
 * by smallest differing sequence number), and the first that reaches the target is chosen. The target comes from
 * one round-wide matching of the remaining residents and every lower player, in which every criterion adds up
 * over the pairs:
 *
 * <ul>
 *   <li>[C5] one per upfloater (a resident paired with a lower player);
 *   <li>[C6] one per upfloater at its score, the lowest score weighing most ("maximise the scores taken in
 *       ascending order");
 *   <li>[C7] one per pair of the bracket whose players want the same colour;
 *   <li>[C8] one per upfloater that is a Maximum Upfloater, [C9] its upfloats, [C10] one per upfloater that
 *       upfloated in the previous round (none of them in the last round).
 * </ul>
 *
 * Pairs of two lower players cost nothing: they only have to exist ([C4]). The same matching, restricted to a
 * partial set, prunes every branch that cannot reach the target, so the first set reached is the answer.
 */
final class UpfloaterSelection {

    record Choice(List<Player> upfloaters, long sameColourPairs) {}

    /** 4.2.2: descending score, then ascending Pairing Number. */
    static final Comparator<Player> SEQUENCE =
            Comparator.comparing(Player::score).reversed().thenComparingInt(Player::tpn);

    private final RoundToPair round;

    UpfloaterSelection(RoundToPair round) {
        this.round = round;
    }

    Choice select(List<Player> residents, List<Player> lower) {
        return new Search(residents, lower).run();
    }

    private final class Search {

        private static final int C5 = 0;

        private final List<Player> residents;
        private final List<Player> lower;
        private final Set<Player> residentSet = Collections.newSetFromMap(new IdentityHashMap<>());
        private final Map<Score, Integer> levelPosition = new HashMap<>();
        private final int c7Position;
        private final LexicographicWeights weights;

        Search(List<Player> residents, List<Player> lower) {
            this.residents = residents;
            this.lower = lower;
            residentSet.addAll(residents);
            var levels = lower.stream().map(Player::score).distinct().sorted().toList();
            for (var index = 0; index < levels.size(); index++) {
                levelPosition.put(levels.get(index), C5 + 1 + index);
            }
            this.c7Position = C5 + 1 + levels.size();
            var players = residents.size() + lower.size();
            var mostUpfloats = lower.stream().mapToInt(Player::upfloats).max().orElse(0);
            this.weights = new LexicographicWeights(players * (mostUpfloats + 1) + 1, c7Position + 4);
        }

        Choice run() {
            var best = matching(Set.of(), Set.of())
                    .orElseThrow(() -> new NoRoundPairingException(
                            "C.04.4.1 3.2.1, [C4]", "no set of upfloaters completes the round-pairing"));
            var target = best.cost();
            var levels = new ArrayList<OrderedSetSearch.Level<Player>>();
            profileOf(best)
                    .descendingMap()
                    .forEach((score, count) -> levels.add(new OrderedSetSearch.Level<>(
                            lower.stream()
                                    .filter(player -> player.score().equals(score))
                                    .sorted(SEQUENCE)
                                    .toList(),
                            count)));
            var chosen = OrderedSetSearch.firstReachable(
                            levels,
                            (floating, notFloating) -> matching(floating, notFloating)
                                    .filter(result -> result.cost().equals(target))
                                    .isPresent())
                    .orElseThrow(() -> new IllegalStateException("Upfloater target unreachable for " + residents));
            return new Choice(chosen, sameColourPairsOf(best));
        }

        private TreeMap<Score, Integer> profileOf(CheapestPerfectMatching.Result<Player> result) {
            var profile = new TreeMap<Score, Integer>();
            for (var pair : result.pairs()) {
                upfloaterIn(pair).ifPresent(player -> profile.merge(player.score(), 1, Integer::sum));
            }
            return profile;
        }

        private long sameColourPairsOf(CheapestPerfectMatching.Result<Player> result) {
            return result.pairs().stream()
                    .filter(pair -> residentSet.contains(pair.get(0)) || residentSet.contains(pair.get(1)))
                    .mapToLong(pair -> RoundToPair.sameColourWanted(pair.get(0), pair.get(1)))
                    .sum();
        }

        private Optional<CheapestPerfectMatching.Result<Player>> matching(
                Set<Player> floating, Set<Player> notFloating) {
            var players = new ArrayList<>(residents);
            players.addAll(lower);
            return CheapestPerfectMatching.of(players, (a, b) -> cost(a, b, floating, notFloating));
        }

        private Optional<BigInteger> cost(Player a, Player b, Set<Player> floating, Set<Player> notFloating) {
            if (!round.mayMeet(a, b)) {
                return Optional.empty();
            }
            var aResident = residentSet.contains(a);
            var bResident = residentSet.contains(b);
            if (aResident && bResident) {
                return Optional.of(weights.of(c7Position, RoundToPair.sameColourWanted(a, b)));
            }
            if (!aResident && !bResident) {
                var eitherMustFloat = floating.contains(a) || floating.contains(b);
                return eitherMustFloat ? Optional.empty() : Optional.of(BigInteger.ZERO);
            }
            var upfloater = aResident ? b : a;
            if (notFloating.contains(upfloater)) {
                return Optional.empty();
            }
            return Optional.of(weights.of(C5, 1)
                    .add(weights.of(levelPosition.get(upfloater.score()), 1))
                    .add(weights.of(c7Position, RoundToPair.sameColourWanted(a, b)))
                    .add(weights.of(c7Position + 1, maximumUpfloater(upfloater)))
                    .add(weights.of(c7Position + 2, upfloatsOfMaximumUpfloater(upfloater)))
                    .add(weights.of(c7Position + 3, upfloatedInThePreviousRound(upfloater))));
        }

        /** [C8]. */
        private long maximumUpfloater(Player upfloater) {
            return !round.isLastRound() && round.isMaximumUpfloater(upfloater) ? 1 : 0;
        }

        /** [C9], read as the sum of the upfloats of the Maximum Upfloaters (a count would only repeat [C8]). */
        private long upfloatsOfMaximumUpfloater(Player upfloater) {
            return !round.isLastRound() && round.isMaximumUpfloater(upfloater) ? upfloater.upfloats() : 0;
        }

        /** [C10]. */
        private long upfloatedInThePreviousRound(Player upfloater) {
            return !round.isLastRound() && upfloater.upfloatedPreviousRound() ? 1 : 0;
        }

        private Optional<Player> upfloaterIn(List<Player> pair) {
            var firstResident = residentSet.contains(pair.get(0));
            var secondResident = residentSet.contains(pair.get(1));
            if (firstResident == secondResident) {
                return Optional.empty();
            }
            return Optional.of(firstResident ? pair.get(1) : pair.get(0));
        }
    }
}
