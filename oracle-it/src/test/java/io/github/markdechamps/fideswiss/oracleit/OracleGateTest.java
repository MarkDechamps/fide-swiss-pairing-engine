package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.generator.Corpus;
import io.github.markdechamps.fideswiss.generator.CorpusSeed;
import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.generator.TournamentGenerator;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OracleGateTest {

    /** An Oracle that always pairs nobody: it differs from the library in every round. */
    private static final PairingOracle WRONG = new PairingOracle() {
        @Override
        public String name() {
            return "Wrong 1.0";
        }

        @Override
        public OracleAnswer pair(String trf) {
            return new OracleAnswer.Paired(new OraclePairing(Set.of("99-98"), Optional.empty()));
        }

        @Override
        public OracleDialect dialect() {
            return OracleDialect.BBP;
        }
    };

    static List<GeneratedTournament.Completed> corpus() {
        var settings = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(5))
                        .with(PairingSystems.dutch())
                        .with(SwissRulesEdition.EDITION_2026))
                .withPlayers(Range.of(10, 12))
                .withRounds(Range.of(4, 5));
        var completed = new ArrayList<GeneratedTournament.Completed>();
        Corpus.of(TournamentGenerator.of(settings), CorpusSeed.of(7L), 3).generate(generated -> {
            if (generated instanceof GeneratedTournament.Completed done) {
                completed.add(done);
            }
        });
        return completed;
    }

    @Test
    void anUnregisteredDifferenceFailsTheGateAndEndsItsTournament() {
        var report = new OracleGate(WRONG, SwissRulesEdition.EDITION_2026, KnownDivergences.none()).compare(corpus());

        assertThat(report.differences()).hasSize(3);
        assertThat(report.registered()).isEmpty();
        assertThat(report.rounds()).isEqualTo(3);
        assertThat(report.agreements()).isZero();
    }

    @Test
    void aRegisteredDifferenceIsCountedApartAndTheTournamentGoesOn() {
        var register = KnownDivergences.parse("KD-9\tWrong\t(?m)^001 ");

        var report = new OracleGate(WRONG, SwissRulesEdition.EDITION_2026, register).compare(corpus());

        assertThat(report.differences()).isEmpty();
        assertThat(report.registered())
                .hasSizeGreaterThan(3)
                .allMatch(entry -> entry.id().equals("KD-9"));
        assertThat(report.rounds()).isEqualTo(report.registered().size());
        assertThat(report.summary())
                .contains("0 with an unregistered difference")
                .contains("KD-9=");
    }

    @Test
    void aRegisterEntryOfAnotherOracleDoesNotCoverIt() {
        var register = KnownDivergences.parse("KD-9\tOther\t(?m)^001 ");

        var report = new OracleGate(WRONG, SwissRulesEdition.EDITION_2026, register).compare(corpus());

        assertThat(report.differences()).hasSize(3);
    }
}
