package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.Name;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Outcome;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.RequestedBye;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Title;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrfWriterTest {

    @Nested
    class GivenATournamentWithOneRoundPlayed {

        private final Tournament tournament = played(settings(), GameOutcome.WHITE_WINS, GameOutcome.DRAW);

        @Test
        void writesTheTrf26HeaderRecordsWithCrlfLineEndings() {
            var text = TrfWriter.write(tournament, TrfWriter.Options.named("Club open"));

            assertThat(text).startsWith("012 Club open\r\n142 3\r\n152 W\r\n192 FIDE_DUTCH\r\n");
            assertThat(text).endsWith("\r\n").doesNotContain("\n\n").doesNotContain("162");
        }

        @Test
        void writesEachPlayerInTheFideColumns() {
            var first = playerLine(TrfWriter.write(tournament, TrfWriter.Options.named("Club open")), 1);

            // TRF26 001: start rank 5-8, title 11-13, name 15-47, rating 49-52, points 81-84, round 1 at 92-99.
            assertThat(PlayerRecord.columns(first, 1, 3)).isEqualTo("001");
            assertThat(PlayerRecord.columns(first, 5, 8)).isEqualTo("   1");
            assertThat(PlayerRecord.columns(first, 11, 13).trim()).isEqualTo("GM");
            assertThat(PlayerRecord.columns(first, 15, 47).trim()).isEqualTo("Player 1");
            assertThat(PlayerRecord.columns(first, 49, 52)).isEqualTo("2490");
            assertThat(PlayerRecord.columns(first, 81, 84)).isEqualTo(" 1.0");
            assertThat(PlayerRecord.columns(first, 92, 99)).isEqualTo("   3 w 1");
        }

        @Test
        void writesThePairingAllocatedByeAsAnUnpairedRound() {
            var fifth = playerLine(TrfWriter.write(tournament, TrfWriter.Options.named("Club open")), 5);

            assertThat(PlayerRecord.columns(fifth, 49, 52).trim()).isEmpty();
            assertThat(PlayerRecord.columns(fifth, 81, 84)).isEqualTo(" 1.0");
            assertThat(PlayerRecord.columns(fifth, 92, 99)).isEqualTo("0000 - U");
        }

        @Test
        void addsTheJaVaFoRoundCountAndInitialColourForOracleRuns() {
            var text = TrfWriter.write(
                    tournament, TrfWriter.Options.named("Club open").withJaVaFoLines());

            assertThat(text).contains("\r\nXXR 3\r\nXXC white1\r\n");
        }
    }

    @Nested
    class GivenAWrittenTournament {

        @Test
        void readsBackTheSameRoundsSettingsAndAbsences() {
            var scoring = new ScoringScheme(Points.of(3), Points.of(1), Points.ZERO, Optional.empty());
            var tournament = played(
                            settings().with(scoring), GameOutcome.WHITE_WINS_BY_FORFEIT, GameOutcome.DOUBLE_FORFEIT)
                    .requestBye(id(3), RoundNumber.of(2), RequestedBye.half());

            var read = TrfReader.read(TrfWriter.write(tournament, TrfWriter.Options.named("Round trip")));

            assertThat(read.recordedRounds()).isEqualTo(tournament.rounds());
            assertThat(read.settings().scoring()).isEqualTo(scoring);
            assertThat(read.settings().numberOfRounds()).isEqualTo(NumberOfRounds.of(3));
            assertThat(read.tournament().absencesInNextRound()).containsEntry(id(3), Bye.HALF_POINT);
            assertThat(read.tournament().pairNextRound().samePairingAs(tournament.pairNextRound()))
                    .isTrue();
        }
    }

    @Nested
    class GivenAnAcceleratedTournamentWithItsTieBreaks {

        private final Tournament tournament = played(
                Profiles.acceleratedOpen(NumberOfRounds.of(3)).with(TieBreakList.parse("BH/M1, KS, TPN")),
                GameOutcome.DRAW,
                GameOutcome.BLACK_WINS);

        @Test
        void writesTheTieBreakListAfterThePointsAndBakuInTheSystemCode() {
            var text = TrfWriter.write(tournament, TrfWriter.Options.named("Accelerated"));

            assertThat(text).contains("\r\n192 FIDE_DUTCH_BAKU\r\n212 PTS,BH/M1,KS,TPN\r\n");
        }

        @Test
        void readsBackTheAccelerationAndTheTieBreakList() {
            var read = TrfReader.read(TrfWriter.write(tournament, TrfWriter.Options.named("Accelerated")));

            assertThat(read.settings().acceleration()).isEqualTo(Acceleration.baku());
            assertThat(read.settings().tieBreakList()).isEqualTo(TieBreakList.parse("BH/M1, KS, TPN"));
        }
    }

    @Nested
    class GivenAnotherPairingSystem {

        @ParameterizedTest
        @CsvSource({"dubov, FIDE_DUBOV", "lim, FIDE_LIM", "dutch-2017, FIDE_DUTCH_2017"})
        void writesItsCodeAndReadsItBack(String name, String code) {
            var settings =
                    switch (name) {
                        case "dubov" -> settings().with(PairingSystems.dubov());
                        case "lim" -> settings().with(PairingSystems.lim());
                        default -> settings().with(SwissRulesEdition.PRE_2026);
                    };
            var tournament = played(settings, GameOutcome.DRAW, GameOutcome.WHITE_WINS);

            var text = TrfWriter.write(tournament, TrfWriter.Options.named("Other system"));

            assertThat(text).contains("\r\n192 " + code + "\r\n");
            assertThat(TrfReader.read(text).settings().pairingSystem().name())
                    .isEqualTo(tournament.settings().pairingSystem().name());
        }
    }

    @Nested
    class GivenAFinishedTournamentWithAWithdrawal {

        @Test
        void writesNoColumnBeyondTheLastRound() {
            var tournament = played(Profiles.individualSwiss(NumberOfRounds.of(1)), GameOutcome.DRAW, GameOutcome.DRAW)
                    .withdraw(id(2), RoundNumber.of(2));

            var text = TrfWriter.write(tournament, TrfWriter.Options.named("Finished"));

            assertThat(PlayerRecord.columns(playerLine(text, 2), 102, 109)).isEmpty();
        }
    }

    private static String playerLine(String text, int rank) {
        return List.of(text.split("\r\n")).stream()
                .filter(line -> line.startsWith("001"))
                .toList()
                .get(rank - 1);
    }

    private static TournamentSettings settings() {
        return Profiles.individualSwiss(NumberOfRounds.of(3));
    }

    private static Tournament played(TournamentSettings settings, Outcome... outcomes) {
        var participants =
                IntStream.rangeClosed(1, 5).mapToObj(TrfWriterTest::participant).toList();
        var tournament = Tournament.of(settings, participants);
        var byBoard = new HashMap<BoardNumber, Outcome>();
        for (var board = 0; board < outcomes.length; board++) {
            byBoard.put(BoardNumber.of(board + 1), outcomes[board]);
        }
        return tournament.withRound(tournament.pairNextRound().completedWith(byBoard));
    }

    private static Participant participant(int rank) {
        var participant = Participant.of(
                id(rank), Name.of("Player " + rank), rank == 5 ? Rating.unrated() : Rating.of(2500 - 10 * rank));
        return rank == 1 ? participant.withTitle(Title.GM) : participant;
    }

    private static ParticipantId id(int rank) {
        return ParticipantId.of(String.valueOf(rank));
    }
}
