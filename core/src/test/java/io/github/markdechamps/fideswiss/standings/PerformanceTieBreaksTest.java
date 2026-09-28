package io.github.markdechamps.fideswiss.standings;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Art. 8.2 and 10 on a round robin of four rated 2490, 2480, 2470 and 2460: 1 beats 2 and draws 3 and 4; 2 beats 4
 * and draws 3; 3 draws everyone. Scores: 1 2, 2 1½, 3 1½, 4 1.
 */
class PerformanceTieBreaksTest {

    private static Standings standings(String tieBreak) {
        return CrossTable.of(4, 3, tieBreak)
                .round("1-2 1-0", "3-4 ½")
                .round("3-1 ½", "2-4 1-0")
                .round("1-4 ½", "2-3 ½")
                .standings();
    }

    @ParameterizedTest(name = "{0} of {1} is {2}")
    @CsvSource({
        // 10.1: (2480 + 2470 + 2460) / 3.
        "ARO, 1, 2470",
        "ARO/C1, 1, 2475",
        // 10.2: p = 2/3, rounded to .67, gives dp 125 (B.02 8.1.1).
        "TPR, 1, 2595",
        // p = 1/3 gives -125 over ARO 2480.
        "TPR, 4, 2355",
        // 10.3: the lowest R with PD(R-2480) + PD(R-2470) + PD(R-2460) >= 2 is 2592 (.65 + .67 + .68).
        "PTP, 1, 2592",
        // 10.4: TPRs of 2, 3 and 4 are 2473, 2477 and 2355.
        "APRO, 1, 2435",
        // 8.2: Buchholz of 2, 3 and 4 is 4½, 4½ and 5.
        "AOB, 1, 4.6667",
    })
    void computesThePerformance(String tieBreak, int participant, String expected) {
        var value = standings(tieBreak)
                .standing(id(participant))
                .tieBreakValues()
                .get(0)
                .value()
                .orElseThrow();

        assertThat(value.setScale(4, RoundingMode.HALF_UP)).isEqualByComparingTo(new BigDecimal(expected));
    }
}
