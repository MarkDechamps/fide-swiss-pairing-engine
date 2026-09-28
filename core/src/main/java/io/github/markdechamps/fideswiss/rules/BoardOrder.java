package io.github.markdechamps.fideswiss.rules;

import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.function.Function;

/**
 * GHR 3.6, the recommended board order: the higher score of the pair's higher-ranked participant first, then the
 * higher sum of the two scores, then the smaller Pairing Number of the higher-ranked participant. The 2026 rules
 * sort on the Pairing Score (C.04.7 1.5).
 */
public final class BoardOrder {

    private BoardOrder() {}

    public static <T> Comparator<T> of(
            Function<T, PairingScore> higherRankedScore,
            Function<T, PairingScore> lowerRankedScore,
            Function<T, PairingNumber> higherRankedNumber) {
        Comparator<T> byHigherScore = Comparator.comparing(higherRankedScore);
        Comparator<T> bySum =
                Comparator.comparing(board -> sum(higherRankedScore.apply(board), lowerRankedScore.apply(board)));
        return byHigherScore.reversed().thenComparing(bySum.reversed()).thenComparing(higherRankedNumber);
    }

    private static BigDecimal sum(PairingScore one, PairingScore other) {
        return one.points().plus(other.points()).toBigDecimal();
    }
}
