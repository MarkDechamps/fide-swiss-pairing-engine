package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.BracketSeating;
import io.github.markdechamps.fideswiss.tournament.PabValue;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every round of the committed Swiss Team corpus must be paired exactly as patched Gacrux, the Oracle, paired it. The
 * corpus was made with the {@code tpn-order} patch and states {@code P 0.5}, and has no Baku with a secondary score, so
 * it replays under the literal bracket seating (ADR 0009); {@code SwissTeamUnpatchedCorpusTest} replays the defaults.
 */
class SwissTeamRegressionCorpusTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void pairsEveryRoundAsPatchedGacruxDid(Path file) throws IOException {
        var trf = Files.readString(file, StandardCharsets.UTF_8);

        assertThat(TrfReader.read(trf).recordedRounds()).isNotEmpty();
        assertThat(RecordedRoundReplay.differences(
                        file.getFileName().toString(), trf, BracketSeating.tpn(), PabValue.draw()))
                .isEmpty();
    }

    static List<Path> corpus() throws IOException, URISyntaxException {
        var directory = Path.of(SwissTeamRegressionCorpusTest.class
                .getResource("/corpus/swiss-team-2026")
                .toURI());
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.toString().endsWith(".trf"))
                    .sorted()
                    .toList();
        }
    }
}
