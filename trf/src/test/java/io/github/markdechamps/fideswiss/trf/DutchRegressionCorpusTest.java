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

/** Every round of the committed bbpPairings v6 corpus must be paired exactly as the Oracle paired it. */
class DutchRegressionCorpusTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus")
    void pairsEveryRoundAsTheOracleDid(Path file) throws IOException {
        var trf = Files.readString(file, StandardCharsets.UTF_8);

        assertThat(TrfReader.read(trf).recordedRounds()).isNotEmpty();
        assertThat(RecordedRoundReplay.differences(file.getFileName().toString(), trf))
                .isEmpty();
    }

    static List<Path> corpus() throws IOException, URISyntaxException {
        var directory = Path.of(DutchRegressionCorpusTest.class
                .getResource("/corpus/dutch-2026")
                .toURI());
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.toString().endsWith(".trf"))
                    .sorted()
                    .toList();
        }
    }
}
