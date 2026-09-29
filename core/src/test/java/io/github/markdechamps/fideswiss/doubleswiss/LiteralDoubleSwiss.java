package io.github.markdechamps.fideswiss.doubleswiss;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

/**
 * The Literal Enumerator of C.04.5: the text by plain enumeration, with no matching. 3.4 tries the players in its
 * order; 3.5 lists every set of potential upfloaters, sorts them as 3.5.3 and 3.5.4 say and assesses each on
 * [C3]–[C7]; 3.6 lists every pairing of the bracket and takes the first identifier among the best on [C8]. "Can
 * these players all be paired" and "how few upfloaters does the following scoregroup need" are answered by a
 * memoised recursion over subsets. Small fields only.
 */
final class LiteralDoubleSwiss {

    /** The round-pairing before colours; empty pairs and PAB when it cannot be completed (3.3.3). */
    record Outcome(List<ContenderPair> pairs, Optional<Contender> pairingAllocatedBye) {}

    /** One set of potential upfloaters with what 3.5 compares; lower is better. */
    private record Candidate(List<Contender> sequence, List<Points> ascendingScores, long c6, long c7) {}

    private final List<Contender> all;
    private final boolean lastRound;
    private final UpfloaterLookAhead lookAhead;
    private final Map<Contender, Integer> bit = new HashMap<>();
    private final Map<Long, Boolean> pairable = new HashMap<>();
    private final Map<Long, Integer> fewestCrossings = new HashMap<>();
    private long followingMask;

    LiteralDoubleSwiss(List<Contender> contenders, boolean lastRound, UpfloaterLookAhead lookAhead) {
        this.all = List.copyOf(contenders);
        this.lastRound = lastRound;
        this.lookAhead = lookAhead;
        for (var index = 0; index < all.size(); index++) {
            bit.put(all.get(index), index);
        }
    }

    Optional<Outcome> pair() {
        var unpaired = new ArrayList<>(all);
        Optional<Contender> bye = Optional.empty();
        if (unpaired.size() % 2 == 1) {
            bye = pairingAllocatedBye(unpaired);
            if (bye.isEmpty()) {
                return Optional.empty();
            }
            unpaired.remove(bye.get());
        } else if (!canPairAll(mask(unpaired))) {
            return Optional.empty();
        }
        var pairs = new ArrayList<ContenderPair>();
        while (!unpaired.isEmpty()) {
            var topScore = unpaired.stream()
                    .map(Contender::score)
                    .max(Comparator.naturalOrder())
                    .orElseThrow();
            var residents =
                    unpaired.stream().filter(c -> c.score().equals(topScore)).toList();
            var lower = unpaired.stream()
                    .filter(c -> c.score().isLessThan(topScore))
                    .toList();
            var upfloaters = upfloaters(residents, lower);
            if (upfloaters.isEmpty()) {
                return Optional.empty();
            }
            var bracket = new ArrayList<>(residents);
            bracket.addAll(upfloaters.get());
            pairs.addAll(pairing(bracket, upfloaters.get()));
            unpaired.removeAll(bracket);
        }
        return Optional.of(new Outcome(pairs, bye));
    }

    /** 3.4: [C2], then 3.4.1 (with [C3]), in the order of 3.4.2–3.4.4. */
    private Optional<Contender> pairingAllocatedBye(List<Contender> players) {
        return players.stream()
                .filter(player -> !player.pairingAllocatedByeBarred())
                .sorted(Comparator.comparing(Contender::score)
                        .thenComparing(Comparator.comparingInt(Contender::matchesPlayed)
                                .reversed())
                        .thenComparing(Comparator.comparingInt(Contender::tpn).reversed()))
                .filter(player -> canPairAll(mask(players) & ~(1L << bit.get(player))))
                .findFirst();
    }

