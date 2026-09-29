package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.generator.Chance;
import io.github.markdechamps.fideswiss.generator.Corpus;
import io.github.markdechamps.fideswiss.generator.CorpusSeed;
import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.generator.TournamentGenerator;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class OracleDialectTest {

    private static Tournament tournament(Chance baku) {
        var settings = GeneratorSettings.of(
                        Profiles.individualSwiss(NumberOfRounds.of(6)).with(PairingSystems.dutch()))
                .withBakuAcceleration(baku)
                .withPlayers(Range.of(16, 16))
                .withRounds(Range.of(4, 4));
        var found = new ArrayList<Tournament>();
        Corpus.of(TournamentGenerator.of(settings), CorpusSeed.of(3L), 1).generate(generated -> {
            if (generated instanceof GeneratedTournament.Completed done) {
                found.add(done.tournament());
            }
        });
        return found.getFirst();
    }

    @Test
    void anAcceleratedTournamentIsWrittenWithXxaRecordsBeforeItsPlayers() {
        var text = OracleDialect.BBP.write(tournament(Chance.always()), "baku");

        var lines = text.split("\r\n");
        var xxa = java.util.Arrays.stream(lines)
                .filter(line -> line.startsWith("XXA "))
                .toList();
        assertThat(xxa).isNotEmpty();
        assertThat(xxa.getFirst()).startsWith("XXA    1  ").matches("XXA +\\d+( +\\d\\.\\d){4}");
        assertThat(text.indexOf("XXA ")).isLessThan(text.indexOf("001 "));
    }

    @Test
    void anUnacceleratedTournamentHasNoXxa() {
        assertThat(OracleDialect.JAVAFO.write(tournament(Chance.never()), "plain"))
                .doesNotContain("XXA");
    }
}
