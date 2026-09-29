package io.github.markdechamps.fideswiss.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 9.5 and 8.4 as one question over the whole round. Of the ways to pair every team not yet paired (6.1 only), take
 * those that keep the most pairings inside the group being paired (at its level of 7.3/7.4): that number is the
 * {@linkplain #target() target}. A step of the group's pairing is allowed when the target can still be reached
 * after it, so the group floats no team it could keep (9.5, 9.4) and no choice leaves the round unpairable (8.2.2,
 * 8.3.2, 8.4). One perfect matching that keeps the most pairings answers it. A known solution is reused while the
 * step it is asked about fits it.
 */
final class KeptPairings {

    private final Matchings matchings;
    private final List<Team> remainder;
    private final Set<Team> group;
    private final ColourLimits limits;
    private int target;
    private Map<Team, Team> partner;

    private KeptPairings(
            Matchings matchings, List<Team> remainder, Set<Team> group, ColourLimits limits, Solution solution) {
        this.matchings = matchings;
        this.remainder = remainder;
        this.group = group;
        this.limits = limits;
        this.target = solution.kept();
        this.partner = solution.partner();
    }

    /** Empty when the teams not yet paired cannot all be paired. */
    static Optional<KeptPairings> of(
            Matchings matchings, Collection<Team> remainder, Collection<Team> members, ColourLimits limits) {
        var rest = new ArrayList<>(remainder);
        var group = new HashSet<>(members);
        return solve(matchings, rest, group, limits)
                .map(solution -> new KeptPairings(matchings, rest, group, limits, solution));
    }

    /** The pairings the group can still keep. */
    int target() {
        return target;
    }

    /** Whether pairing {@code one} with {@code other} inside the group still reaches the target. */
    boolean allowsPair(Team one, Team other) {
        if (!kept(one, other)) {
            return false;
        }
        if (other == partner.get(one)) {
            return true;
        }
        var solution = solve(matchings, without(remainder, one, other), without(group, one, other), limits);
        if (solution.isEmpty() || solution.get().kept() != target - 1) {
            return false;
        }
        partner = new HashMap<>(solution.get().partner());
        partner.put(one, other);
        partner.put(other, one);
        return true;
    }

    void fixPair(Team one, Team other) {
        remainder.remove(one);
        remainder.remove(other);
        group.remove(one);
        group.remove(other);
        target--;
    }

    /** Whether the group still reaches its target when {@code team} floats out of it. */
    boolean allowsOut(Team team) {
        if (!kept(team, partner.get(team))) {
            return true;
        }
        var rest = new HashSet<>(group);
        rest.remove(team);
        var solution = solve(matchings, remainder, rest, limits);
        if (solution.isEmpty() || solution.get().kept() != target) {
            return false;
        }
        partner = solution.get().partner();
        return true;
    }

    void forceOut(Team team) {
        group.remove(team);
    }

    private boolean kept(Team one, Team other) {
        return inGroup(group, limits, one, other);
    }

    private static boolean inGroup(Set<Team> group, ColourLimits limits, Team one, Team other) {
        return group.contains(one) && group.contains(other) && limits.compatible(one, other);
    }

    private record Solution(int kept, Map<Team, Team> partner) {}

    private static Optional<Solution> solve(
            Matchings matchings, List<Team> teams, Set<Team> group, ColourLimits limits) {
        return matchings
                .mostKept(teams, Team::mayMeet, (one, other) -> inGroup(group, limits, one, other))
                .map(partner -> {
                    var kept = (int) partner.entrySet().stream()
                                    .filter(entry -> inGroup(group, limits, entry.getKey(), entry.getValue()))
                                    .count()
                            / 2;
                    return new Solution(kept, partner);
                });
    }

    private static List<Team> without(List<Team> teams, Team one, Team other) {
        var rest = new ArrayList<>(teams);
        rest.remove(one);
        rest.remove(other);
        return rest;
    }

    private static Set<Team> without(Set<Team> teams, Team one, Team other) {
        var rest = new HashSet<>(teams);
        rest.remove(one);
        rest.remove(other);
        return rest;
    }
}
