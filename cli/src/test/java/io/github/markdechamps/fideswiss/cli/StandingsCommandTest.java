package io.github.markdechamps.fideswiss.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StandingsCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    void printsRankScoreAndEveryTieBreakOfTheFilesList() throws IOException {
        var input = write(resource("half-point-bye-in-round-2.trf") + "202 WIN,BH\r");

        var exit = run("standings", input.toString());

        assertThat(exit).isZero();
        assertThat(stdout().lines().toList())
                .first()
                .asString()
                .contains("rank", "id", "name", "score", "WIN", "BH", "decided by");
        // After round 1, 1 and 5 have 1 point and one win; 5's PAB counts a dummy of ½ (C.07 16.4.2), 1 beat 3 (0).
        assertThat(stdout()).containsPattern("(?m)^1\\s+5\\s+Echo, Emil\\s+1\\s+1\\s+0.5\\s*$");
        assertThat(stdout()).containsPattern("(?m)^2\\s+1\\s+Alpha, Anna\\s+1\\s+1\\s+0\\s+BH 0.5 > 0$");
    }

    @Test
    void explainsWhyOneParticipantRanksAboveAnother() throws IOException {
        var input = write(resource("half-point-bye-in-round-2.trf"));

        var exit = run("standings", input.toString(), "--why", "2", "3");

        assertThat(exit).isZero();
        assertThat(stdout()).contains("2 ranks above 3: score 0.5 > 0");
    }

    @Test
    void rejectsAnUnknownTieBreakWith3() throws IOException {
        var input = write(resource("half-point-bye-in-round-2.trf") + "202 XYZ\r");

        assertThat(run("standings", input.toString())).isEqualTo(3);
    }

    private int run(String... arguments) {
        return new Main(
                        new ByteArrayInputStream(new byte[0]),
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8))
                .run(arguments);
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private Path write(String content) throws IOException {
        var target = directory.resolve("in.trf");
        Files.writeString(target, content);
        return target;
    }

    private static String resource(String name) throws IOException {
        try (var stream = StandingsCommandTest.class.getResourceAsStream("/trf/" + name)) {
            return new String(Objects.requireNonNull(stream, name).readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
