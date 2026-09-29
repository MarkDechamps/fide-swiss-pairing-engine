package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CorpusTest {

    @Test
    void generatesOneTournamentPerIndexFromTheCorpusSeed() {
        var settings = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(5)))
                .withPlayers(Range.of(12))
                .withRounds(Range.of(5));
        var generated = new ArrayList<GeneratedTournament>();

        var corpus =
                Corpus.of(TournamentGenerator.of(settings), CorpusSeed.of(3), 4).generate(generated::add);

        assertThat(generated)
                .extracting(GeneratedTournament::seed)
                .containsExactly(
                        CorpusSeed.of(3).tournament(0),
                        CorpusSeed.of(3).tournament(1),
                        CorpusSeed.of(3).tournament(2),
                        CorpusSeed.of(3).tournament(3));
        assertThat(corpus.skipped()).isZero();
        assertThat(corpus.skipsTooMany()).isFalse();
    }

    @Test
    void skipsTooManyWhenMoreThanATenthOfAPercentIsSkipped() {
        var unpairable = GeneratorSettings.of(
                Profiles.individualSwiss(NumberOfRounds.of(5)).with(new Unpairable()));

        var corpus = Corpus.of(TournamentGenerator.of(unpairable), CorpusSeed.of(1), 3)
                .generate(tournament -> {});

        assertThat(corpus.skipped()).isEqualTo(3);
        assertThat(corpus.skipsTooMany()).isTrue();
    }

    private static final class Unpairable implements PairingSystem {
        @Override
        public RoundPairing pairNextRound(Tournament tournament) {
            throw new NoLegalPairingException(List.of(Problem.of("no pairing")), new PairingTrace(List.of()));
        }
    }
}
