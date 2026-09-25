package io.github.markdechamps.fideswiss.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PairCommandTest {

    @TempDir
    Path directory;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Nested
    class GivenTheCompatibleShortForm {

        @Test
        void writesTheJaVaFoPairingReplyToTheOutputFile() throws IOException {
            var input = copy("half-point-bye-in-round-2.trf");
            var reply = directory.resolve("reply.txt");

            var exit = run("--dutch", input.toString(), "-p", reply.toString());

            assertThat(exit).isZero();
            assertThat(Files.readString(reply)).isEqualTo("2\n5 1\n3 4\n");
        }

        @Test
        void writesTheReplyToStandardOutputWithoutAnOutputFile() throws IOException {
            var input = copy("half-point-bye-in-round-2.trf");

            var exit = run(input.toString(), "-p");

            assertThat(exit).isZero();
            assertThat(stdout()).isEqualTo("2\n5 1\n3 4\n");
        }
    }

    @Nested
    class GivenTheCanonicalSubcommand {

        @Test
        void readsTheTournamentFromStandardInput() throws IOException {
            var exit = runWithInput(resource("half-point-bye-in-round-2.trf"), "pair", "-");

            assertThat(exit).isZero();
            assertThat(stdout()).isEqualTo("2\n5 1\n3 4\n");
        }

        @Test
        void writesTheTraceBesideTheReply() throws IOException {
            var input = copy("half-point-bye-in-round-2.trf");
            var trace = directory.resolve("trace.txt");

            run("pair", input.toString(), "-l", trace.toString());

            assertThat(Files.readString(trace)).contains("bracket 1", "C.04.3 5.2");
        }
    }

    @Nested
    class GivenAnInvalidRequest {

        @Test
        void exitsWith3ForAMissingInputFileArgument() {
            var exit = run("pair");

            assertThat(exit).isEqualTo(3);
            assertThat(stderr()).startsWith("error:");
        }

        @Test
        void exitsWith5ForAnUnreadableFile() {
            var exit = run("pair", directory.resolve("missing.trf").toString());

            assertThat(exit).isEqualTo(5);
        }

        @Test
        void exitsWith3WhenTheLastRoundIsAlreadyPaired() throws IOException {
            var input = directory.resolve("finished.trf");
            Files.writeString(input, resource("half-point-bye-in-round-2.trf").replace("142 5", "142 1"));

            var exit = run("pair", input.toString());

            assertThat(exit).isEqualTo(3);
        }
    }

    private int run(String... arguments) {
        return runWithInput("", arguments);
    }

    private int runWithInput(String stdin, String... arguments) {
        InputStream in = new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8));
        return new Main(
                        in,
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
        Files.writeString(target, resource(name));
        return target;
    }

    private static String resource(String name) throws IOException {
        try (var stream = PairCommandTest.class.getResourceAsStream("/trf/" + name)) {
            return new String(Objects.requireNonNull(stream, name).readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
