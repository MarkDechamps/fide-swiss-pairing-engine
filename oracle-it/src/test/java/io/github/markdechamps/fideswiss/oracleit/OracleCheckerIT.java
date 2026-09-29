package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 * Our pairings, their checker (Verification strategy): the tournaments our generator plays, with all their rounds, are
 * given to the Oracle's own check mode ({@code -c}), which must accept every round. Sized by {@link OracleCorpus}; each
 * rejected tournament is written under {@code target/oracle-failures} as the exact file the checker was given.
 */
class OracleCheckerIT {

    @Test
    void bbpPairingsV6AcceptsOurDutch2026Tournaments() throws IOException {
        gate(OraclePrograms.BBP_V6, SwissRulesEdition.EDITION_2026);
    }

    @Test
    void bbpPairingsV5AcceptsOurDutch2017Tournaments() throws IOException {
        gate(OraclePrograms.BBP_V5, SwissRulesEdition.PRE_2026);
    }

    @Test
    void jaVaFoAcceptsOurDutch2017Tournaments() throws IOException {
        gate(OraclePrograms.JAVAFO, SwissRulesEdition.PRE_2026);
    }

    private static void gate(OraclePrograms program, SwissRulesEdition edition) throws IOException {
        var checker = program.locateChecker();
        assumeTrue(checker.isPresent(), program::absence);
        var tournaments = OracleCorpus.corpus(edition, OracleCorpus.events(OracleCorpus.ALL_EVENTS));
        var report = new OracleCheckerGate(checker.get(), KnownDivergences.register()).compare(tournaments);
        System.out.println(report.summary() + "; " + OracleCorpus.accelerated(tournaments) + " accelerated");
        for (var rejection : report.rejections()) {
            System.out.println("  " + rejection.describe());
            OracleCorpus.save(program.name() + "-checker-" + rejection.tournament() + ".trf", rejection.input());
        }
        assertThat(report.rejections()).as(report.summary()).isEmpty();
    }
}
