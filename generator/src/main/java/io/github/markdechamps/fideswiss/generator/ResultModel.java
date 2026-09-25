package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import java.util.random.RandomGenerator;

/** The probability rule that decides a simulated game from the two players' strengths and colours. */
public interface ResultModel {

    GameOutcome outcome(int whiteStrength, int blackStrength, RandomGenerator random);

    /** The chances of each result; they add up to 1. */
    record Probabilities(double whiteWins, double draw, double blackWins) {

        GameOutcome drawn(RandomGenerator random) {
            var roll = random.nextDouble();
            if (roll < whiteWins) {
                return GameOutcome.WHITE_WINS;
            }
            return roll < whiteWins + draw ? GameOutcome.DRAW : GameOutcome.BLACK_WINS;
        }
    }
}
