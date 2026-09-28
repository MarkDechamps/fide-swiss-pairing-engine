package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.matching.CheapestPerfectMatching;
import io.github.markdechamps.fideswiss.matching.OrderedSetSearch;
import io.github.markdechamps.fideswiss.tournament.Colour;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 3.2.3–3.2.6: the pairing of a bracket once its upfloaters are chosen.
 *
 * <p>Every pairing the procedure can reach pairs G1 against G2. So a pair of two players from the same initial
 * subgroup needs exactly one of them shifted, and the fewest such pairs are what "complies at best with [C7]"
 * asks. The shifts of 3.2.4 are generated literally, as sets in the order of 4.3, and a branch is followed only
 * while a matching with the chosen players forced to shift and the skipped ones barred still reaches that fewest
 * number. With those shifts every legal transposition has the fewest same-colour pairs, so 3.2.6's "first legal"
 * never loses [C7]. T2 is generated literally too, position by position in the order of 4.4, pruned by a
 * bipartite matching of what is left.
 */
final class BracketPairing {

    record Result(List<Pair> pairs, long sameColourPairs, List<Player> shifted) {}

    private static final Comparator<Player> BY_PAIRING_NUMBER = Comparator.comparingInt(Player::tpn);

    private final RoundToPair round;

    BracketPairing(RoundToPair round) {
        this.round = round;
    }

    Result pair(List<Player> bracket) {
        return new Search(bracket).run();
    }

    private final class Search {

        private final List<Player> bracket;
        private final List<Player> g1 = new ArrayList<>();
        private final List<Player> g2 = new ArrayList<>();
        private final Set<Player> smaller = identitySet();
        private final Set<Player> shifted = identitySet();
        private final Set<Player> staying = identitySet();

        Search(List<Player> bracket) {
            this.bracket = bracket.stream().sorted(BY_PAIRING_NUMBER).toList();
            if (this.bracket.stream().noneMatch(Player::hasPlayed)) {
                var half = this.bracket.size() / 2;
                g1.addAll(this.bracket.subList(0, half));
                g2.addAll(this.bracket.subList(half, this.bracket.size()));
            } else {
                this.bracket.forEach(player -> (player.preference().colour() == Colour.WHITE ? g1 : g2).add(player));
            }
        }

        Result run() {
            var smallerIsG1 = g1.size() <= g2.size();
            var smallerGroup = smallerIsG1 ? g1 : g2;
            var largerGroup = smallerIsG1 ? g2 : g1;
            smaller.addAll(smallerGroup);
            var fewest = cheapest(Set.of(), Set.of())
                    .orElseThrow(() -> new IllegalStateException("The bracket has no legal pairing: " + bracket));
            var together = unavoidablePairs(fewest);
            var smallerColour = smallerIsG1 ? Colour.WHITE : Colour.BLACK;
            var first = chooseShifted(ShifterOrder.of(smallerGroup, smallerColour), together, fewest);
            var equalising = (largerGroup.size() + together - (smallerGroup.size() - together)) / 2;
            var second = chooseShifted(ShifterOrder.of(largerGroup, smallerColour.opposite()), equalising, fewest);
            var newG1 = new ArrayList<Player>();
            var newG2 = new ArrayList<Player>();
            for (var player : bracket) {
                var inG1 = g1.contains(player) != (first.contains(player) || second.contains(player));
                (inG1 ? newG1 : newG2).add(player);
            }
            if (newG1.size() != newG2.size()) {
                throw new IllegalStateException("The subgroups of " + bracket + " were not equalised");
            }
            var pairs = transposition(
                    sortedS1(newG1), newG2.stream().sorted(BY_PAIRING_NUMBER).toList());
            var sameColourPairs = pairs.stream()
                    .mapToLong(pair -> RoundToPair.sameColourWanted(pair.first(), pair.second()))
                    .sum();
            var allShifted = new ArrayList<>(first);
            allShifted.addAll(second);
            return new Result(pairs, sameColourPairs, allShifted);
        }

