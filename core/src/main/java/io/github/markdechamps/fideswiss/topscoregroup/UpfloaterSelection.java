package io.github.markdechamps.fideswiss.topscoregroup;

import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;

/**
 * 3.5: the set of upfloaters for the top-scoregroup.
 *
 * <p>The sets are generated literally, in the order of 3.5.3 and 3.5.4, and the first that reaches the target is
 * chosen (3.5.5, ruling A7). The target comes from one round-wide matching of the residents and every lower
 * contender, in which every criterion adds up over the pairs:
 *
 * <ul>
 *   <li>[C4] one per upfloater (a resident paired with a lower contender);
 *   <li>[C5] one per upfloater at its score, the lowest score weighing most;
 *   <li>[C6] one per contender of the following scoregroup paired below it: the fewest upfloaters that group
 *       needs, which [C3] and [C4] ask of its bracket;
 *   <li>[C7] one per upfloater that floated in the previous round.
 * </ul>
 *
 * [C3] holds because the matching is perfect. The same matching, restricted to a partial set, prunes every branch
 * that cannot reach the target, so the first set reached is the answer. A branch the last matching that reached
 * the target already lies in needs no matching of its own (a known optimal candidate).
 */
final class UpfloaterSelection {

    private enum Role {
        MUST_FLOAT,
        MUST_NOT_FLOAT
    }

    private static final int C4 = 0;
    private static final int C5 = 1;

    private final TopScoregroupRound round;

    UpfloaterSelection(TopScoregroupRound round) {
        this.round = round;
    }

    /** The upfloaters; empty when no set leaves the round completable (3.3.3). */
    Optional<List<Contender>> select(List<Contender> residents, List<Contender> lower) {
        return new Search(residents, lower).run();
    }

    private final class Search {
        private final List<Contender> residents;
        private final List<Contender> lower;
        private final Set<Contender> residentSet;
        private final Optional<Points> followingScore;
        private final Map<Points, Integer> levelPosition = new HashMap<>();
        private final int c6Position;
        private final int c7Position;
        private final LexicographicWeights weights;
        private final Map<Contender, Role> roles = new HashMap<>();
        private boolean countFollowingScoregroup = true;
        private BigInteger target;
        private List<ContenderPair> knownOptimal = List.of();

        Search(List<Contender> residents, List<Contender> lower) {
            this.residents = residents;
            this.lower = lower;
            this.residentSet = new HashSet<>(residents);
            this.followingScore = round.followingScore(residents.getFirst().score());
            var levels =
                    lower.stream().map(Contender::score).distinct().sorted().toList();
            for (var i = 0; i < levels.size(); i++) {
                levelPosition.put(levels.get(i), C5 + i);
            }
            this.c6Position = C5 + levels.size();
            this.c7Position = c6Position + 1;
            this.weights = new LexicographicWeights(residents.size() + lower.size() + 1, c7Position + 1);
        }

        Optional<List<Contender>> run() {
            var cheapest = matching();
            if (cheapest.isEmpty()) {
                return Optional.empty();
            }
            var best = cheapest.get();
            if (parityMinimumOutOfReach(best)) {
                countFollowingScoregroup = false; // every set fails [C6]: it no longer tells them apart
                best = matching().orElseThrow();
            }
            target = best.cost();
            knownOptimal = best.pairs();
            var profile = profileOf(best);
            var chosen = new ArrayList<Contender>();
            if (!chooseLevel(new ArrayList<>(profile.descendingMap().entrySet()), 0, chosen)) {
                throw new IllegalStateException("Target unreachable choosing upfloaters for " + residents);
            }
            return Optional.of(List.copyOf(chosen));
        }

        /** Ruling A6: under the parity minimum, [C6] is complied with or not. */
        private boolean parityMinimumOutOfReach(CheapestPerfectMatching.Result best) {
            if (round.upfloaterLookAhead() != UpfloaterLookAhead.PARITY_MINIMUM) {
                return false;
            }
            var upfloaters = best.pairs().stream()
                    .flatMap(pair -> upfloaterIn(pair).stream())
                    .toList();
            var left = lower.stream()
                    .filter(this::inFollowingScoregroup)
                    .filter(contender -> !upfloaters.contains(contender))
                    .count();
            var pairedBelow = best.pairs().stream()
                    .filter(this::leavesFollowingScoregroup)
                    .count();
            return pairedBelow > left % 2;
        }

        /** 3.5.3: descending score, then ascending TPN; 3.5.4: sets in lexicographic order. */
        private boolean chooseLevel(List<Map.Entry<Points, Integer>> levels, int level, List<Contender> chosen) {
            if (level == levels.size()) {
                return true;
            }
            var score = levels.get(level).getKey();
            var candidates = lower.stream()
                    .filter(contender -> contender.score().equals(score))
                    .sorted(Comparator.comparingInt(Contender::tpn))
                    .toList();
            return choose(levels, level, candidates, 0, levels.get(level).getValue(), chosen);
        }

