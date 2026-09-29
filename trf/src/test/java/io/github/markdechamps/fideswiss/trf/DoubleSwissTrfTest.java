package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** ADR 0007: a Double-Swiss match is two TRF rounds, one per game. */
class DoubleSwissTrfTest {

    // Match 1: 1 beat 3 1½-½ (1-0, then ½-½); 2 and 4 drew game 1 ½-0 and 4 forfeited game 2.
    private static final String ONE_MATCH = header("192 FIDE_DOUBLESWISS\r")
            + player(1, "   3 w 1", "   3 b =")
            + player(2, "   4 w =", "   4 b +")
            + player(3, "   1 b 0", "   1 w =")
            + player(4, "   2 b 0", "   2 w -");

    @Nested
    class GivenAMatchRecordedAsTwoRounds {

        private final TrfTournament file = TrfReader.read(ONE_MATCH);

        @Test
        void readsOneMatchPerPairOfRounds() {
            assertThat(file.settings().pairingSystem().name()).isEqualTo("C.04.5 Double-Swiss System 2026");
            assertThat(file.settings().numberOfRounds().value()).isEqualTo(3);
            assertThat(file.recordedRounds()).hasSize(1);
        }

        @Test
        void readsEachGameFromItsOwnResultCodes() {
            var match = file.recordedRounds()
                    .getFirst()
                    .boardOf(ParticipantId.of("2"))
                    .orElseThrow();

            // Game 2 is seen from game 1's White, 2, who won it by forfeit with Black.
            assertThat(match.outcome())
                    .isEqualTo(MatchOutcome.ofGames(
                            List.of(GameOutcome.WHITE_HALF_BLACK_ZERO, GameOutcome.WHITE_WINS_BY_FORFEIT)));
        }

        @Test
        void scoresEachMatchByItsGamePoints() {
            var standings = file.tournament().standings();

            assertThat(standings.ranked())
                    .extracting(
                            standing -> standing.participant().id().value(),
                            standing -> standing.score().points())
                    .containsExactlyInAnyOrder(
                            tuple("1", Points.of("1.5")),
                            tuple("2", Points.of("1.5")),
                            tuple("3", Points.of("0.5")),
                            tuple("4", Points.ZERO));
        }

        @Test
        void writesItBackUnchanged() {
            var written = TrfWriter.write(file.tournament(), TrfWriter.Options.named("Test"));
            var reread = TrfReader.read(written);

            assertThat(written).contains("142 6\r\n").contains("192 FIDE_DOUBLESWISS\r\n");
            assertThat(written).contains("162  P 1.5    F 2.0    H 1.0");
            assertThat(reread.recordedRounds()).isEqualTo(file.recordedRounds());
        }
    }

    @Test
    void readsTheEncodingWhenTheCommandLineChoosesDoubleSwiss() {
        var withoutCode = ONE_MATCH.replace("192 FIDE_DOUBLESWISS\r", "");

        assertThat(TrfReader.read(withoutCode).recordedRounds()).hasSize(2);
        assertThat(TrfReader.read(withoutCode, true).recordedRounds()).hasSize(1);
    }

    @Test
    void readsAByeInBothColumnsAsOneByeForTheMatch() {
        var text = header("192 FIDE_DOUBLESWISS\r")
                + player(1, "   2 w 1", "   2 b 1")
                + player(2, "   1 b 0", "   1 w 0")
                + player(3, "0000 - U", "0000 - U");

        var round = TrfReader.read(text).recordedRounds().getFirst();

        assertThat(round.byeOf(ParticipantId.of("3"))).isPresent();
    }

    @Test
    void readsAbsencesMarkedForTheNextMatch() {
        var text = ONE_MATCH.replace("   3 b =", "   3 b =  0000 - H  0000 - H");

        var file = TrfReader.read(text);

        assertThat(file.recordedRounds()).hasSize(1);
        assertThat(file.tournament().absencesInNextRound()).containsKey(ParticipantId.of("1"));
    }

    @Test
    void refusesAHalfRecordedMatch() {
        var text = header("192 FIDE_DOUBLESWISS\r") + player(1, "   2 w 1") + player(2, "   1 b 0");

        assertThatThrownBy(() -> TrfReader.read(text))
                .isInstanceOf(InvalidTrfException.class)
                .hasMessageContaining("half recorded");
    }

    @Test
    void refusesAByeForOneGameOnly() {
        var text = header("192 FIDE_DOUBLESWISS\r")
                + player(1, "   2 w 1", "   2 b 1")
                + player(2, "   1 b 0", "   1 w 0")
                + player(3, "0000 - U", "0000 - Z");

        assertThatThrownBy(() -> TrfReader.read(text))
                .isInstanceOf(InvalidTrfException.class)
                .hasMessageContaining("byes apply only to matches");
    }

    @Test
    void refusesTheSameColourInBothGames() {
        var text = header("192 FIDE_DOUBLESWISS\r")
                + player(1, "   2 w 1", "   2 w 1")
                + player(2, "   1 b 0", "   1 b 0");

        assertThatThrownBy(() -> TrfReader.read(text))
                .isInstanceOf(InvalidTrfException.class)
                .hasMessageContaining("same colour");
    }

    @Test
    void refusesAnOddNumberOfTrfRounds() {
        var text = ONE_MATCH.replace("142 6", "142 5");

        assertThatThrownBy(() -> TrfReader.read(text))
                .isInstanceOf(InvalidTrfException.class)
                .hasMessageContaining("two per match");
    }

    private static String header(String system) {
        return "012 Test\r142 6\r" + system;
    }

    private static String player(int startRank, String... games) {
        var line = new StringBuilder(
                String.format("001 %4d      %-33s %4d", startRank, "Player " + startRank, 2600 - startRank));
        while (line.length() < 91) {
            line.append(' ');
        }
        for (var game : games) {
            line.append(game).append("  ");
        }
        return line.toString().stripTrailing() + "\r";
    }
}