        /** The fewest pairs inside the smaller subgroup of any legal pairing: they "must unavoidably be paired". */
        private int unavoidablePairs(BigInteger fewest) {
            return fewest.divide(BigInteger.valueOf(bracket.size() + 1L)).intValueExact();
        }

        /** 4.1.3 with the sequence numbers of 4.3: the first set of this size that keeps the fewest reachable. */
        private List<Player> chooseShifted(List<Player> pool, int size, BigInteger fewest) {
            var chosen = OrderedSetSearch.firstReachable(
                            List.of(new OrderedSetSearch.Level<>(pool, size)),
                            (shifting, notShifting) -> cheapest(shifting, notShifting)
                                    .filter(fewest::equals)
                                    .isPresent())
                    .orElseThrow(() -> new IllegalStateException("No set of shifters reaches the fewest"));
            pool.forEach(player -> (chosen.contains(player) ? shifted : staying).add(player));
            return chosen;
        }

        /**
         * Same-subgroup pairs, the smaller subgroup's weighing most. A consistent pairing has exactly one shifted
         * player in each same-subgroup pair and none in a pair across the subgroups.
         */
        private Optional<BigInteger> cheapest(Set<Player> shifting, Set<Player> notShifting) {
            var base = BigInteger.valueOf(bracket.size() + 1L);
            return CheapestPerfectMatching.of(bracket, (a, b) -> {
                        if (!round.mayMeet(a, b)) {
                            return Optional.empty();
                        }
                        var shiftA = shiftOf(a, shifting, notShifting);
                        var shiftB = shiftOf(b, shifting, notShifting);
                        if (g1.contains(a) == g1.contains(b)) {
                            if (shiftA.isPresent() && shiftA.equals(shiftB)) {
                                return Optional.empty();
                            }
                            return Optional.of(smaller.contains(a) ? base : BigInteger.ONE);
                        }
                        if (shiftA.orElse(false) || shiftB.orElse(false)) {
                            return Optional.empty();
                        }
                        return Optional.of(BigInteger.ZERO);
                    })
                    .map(CheapestPerfectMatching.Result::cost);
        }

        /** True when the player is shifted, false when it stays, empty while that is still open. */
        private Optional<Boolean> shiftOf(Player player, Set<Player> shifting, Set<Player> notShifting) {
            if (shifted.contains(player) || shifting.contains(player)) {
                return Optional.of(true);
            }
            if (staying.contains(player) || notShifting.contains(player)) {
                return Optional.of(false);
            }
            return Optional.empty();
        }

        /** 3.2.5: ascending ARO, then ascending Pairing Number. */
        private List<Player> sortedS1(List<Player> players) {
            return players.stream()
                    .sorted(Comparator.comparingInt(Player::aro).thenComparing(BY_PAIRING_NUMBER))
                    .toList();
        }

        /** 3.2.6 with 4.4: the first transposition of G2, in lexicographic order, that yields a legal pairing. */
        private List<Pair> transposition(List<Player> s1, List<Player> g2Sorted) {
            var pairs = new ArrayList<Pair>();
            var left = new ArrayList<>(g2Sorted);
            for (var position = 0; position < s1.size(); position++) {
                var top = s1.get(position);
                var rest = s1.subList(position + 1, s1.size());
                var opponent = left.stream()
                        .filter(candidate -> round.mayMeet(top, candidate))
                        .filter(candidate -> completes(rest, without(left, candidate)))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No legal transposition in " + bracket));
                left.remove(opponent);
                pairs.add(Pair.of(top, opponent));
            }
            return pairs;
        }

        private boolean completes(List<Player> s1Rest, List<Player> g2Rest) {
            var s1Set = identitySet();
            s1Set.addAll(s1Rest);
            var players = new ArrayList<>(s1Rest);
            players.addAll(g2Rest);
            return CheapestPerfectMatching.canPairAll(
                    players, (a, b) -> s1Set.contains(a) != s1Set.contains(b) && round.mayMeet(a, b));
        }
    }

    private static List<Player> without(List<Player> players, Player removed) {
        return players.stream().filter(player -> player != removed).toList();
    }

    private static Set<Player> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }
}
