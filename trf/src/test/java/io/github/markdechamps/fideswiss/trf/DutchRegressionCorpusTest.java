package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
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
 * Every round of the committed Oracle corpora must be paired exactly as the Oracle paired it: bbpPairings v6 for
 * Dutch 2026, bbpPairings v5.0.1 for Dutch 2017 (the pre-2026 Swiss Rules Edition).
 */
class DutchRegressionCorpusTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus2026")
    void pairsEveryRoundAsBbpPairingsV6Did(Path file) throws IOException {
        var trf = Files.readString(file, StandardCharsets.UTF_8);

        assertThat(TrfReader.read(trf).recordedRounds()).isNotEmpty();
        assertThat(RecordedRoundReplay.differences(file.getFileName().toString(), trf))
                .isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("corpus2017")
    void pairsEveryRoundOfDutch2017AsBbpPairingsV5Did(Path file) throws IOException {
        var trf = Files.readString(file, StandardCharsets.UTF_8);

        assertThat(TrfReader.read(trf).recordedRounds()).isNotEmpty();
        assertThat(RecordedRoundReplay.differences(file.getFileName().toString(), trf, SwissRulesEdition.PRE_2026))
                .isEmpty();
    }

    static List<Path> corpus2026() throws IOException, URISyntaxException {
        return corpus("dutch-2026");
    }

    static List<Path> corpus2017() throws IOException, URISyntaxException {
        return corpus("dutch-2017");
    }

    private static List<Path> corpus(String name) throws IOException, URISyntaxException {
        var directory = Path.of(
                DutchRegressionCorpusTest.class.getResource("/corpus/" + name).toURI());
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(path -> path.toString().endsWith(".trf"))
                    .sorted()
                    .toList();
        }
    }
}
