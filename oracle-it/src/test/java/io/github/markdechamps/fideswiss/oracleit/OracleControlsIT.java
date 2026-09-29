package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Positive controls for the two check directions: a tournament with one round tampered with must be refused by every
 * checker and by ours, or an "everything accepted" report would prove nothing. The file is bbpPairings v6.0.0's own
 * tournament for seed 1 (its default configuration) with round 1 re-paired by hand: 1-28 and 27-2 for 1-27 and 28-2,
 * results and points kept consistent, so the file is well formed and only the pairing is not the Oracle's.
 */
class OracleControlsIT {

    private static String tamperedTournament() {
        var generator = OraclePrograms.BBP_V6.locateChecker();
        assumeTrue(generator.isPresent(), OraclePrograms.BBP_V6::absence);
        var trf = generator.get().generate(1, "");
        var replacements = List.of(
                List.of("   27 w 1    20 b 1", "   28 w 1    20 b 1"),
                List.of("     1 b 0    44 w =", "     2 w +    44 w ="),
                List.of("   28 b -    10 b 0", "   27 b -    10 b 0"),
                List.of("     2 w +    14 b =", "     1 b 0    14 b ="),
                List.of(
                        "Player0027               2252                             2.5",
                        "Player0027               2252                             3.5"),
                List.of(
                        "Player0028               2249                             2.5",
                        "Player0028               2249                             1.5"));
        for (var replacement : replacements) {
            assertThat(trf).contains(replacement.get(0));
            trf = trf.replace(replacement.get(0), replacement.get(1));
        }
        return trf;
    }

    @Test
    void everyOracleCheckerRefusesATamperedRound() {
        var trf = tamperedTournament();
        for (var program : List.of(OraclePrograms.BBP_V6, OraclePrograms.BBP_V5, OraclePrograms.JAVAFO)) {
            var checker = program.locateChecker();
            if (checker.isPresent()) {
                assertThat(checker.get().check(trf)).as(program.name()).isInstanceOf(OracleVerdict.Rejected.class);
            }
        }
    }

    @Test
    void ourCheckerRefusesATamperedRound() {
        var report = new OracleTournamentCheck(SwissRulesEdition.EDITION_2026, KnownDivergences.none())
                .check("control", List.of(new OracleTournamentCheck.Generated("tampered", tamperedTournament())));
        assertThat(report.inconsistencies()).hasSize(1);
        assertThat(report.inconsistencies().get(0).round()).isEqualTo(1);
    }
}
