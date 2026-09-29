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

/** Olympiad team files through the command line: the reply, the system flag and the checker. */
class OlympiadCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Test
    void pairsAFileWhose192IsFideOlympiadByTheOlympiadPairingRules() throws IOException {
        // The hand-worked round 2 of the TRF test: 2-1, 4-3, 6-5 in the order of 11.1.
        var exit = run("pair", copy("olympiad-six-teams.trf").toString());

        assertThat(exit).isZero();
        assertThat(stdout()).isEqualTo("3\n2 1\n4 3\n6 5\n");
    }

    @Test
    void checksARoundPairedByTheOlympiadPairingRulesAsConsistent() throws IOException {
        var exit = run("check", copy("olympiad-six-teams.trf").toString());

        assertThat(stdout()).contains("2 consistent, 0 not");
        assertThat(exit).isZero();
    }

    @Test
    void pairsASwissTeamFileByTheOlympiadPairingRulesWhenAsked() throws IOException {
        var exit = run("--olympiad", copy("team-c6-graded.trf").toString(), "-p");

        assertThat(exit).isZero();
        assertThat(stdout()).isNotEqualTo("5\n2 1\n9 3\n10 4\n6 8\n5 7\n");
    }

    @Test
    void pairsAnOlympiadFileByTheSwissTeamSystemWhenAsked() throws IOException {
        var exit = run("--swiss-team", copy("olympiad-six-teams.trf").toString(), "-p");

        assertThat(exit).isZero();
        assertThat(stdout()).startsWith("3\n");
    }

    @Test
    void refusesToPairAnOlympiadFileWithTheDutchSystem() throws IOException {
        var exit = run("--dutch", copy("olympiad-six-teams.trf").toString(), "-p");

        assertThat(exit).isEqualTo(3);
        assertThat(stderr()).contains("team file");
    }

    @Test
    void listsTheOlympiadPairingRulesInTheVersion() {
        run("version");

        assertThat(stdout()).contains("D.02 Olympiad Pairing Rules 2022: experimental");
    }

    private int run(String... arguments) {
        return new Main(
                        new ByteArrayInputStream(new byte[0]),
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8))
                .run(arguments);
    }

    private String stdout() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String stderr() {
        return err.toString(StandardCharsets.UTF_8);
    }

    private Path copy(String name) throws IOException {
        var target = directory.resolve(name);
        try (var stream = OlympiadCommandTest.class.getResourceAsStream("/trf/" + name)) {
            Files.write(target, Objects.requireNonNull(stream).readAllBytes());
        }
        return target;
    }
}