        private boolean choose(
                List<Map.Entry<Points, Integer>> levels,
                int level,
                List<Contender> candidates,
                int from,
                int still,
                List<Contender> chosen) {
            if (still == 0) {
                var closed = candidates.subList(from, candidates.size());
                return withRole(closed, Role.MUST_NOT_FLOAT, () -> chooseLevel(levels, level + 1, chosen));
            }
            for (var i = from; i <= candidates.size() - still; i++) {
                var next = i + 1;
                var contender = candidates.get(i);
                var skipped = candidates.subList(from, i);
                var found = withRole(
                        skipped,
                        Role.MUST_NOT_FLOAT,
                        () -> withRole(List.of(contender), Role.MUST_FLOAT, () -> {
                            if (!canReach()) {
                                return false;
                            }
                            chosen.add(contender);
                            if (choose(levels, level, candidates, next, still - 1, chosen)) {
                                return true;
                            }
                            chosen.removeLast();
                            return false;
                        }));
                if (found) {
                    return true;
                }
            }
            return false;
        }

        private boolean countsForC7(Contender upfloater) {
            return !round.floatCriteriaLapse() && upfloater.floatedInPreviousRound();
        }

        private boolean canReach() {
            if (knownOptimalRespectsRoles()) {
                return true;
            }
            var reached = matching().filter(result -> result.cost().equals(target));
            reached.ifPresent(result -> knownOptimal = result.pairs());
            return reached.isPresent();
        }

        private boolean knownOptimalRespectsRoles() {
            for (var pair : knownOptimal) {
                var upfloater = upfloaterIn(pair);
                for (var member : List.of(pair.top(), pair.bottom())) {
                    var role = roles.get(member);
                    var floats = upfloater.filter(member::equals).isPresent();
                    if (role == Role.MUST_FLOAT && !floats || role == Role.MUST_NOT_FLOAT && floats) {
                        return false;
                    }
                }
            }
            return true;
        }

        private TreeMap<Points, Integer> profileOf(CheapestPerfectMatching.Result result) {
            var profile = new TreeMap<Points, Integer>();
            for (var pair : result.pairs()) {
                upfloaterIn(pair).ifPresent(contender -> profile.merge(contender.score(), 1, Integer::sum));
            }
            return profile;
        }

        private Optional<CheapestPerfectMatching.Result> matching() {
            var contenders = new ArrayList<>(residents);
            contenders.addAll(lower);
            return CheapestPerfectMatching.of(contenders, this::cost);
        }

        private Optional<BigInteger> cost(Contender a, Contender b) {
            if (!a.mayMeet(b)) {
                return Optional.empty();
            }
            var aResident = residentSet.contains(a);
            var bResident = residentSet.contains(b);
            if (aResident && bResident) {
                return Optional.of(BigInteger.ZERO);
            }
            if (!aResident && !bResident) {
                return restPairCost(a, b);
            }
            var upfloater = aResident ? b : a;
            if (roles.get(upfloater) == Role.MUST_NOT_FLOAT) {
                return Optional.empty();
            }
            return Optional.of(weights.of(C4, 1)
                    .add(weights.of(levelPosition.get(upfloater.score()), 1))
                    .add(weights.of(c7Position, countsForC7(upfloater) ? 1 : 0)));
        }

        /** Two contenders left for later brackets: free, unless one must float, or it is [C6]'s concern. */
        private Optional<BigInteger> restPairCost(Contender a, Contender b) {
            if (roles.get(a) == Role.MUST_FLOAT || roles.get(b) == Role.MUST_FLOAT) {
                return Optional.empty();
            }
            var leaves = countFollowingScoregroup && inFollowingScoregroup(a) != inFollowingScoregroup(b);
            return Optional.of(weights.of(c6Position, leaves ? 1 : 0));
        }

        private boolean leavesFollowingScoregroup(ContenderPair pair) {
            return upfloaterIn(pair).isEmpty()
                    && !residentSet.contains(pair.top())
                    && inFollowingScoregroup(pair.top()) != inFollowingScoregroup(pair.bottom());
        }

        private boolean inFollowingScoregroup(Contender contender) {
            return followingScore.filter(contender.score()::equals).isPresent();
        }

        private Optional<Contender> upfloaterIn(ContenderPair pair) {
            var topResident = residentSet.contains(pair.top());
            var bottomResident = residentSet.contains(pair.bottom());
            if (topResident == bottomResident) {
                return Optional.empty();
            }
            return Optional.of(topResident ? pair.bottom() : pair.top());
        }

        private boolean withRole(List<Contender> contenders, Role role, BooleanSupplier body) {
            var previous = new HashMap<Contender, Role>();
            contenders.forEach(contender -> previous.put(contender, roles.put(contender, role)));
            try {
                return body.getAsBoolean();
            } finally {
                previous.forEach((contender, old) -> {
                    if (old == null) {
                        roles.remove(contender);
                    } else {
                        roles.put(contender, old);
                    }
                });
            }
        }
    }
}
