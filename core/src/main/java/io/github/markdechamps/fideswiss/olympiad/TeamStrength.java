package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.RankingKey;
import io.github.markdechamps.fideswiss.tournament.Rating;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * What 3.1 ranks an Olympiad team by: the ratings of its players. An unrated or missing player counts as 0.
 *
 * @param ratings the team's players' ratings, in any order
 */
public record TeamStrength(List<Rating> ratings) {

    private static final Comparator<TeamStrength> STRONGEST_FIRST = Comparator.comparingInt(
                    TeamStrength::sumOfTheFourHighest)
            .thenComparingInt(TeamStrength::fifthRating)
            .reversed();

    public TeamStrength {
        ratings = ratings.stream()
                .sorted(Comparator.comparingInt(Rating::valueOrZero).reversed())
                .toList();
    }

    public static TeamStrength of(Rating... ratings) {
        return new TeamStrength(List.of(ratings));
    }

    /** 3.1.1: the average rating of the four highest-rated players, rounded half up (11.1.3 publishes by it). */
    public Rating averageOfTheFourHighest() {
        return Rating.of((sumOfTheFourHighest() * 2 + 4) / 8);
    }

    /**
     * The initial ranking of 3.1 as a declared Ranking Key: by the exact average of the four highest ratings, then
     * the fifth player's rating (3.1.2), then alphabetically by name (3.1.3). A team without a strength comes last.
     */
    public static RankingKey initialRanking(List<Participant> teams, Map<ParticipantId, TeamStrength> strengths) {
        var none = new TeamStrength(List.of());
        var order = teams.stream()
                .sorted(Comparator.<Participant, TeamStrength>comparing(
                                team -> strengths.getOrDefault(team.id(), none), STRONGEST_FIRST)
                        .thenComparing(Participant::name))
                .map(Participant::id)
                .toList();
        return RankingKey.declared(order);
    }

    private int sumOfTheFourHighest() {
        return ratings.stream().limit(4).mapToInt(Rating::valueOrZero).sum();
    }

    private int fifthRating() {
        return ratings.size() < 5 ? 0 : ratings.get(4).valueOrZero();
    }
}
