package io.github.markdechamps.fideswiss.tiebreak;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** The FIDE Rating Regulations' conversion tables (B.02 8.1.1 and 8.1.2), which the performance tie-breaks use. */
final class RatingTables {

    /** 8.1.1: dp for p = .50, .51, … 1.00; p = 1.00 is the notional 800. */
    private static final int[] RATING_DIFFERENCE = {
        0, 7, 14, 21, 29, 36, 43, 50, 57, 65, 72, 80, 87, 95, 102, 110, 117, 125, 133, 141, 149, 158, 166, 175, 184,
        193, 202, 211, 220, 230, 240, 251, 262, 273, 284, 296, 309, 322, 336, 351, 366, 383, 401, 422, 444, 470, 501,
        538, 589, 677, 800
    };

    /** 8.1.2: the highest difference D for which the higher-rated scores PD = .50, .51, … .99; above 735, 1.00. */
    private static final int[] UPPER_DIFFERENCE = {
        3, 10, 17, 25, 32, 39, 46, 53, 61, 68, 76, 83, 91, 98, 106, 113, 121, 129, 137, 145, 153, 162, 170, 179, 188,
        197, 206, 215, 225, 235, 245, 256, 267, 278, 290, 302, 315, 328, 344, 357, 374, 391, 411, 432, 456, 484, 517,
        559, 619, 735
    };

    private RatingTables() {}

    /** dp for a fractional score, which is rounded half up to two decimals first (Tie-break rulings #5). */
    static int ratingDifference(BigDecimal fractionalScore) {
        var hundredths = fractionalScore
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .intValueExact();
        return hundredths >= 50 ? RATING_DIFFERENCE[hundredths - 50] : -RATING_DIFFERENCE[50 - hundredths];
    }

    /** PD, the expected score of the player whose rating is higher by {@code difference} (negative when lower). */
    static BigDecimal expectedScore(int difference) {
        var magnitude = Math.abs(difference);
        var hundredths = 100;
        for (var index = 0; index < UPPER_DIFFERENCE.length; index++) {
            if (magnitude <= UPPER_DIFFERENCE[index]) {
                hundredths = 50 + index;
                break;
            }
        }
        var expected = BigDecimal.valueOf(hundredths, 2);
        return difference >= 0 ? expected : BigDecimal.ONE.subtract(expected);
    }
}
