package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The Olympiad Pairing Rules for one round: the bye (Article 4); then the groups (6.3) in the order of 6.4, from the
 * top group down to the Median Group, from the bottom group up to it, and the Median Group last, each passing its
 * floaters on towards the Median Group (Article 8); then board 1's colours (Article 7), in the order of 11.1.
 */
final class OlympiadProcedure {

    /** One group as it was paired, for the trace. */
    record GroupStep(
            Points matchPoints,
            Direction direction,
            List<Team> residents,
            List<Team> incoming,
            GroupPairing.Result result) {}

    record Outcome(List<Game> games, Optional<Team> bye, List<GroupStep> groups) {}

    private final ColourAllocation colours;
    private final Matchings matchings;

    OlympiadProcedure(InitialColour lot, int round, Matchings matchings) {
        this.colours = new ColourAllocation(lot, round);
        this.matchings = matchings;
    }

    Outcome pair(List<Team> teams) {
        var bye = ByeAssignment.choose(teams, matchings);
        var toPair = teams.stream()
                .filter(team -> bye.filter(team::equals).isEmpty())
                .toList();
        if (toPair.isEmpty()) {
            return new Outcome(List.of(), bye, List.of());
        }
        var median = medianGroup(toPair);
        var groups =
                toPair.stream().collect(Collectors.groupingBy(Team::matchPoints, TreeMap::new, Collectors.toList()));
        var remainder = new ArrayList<>(toPair);
        var steps = new ArrayList<GroupStep>();
        var downfloaters =
                pairSide(higherGroups(groups.keySet(), median), Direction.DOWNWARD, groups, median, remainder, steps);
        var upfloaters =
                pairSide(lowerGroups(groups.keySet(), median), Direction.UPWARD, groups, median, remainder, steps);
        var incoming = new ArrayList<>(downfloaters);
        incoming.addAll(upfloaters);
        var residents = groups.get(median);
        var result = new GroupPairing(Direction.DOWNWARD, remainder, matchings).pair(residents, incoming, List.of());
        if (!result.floaters().isEmpty()) {
            throw new IllegalStateException("The Median Group left floaters " + result.floaters());
        }
        steps.add(new GroupStep(median, Direction.DOWNWARD, residents, incoming, result));
        var games = steps.stream()
                .flatMap(step -> step.result().pairs().stream())
                .map(colours::allocate)
                .sorted(PublicationOrder.ORDER)
                .toList();
        return new Outcome(games, bye, steps);
    }

    /** 6.4: the group of the median team, the lower ranked of the two middle teams when their number is even. */
    static Points medianGroup(List<Team> toPair) {
        var ranked = toPair.stream().sorted(Team.RANKING).toList();
        return ranked.get(ranked.size() / 2).matchPoints();
    }

    private static List<Points> higherGroups(Set<Points> matchPoints, Points median) {
        return matchPoints.stream()
                .filter(points -> points.isGreaterThan(median))
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private static List<Points> lowerGroups(Set<Points> matchPoints, Points median) {
        return matchPoints.stream()
                .filter(points -> points.isLessThan(median))
                .sorted()
                .toList();
    }

    /** Pairs one side of the Median Group in the order of 6.4, and returns the floaters it hands to it. */
    private List<Team> pairSide(
            List<Points> order,
            Direction direction,
            Map<Points, List<Team>> groups,
            Points median,
            List<Team> remainder,
            List<GroupStep> steps) {
        var incoming = List.<Team>of();
        for (var index = 0; index < order.size(); index++) {
            var residents = groups.get(order.get(index));
            var adjacent = groups.get(index + 1 < order.size() ? order.get(index + 1) : median);
            var result = new GroupPairing(direction, remainder, matchings).pair(residents, incoming, adjacent);
            steps.add(new GroupStep(order.get(index), direction, residents, incoming, result));
            incoming = result.floaters().stream().sorted(direction.priority()).toList();
        }
        return incoming;
    }
}
