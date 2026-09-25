package io.github.markdechamps.fideswiss.rules;

import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.Comparator;
import java.util.function.Function;

/**
 * GHR 3.6, the recommended board order: the higher score of the pair's higher-ranked participant first, then the
 * higher sum of the two scores, then the smaller Pairing Number of the higher-ranked participant.
 */
public final class BoardOrder {

    private BoardOrder() {}

    public static <T> Comparator<T> of(
            Function<T, Score> higherRankedScore,
            Function<T, Score> lowerRankedScore,
            Function<T, PairingNumber> higherRankedNumber) {
        Comparator<T> byHigherScore = Comparator.comparing(higherRankedScore);
        Comparator<T> bySum = Comparator.comparing(board -> higherRankedScore
                .apply(board)
                .plus(lowerRankedScore.apply(board).points()));
        return byHigherScore.reversed().thenComparing(bySum.reversed()).thenComparing(higherRankedNumber);
    }
}
