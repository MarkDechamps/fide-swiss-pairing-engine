package io.github.markdechamps.fideswiss.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Article 4: with an odd number of teams to pair, the bye goes to the eligible team (4.2) with the lowest ranking in
 * the order of 3.2. We read "eligible" to include that the bye leaves the others pairable, as the Swiss Team and
 * Double-Swiss Systems say in 3.4.1.
 */
final class ByeAssignment {

    private ByeAssignment() {}

    static Optional<Team> choose(List<Team> teams, Matchings matchings) {
        if (teams.size() % 2 == 0) {
            if (!matchings.canPairAll(teams, Team::mayMeet)) {
                throw new UnpairableRound("D.02 6.1", "The teams cannot all be paired without a rematch");
            }
            return Optional.empty();
        }
        return Optional.of(teams.stream()
                .filter(Team::mayReceiveBye)
                .sorted(Team.RANKING.reversed())
                .filter(bye -> matchings.canPairAll(without(teams, bye), Team::mayMeet))
                .findFirst()
                .orElseThrow(() -> new UnpairableRound(
                        "D.02 4.1, 4.2", "No eligible team's bye leaves the other teams pairable")));
    }

    private static List<Team> without(List<Team> teams, Team bye) {
        var rest = new ArrayList<>(teams);
        rest.remove(bye);
        return rest;
    }
}
