package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

/**
 * Their pairings, our checker (Verification strategy): the Oracle plays tournaments with its own random tournament
 * generator, and our {@code check} must find every round legal and the system's own pairing. Sized by {@link
 * OracleCorpus}; the players and rounds of each tournament are drawn from the seed (14..60 players, 5..11 rounds), the
 * rest is the program's own random choice. JaVaFo takes no seed, so a failing JaVaFo tournament is kept as generated.
 */
class OracleOwnTournamentsIT {

    @Test
    void bbpPairingsV6TournamentsAreConsistentForUs() throws IOException {
        gate(OraclePrograms.BBP_V6, SwissRulesEdition.EDITION_2026);
    }

    @Test
    void bbpPairingsV5TournamentsAreConsistentForUs() throws IOException {
        gate(OraclePrograms.BBP_V5, SwissRulesEdition.PRE_2026);
    }

    @Test
    void jaVaFoTournamentsAreConsistentForUs() throws IOException {
        gate(OraclePrograms.JAVAFO, SwissRulesEdition.PRE_2026);
    }

    private static void gate(OraclePrograms program, SwissRulesEdition edition) throws IOException {
        var oracle = program.locateChecker();
        assumeTrue(oracle.isPresent(), program::absence);
        var generated = new ArrayList<OracleTournamentCheck.Generated>();
        var first = OracleCorpus.CORPUS.value();
        for (var index = 0; index < OracleCorpus.TOURNAMENTS; index++) {
            var seed = first + index;
            var random = new SplittableRandom(seed);
            var configuration =
                    "PlayersNumber=" + (14 + random.nextInt(47)) + "\nRoundsNumber=" + (5 + random.nextInt(7)) + "\n";
            generated.add(new OracleTournamentCheck.Generated(
                    "seed-" + seed, oracle.get().generate(seed, configuration)));
        }
        var report = new OracleTournamentCheck(edition, KnownDivergences.register())
                .check(oracle.get().name(), generated);
        System.out.println(report.summary());
        for (var inconsistency : report.inconsistencies()) {
            System.out.println("  " + inconsistency.describe());
            OracleCorpus.save(program.name() + "-own-" + inconsistency.tournament() + ".trf", inconsistency.trf());
        }
        assertThat(report.inconsistencies()).as(report.summary()).isEmpty();
    }
}
