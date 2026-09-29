package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.PabValue;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** The value of the PAB under game points (ADR 0009): a win per board without a {@code P} in 162, else {@code P}'s. */
class SwissTeamPabValueTest {

    private static final String WITH_P = "P 0.5";

    @Test
    void isAWinOnEveryBoardWhenTheFileStatesNoP() throws IOException {
        // Gacrux's reading and the engine's default: three boards, a win each.
        assertThat(pabValueOf(gamePointsFile().replace(WITH_P, ""))).isEqualTo(Points.of(3));
    }

    @Test
    void isTheDrawnMatchWhenTheFileStatesAHalfPoint() throws IOException {
        assertThat(pabValueOf(gamePointsFile())).isEqualTo(Points.of("1.5"));
    }

    @Test
    void isAWinWhenTheFileStatesOnePoint() throws IOException {
        assertThat(pabValueOf(gamePointsFile().replace(WITH_P, "P 1.0"))).isEqualTo(Points.of(3));
    }

    @Test
    void isNothingWhenTheFileStatesZero() throws IOException {
        assertThat(pabValueOf(gamePointsFile().replace(WITH_P, "P 0.0"))).isEqualTo(Points.ZERO);
    }

    @Test
    void followsThePabValueReadingOverAPThatStatesAResultUnderGamePoints() throws IOException {
        // ADR 0009: the reading is selectable under game points too; a chosen win overrides P 0.5.
        var file = TrfReader.read(gamePointsFile());
        var win = file.settings().with(PabValue.win());
        var draw = file.settings().with(PabValue.draw());

        assertThat(win.pairingAllocatedByeValue()).isEqualTo(Points.of(3));
        assertThat(draw.pairingAllocatedByeValue()).isEqualTo(Points.of("1.5"));
    }

    private static Points pabValueOf(String trf) {
        return TrfReader.read(trf).settings().pairingAllocatedByeValue();
    }

    private static String gamePointsFile() throws IOException {
        var file = Path.of("src/test/resources/corpus/swiss-team-2026/team-2018.trf");
        return Files.readString(file, StandardCharsets.UTF_8);
    }
}
