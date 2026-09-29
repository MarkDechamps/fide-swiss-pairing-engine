package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ADR 0007: Double-Swiss match k is TRF rounds 2k−1 (game 1) and 2k (game 2) of every {@code 001} record. The two
 * columns must agree: the same opponent with reversed colours, or the same bye in both ("Byes apply only to
 * matches", C.04.5 Preface). Each game is read from its own pair of result codes, so ½-0, 0-0 and a single forfeited
 * game are native.
 */
final class MatchColumns {

    /** The matches recorded, and the absences marked for the next one. */
    record Matches(List<Round> rounds, Map<ParticipantId, Bye> absences) {}

    private final List<PlayerRecord> players;

    MatchColumns(List<PlayerRecord> players) {
        this.players = players;
    }

    Matches read() {
        var columns = players.stream()
                .mapToInt(player -> player.rounds().size())
                .max()
                .orElse(0);
        var rounds = new ArrayList<Round>();
        var absences = Map.<ParticipantId, Bye>of();
        var matches = (columns + 1) / 2;
        for (var match = 1; match <= matches; match++) {
            var gameOneMarksOnly = TrfTournament.isOnlyAbsenceMarks(players, 2 * match - 1);
            var gameTwoMarksOnly = TrfTournament.isOnlyAbsenceMarks(players, 2 * match);
            if (match == matches && gameOneMarksOnly && gameTwoMarksOnly) {
                absences = absenceMarks(match);
            } else if (gameTwoMarksOnly && !gameOneMarksOnly) {
                throw new InvalidTrfException("Double-Swiss match " + match + " is half recorded: TRF round "
                        + (2 * match) + " has no games (ADR 0007)");
            } else {
                rounds.add(matchOf(match));
            }
        }
        return new Matches(rounds, absences);
    }

    private Map<ParticipantId, Bye> absenceMarks(int match) {
        var absences = new HashMap<ParticipantId, Bye>();
        for (var player : players) {
            var cell = player.cellOf(2 * match - 1);
            if (cell.result() != ' ') {
                absences.put(player.participant().id(), TrfTournament.byeOf(cell));
            }
        }
        return absences;
    }

    private Round matchOf(int match) {
        var boards = new ArrayList<Board>();
        var byes = new HashMap<ParticipantId, Bye>();
        for (var player : players) {
            var gameOne = player.cellOf(2 * match - 1);
            var gameTwo = player.cellOf(2 * match);
            var id = player.participant().id();
            if (!gameOne.hasOpponent() && !gameTwo.hasOpponent()) {
                byes.put(id, byeOf(player, gameOne, gameTwo, match));
            } else if (gameOne.hasOpponent() != gameTwo.hasOpponent()
                    || !gameOne.opponent().equals(gameTwo.opponent())) {
                throw invalid(player, match, "has a different opponent, or a bye, in one of its two games");
            } else if (isGameOneWhite(player, gameOne, gameTwo, match)) {
                var opponent = ParticipantId.of(gameOne.opponent().orElseThrow());
                var opponentGameOne = TrfTournament.cellOfPlayer(players, opponent, 2 * match - 1);
                var opponentGameTwo = TrfTournament.cellOfPlayer(players, opponent, 2 * match);
                var games = List.of(
                        gameOf(gameOne.result(), opponentGameOne.result()),
                        gameOf(opponentGameTwo.result(), gameTwo.result()).mirrored());
                boards.add(new Board(BoardNumber.of(boards.size() + 1), id, opponent, MatchOutcome.ofGames(games)));
            }
        }
        return Round.of(RoundNumber.of(match), boards, byes);
    }

    private static Bye byeOf(PlayerRecord player, RoundCell gameOne, RoundCell gameTwo, int match) {
        var bye = TrfTournament.byeOf(gameOne);
        if (bye != TrfTournament.byeOf(gameTwo)) {
            throw invalid(player, match, "has a bye in one game only: byes apply only to matches (C.04.5 Preface)");
        }
        return bye;
    }

    /** Game 1's {@code w}, else game 2's {@code b}; without colours (a forfeit), the lower starting rank. */
    private static boolean isGameOneWhite(PlayerRecord player, RoundCell gameOne, RoundCell gameTwo, int match) {
        if (gameOne.colour().isPresent()
                && gameTwo.colour().isPresent()
                && gameOne.colour().equals(gameTwo.colour())) {
            throw invalid(player, match, "has the same colour in both games; they alternate (C.04.5 Preface)");
        }
        return gameOne.colour()
                .map(Colour.WHITE::equals)
                .or(() -> gameTwo.colour().map(Colour.BLACK::equals))
                .orElseGet(() ->
                        player.startRank() < Integer.parseInt(gameOne.opponent().orElseThrow()));
    }

    /** One game from its White's and its Black's result codes, the Preface's ½-0, 0-½ and 0-0 included. */
    static GameOutcome gameOf(char white, char black) {
        var whiteHalf = white == '=' || white == 'D';
        var whiteZero = white == '0' || white == 'L';
        var blackHalf = black == '=' || black == 'D';
        var blackZero = black == '0' || black == 'L';
        if (whiteHalf && blackZero) {
            return GameOutcome.WHITE_HALF_BLACK_ZERO;
        }
        if (whiteZero && blackHalf) {
            return GameOutcome.WHITE_ZERO_BLACK_HALF;
        }
        if (whiteZero && blackZero) {
            return GameOutcome.BOTH_ZERO;
        }
        return TrfTournament.outcomeOf(white, black);
    }

    private static InvalidTrfException invalid(PlayerRecord player, int match, String problem) {
        return new InvalidTrfException("Player " + player.startRank() + " in Double-Swiss match " + match
                + " (TRF rounds " + (2 * match - 1) + " and " + (2 * match) + ") " + problem);
    }
}
