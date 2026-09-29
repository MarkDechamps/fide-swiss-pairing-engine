package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Builds recorded team tournaments from matches written like a team crosstable: {@code "1-2 1 1 0 ½"} is team 1
 * (with White on board 1) against team 2, team 1 scoring 1, 1, 0 and ½ on boards one to four; {@code +} and
 * {@code -} are a forfeit win and loss of a board for the first team; {@code "5 PAB"} is a bye.
 */
final class TeamTable {

    private Tournament tournament;

    private TeamTable(Tournament tournament) {
        this.tournament = tournament;
    }

    static TeamTable of(int teams, int rounds, int boards, PrimaryScore primary, String tieBreaks) {
        var matches = ScoringScheme.teams().withPrimaryScore(primary);
        var scoring = matches.with(matches.matches().orElseThrow().withBoards(boards));
        var settings =
                Profiles.teamSwiss(NumberOfRounds.of(rounds)).with(scoring).with(TieBreakList.parse(tieBreaks));
        return new TeamTable(Tournament.of(settings, TournamentMother.participants(teams)));
    }

    TeamTable round(String... entries) {
        var boards = new ArrayList<Board>();
        var byes = new HashMap<ParticipantId, Bye>();
        for (var entry : entries) {
            var parts = entry.trim().split("\\s+");
            if (parts[0].contains("-")) {
                var ids = parts[0].split("-");
                var games = new ArrayList<GameOutcome>();
                for (var index = 1; index < parts.length; index++) {
                    games.add(game(parts[index]));
                }
                boards.add(new Board(
                        BoardNumber.of(boards.size() + 1),
                        ParticipantId.of(ids[0]),
                        ParticipantId.of(ids[1]),
                        MatchOutcome.ofGames(games)));
            } else {
                byes.put(
                        ParticipantId.of(parts[0]),
                        Bye.valueOf(parts[1].equals("PAB") ? "PAIRING_ALLOCATED" : parts[1]));
            }
        }
        tournament = tournament.withRound(Round.of(tournament.nextRound(), boards, byes));
        return this;
    }

    Tournament tournament() {
        return tournament;
    }

    Standings standings() {
        return tournament.standings();
    }

    private static GameOutcome game(String code) {
        return switch (code) {
            case "1" -> GameOutcome.WHITE_WINS;
            case "0" -> GameOutcome.BLACK_WINS;
            case "½" -> GameOutcome.DRAW;
            case "+" -> GameOutcome.WHITE_WINS_BY_FORFEIT;
            case "-" -> GameOutcome.BLACK_WINS_BY_FORFEIT;
            default -> throw new IllegalArgumentException(code);
        };
    }
}
