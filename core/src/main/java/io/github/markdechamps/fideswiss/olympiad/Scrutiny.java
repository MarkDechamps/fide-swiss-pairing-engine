package io.github.markdechamps.fideswiss.olympiad;

import java.util.ArrayList;
import java.util.List;

/**
 * 9.1–9.3: the teams of a group that stay in it, all of which can be paired. Arranged in the direction's priority
 * (9.2), the first takes the first opponent in the order of 9.3 with which the rest of the subgroup can still be
 * paired ("or the subgroup is unsolvable"), and the subgroup left is paired the same way.
 */
final class Scrutiny {

    private final Direction direction;
    private final ColourLimits limits;
    private final Matchings matchings;

    Scrutiny(Direction direction, ColourLimits limits, Matchings matchings) {
        this.direction = direction;
        this.limits = limits;
        this.matchings = matchings;
    }

    List<Pair> pair(List<Team> teams) {
        var remaining = new ArrayList<>(teams);
        var pairs = new ArrayList<Pair>();
        while (!remaining.isEmpty()) {
            var arranged = remaining.stream().sorted(direction.priority()).toList();
            var first = arranged.getFirst();
            var opponent = ExchangeOrder.forFirstOf(arranged).stream()
                    .filter(candidate -> limits.compatible(first, candidate))
                    .filter(candidate -> matchings.canPairAll(without(remaining, first, candidate), limits::compatible))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("The group cannot be paired: " + remaining));
            pairs.add(new Pair(first, opponent));
            remaining.remove(first);
            remaining.remove(opponent);
        }
        return pairs;
    }

    private static List<Team> without(List<Team> teams, Team one, Team other) {
        var rest = new ArrayList<>(teams);
        rest.remove(one);
        rest.remove(other);
        return rest;
    }
}
