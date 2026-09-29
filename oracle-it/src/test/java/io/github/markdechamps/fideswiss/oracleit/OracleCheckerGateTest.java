package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.trf.TrfWriter;
import java.util.List;
import org.junit.jupiter.api.Test;

class OracleCheckerGateTest {

    private static OracleChecker checker(OracleVerdict verdict) {
        return new OracleChecker() {
            @Override
            public String name() {
                return "Fake 1.0";
            }

            @Override
            public OracleDialect dialect() {
                return OracleDialect.BBP;
            }

            @Override
            public OracleVerdict check(String trf) {
                return verdict;
            }
        };
    }

    @Test
    void acceptedTournamentsAreCounted() {
        var report = new OracleCheckerGate(checker(new OracleVerdict.Accepted()), KnownDivergences.none())
                .compare(OracleGateTest.corpus());

        assertThat(report.accepted()).isEqualTo(3);
        assertThat(report.rejections()).isEmpty();
    }

    @Test
    void anUnregisteredRejectionIsAFailure() {
        var report = new OracleCheckerGate(
                        checker(new OracleVerdict.Rejected(0, "Failure in round 2")), KnownDivergences.none())
                .compare(OracleGateTest.corpus());

        assertThat(report.rejections()).hasSize(3);
        assertThat(report.rejections().get(0).describe()).contains("Failure in round 2");
    }

    @Test
    void aRegisteredRejectionIsCountedApart() {
        var register = KnownDivergences.parse("KD-9\tFake\t(?m)^001 ");

        var report = new OracleCheckerGate(checker(new OracleVerdict.Rejected(0, "x")), register)
                .compare(OracleGateTest.corpus());

        assertThat(report.rejections()).isEmpty();
        assertThat(report.registered()).hasSize(3);
        assertThat(report.summary()).contains("KD-9=3");
    }

    @Test
    void ourCheckFindsTheLibrarysOwnTournamentsConsistent() {
        var files = OracleGateTest.corpus().stream()
                .map(done -> new OracleTournamentCheck.Generated(
                        "seed-" + done.seed(),
                        TrfWriter.write(
                                done.tournament(),
                                TrfWriter.Options.named("own").withJaVaFoLines())))
                .toList();

        var report = new OracleTournamentCheck(SwissRulesEdition.EDITION_2026, KnownDivergences.none())
                .check("Library", files);

        assertThat(report.inconsistencies()).isEmpty();
        assertThat(report.consistent()).isEqualTo(report.rounds()).isPositive();
    }

    @Test
    void aFileWithoutRoundsIsOneInconsistency() {
        var report = new OracleTournamentCheck(SwissRulesEdition.EDITION_2026, KnownDivergences.none())
                .check("Library", List.of(new OracleTournamentCheck.Generated("junk", "not a TRF")));

        assertThat(report.inconsistencies()).hasSize(1);
        assertThat(report.inconsistencies().get(0).describe()).contains("junk").contains("records no round");
    }
}
