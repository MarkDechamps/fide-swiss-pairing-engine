package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TournamentModelTest {

    private static final GeneratorSettings DUTCH = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9)));

    private final Tournament model = completed(
            DUTCH.with(DUTCH.tournament().with(SwissRulesEdition.PRE_2026))
                    .withPlayers(Range.of(24))
                    .withRounds(Range.of(6))
                    .withForfeitRate(Range.of(5))
                    .withHalfPointByeRate(Range.of(10))
                    .withZeroPointByeRate(Range.of(0))
                    .withLateEntryPercentage(Range.of(0)),
            21);

    @Nested
    class GivenAModelTournament {

        private final GeneratorSettings settings = TournamentModel.applyTo(DUTCH, model);

        @Test
        void replaysItsFieldUnderItsSettings() {
            var replayed = completed(settings, 22);

            assertThat(replayed.participants()).isEqualTo(model.participants());
            assertThat(replayed.rounds()).hasSize(6);
            assertThat(replayed.settings().swissRulesEdition()).isEqualTo(SwissRulesEdition.PRE_2026);
        }

        @Test
        void takesItsRatesFromWhatHappenedInIt() {
            assertThat(settings.forfeitRate().min()).isBetween(2, 20);
            assertThat(settings.halfPointByeRate().min()).isBetween(3, 40);
            assertThat(settings.zeroPointByeRate()).isEqualTo(Range.of(0));
            assertThat(settings.lateEntryPercentage()).isEqualTo(Range.of(0));
        }
    }

    private static Tournament completed(GeneratorSettings settings, long seed) {
        var generated = TournamentGenerator.of(settings).generate(TournamentSeed.of(seed));
        assertThat(generated).isInstanceOf(GeneratedTournament.Completed.class);
        return ((GeneratedTournament.Completed) generated).tournament();
    }
}
