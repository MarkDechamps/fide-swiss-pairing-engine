package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import java.util.EnumMap;
import java.util.random.RandomGeneratorFactory;
import org.junit.jupiter.api.Test;

class MilvangModelTest {

    private final MilvangModel model = new MilvangModel();

    @Test
    void matchesThePapersWorkedExample() {
        // Milvang (2015): White 2467 against Black 2344 gives Pw 0.511, Pb 0.138, Pd 0.351.
        var probabilities = model.probabilities(2467, 2344);

        assertThat(probabilities.whiteWins()).isCloseTo(0.511, within(0.001));
        assertThat(probabilities.blackWins()).isCloseTo(0.138, within(0.001));
        assertThat(probabilities.draw()).isCloseTo(0.351, within(0.001));
    }

    @Test
    void drawsOutcomesWithThoseFrequencies() {
        var random = RandomGeneratorFactory.of("L64X128MixRandom").create(1L);
        var counts = new EnumMap<GameOutcome, Integer>(GameOutcome.class);
        for (var game = 0; game < 100_000; game++) {
            counts.merge(model.outcome(2467, 2344, random), 1, Integer::sum);
        }

        assertThat(counts.get(GameOutcome.WHITE_WINS) / 100_000.0).isCloseTo(0.511, within(0.01));
        assertThat(counts.get(GameOutcome.BLACK_WINS) / 100_000.0).isCloseTo(0.138, within(0.01));
    }

    @Test
    void givesTheFarStrongerPlayerEveryGame() {
        var probabilities = model.probabilities(2800, 1400);

        assertThat(probabilities.whiteWins()).isEqualTo(1.0);
        assertThat(probabilities.draw()).isEqualTo(0.0);
    }
}
