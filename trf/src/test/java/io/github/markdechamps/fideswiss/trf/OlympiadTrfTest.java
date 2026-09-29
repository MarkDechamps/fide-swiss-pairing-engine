package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/** A team file with the provisional {@code 192 FIDE_OLYMPIAD}, paired by the Olympiad Pairing Rules. */
class OlympiadTrfTest {

    private static final String FILE = resource("six-teams-round-2.trf");

    @Test
    void readsFideOlympiadAsTheOlympiadPairingRules() {
        var file = TrfReader.read(FILE);

        assertThat(file.settings().pairingSystem().name()).isEqualTo("D.02 Olympiad Pairing Rules 2022");
        assertThat(file.settings().pairingSystem().competitionType()).isEqualTo(CompetitionType.TEAM);
        assertThat(file.settings().pairingAllocatedByeValue()).isEqualTo(Points.of(1));
    }

    @Test
    void pairsTheBottomGroupUpIntoTheMedianGroupWhoseTeamsHaveMet() {
        // Round 1: 1 and 2 won, 3-6 drew, 4 and 5 lost. The median team is the fourth (6), so the 1-MP group {3, 6}
        // is the Median Group, but 3 and 6 have met. The top group pairs 1-2; Kept Pairings floats 4 and 5 up
        // (8.2.2, 8.4): 5 takes 6, the lowest ranked (8.2.1), and 4 takes 3. Colours by 7.5.1; order by 11.1.
        var pairing = TrfReader.read(FILE).tournament().pairNextRound();

        assertThat(pairing.boards().stream()
                        .map(board -> board.white() + "-" + board.black())
                        .toList())
                .containsExactly("2-1", "4-3", "6-5");
    }

    @Test
    void refusesBakuAcceleration() {
        var file = TrfReader.read(FILE.replace("192 FIDE_OLYMPIAD", "192 FIDE_OLYMPIAD_BAKU"));

        assertThatThrownBy(file::tournament)
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("C.04.7");
    }

    private static String resource(String name) {
        try (var stream =
                Objects.requireNonNull(OlympiadTrfTest.class.getResourceAsStream("/olympiad/" + name), name)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
