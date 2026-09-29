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

/** Team files through the command line: the reply, the Interpretation flag, the checker and misuse. */
class SwissTeamCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Test
    void repliesOneLineOfTeamNumbersPerMatchWithBoardOnesWhiteFirst() throws IOException {
        // TRF CLI surface: the 310 team numbers, GHR 3.6 order on the pair's scores (the file is a patched-Gacrux one).
        var exit = run(
                "--swiss-team", copy("team-c6-graded.trf").toString(), "-p", "--interpretation", "bracket-seating=tpn");

        assertThat(exit).isZero();
        assertThat(stdout()).isEqualTo("5\n2 1\n9 3\n10 4\n6 8\n5 7\n");
    }

    @Test
    void takesAnInterpretation() throws IOException {
        // Ruling A6, the graded [C6].
        var exit =
                run("pair", copy("team-c6-graded.trf").toString(), "--interpretation", "upfloater-look-ahead=graded");

        assertThat(exit).isZero();
        assertThat(stdout()).isEqualTo("5\n3 1\n6 2\n9 4\n5 8\n10 7\n");
    }

    @Test
    void checksATournamentPairedByTheOracleAsConsistent() throws IOException {
        // Every round and the final match points of a patched-Gacrux tournament (the regression corpus).
        var exit = run("check", copy("team-gacrux.trf").toString(), "--interpretation", "bracket-seating=tpn");

        assertThat(stdout()).contains("6 consistent, 0 not");
        assertThat(exit).isZero();
    }

    @Test
    void refusesToPairATeamFileWithTheDutchSystem() throws IOException {
        var exit = run("--dutch", copy("team-c6-graded.trf").toString(), "-p");

        assertThat(exit).isEqualTo(3);
        assertThat(stderr()).contains("team file");
    }

    @Test
    void takesEveryReferenceAppReadingAndItsLiteralCounterpart() throws IOException {
        // ADR 0009: each name=value of the four departures from the text is accepted.
        for (var reading : new String[] {
            "bracket-seating=score-then-tpn",
            "bracket-seating=tpn",
            "pab-value=win",
            "pab-value=draw",
            "pab-value=loss",
            "baku-secondary-score=virtual-match-points",
            "baku-secondary-score=real",
            "edebt-board-count=higher",
            "edebt-board-count=lower"
        }) {
            assertThat(run("pair", copy("team-c6-graded.trf").toString(), "--interpretation", reading))
                    .as(reading)
                    .isZero();
            out.reset();
        }
    }

    @Test
    void refusesAnUnknownInterpretation() throws IOException {
        var exit = run("pair", copy("team-c6-graded.trf").toString(), "--interpretation", "c6=maybe");

        assertThat(exit).isEqualTo(3);
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
        try (var stream = SwissTeamCommandTest.class.getResourceAsStream("/trf/" + name)) {
            Files.write(target, Objects.requireNonNull(stream).readAllBytes());
        }
        return target;
    }
}
