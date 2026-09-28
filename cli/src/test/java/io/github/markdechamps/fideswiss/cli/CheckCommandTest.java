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

class CheckCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Test
    void findsEveryRoundOfTheOraclesTournamentConsistent() throws IOException {
        var input = write("bbp.trf", resource("bbp-small.trf"));

        var exit = run("check", input.toString());

        assertThat(exit).isZero();
        assertThat(stdout()).containsPattern("checked \\d+ rounds and 1 set of standings: \\d+ consistent, 0 not");
    }

    @Test
    void reportsARoundThatIsNotTheSystemsPairing() throws IOException {
        // Round 1 of the small file with the colours of 1-3 reversed: legal, but 5.2.5 gives 1 White.
        var original = resource("half-point-bye-in-round-2.trf");
        var swapped = original.replace("0.0          1 b 0", "0.0          1 w 0")
                .replace("1.0          3 w 1", "1.0          3 b 1");
        var input = write("swapped.trf", swapped);

        var exit = run(input.toString(), "-c");

        assertThat(exit).isEqualTo(6);
        assertThat(stdout())
                .contains("round 1: DIFFERENT", "checked 1 rounds and 1 set of standings: 1 consistent, 1 not");
    }

    @Test
    void reportsAnIllegalRoundWithTheArticle() throws IOException {
        // Player 3 meets player 1 again in round 2.
        var original = resource("half-point-bye-in-round-2.trf").replace("142 5", "142 5");
        var rematch = original.replace("1.0          3 w 1", "1.0          3 w 1     3 b 1")
                .replace("0.0          1 b 0", "0.0          1 b 0     1 w 0")
                .replace("0.5          2 w =", "0.5          2 w =     5 w 1")
                .replace("1.0       0000 - U", "1.0       0000 - U     4 b 0");
        var input = write("rematch.trf", rematch);

        var exit = run("check", input.toString());

        assertThat(exit).isEqualTo(6);
        assertThat(stdout()).contains("round 2: ILLEGAL", "[C.04.1 Art. 2]");
    }

    @Test
    void reportsPointsThatDifferFromTheComputedScore() throws IOException {
        var wrongPoints = resource("half-point-bye-in-round-2.trf")
                .replace(
                        "Alpha, Anna                       2600                             1.0",
                        "Alpha, Anna                       2600                             1.5");
        var input = write("points.trf", wrongPoints);

        var exit = run("check", input.toString());

        assertThat(exit).isEqualTo(6);
        assertThat(stdout()).contains("standings after round 1: DIFFERENT", "1: points 1.5 in file, 1 computed");
    }

    @Test
    void reportsARankOutsideTheComputedRangeOfItsScore() throws IOException {
        // Player 3 (0 points, last) claims rank 1.
        var wrongRank = resource("half-point-bye-in-round-2.trf").replace("0.0          1 b 0", "0.0    1     1 b 0");
        var input = write("rank.trf", wrongRank);

        var exit = run("check", input.toString());

        assertThat(exit).isEqualTo(6);
        assertThat(stdout()).contains("3: rank 1 in file, 5 computed");
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

    private Path write(String name, String content) throws IOException {
        var target = directory.resolve(name);
        Files.writeString(target, content);
        return target;
    }

    private static String resource(String name) throws IOException {
        try (var stream = CheckCommandTest.class.getResourceAsStream("/trf/" + name)) {
            return new String(Objects.requireNonNull(stream, name).readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