    /** 3.5: every set, legal with the top-scoregroup and leaving the rest pairable, first best on [C4]–[C7]. */
    private Optional<List<Contender>> upfloaters(List<Contender> residents, List<Contender> lower) {
        var following = followingScore(residents.getFirst().score());
        followingMask = 0;
        for (var player : lower) {
            if (following.filter(player.score()::equals).isPresent()) {
                followingMask |= 1L << bit.get(player);
            }
        }
        fewestCrossings.clear();
        var candidates = new ArrayList<Candidate>();
        var residentMask = mask(residents);
        for (var subset = 0L; subset < 1L << lower.size(); subset++) {
            var set = new ArrayList<Contender>();
            for (var index = 0; index < lower.size(); index++) {
                if ((subset >> index & 1) == 1) {
                    set.add(lower.get(index));
                }
            }
            var setMask = mask(set);
            var restMask = mask(lower) & ~setMask;
            if (!canPairAll(residentMask | setMask) || !canPairAll(restMask)) {
                continue;
            }
            set.sort(Comparator.comparing(Contender::score).reversed().thenComparingInt(Contender::tpn));
            var ascending = set.stream().map(Contender::score).sorted().toList();
            candidates.add(new Candidate(set, ascending, c6(restMask), c7(set)));
        }
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        var fewest = candidates.stream()
                .mapToInt(candidate -> candidate.sequence().size())
                .min()
                .orElseThrow();
        var c4 = candidates.stream()
                .filter(candidate -> candidate.sequence().size() == fewest)
                .toList();
        var bestScores = c4.stream()
                .map(Candidate::ascendingScores)
                .max(LiteralDoubleSwiss::compareScores)
                .orElseThrow();
        var c5 = c4.stream()
                .filter(candidate -> candidate.ascendingScores().equals(bestScores))
                .toList();
        var everyOneFailsC6 =
                lookAhead == UpfloaterLookAhead.PARITY_MINIMUM && c5.stream().allMatch(c -> c.c6() > 0);
        return c5.stream()
                .min(Comparator.comparingLong((Candidate c) -> everyOneFailsC6 ? 0 : c.c6())
                        .thenComparingLong(Candidate::c7)
                        .thenComparing(Candidate::sequence, LiteralDoubleSwiss::byTpn))
                .map(Candidate::sequence);
    }

    /** The scoregroup right below in the round's standings, the PAB player included. */
    private Optional<Points> followingScore(Points score) {
        var scores = new TreeSet<Points>();
        all.forEach(player -> scores.add(player.score()));
        return Optional.ofNullable(scores.lower(score));
    }

    /** [C6]: graded, the fewest upfloaters the following scoregroup needs; by default whether that is its parity. */
    private long c6(long restMask) {
        var left = restMask & followingMask;
        if (left == 0) {
            return 0;
        }
        var needed = fewestCrossings(restMask);
        if (lookAhead == UpfloaterLookAhead.GRADED) {
            return needed;
        }
        return needed == Long.bitCount(left) % 2 ? 0 : 1;
    }

    /** [C7]: upfloaters who were floaters in the previous round, except in the last round. */
    private long c7(List<Contender> set) {
        return lastRound
                ? 0
                : set.stream().filter(Contender::floatedInPreviousRound).count();
    }

    /** 3.6: every legal pairing of the bracket; the first identifier among those best on [C8]. */
    private List<ContenderPair> pairing(List<Contender> bracket, List<Contender> upfloaters) {
        var sorted =
                bracket.stream().sorted(Comparator.comparingInt(Contender::tpn)).toList();
        var pairings = new ArrayList<List<ContenderPair>>();
        enumerate(sorted, new ArrayList<>(), pairings);
        return pairings.stream()
                .min(Comparator.comparingLong((List<ContenderPair> p) -> c8(p, upfloaters))
                        .thenComparing(LiteralDoubleSwiss::identifier, LiteralDoubleSwiss::byInt))
                .orElseThrow(() -> new IllegalStateException("bracket unpairable: " + bracket));
    }

