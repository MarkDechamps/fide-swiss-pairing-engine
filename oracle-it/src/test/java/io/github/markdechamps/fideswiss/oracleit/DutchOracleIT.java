package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * The nightly Oracle gates for the Dutch System (Verification strategy): the tournaments our generator plays are
 * re-paired round by round by the Oracle, and every round must be identical. Each gate is skipped, with the way to
 * configure it, when its program is not configured ({@link OraclePrograms}). The corpus is {@link OracleCorpus}; every
 * difference is written under {@code target/oracle-failures} as the Oracle's exact input, for CI to keep.
 */
class DutchOracleIT {

    @Test
    void bbpPairingsV6PairsDutch2026AsWeDo() throws IOException {
        gate(OraclePrograms.BBP_V6, SwissRulesEdition.EDITION_2026);
    }

    @Test
    void bbpPairingsV5PairsDutch2017AsWeDo() throws IOException {
        gate(OraclePrograms.BBP_V5, SwissRulesEdition.PRE_2026);
    }

    @Test
    void jaVaFoPairsDutch2017AsWeDo() throws IOException {
        gate(OraclePrograms.JAVAFO, SwissRulesEdition.PRE_2026);
    }

    private static void gate(OraclePrograms program, SwissRulesEdition edition) throws IOException {
        var oracle = program.locate();
        assumeTrue(oracle.isPresent(), program::absence);
        var tournaments = OracleCorpus.corpus(edition, OracleCorpus.events(OracleCorpus.ALL_EVENTS));
        var report = new OracleGate(oracle.get(), edition).compare(tournaments);
        System.out.println(report.summary() + "; " + OracleCorpus.accelerated(tournaments) + " accelerated");
        for (var difference : report.differences()) {
            System.out.println("  " + difference.describe());
            OracleCorpus.save(
                    program.name() + "-" + difference.tournament() + "-round" + difference.round() + ".trf",
                    difference.input());
        }
        assertThat(report.differences()).as(report.summary()).isEmpty();
    }
}
