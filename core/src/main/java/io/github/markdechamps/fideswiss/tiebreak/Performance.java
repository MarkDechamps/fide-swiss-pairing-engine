package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * A participant's performance over the games it played over the board, scored 1 / ½ / 0 whatever the Scoring
 * Scheme, since the B.02 tables assume that scale (Tie-break interpretation rulings #5, #6).
 */
final class Performance {

    private static final BigDecimal HALF = new BigDecimal("0.5");

    private Performance() {}

    /** 10.1: the opponents' average rating, rounded half up. */
    static Optional<BigDecimal> averageRatingOfOpponents(ParticipantId participant, TieBreakContext context) {
        var ratings = opponentRatings(participant, context);
        if (ratings.isEmpty()) {
            return Optional.empty();
        }
        var sum = ratings.stream().mapToInt(Integer::intValue).sum();
        return Optional.of(BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(ratings.size()), 0, RoundingMode.HALF_UP));
    }

    /** 10.2: the rounded ARO plus dp for the fractional score. */
    static Optional<BigDecimal> tournamentPerformanceRating(ParticipantId participant, TieBreakContext context) {
        var games = context.of(participant).games();
        return averageRatingOfOpponents(participant, context).map(aro -> {
            var fraction = standardPoints(games).divide(BigDecimal.valueOf(games.size()), 10, RoundingMode.HALF_UP);
            return aro.add(BigDecimal.valueOf(RatingTables.ratingDifference(fraction)));
        });
    }

    /**
     * 10.3: the lowest whole rating R whose expected score over the opponents reaches the score, on the full 8.1.2
     * scale; a zero score gives the lowest-rated opponent minus 800.
     */
    static Optional<BigDecimal> perfectTournamentPerformance(ParticipantId participant, TieBreakContext context) {
        var ratings = opponentRatings(participant, context);
        if (ratings.isEmpty()) {
            return Optional.empty();
        }
        var score = standardPoints(context.of(participant).games());
        var lowest = ratings.stream().mapToInt(Integer::intValue).min().orElseThrow();
        if (score.signum() == 0) {
            return Optional.of(BigDecimal.valueOf(lowest - 800L));
        }
        var highest = ratings.stream().mapToInt(Integer::intValue).max().orElseThrow();
        for (var rating = lowest - 800; rating <= highest + 800; rating++) {
            final var candidate = rating;
            var expected = ratings.stream()
                    .map(opponent -> RatingTables.expectedScore(candidate - opponent))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (expected.compareTo(score) >= 0) {
                return Optional.of(BigDecimal.valueOf(rating));
            }
        }
        return Optional.of(BigDecimal.valueOf(highest + 800L));
    }

    static List<ParticipantId> opponentsOverTheBoard(ParticipantId participant, TieBreakContext context) {
        return context.of(participant).games().stream()
                .map(game -> game.opponent().orElseThrow())
                .toList();
    }

    private static List<Integer> opponentRatings(ParticipantId participant, TieBreakContext context) {
        return opponentsOverTheBoard(participant, context).stream()
                .map(opponent -> context.of(opponent).participant().rating().valueOrZero())
                .toList();
    }

    private static BigDecimal standardPoints(List<RoundEntry> games) {
        return games.stream()
                .map(game -> switch (game.result().orElseThrow()) {
                    case WIN -> BigDecimal.ONE;
                    case DRAW -> HALF;
                    case LOSS -> BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
