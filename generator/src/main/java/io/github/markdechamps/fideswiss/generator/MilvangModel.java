package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import java.util.random.RandomGenerator;

/**
 * Otto Milvang's result model (Probability for the outcome of a chess game based on rating, 2015), which C.02.03
 * §7.2.4 asks generated results to follow roughly. It includes White's advantage.
 */
public final class MilvangModel implements ResultModel {

    private static final double WHITE_CENTRE = 40;
    private static final double BLACK_CENTRE = -80;

    public MilvangModel() {}

    @Override
    public GameOutcome outcome(int whiteStrength, int blackStrength, RandomGenerator random) {
        return probabilities(whiteStrength, blackStrength).drawn(random);
    }

    Probabilities probabilities(int whiteStrength, int blackStrength) {
        var mean = (whiteStrength + blackStrength) / 2.0;
        var difference = (double) whiteStrength - blackStrength;
        var q = mean > 1200 ? (mean - 1200) / 1200 : 0;
        var whiteWins = whiteWins(difference, mean, 0.45 - 0.10 * q * q);
        var blackWins = blackWins(difference, mean, 0.46 - 0.13 * q * q);
        return new Probabilities(whiteWins, Math.max(0, 1 - whiteWins - blackWins), blackWins);
    }

    private static double whiteWins(double difference, double mean, double centreValue) {
        var lower = -1492 + 0.391 * mean;
        var upper = 1691 - 0.428 * mean;
        if (difference <= lower) {
            return 0;
        }
        if (difference <= WHITE_CENTRE) {
            return centreValue * square((difference - lower) / (WHITE_CENTRE - lower));
        }
        if (difference <= upper) {
            return 1 - (1 - centreValue) * square((difference - upper) / (WHITE_CENTRE - upper));
        }
        return 1;
    }

    private static double blackWins(double difference, double mean, double centreValue) {
        var lower = -1753 + 0.416 * mean;
        var upper = 1428 - 0.388 * mean;
        if (difference <= lower) {
            return 1;
        }
        if (difference <= BLACK_CENTRE) {
            return 1 - (1 - centreValue) * square((difference - lower) / (BLACK_CENTRE - lower));
        }
        if (difference <= upper) {
            return centreValue * square((difference - upper) / (BLACK_CENTRE - upper));
        }
        return 0;
    }

    private static double square(double value) {
        return value * value;
    }
}
