package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.matching.CheapestPerfectMatching;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
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
import java.util.stream.Stream;

/**
 * 3.2 and Article 4: the pairing of one bracket.
 *
 * <p>One cheapest perfect matching covers the bracket and every lower player. Its cost holds the quality criteria
 * of 2.3 as digits, most significant first, and the order of 4.3 as digits below them, so the matching is the first
 * pairing, in the order of 4.3, that complies best with [C5]–[C8] (3.2.2). [C1]–[C3] forbid a pair; [C4] is the
 * matching being perfect.
 */
final class BracketPairing {

    /** The pairs made in the bracket and its outgoing floaters. */
    record Result(List<Pair> pairs, List<Player> outgoing) {}

    private static final Comparator<PairingScore> HIGHEST_FIRST = Comparator.reverseOrder();

    private final RoundToPair round;

    BracketPairing(RoundToPair round) {
        this.round = round;
    }

    /**
     * @param bracket ranked by 1.8, so a position plus one is the BSN (4.1)
     * @param lower every unpaired player below the bracket
     */
    Result pair(List<Player> bracket, List<Player> lower) {
        var criteria = new Criteria(bracket, lower);
        var everyone = Stream.concat(bracket.stream(), lower.stream()).toList();
        var best = CheapestPerfectMatching.of(everyone, criteria::costThenOrder)
                .orElseThrow(() -> new NoRoundPairingException("C.04.4.2 1.9.3", "no complete round-pairing"));
        var partner = new IdentityHashMap<Player, Player>();
        best.pairs().forEach(pair -> {
            partner.put(pair.getFirst(), pair.getLast());
            partner.put(pair.getLast(), pair.getFirst());
        });
        var pairs = new ArrayList<Pair>();
        var outgoing = new ArrayList<Player>();
        for (var player : bracket) {
            var opponent = partner.get(player);
            if (!criteria.inBracket(opponent)) {
                outgoing.add(player);
            } else if (player.ranksAbove(opponent)) {
                pairs.add(Pair.of(player, opponent));
            }
        }
        return new Result(List.copyOf(pairs), List.copyOf(outgoing));
    }

    /**
     * [C5]–[C8] as digits of one number, most significant first:
     *
     * <ul>
     *   <li>[C5] one per outgoing floater, a bracket player paired below the bracket;
     *   <li>[C6] one per outgoing floater at its score, the highest score most significant;
     *   <li>[C7] the following bracket's [C5] and [C6]: one per player of the following bracket (an outgoing
     *       floater of this one, or a resident of the next scoregroup) paired below the next scoregroup, at its
     *       score;
     *   <li>[C8] one per pair of the bracket whose players want the same colour.
     * </ul>
     */
    private final class Criteria {

        private final Set<Player> bracket = identitySet();
        private final Set<Player> nextScoregroup = identitySet();
        private final Set<Player> further = identitySet();
        private final Map<Player, Integer> bsn = new IdentityHashMap<>();
        private final Map<PairingScore, Integer> floaterPositions = new HashMap<>();
        private final Map<PairingScore, Integer> onwardFloaterPositions = new HashMap<>();
        private final int onwardFloatersPosition;
        private final int sameColourPosition;
        private final LexicographicWeights weights;
        private final LexicographicWeights order;
        private final BigInteger orderCeiling;

        Criteria(List<Player> bracket, List<Player> lower) {
            this.bracket.addAll(bracket);
            var nextScore = lower.stream().map(Player::pairingScore).max(Comparator.naturalOrder());
            lower.forEach(
                    player -> (nextScore.filter(player.pairingScore()::equals).isPresent() ? nextScoregroup : further)
                            .add(player));
            var position = 1;
            for (var score : scoresOf(bracket.stream())) {
                floaterPositions.put(score, position++);
            }
            onwardFloatersPosition = position++;
            for (var score : scoresOf(Stream.concat(bracket.stream(), nextScoregroup.stream()))) {
                onwardFloaterPositions.put(score, position++);
            }
            sameColourPosition = position++;
            weights = new LexicographicWeights(bracket.size() + lower.size() + 1, position);
            for (var index = 0; index < bracket.size(); index++) {
                bsn.put(bracket.get(index), index + 1);
            }
            order = new LexicographicWeights(bracket.size() + 2, bracket.size());
            orderCeiling = BigInteger.valueOf(bracket.size() + 2L).pow(bracket.size());
        }

        boolean inBracket(Player player) {
            return bracket.contains(player);
        }

        /** The criteria, shifted above the digits of 4.3. */
        Optional<BigInteger> costThenOrder(Player a, Player b) {
            return cost(a, b).map(cost -> cost.multiply(orderCeiling).add(precedence(a, b)));
        }

        private Optional<BigInteger> cost(Player a, Player b) {
            if (!round.mayMeet(a, b)) {
                return Optional.empty();
            }
            if (inBracket(a) && inBracket(b)) {
                return Optional.of(weights.of(sameColourPosition, RoundToPair.sameColourWanted(a, b)));
            }
            if (inBracket(a) || inBracket(b)) {
                var floater = inBracket(a) ? a : b;
                var opponent = inBracket(a) ? b : a;
                var onward = further.contains(opponent) ? floatsOnward(floater) : BigInteger.ZERO;
                return Optional.of(floats(floater).add(onward));
            }
            if (nextScoregroup.contains(a) && further.contains(b)) {
                return Optional.of(floatsOnward(a));
            }
            if (nextScoregroup.contains(b) && further.contains(a)) {
                return Optional.of(floatsOnward(b));
            }
            return Optional.of(BigInteger.ZERO);
        }

        /** [C5] and [C6]. */
        private BigInteger floats(Player floater) {
            return weights.of(0, 1).add(weights.of(floaterPositions.get(floater.pairingScore()), 1));
        }

        /** [C7]: an outgoing floater of the following bracket. */
        private BigInteger floatsOnward(Player floater) {
            return weights.of(onwardFloatersPosition, 1)
                    .add(weights.of(onwardFloaterPositions.get(floater.pairingScore()), 1));
        }

        /**
         * 4.2–4.3 as digits: one per BSN, BSN #1 most significant, holding how far its opponent is from the last BSN;
         * a virtual player (BSN 0, the player floats) is farthest. A smaller sum is a pairing that comes first.
         */
        private BigInteger precedence(Player a, Player b) {
            var size = bracket.size();
            var first = bsn.getOrDefault(a, 0);
            var second = bsn.getOrDefault(b, 0);
            var digits = BigInteger.ZERO;
            if (first > 0) {
                digits = digits.add(order.of(first - 1, size + 1L - second));
            }
            if (second > 0) {
                digits = digits.add(order.of(second - 1, size + 1L - first));
            }
            return digits;
        }
    }

    private static List<PairingScore> scoresOf(Stream<Player> players) {
        return players.map(Player::pairingScore)
                .distinct()
                .sorted(HIGHEST_FIRST)
                .toList();
    }

    private static Set<Player> identitySet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }
}
