package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.ArrayList;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * The PR-gate invariant run: a few hundred seeded Dutch tournaments per Swiss Rules Edition must break no absolute
 * criterion.
 */
class InvariantRunTest {

    private static final CorpusSeed CORPUS = CorpusSeed.of(20260925L);
    private static final int TOURNAMENTS = 200;

    @ParameterizedTest
    @EnumSource(SwissRulesEdition.class)
    void generatedDutchTournamentsKeepEveryInvariant(SwissRulesEdition edition) {
        var settings = GeneratorSettings.of(
                        Profiles.individualSwiss(NumberOfRounds.of(9)).with(edition))
                .withPlayers(Range.of(14, 48))
                .withRounds(Range.of(5, 11))
                .withForfeitRate(Range.of(5, 30))
                .withHalfPointByeRate(Range.of(10, 200))
                .withZeroPointByeRate(Range.of(10, 200))
                .withFullPointByeRate(Range.of(20, 200))
                .withWithdrawalPercentage(Range.of(0, 8))
                .withRandomAcceleration()
                .withRandomScoring()
                .withRandomTieBreaks();
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