    private void enumerate(List<Contender> left, List<ContenderPair> sofar, List<List<ContenderPair>> out) {
        if (left.isEmpty()) {
            out.add(List.copyOf(sofar));
            return;
        }
        var first = left.getFirst();
        for (var index = 1; index < left.size(); index++) {
            var other = left.get(index);
            if (!mayMeet(first, other)) {
                continue;
            }
            var rest = new ArrayList<>(left);
            rest.remove(other);
            rest.removeFirst();
            sofar.add(ContenderPair.of(first, other));
            enumerate(rest, sofar, out);
            sofar.removeLast();
        }
    }

    /** 3.6.2, literally: the top members in ascending order, then the bottom member of each. */
    static List<Integer> identifier(List<ContenderPair> pairing) {
        var byTop = pairing.stream()
                .sorted(Comparator.comparingInt(pair -> pair.top().tpn()))
                .toList();
        var identifier = new ArrayList<Integer>();
        byTop.forEach(pair -> identifier.add(pair.top().tpn()));
        byTop.forEach(pair -> identifier.add(pair.bottom().tpn()));
        return identifier;
    }

    /** [C8]: the resident opponents of upfloaters who were floaters in the previous round, except in the last. */
    private long c8(List<ContenderPair> pairing, List<Contender> upfloaters) {
        if (lastRound) {
            return 0;
        }
        return pairing.stream()
                .filter(pair -> upfloaters.contains(pair.top()) != upfloaters.contains(pair.bottom()))
                .map(pair -> upfloaters.contains(pair.top()) ? pair.bottom() : pair.top())
                .filter(Contender::floatedInPreviousRound)
                .count();
    }

    private static boolean mayMeet(Contender one, Contender other) {
        return !one.met().contains(other.id());
    }

    private boolean canPairAll(long mask) {
        if (mask == 0) {
            return true;
        }
        var cached = pairable.get(mask);
        if (cached != null) {
            return cached;
        }
        var first = Long.numberOfTrailingZeros(mask);
        var result = false;
        for (var rest = mask & ~(1L << first); rest != 0 && !result; rest &= rest - 1) {
            var other = Long.numberOfTrailingZeros(rest);
            if (mayMeet(all.get(first), all.get(other))) {
                result = canPairAll(mask & ~(1L << first) & ~(1L << other));
            }
        }
        pairable.put(mask, result);
        return result;
    }

    /** The fewest pairs between the following scoregroup and a player outside it, over every pairing of the mask. */
    private int fewestCrossings(long mask) {
        if (mask == 0) {
            return 0;
        }
        var cached = fewestCrossings.get(mask);
        if (cached != null) {
            return cached;
        }
        var first = Long.numberOfTrailingZeros(mask);
        var best = Integer.MAX_VALUE;
        for (var rest = mask & ~(1L << first); rest != 0; rest &= rest - 1) {
            var other = Long.numberOfTrailingZeros(rest);
            var remaining = mask & ~(1L << first) & ~(1L << other);
            if (!mayMeet(all.get(first), all.get(other)) || !canPairAll(remaining)) {
                continue;
            }
            var crossing = (followingMask >> first & 1) != (followingMask >> other & 1) ? 1 : 0;
            best = Math.min(best, crossing + fewestCrossings(remaining));
        }
        fewestCrossings.put(mask, best);
        return best;
    }

    private long mask(List<Contender> players) {
        var mask = 0L;
        for (var player : players) {
            mask |= 1L << bit.get(player);
        }
        return mask;
    }

    /** [C5]: ascending score lists of one length; the larger is better. */
    private static int compareScores(List<Points> one, List<Points> other) {
        for (var index = 0; index < one.size(); index++) {
            var compared = one.get(index).compareTo(other.get(index));
            if (compared != 0) {
                return compared;
            }
        }
        return 0;
    }

    private static int byTpn(List<Contender> one, List<Contender> other) {
        return byInt(
                one.stream().map(Contender::tpn).toList(),
                other.stream().map(Contender::tpn).toList());
    }

    static int byInt(List<Integer> one, List<Integer> other) {
        for (var index = 0; index < Math.min(one.size(), other.size()); index++) {
            var compared = Integer.compare(one.get(index), other.get(index));
            if (compared != 0) {
                return compared;
            }
        }
        return Integer.compare(one.size(), other.size());
    }
}
