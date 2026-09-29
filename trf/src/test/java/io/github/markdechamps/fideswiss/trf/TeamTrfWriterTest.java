package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.Name;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Outcome;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.RequestedBye;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SecondaryScore;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** A team tournament written as TRF26 reads back as the same settings, teams and matches (round trip). */
class TeamTrfWriterTest {

    private static final GameOutcome[] RESULTS = {
        GameOutcome.WHITE_WINS,
        GameOutcome.DRAW,
        GameOutcome.BLACK_WINS,
        GameOutcome.DRAW,
        GameOutcome.WHITE_WINS_BY_FORFEIT,
        GameOutcome.BLACK_WINS_BY_FORFEIT,
        GameOutcome.DOUBLE_FORFEIT
    };

    @ParameterizedTest
    @EnumSource(ColourPreferenceType.class)
    void readsBackEveryColourPreferenceTypeWithItsScores(ColourPreferenceType type) {
        var settings = Profiles.teamSwiss(NumberOfRounds.of(4))
                .with(PairingSystems.swissTeam(type))
                .with(InitialColour.black())
                .with(TieBreakList.parse("MPvGP, EDET, EMGSB/C1"));

        assertRoundTrips(played(settings, 7, 4));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void readsBackGamePointsPrimaryAndTheSecondaryScoreLeftOut(boolean gamePointsPrimary) {
        var matches = MatchScoring.standard()
                .withBoards(3)
                .withPrimary(gamePointsPrimary ? PrimaryScore.GAME_POINTS : PrimaryScore.MATCH_POINTS)
                .with(SecondaryScore.NOT_USED);
        var settings = Profiles.teamSwiss(NumberOfRounds.of(3))
                .with(ScoringScheme.teams().with(matches));

        assertRoundTrips(played(settings, 6, 3));
    }

    @Test
    void readsBackBakuAndAStatedPairingAllocatedByeValue() {
        var scoring = ScoringScheme.teams().withPairingAllocatedBye(Points.of(2));
        var settings = Profiles.teamSwiss(NumberOfRounds.of(4)).with(scoring).with(Acceleration.baku());

        assertRoundTrips(played(settings, 9, 4));
    }

    @Test
    void readsBackTheOlympiadPairingRules() {
        var settings = Profiles.olympiad(NumberOfRounds.of(5)).with(TieBreakList.parse("MPvGP, EDE"));

        var read = assertRoundTrips(played(settings, 8, 4));

        assertThat(read.settings().pairingSystem().name()).startsWith("D.02 ");
    }

    @Test
    void writesTheTeamRecordsInTheirColumns() {
        var text =
                TrfWriter.write(played(Profiles.teamSwiss(NumberOfRounds.of(2)), 4, 2), TrfWriter.Options.named("x"));

        assertThat(text)
                .contains("192 FIDE_TEAM_TYPEA_MP_GP\r\n")
                .contains("352 WB\r\n")
                .contains("362 TW ");
        var team =
                text.lines().filter(line -> line.startsWith("310")).findFirst().orElseThrow();
        assertThat(PlayerRecord.columns(team, 5, 7).trim()).isEqualTo("1");
        assertThat(PlayerRecord.columns(team, 9, 40).trim()).isEqualTo("Team 1");
        assertThat(PlayerRecord.columns(team, 74, 77).trim()).isEqualTo("1");
        assertThat(PlayerRecord.columns(team, 79, 82).trim()).isEqualTo("2");
    }

    private static TrfTournament assertRoundTrips(Tournament written) {
        var text = TrfWriter.write(written, TrfWriter.Options.named("round trip"));

        var read = TrfReader.read(text);

        var expected = written.settings();
        var actual = read.settings();
        assertThat(actual.pairingSystem().name())
                .isEqualTo(expected.pairingSystem().name());
        assertThat(actual.pairingSystem().teamColourPreferences())
                .isEqualTo(expected.pairingSystem().teamColourPreferences());
        assertThat(actual.scoring().matches()).isEqualTo(expected.scoring().matches());
        assertThat(actual.pairingAllocatedByeValue()).isEqualTo(expected.pairingAllocatedByeValue());
        assertThat(actual.acceleration()).isEqualTo(expected.acceleration());
        assertThat(actual.initialColour()).isEqualTo(expected.initialColour());
        assertThat(actual.numberOfRounds()).isEqualTo(expected.numberOfRounds());
        assertThat(actual.tieBreakList()).hasToString(expected.tieBreakList().toString());
        assertThat(read.participants()).isEqualTo(written.participants());
        // A TRF does not number the boards of a round: they come back in team order, so compare the matches.
        assertThat(read.recordedRounds()).hasSameSizeAs(written.rounds());
        for (var index = 0; index < written.rounds().size(); index++) {
            assertThat(read.recordedRounds().get(index).byes())
                    .isEqualTo(written.rounds().get(index).byes());
            assertThat(matchesOf(read.recordedRounds().get(index)))
                    .isEqualTo(matchesOf(written.rounds().get(index)));
        }
        assertThat(TrfWriter.write(read.tournament(), TrfWriter.Options.named("round trip")))
                .isEqualTo(text);
        return read;
    }

    private static java.util.Set<String> matchesOf(io.github.markdechamps.fideswiss.tournament.Round round) {
        return round.boards().stream()
                .map(board -> board.white() + "-" + board.black() + " " + board.outcome())
                .collect(java.util.stream.Collectors.toSet());
    }

    /** Every round paired by the library; results cycle through wins, draws and forfeits, the first team skips round 2. */
    private static Tournament played(TournamentSettings settings, int teams, int boards) {
        var scoring = settings.scoring()
                .matches()
                .map(matches -> matches.withBoards(boards))
                .orElseThrow();
        var withBoards = settings.with(settings.scoring().with(scoring));
        var field = new ArrayList<Participant>();
        for (var number = 1; number <= teams; number++) {
            field.add(Participant.of(
                    ParticipantId.of(String.valueOf(number)),
                    Name.of("Team " + number),
                    Rating.of(2500 - 10 * number)));
        }
        var tournament = Tournament.of(withBoards, field);
        var rounds = settings.numberOfRounds().value();
        var cursor = 0;
        for (var round = 1; round <= rounds; round++) {
            if (round == 2) {
                tournament = tournament.requestBye(ParticipantId.of("1"), RoundNumber.of(2), RequestedBye.half());
            }
            var pairing = tournament.pairNextRound();
            var outcomes = new HashMap<BoardNumber, Outcome>();
            for (var board : pairing.boards()) {
                var games = new ArrayList<GameOutcome>();
                for (var index = 0; index < boards; index++) {
                    games.add(RESULTS[(cursor++ * 3 + index) % (round == 1 ? 4 : RESULTS.length)]);
                }
                outcomes.put(board.number(), MatchOutcome.ofGames(games));
            }
            tournament = tournament.withRound(pairing.completedWith(outcomes));
        }
        return tournament;
    }
}
