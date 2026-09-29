package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.standings.Rank;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * A team file's record 212 names the team tie-breaks of C.07 articles 12 and 13, and the Standings apply them. The file
 * is the four-team round robin of {@code TeamTieBreaksTest} in the core module, every team on 3 match points.
 */
class TeamTieBreaksTrfTest {

    private static String resource() {
        try (var stream = Objects.requireNonNull(
                TeamTieBreaksTrfTest.class.getResourceAsStream("/team-tie-breaks/roundrobin.trf"))) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void readsTheTeamTieBreaksOfRecord212() {
        var file = TrfReader.read(resource());

        assertThat(file.declaresTieBreaks()).isTrue();
        assertThat(file.settings().tieBreakList()).hasToString("MPvGP, EDET");
    }

    @Test
    void ranksTheTeamsByTheDeclaredTieBreaks() {
        var standings = TrfReader.read(resource()).tournament().standings();

        // 13.1: game points 6.5, 6, 6, 5.5; 13.3.2 with 12.2: team 3's top board beats team 1's.
        assertThat(rank(standings, "2")).isEqualTo(Rank.of(1));
        assertThat(rank(standings, "3")).isEqualTo(Rank.of(2));
        assertThat(rank(standings, "1")).isEqualTo(Rank.of(3));
        assertThat(rank(standings, "4")).isEqualTo(Rank.of(4));
    }

    private static Rank rank(io.github.markdechamps.fideswiss.standings.Standings standings, String team) {
        return standings.standing(ParticipantId.of(team)).rank();
    }
}
