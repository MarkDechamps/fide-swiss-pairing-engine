package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import java.util.random.RandomGenerator;

/**
 * The result model of bbp/JaVaFo's {@code DrawPercentage}: a fixed share of draws; a decisive game goes to White
 * with the Elo expectation of White's strength against Black's.
 */
public record FlatDrawModel(int drawPercentage) implements ResultModel {

    public FlatDrawModel {
        if (drawPercentage < 0 || drawPercentage > 100) {
            throw new IllegalArgumentException("A draw percentage lies in 0..100: " + drawPercentage);
        }
    }

    @Override
    public GameOutcome outcome(int whiteStrength, int blackStrength, RandomGenerator random) {
        var draw = drawPercentage / 100.0;
        var whiteExpectation = 1 / (1 + Math.pow(10, (blackStrength - whiteStrength) / 400.0));
        return new Probabilities((1 - draw) * whiteExpectation, draw, (1 - draw) * (1 - whiteExpectation))
                .drawn(random);
    }
}
