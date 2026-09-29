package io.github.markdechamps.fideswiss.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Double-Swiss files through the command line: two TRF rounds per match (ADR 0007), one reply line per match. */
class DoubleSwissCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private Path generated;

    @BeforeEach
    void generateATournament() {
        generated = directory.resolve("double.trf");
        var exit = run(
                "generate",
                "--profile",
                "double-swiss",
                "--seed",
                "11",
                "--players",
                "15",
                "--rounds",
                "5",
                "-o",
                generated.toString());
        assertThat(exit).isZero();
        out.reset();
    }

    @Test
    void generatesTwoTrfRoundsPerMatch() throws IOException {
        var text = Files.readString(generated);

        assertThat(text).contains("\r\n142 10\r\n").contains("\r\n192 FIDE_DOUBLESWISS\r\n");
    }

    @Test
    void checksAGeneratedTournamentAsConsistentMatchByMatch() {
        var exit = run("check", generated.toString());

        assertThat(stdout()).contains("checked 5 rounds and 1 set of standings: 6 consistent, 0 not");
        assertThat(exit).isZero();
    }

    @Test
    void pairsTheNextMatchWithOneLinePerMatch() {
        var exit = run("pair", generated.toString(), "--rounds", "6");

        // 15 players: 7 matches and the PAB, one "white black" line each after the count.
        assertThat(exit).isZero();
        assertThat(stdout().lines()).hasSize(9).first().isEqualTo("8");
    }

    @Test
    void readsAFileWithoutA192AsDoubleSwissWhenTheFlagSaysSo() throws IOException {
        run("pair", generated.toString(), "--rounds", "6");
        var withCode = stdout();
        out.reset();
        var withoutCode = directory.resolve("no-code.trf");
        Files.writeString(withoutCode, Files.readString(generated).replace("192 FIDE_DOUBLESWISS\r\n", ""));

        var exit = run("--double-swiss", withoutCode.toString(), "-p", "--rounds", "6");

        assertThat(exit).isZero();
        assertThat(stdout()).isEqualTo(withCode);
    }

    @Test
    void generatesWithTheSystemFlagAsWithTheProfile() throws IOException {
        var flagged = directory.resolve("flagged.trf");
        run("-g", "11", "--double-swiss", "-o", flagged.toString());

        assertThat(Files.readString(flagged))
                .contains("\r\n192 FIDE_DOUBLESWISS\r\n")
                .contains("F 2.0    H 1.0");
    }

    @Test
    void listsTheSystemInTheVersion() {
        run("version");

        assertThat(stdout()).contains("C.04.5 Double-Swiss System 2026: experimental");
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
}
