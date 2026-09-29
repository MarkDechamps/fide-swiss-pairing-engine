package io.github.markdechamps.fideswiss.topscoregroup;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 3.6: the first pairing, in the order of the Pairing Identifiers (3.6.2, 3.6.3), that complies with [C1] and best
 * with the system's bracket criteria (3.6.4, ruling A7).
 *
 * <p>The pairings are generated literally in identifier order: first the top members (each contender, in TPN
 * order, is tried as a top member before a bottom member), then each top member's opponent in TPN order. The
 * target comes from one matching of the bracket; the same matching, restricted to a partial identifier, prunes
 * every branch that cannot reach it, so the first pairing reached is the answer.
 */
final class BracketPairing {

    private enum Role {
        TOP,
        BOTTOM
    }

    private final TopScoregroupRound round;

    BracketPairing(TopScoregroupRound round) {
        this.round = round;
    }

    List<ContenderPair> pair(List<Contender> residents, List<Contender> upfloaters) {
        return new Search(residents, upfloaters).run();
    }

    private final class Search {
        private final List<Contender> contenders;
        private final Set<Contender> upfloaters;
        private final LexicographicWeights weights;
        private final Map<Contender, Role> roles = new HashMap<>();
        private final Map<Contender, Contender> fixed = new HashMap<>();
        private final BigInteger target;
        private List<ContenderPair> knownOptimal;

        Search(List<Contender> residents, List<Contender> upfloaters) {
            this.contenders = new ArrayList<>(residents);
            this.contenders.addAll(upfloaters);
            this.contenders.sort(Comparator.comparingInt(Contender::tpn));
            this.upfloaters = Set.copyOf(upfloaters);
            this.weights = new LexicographicWeights(
                    contenders.size() + 1, Math.max(1, round.pairCriteria().size()));
            var best = CheapestPerfectMatching.of(contenders, this::cost)
                    .orElseThrow(() -> new IllegalStateException("The bracket has no legal pairing"));
            this.target = best.cost();
            this.knownOptimal = best.pairs();
        }

        List<ContenderPair> run() {
            if (!chooseRoles(0)) {
                throw new IllegalStateException("Target unreachable pairing the bracket " + contenders);
            }
            return contenders.stream()
                    .filter(contender -> roles.get(contender) == Role.TOP)
                    .map(top -> new ContenderPair(top, fixed.get(top)))
                    .toList();
        }

        /** The top members of the identifier: the smallest possible list, so a contender is a top member if it can be. */
        private boolean chooseRoles(int index) {
            if (index == contenders.size()) {
                var tops = contenders.stream()
                        .filter(contender -> roles.get(contender) == Role.TOP)
                        .toList();
                return chooseOpponents(tops, 0);
            }
            var contender = contenders.get(index);
            for (var role : Role.values()) {
                roles.put(contender, role);
                if (canReach() && chooseRoles(index + 1)) {
                    return true;
                }
            }
            roles.remove(contender);
            return false;
        }

        /** The bottom members, in the order of their top members: each the smallest TPN possible. */
        private boolean chooseOpponents(List<Contender> tops, int index) {
            if (index == tops.size()) {
                return true;
            }
            var top = tops.get(index);
            for (var bottom : contenders) {
                if (roles.get(bottom) != Role.BOTTOM || fixed.containsKey(bottom) || bottom.tpn() < top.tpn()) {
                    continue;
                }
                fixed.put(top, bottom);
                fixed.put(bottom, top);
                if (canReach() && chooseOpponents(tops, index + 1)) {
                    return true;
                }
                fixed.remove(top);
                fixed.remove(bottom);
            }
            return false;
        }

        private boolean canReach() {
            if (knownOptimal.stream().allMatch(this::fitsIdentifier)) {
                return true;
            }
            var reached = CheapestPerfectMatching.of(contenders, this::cost)
                    .filter(result -> result.cost().equals(target));
            reached.ifPresent(result -> knownOptimal = result.pairs());
            return reached.isPresent();
        }

        private Optional<BigInteger> cost(Contender a, Contender b) {
            var pair = ContenderPair.of(a, b);
            if (!a.mayMeet(b) || !fitsIdentifier(pair)) {
                return Optional.empty();
            }
            var total = BigInteger.ZERO;
            for (var position = 0; position < round.pairCriteria().size(); position++) {
                total = total.add(
                        weights.of(position, round.pairCriteria().get(position).failureOf(pair, upfloaters)));
            }
            return Optional.of(total);
        }

        private boolean fitsIdentifier(ContenderPair pair) {
            if (roles.get(pair.top()) == Role.BOTTOM || roles.get(pair.bottom()) == Role.TOP) {
                return false;
            }
            var partner = fixed.get(pair.top());
            return partner == null ? !fixed.containsKey(pair.bottom()) : partner.equals(pair.bottom());
        }
    }
}
