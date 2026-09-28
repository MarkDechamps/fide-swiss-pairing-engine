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

class EditionFlagTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Test
    void checksATournamentPairedByDutch2017UnderThePre2026Edition() throws IOException {
        var input = write("bbp5.trf", resource("bbp5-small.trf"));

        var exit = run("check", input.toString(), "--edition", "pre-2026");

        assertThat(exit).isZero();
    }

    @Test
    void findsTheSameTournamentDifferentUnderThe2026Rules() throws IOException {
        var input = write("bbp5.trf", resource("bbp5-small.trf"));

        var exit = run("check", input.toString(), "--edition", "2026");

        assertThat(exit).isEqualTo(6);
    }

    @Test
    void selectsThePre2026EditionFromRecord192() throws IOException {
        var input = write("bbp5.trf", resource("bbp5-small.trf") + "192 FIDE_DUTCH_2017\r\n");

        var exit = run("check", input.toString());

        assertThat(exit).isZero();
    }

    @Test
    void rejectsAnUnknownEdition() throws IOException {
        var input = write("bbp5.trf", resource("bbp5-small.trf"));

        var exit = run("check", input.toString(), "--edition", "1999");

        assertThat(exit).isEqualTo(3);
    }

    private int run(String... arguments) {
        return new Main(
                        new ByteArrayInputStream(new byte[0]),
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8))
                .run(arguments);
    }

    private Path write(String name, String content) throws IOException {
        var target = directory.resolve(name);
        Files.writeString(target, content);
        return target;
    }

    private static String resource(String name) throws IOException {
        try (var stream = EditionFlagTest.class.getResourceAsStream("/trf/" + name)) {
            return new String(Objects.requireNonNull(stream, name).readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
