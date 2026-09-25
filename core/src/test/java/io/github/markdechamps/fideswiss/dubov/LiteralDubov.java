package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The Literal Enumerator for the Dubov System: C.04.4.1 by plain enumeration, with no matching anywhere. Every
 * set of upfloaters, every set of shifters and every transposition is generated in the order of Article 4 and
 * assessed by enumerating the pairings it allows. Exponential; for small fields only.
 */
final class LiteralDubov {

    private final RoundToPair round;

    LiteralDubov(RoundToPair round) {
        this.round = round;
    }

    DubovProcedure.Outcome pair(List<Player> players) {
        var bye = players.size() % 2 == 0 ? Optional.<Player>empty() : Optional.of(pairingAllocatedBye(players));
        var unpaired = new ArrayList<>(players.stream()
                .filter(player -> bye.filter(assignee -> assignee == player).isEmpty())
                .toList());
        var colours = new ColourAllocation(round);
        var games = new ArrayList<Game>();
        while (!unpaired.isEmpty()) {
            var score = unpaired.stream()
                    .map(Player::score)
                    .max(Comparable::compareTo)
                    .orElseThrow();
            var residents = unpaired.stream()
                    .filter(player -> player.score().equals(score))
                    .toList();
            var lower = unpaired.stream()
                    .filter(player -> score.isHigherThan(player.score()))
                    .sorted(UpfloaterSelection.SEQUENCE)
                    .toList();
            var bracket = new ArrayList<>(residents);
            bracket.addAll(upfloaters(residents, lower, unpaired));
            pairBracket(bracket).forEach(pair -> games.add(colours.allocate(pair)));
            unpaired.removeAll(bracket);
        }
        return new DubovProcedure.Outcome(games, bye, List.of());
    }

    private Player pairingAllocatedBye(List<Player> players) {
        return players.stream()
                .filter(Player::mayReceivePairingAllocatedBye)
                .sorted(Comparator.comparing(Player::score)
                        .thenComparing(
                                Comparator.comparingInt(Player::gamesPlayed).reversed())
                        .thenComparing(Comparator.comparingInt(Player::tpn).reversed()))
                .filter(bye -> isPairable(
                        players.stream().filter(player -> player != bye).toList()))
                .findFirst()
                .orElseThrow(() -> new NoRoundPairingException("C.04.4.1 3.1", "no PAB"));
    }

    /** A bracket's quality, compared criterion by criterion; lower is better. */
    private record Quality(int upfloaters, List<Score> upfloaterScores, long c7, long c8, long c9, long c10) {

        int compareAt(Quality other) {
            if (upfloaters != other.upfloaters) {
                return Integer.compare(upfloaters, other.upfloaters);
            }
            for (var index = 0; index < upfloaterScores.size(); index++) {
                var difference = upfloaterScores.get(index).compareTo(other.upfloaterScores.get(index));
                if (difference != 0) {
                    return -difference;
                }
            }
            if (c7 != other.c7) {
                return Long.compare(c7, other.c7);
            }
            if (c8 != other.c8) {
                return Long.compare(c8, other.c8);
            }
            if (c9 != other.c9) {
                return Long.compare(c9, other.c9);
            }
            return Long.compare(c10, other.c10);
        }
    }

    /** 3.2.1–3.2.2: the fewest upfloaters, then the first set (4.2) whose bracket is paired best on [C6]–[C10]. */
    private List<Player> upfloaters(List<Player> residents, List<Player> lower, List<Player> unpaired) {
        for (var size = 0; size <= lower.size(); size++) {
            List<Player> best = null;
            Quality bestQuality = null;
            for (var set : subsets(lower, size)) {
                var bracket = new ArrayList<>(residents);
                bracket.addAll(set);
                var rest = unpaired.stream()
                        .filter(player -> !bracket.contains(player))
                        .toList();
                var fewestC7 = fewestSameColour(bracket);
                if (fewestC7.isEmpty() || !isPairable(rest)) {
                    continue;
                }
                var quality = quality(set, fewestC7.getAsLong());
                if (bestQuality == null || quality.compareAt(bestQuality) < 0) {
                    best = set;
                    bestQuality = quality;
                }
            }
            if (best != null) {
                return best;
            }
        }
        throw new NoRoundPairingException("C.04.4.1 3.2.1", "no upfloaters");
    }

