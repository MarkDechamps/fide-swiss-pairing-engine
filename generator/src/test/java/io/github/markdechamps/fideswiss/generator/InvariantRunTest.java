package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/** The PR-gate invariant run: a few hundred seeded Dutch 2026 tournaments must break no absolute criterion. */
class InvariantRunTest {

    private static final CorpusSeed CORPUS = CorpusSeed.of(20260925L);
    private static final int TOURNAMENTS = 200;

    @Test
    void generatedDutchTournamentsKeepEveryInvariant() {
        var settings = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9)))
                .withPlayers(Range.of(14, 48))
                .withRounds(Range.of(5, 11))
                .withForfeitRate(Range.of(5, 30))
                .withHalfPointByeRate(Range.of(10, 200))
                .withZeroPointByeRate(Range.of(10, 200))
                .withWithdrawalPercentage(Range.of(0, 8));
        var generator = TournamentGenerator.of(settings);
        var violations = new ArrayList<String>();
        var skipped = 0;
        for (var index = 0; index < TOURNAMENTS; index++) {
            var seed = CORPUS.tournament(index);
            switch (generator.generate(seed)) {
                case GeneratedTournament.Completed completed ->
                    InvariantChecker.violationsOf(completed.tournament())
                            .forEach(violation -> violations.add("seed " + seed + " " + violation));
                case GeneratedTournament.Skipped skip -> skipped++;
            }
        }

        assertThat(violations).isEmpty();
        assertThat(skipped).isLessThanOrEqualTo(TOURNAMENTS / 100);
    }
}
