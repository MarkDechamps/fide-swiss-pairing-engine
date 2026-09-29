package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.Points;
import java.math.BigDecimal;
import java.util.Comparator;

/**
 * 1.8.1: the (Opposition Evaluation) Index, Buchholz then Sonneborn-Berger (1.7.1). Higher ranks higher.
 *
 * @param sonnebornBerger points earned times the opponent's score, summed, so not itself a number of points
 */
record OppositionIndex(Points buchholz, BigDecimal sonnebornBerger) {

    static final OppositionIndex NONE = new OppositionIndex(Points.ZERO, BigDecimal.ZERO);

    /** Highest Index first. */
    static final Comparator<OppositionIndex> HIGHEST_FIRST = Comparator.comparing(OppositionIndex::buchholz)
            .thenComparing(OppositionIndex::sonnebornBerger)
            .reversed();

    OppositionIndex plus(Points opponentScore, Points earned) {
        return new OppositionIndex(
                buchholz.plus(opponentScore),
                sonnebornBerger.add(earned.toBigDecimal().multiply(opponentScore.toBigDecimal())));
    }

    @Override
    public String toString() {
        return "BH " + buchholz + " SB " + sonnebornBerger.stripTrailingZeros().toPlainString();
    }
}