    private Quality quality(List<Player> upfloaters, long c7) {
        var last = round.isLastRound();
        var maximum = upfloaters.stream().filter(round::isMaximumUpfloater).toList();
        return new Quality(
                upfloaters.size(),
                upfloaters.stream().map(Player::score).sorted().toList(),
                c7,
                last ? 0 : maximum.size(),
                last ? 0 : maximum.stream().mapToLong(Player::upfloats).sum(),
                last
                        ? 0
                        : upfloaters.stream()
                                .filter(Player::upfloatedPreviousRound)
                                .count());
    }

    /** 3.2.3–3.2.6, literally. */
    private List<Pair> pairBracket(List<Player> players) {
        var bracket =
                players.stream().sorted(Comparator.comparingInt(Player::tpn)).toList();
        var g1 = new ArrayList<Player>();
        var g2 = new ArrayList<Player>();
        if (bracket.stream().noneMatch(Player::hasPlayed)) {
            g1.addAll(bracket.subList(0, bracket.size() / 2));
            g2.addAll(bracket.subList(bracket.size() / 2, bracket.size()));
        } else {
            bracket.forEach(player -> (player.preference().colour() == Colour.WHITE ? g1 : g2).add(player));
        }
        var fewestC7 = fewestSameColour(bracket).orElseThrow();
        var smallerIsG1 = g1.size() <= g2.size();
        var smaller = smallerIsG1 ? g1 : g2;
        var larger = smallerIsG1 ? g2 : g1;
        var together = new long[] {Long.MAX_VALUE};
        forEachPairing(
                bracket,
                pairing -> together[0] = Math.min(
                        together[0],
                        pairing.stream()
                                .filter(pair -> smaller.contains(pair.first()) && smaller.contains(pair.second()))
                                .count()));
        var k = (int) together[0];
        var smallerPool = ShifterOrder.of(smaller, smallerIsG1 ? Colour.WHITE : Colour.BLACK);
        var largerPool = ShifterOrder.of(larger, smallerIsG1 ? Colour.BLACK : Colour.WHITE);
        var d = (larger.size() + k - (smaller.size() - k)) / 2;
        var first = subsets(smallerPool, k).stream()
                .filter(x -> subsets(largerPool, d).stream().anyMatch(y -> yieldsBest(g1, g2, x, y, fewestC7)))
                .findFirst()
                .orElseThrow();
        var second = subsets(largerPool, d).stream()
                .filter(y -> yieldsBest(g1, g2, first, y, fewestC7))
                .findFirst()
                .orElseThrow();
        var newG1 = composition(bracket, g1, first, second, true);
        var newG2 = composition(bracket, g1, first, second, false);
        var s1 = newG1.stream()
                .sorted(Comparator.comparingInt(Player::aro).thenComparingInt(Player::tpn))
                .toList();
        for (var t2 : permutations(
                newG2.stream().sorted(Comparator.comparingInt(Player::tpn)).toList())) {
            var pairs = new ArrayList<Pair>();
            for (var index = 0; index < s1.size(); index++) {
                pairs.add(Pair.of(s1.get(index), t2.get(index)));
            }
            if (pairs.stream().allMatch(pair -> round.mayMeet(pair.first(), pair.second()))) {
                return pairs;
            }
        }
        throw new IllegalStateException("no legal transposition");
    }

