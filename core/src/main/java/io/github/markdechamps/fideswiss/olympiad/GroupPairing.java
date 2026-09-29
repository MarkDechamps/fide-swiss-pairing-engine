package io.github.markdechamps.fideswiss.olympiad;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Pairs one group (6.3): its residents plus the floaters moved into it.
 *
 * <ol>
 *   <li>7.4: the group is paired inside the 7.3 limits unless they would make it keep fewer pairings.
 *   <li>8.2.1, 8.3.1: each incoming floater is paired first, with the first team of the group, in the direction's
 *       floater-opponent order, that it may meet; a floater no team can take moves on (8.2.4, 8.3.4).
 *   <li>8.2.2, 8.3.2, 9.4, 9.5: the group keeps as many pairings as it can; the teams left over float, each the first
 *       in the order of 8.2.2/8.3.2 whose move still reaches that number, a team with an opponent in the adjacent
 *       group first (8.2.3, 8.3.3).
 *   <li>9.1–9.3: the rest are paired in the order of 9.3.
 * </ol>
 *
 * "Still reaches" is always asked of the whole round ({@link KeptPairings}), so a step never leaves the round
 * unpairable and no floater has to be chosen again (8.4).
 */
final class GroupPairing {

    /**
     * @param floaters the teams the group passes on, towards the Median Group
     * @param passedOn how many of them were incoming floaters no team of the group could take (8.2.4, 8.3.4)
     */
    record Result(List<Pair> pairs, List<Team> floaters, int passedOn, ColourLimits limits) {}

    private final Direction direction;
    private final List<Team> remainder;
    private final Matchings matchings;

    /** @param remainder every team of the round not yet paired, this group included; paired teams are removed */
    GroupPairing(Direction direction, List<Team> remainder, Matchings matchings) {
        this.direction = direction;
        this.remainder = remainder;
        this.matchings = matchings;
    }

    /**
     * @param incoming the floaters moved into the group, in the order they are paired
     * @param adjacent the residents of the group its floaters would move to; empty for the Median Group
     */
    Result pair(List<Team> residents, List<Team> incoming, List<Team> adjacent) {
        var members = new ArrayList<>(incoming);
        members.addAll(residents);
        var limits = ColourLimits.OBSERVED;
        var kept = keptPairings(members, limits);
        var withoutLimits = keptPairings(members, ColourLimits.DISREGARDED);
        if (withoutLimits.target() > kept.target()) {
            limits = ColourLimits.DISREGARDED;
            kept = withoutLimits;
        }
        var staying = new ArrayList<>(members);
        var pairs = new ArrayList<Pair>();
        var floaters = new ArrayList<Team>();
        for (var floater : incoming) {
            if (!staying.contains(floater)) {
                continue; // already taken by an earlier floater
            }
            var opponent = opponentOf(floater, residents, incoming, staying, kept);
            if (opponent.isPresent()) {
                kept.fixPair(floater, opponent.get());
                pairs.add(new Pair(floater, opponent.get()));
                staying.remove(floater);
                staying.remove(opponent.get());
            } else {
                kept.forceOut(floater);
                floaters.add(floater);
                staying.remove(floater);
            }
        }
        var passedOn = floaters.size();
        chooseFloaters(staying, adjacent, kept, floaters);
        staying.removeAll(floaters);
        pairs.addAll(new Scrutiny(direction, limits, matchings).pair(staying));
        pairs.forEach(pair -> {
            remainder.remove(pair.first());
            remainder.remove(pair.second());
        });
        return new Result(pairs, floaters, passedOn, limits);
    }

    private KeptPairings keptPairings(List<Team> members, ColourLimits limits) {
        return KeptPairings.of(matchings, remainder, members, limits)
                .orElseThrow(() -> new IllegalStateException("The teams not yet paired cannot all be paired"));
    }

    /**
     * 8.2.1: an upfloater takes the lowest ranked team it has not played, 8.3.1: a downfloater the highest ranked.
     * Residents come first, then other floaters (6.2); the pairing must still reach the target.
     */
    private Optional<Team> opponentOf(
            Team floater, List<Team> residents, List<Team> incoming, List<Team> staying, KeptPairings kept) {
        var fromAbove = floater.matchPoints().isGreaterThan(residents.getFirst().matchPoints());
        var order = fromAbove ? Team.RANKING : Team.RANKING.reversed();
        var candidates = new ArrayList<>(
                residents.stream().filter(staying::contains).sorted(order).toList());
        candidates.addAll(incoming.stream()
                .filter(staying::contains)
                .filter(team -> team != floater)
                .sorted(order)
                .toList());
        return candidates.stream()
                .filter(candidate -> kept.allowsPair(floater, candidate))
                .findFirst();
    }

    private void chooseFloaters(List<Team> staying, List<Team> adjacent, KeptPairings kept, List<Team> floaters) {
        var needed = staying.size() - 2 * kept.target();
        var preference = Comparator.<Team, Boolean>comparing(team -> !hasOpponentIn(team, adjacent))
                .thenComparing(direction.floaterOrder());
        for (var chosen = 0; chosen < needed; chosen++) {
            var floater = staying.stream()
                    .filter(team -> !floaters.contains(team))
                    .sorted(preference)
                    .filter(kept::allowsOut)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No floater keeps the group's pairings"));
            kept.forceOut(floater);
            floaters.add(floater);
        }
    }

    /** 8.2.3, 8.3.3: the team has not already played every team of the adjacent group. */
    private static boolean hasOpponentIn(Team team, List<Team> adjacent) {
        return adjacent.stream().anyMatch(team::mayMeet);
    }
}
