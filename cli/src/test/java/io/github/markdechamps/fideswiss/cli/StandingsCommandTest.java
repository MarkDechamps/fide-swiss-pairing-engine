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

    @Test
    void rankTeamsByTheTeamTieBreaksOfTheFilesList() throws IOException {
        // C.07 13.1, 13.3: every team has 3 match points; game points order 2, {1, 3}, 4; the top board (12.2) puts 3
        // first.
        var input = write(resource("team-tie-breaks-roundrobin.trf"));

        var exit = run("standings", input.toString());

        assertThat(exit).isZero();
        assertThat(stdout()).contains("MPvGP", "EDET");
        assertThat(stdout()).containsPattern("(?m)^1\\s+2\\s+Team 2\\s+3\\s+6.5\\s+1\\s*$");
        assertThat(stdout()).containsPattern("(?m)^2\\s+3\\s+Team 3\\s+3\\s+6\\s+2\\s+MPvGP 6.5 > 6\\s*$");
        assertThat(stdout()).containsPattern("(?m)^3\\s+1\\s+Team 1\\s+3\\s+6\\s+1\\s+EDET 2 > 1\\s*$");
    }

    @Test
    void takesTheTeamTieBreaksOfTheTiebreaksOption() throws IOException {
        var input = write(resource("team-tie-breaks-roundrobin.trf"));

        var exit = run("standings", input.toString(), "--tiebreaks", "MPvGP, EGGSB, SSSC/K2, BH:GP/C1");

        assertThat(exit).isZero();
        // EGGSB (13.2.4) of team 1 is 6.5 * 2.5 + 6 * 2 + 5.5 * 1.5 = 36.5, and team 3's is 36.
        assertThat(stdout()).containsPattern("(?m)^2\\s+1\\s+Team 1\\s+3\\s+6\\s+36.5\\s+");
        assertThat(stdout()).containsPattern("(?m)^3\\s+3\\s+Team 3\\s+3\\s+6\\s+36\\s+");
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