    private boolean yieldsBest(List<Player> g1, List<Player> g2, List<Player> x, List<Player> y, long fewestC7) {
        var bracket = new ArrayList<>(g1);
        bracket.addAll(g2);
        var newG1 = composition(bracket, g1, x, y, true);
        var newG2 = composition(bracket, g1, x, y, false);
        if (newG1.size() != newG2.size()) {
            return false;
        }
        for (var t2 : permutations(newG2)) {
            var legal = true;
            var c7 = 0L;
            for (var index = 0; index < newG1.size() && legal; index++) {
                legal = round.mayMeet(newG1.get(index), t2.get(index));
                c7 += RoundToPair.sameColourWanted(newG1.get(index), t2.get(index));
            }
            if (legal && c7 == fewestC7) {
                return true;
            }
        }
        return false;
    }

    private static List<Player> composition(
            List<Player> bracket, List<Player> g1, List<Player> x, List<Player> y, boolean wantG1) {
        return bracket.stream()
                .filter(player -> (g1.contains(player) != (x.contains(player) || y.contains(player))) == wantG1)
                .toList();
    }

    private OptionalLong fewestSameColour(List<Player> bracket) {
        var best = new long[] {Long.MAX_VALUE};
        forEachPairing(
                bracket,
                pairing -> best[0] = Math.min(
                        best[0],
                        pairing.stream()
                                .mapToLong(pair -> RoundToPair.sameColourWanted(pair.first(), pair.second()))
                                .sum()));
        return best[0] == Long.MAX_VALUE ? OptionalLong.empty() : OptionalLong.of(best[0]);
    }

    private boolean isPairable(List<Player> players) {
        var found = new boolean[1];
        forEachPairing(players, pairing -> found[0] = true);
        return found[0];
    }

    private void forEachPairing(List<Player> players, Consumer<List<Pair>> action) {
        forEachPairing(players, Collections.newSetFromMap(new IdentityHashMap<>()), new ArrayList<>(), action);
    }

    private void forEachPairing(List<Player> players, Set<Player> used, List<Pair> pairs, Consumer<List<Pair>> action) {
        var first = players.stream().filter(player -> !used.contains(player)).findFirst();
        if (first.isEmpty()) {
            action.accept(pairs);
            return;
        }
        used.add(first.get());
        for (var other : players) {
            if (!used.contains(other) && round.mayMeet(first.get(), other)) {
                used.add(other);
                pairs.add(Pair.of(first.get(), other));
                forEachPairing(players, used, pairs, action);
                pairs.removeLast();
                used.remove(other);
            }
        }
        used.remove(first.get());
    }

    /** 4.1.3: the sets of this size, by smallest differing sequence number. */
    static List<List<Player>> subsets(List<Player> pool, int size) {
        var result = new ArrayList<List<Player>>();
        subsets(pool, size, 0, new ArrayList<>(), result);
        return result;
    }

    private static void subsets(List<Player> pool, int size, int from, List<Player> chosen, List<List<Player>> result) {
        if (chosen.size() == size) {
            result.add(List.copyOf(chosen));
            return;
        }
        for (var index = from; index < pool.size(); index++) {
            chosen.add(pool.get(index));
            subsets(pool, size, index + 1, chosen, result);
            chosen.removeLast();
        }
    }

    /** 4.4: the transpositions in lexicographic order of the sorted list. */
    static List<List<Player>> permutations(List<Player> sorted) {
        var result = new ArrayList<List<Player>>();
        permute(sorted, new ArrayList<>(), new boolean[sorted.size()], result);
        return result;
    }

    private static void permute(List<Player> sorted, List<Player> current, boolean[] used, List<List<Player>> result) {
        if (current.size() == sorted.size()) {
            result.add(List.copyOf(current));
            return;
        }
        for (var index = 0; index < sorted.size(); index++) {
            if (!used[index]) {
                used[index] = true;
                current.add(sorted.get(index));
                permute(sorted, current, used, result);
                current.removeLast();
                used[index] = false;
            }
        }
    }
}
