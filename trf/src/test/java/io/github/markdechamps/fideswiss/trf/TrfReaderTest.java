package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Title;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TrfReaderTest {

    @Nested
    class GivenATrf26FileWithAHalfPointByeMarkedForTheNextRound {

        private final TrfTournament file = TrfReader.read(resource("half-point-bye-in-round-2.trf"));

        @Test
        void readsTheParticipantsFromThe001Records() {
            var first = file.participants().getFirst();

            assertThat(file.participants()).hasSize(5);
            assertThat(first.id()).isEqualTo(ParticipantId.of("1"));
            assertThat(first.name().value()).isEqualTo("Alpha, Anna");
            assertThat(first.title()).contains(Title.GM);
            assertThat(first.rating()).isEqualTo(Rating.of(2600));
            assertThat(file.participants().getLast().rating()).isEqualTo(Rating.unrated());
        }

        @Test
        void readsTheRecordedRoundFromTheRoundColumns() {
            var round = file.recordedRounds().getFirst();

            assertThat(file.recordedRounds()).hasSize(1);
            assertThat(round.boards())
                    .extracting(
                            board -> board.white().value() + "-" + board.black().value(), board -> board.outcome())
                    .containsExactlyInAnyOrder(
                            org.assertj.core.groups.Tuple.tuple("1-3", GameOutcome.WHITE_WINS),
                            org.assertj.core.groups.Tuple.tuple("4-2", GameOutcome.DRAW));
            assertThat(round.byes()).isEqualTo(Map.of(ParticipantId.of("5"), Bye.PAIRING_ALLOCATED));
        }

        @Test
        void takesTheRoundCountAndInitialColourFrom142And152() {
            assertThat(file.settings().numberOfRounds().value()).isEqualTo(5);
            assertThat(file.settings().initialColour().colour())
                    .isEqualTo(io.github.markdechamps.fideswiss.tournament.Colour.WHITE);
        }

        @Test
        void fallsBackToTheProfilesTieBreakListWithout202Or212() {
            assertThat(file.settings().tieBreakList().toString()).isEqualTo("BH/C1, BH, SB, DE");
        }

        @Test
        void pairsTheNextRoundWithoutTheParticipantMarkedAbsent() {
            var pairing = file.tournament().pairNextRound();

            // bbpPairings v6.0.0 replies "5 1 / 3 4" for this file.
            assertThat(boardsOf(pairing)).containsExactlyInAnyOrder("5-1", "3-4");
            assertThat(pairing.roundNumber()).isEqualTo(RoundNumber.of(2));
            assertThat(pairing.unpaired()).isEqualTo(Map.of(ParticipantId.of("2"), Bye.HALF_POINT));
        }
    }

    @Nested
    class GivenATieBreakRecord {

        @Test
        void takesTheListFrom202() {
            var file = TrfReader.read(resource("half-point-bye-in-round-2.trf") + "202 SB,BH/C1/P,DE\r");

            assertThat(file.settings().tieBreakList().toString()).isEqualTo("SB, BH/C1/P, DE");
        }

        @Test
        void takesTheListFrom212WithoutItsLeadingPts() {
            var file = TrfReader.read(resource("half-point-bye-in-round-2.trf") + "212 PTS,WIN,BH\r");

            assertThat(file.settings().tieBreakList().toString()).isEqualTo("WIN, BH");
        }
    }

    private static List<String> boardsOf(RoundPairing pairing) {
        return pairing.boards().stream()
                .map(board -> board.white().value() + "-" + board.black().value())
                .toList();
    }

    static String resource(String name) {
        try (var stream = TrfReaderTest.class.getResourceAsStream("/trf/" + name)) {
            return new String(Objects.requireNonNull(stream, name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
