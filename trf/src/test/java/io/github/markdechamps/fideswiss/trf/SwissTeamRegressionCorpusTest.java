package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Every round of the committed Swiss Team corpus must be paired exactly as patched Gacrux, the Oracle, paired it. */
class SwissTeamRegressionCorpusTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void pairsEveryRoundAsPatchedGacruxDid(Path file) throws IOException {
        var trf = Files.readString(file, StandardCharsets.UTF_8);

        assertThat(TrfReader.read(trf).recordedRounds()).isNotEmpty();
        assertThat(RecordedRoundReplay.differences(file.getFileName().toString(), trf))
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
