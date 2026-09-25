package io.github.markdechamps.fideswiss.cli;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.trf.TrfReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GenerateCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Nested
    class GivenTheCanonicalSubcommand {

        @Test
        void writesOneTrfPerSeedAndAManifest() throws IOException {
            var pattern = directory.resolve("t%d.trf").toString();

            var exit = run(
                    "generate", "--seed", "5", "--count", "3", "--players", "12..20", "--rounds", "5", "-o", pattern);

            assertThat(exit).isZero();
            for (var index = 0; index < 3; index++) {
                var file = TrfReader.read(Files.readString(directory.resolve("t" + index + ".trf")));
                assertThat(file.recordedRounds()).hasSize(5);
                assertThat(file.participants()).hasSizeBetween(12, 20);
            }
            assertThat(Files.readAllLines(directory.resolve("t%d.trf.manifest.tsv")))
                    .hasSize(4);
        }

        @Test
        void reproducesTheSameFilesFromTheSameSeed() throws IOException {
            run(
                    "generate",
                    "--seed",
                    "9",
                    "--players",
                    "14",
                    "-o",
                    directory.resolve("a.trf").toString());
            run(
                    "generate",
                    "--seed",
                    "9",
                    "--players",
                    "14",
                    "-o",
                    directory.resolve("b.trf").toString());

            assertThat(Files.readString(directory.resolve("a.trf")))
                    .isEqualTo(Files.readString(directory.resolve("b.trf")));
        }

        @Test
        void checksAsConsistentWithItself() {
            var file = directory.resolve("own.trf").toString();
            run("generate", "--seed", "3", "--players", "24", "--rounds", "7", "-o", file);

            assertThat(run("check", file)).isZero();
        }
    }

    @Nested
    class GivenTheJaVaFoShortForm {

        @Test
        void readsAConfigurationFile() throws IOException {
            var config = directory.resolve("rtg.cfg");
            Files.writeString(config, "PlayersNumber=17\nRoundsNumber=6\n");
            var out = directory.resolve("out.trf");

            var exit = run("-g", config.toString(), "-o", out.toString(), "-s", "4");

            assertThat(exit).isZero();
            var file = TrfReader.read(Files.readString(out));
            assertThat(file.participants()).hasSize(17);
            assertThat(file.recordedRounds()).hasSize(6);
        }

        @Test
        void takesASeedInPlaceOfAConfiguration() throws IOException {
            var out = directory.resolve("seeded.trf");

            var exit = run("-g", "12", "-o", out.toString());

            assertThat(exit).isZero();
            assertThat(Files.readString(out)).startsWith("012 RTG ");
        }
    }

    private int run(String... arguments) {
        return new Main(
                        new ByteArrayInputStream(new byte[0]),
                        new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8))
                .run(arguments);
    }
}
