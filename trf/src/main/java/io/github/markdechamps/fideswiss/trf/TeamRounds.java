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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The matches of a team file (TRF26): each round's boards come from the members' {@code 001} blocks in line-up
 * order ({@code 310}, or a {@code 300} for that round), a match that no member played from {@code 330}, and the
 * team PAB from the members' {@code U} blocks or {@code 320}. A team's colour is its board-1 player's (C.04.6
 * 1.6.1); when that board has none, another board's colour and the {@code 352} pattern give it.
 */
final class TeamRounds {

    private final List<TeamRecord> teams;
    private final Map<Integer, PlayerRecord> players;
    private final Map<Integer, TeamRecord> teamOfPlayer;
    private final Map<String, List<String>> records;
    private final String boardColours;
    private final int boards;

    TeamRounds(List<TeamRecord> teams, List<PlayerRecord> players, Map<String, List<String>> records) {
        this.teams = List.copyOf(teams);
        this.players = players.stream().collect(Collectors.toMap(PlayerRecord::startRank, Function.identity()));
        this.teamOfPlayer = new HashMap<>();
        teams.forEach(team -> team.members().forEach(member -> teamOfPlayer.put(member, team)));
        this.records = records;
        this.boardColours = records.getOrDefault("352", List.of()).stream()
                .map(TrfReader::valueOf)
                .map(String::toUpperCase)
                .findFirst()
                .orElse("");
        this.boards = boardColours.isEmpty() ? longestLineUp() : boardColours.length();
    }

    /** The boards of a match: the {@code 352} pattern's length, else the longest line-up of the file. */
    int boards() {
        return boards;
    }

    /** The round blocks the members' records have, the last one included. */
    int columns() {
        return players.values().stream()
                .mapToInt(player -> player.rounds().size())
                .max()
                .orElse(0);
    }

    /** Whether no member is paired or given the PAB in the column: it only marks next-round absences. */
    boolean isOnlyAbsenceMarks(int column) {
        return players.values().stream()
                        .map(player -> player.cellOf(column))
                        .noneMatch(cell -> cell.hasOpponent() || cell.isPairingAllocatedBye())
                && pairingAllocatedByeOf(column).isEmpty()
                && forfeitedMatches(column).isEmpty();
    }

    Map<ParticipantId, Bye> absenceMarks(int column) {
        var absences = new HashMap<ParticipantId, Bye>();
        for (var team : teams) {
            team.members().stream()
                    .map(member -> cellOf(member, column))
                    .filter(cell -> cell.result() != ' ')
                    .findFirst()
                    .ifPresent(cell -> absences.put(team.id(), TrfTournament.byeOf(cell)));
        }
        return absences;
    }

    Round roundOf(int round) {
        var boardsOfRound = new ArrayList<Board>();
        var byes = new HashMap<ParticipantId, Bye>();
        var handled = new HashSet<Integer>();
        for (var team : teams) {
            if (handled.contains(team.number())) {
                continue;
            }
            var lineUp = lineUpOf(team, round);
            if (lineUp.isEmpty()) {
                continue;
            }
            var opponent = opponentOf(team, lineUp, round);
            handled.add(team.number());
            handled.add(opponent.number());
            var teamIsWhite = hasWhiteOnBoardOne(team, lineUp, opponent, round);
            var white = teamIsWhite ? team : opponent;
            var black = teamIsWhite ? opponent : team;
            boardsOfRound.add(new Board(
                    BoardNumber.of(boardsOfRound.size() + 1),
                    white.id(),
                    black.id(),
                    matchOutcome(teamIsWhite ? lineUp : lineUpOf(opponent, round), round)));
        }
        for (var forfeit : forfeitedMatches(round)) {
            if (handled.add(forfeit.white()) && handled.add(forfeit.black())) {
                boardsOfRound.add(new Board(
                        BoardNumber.of(boardsOfRound.size() + 1),
                        ParticipantId.of(String.valueOf(forfeit.white())),
                        ParticipantId.of(String.valueOf(forfeit.black())),
                        MatchOutcome.ofGames(Collections.nCopies(boards, forfeit.outcome()))));
            }
        }
        for (var team : teams) {
            if (!handled.contains(team.number())) {
                byes.put(team.id(), byeOf(team, round));
            }
        }
        return Round.of(RoundNumber.of(round), boardsOfRound, byes);
    }

    /** The members on the boards, in board order: a {@code 300} line-up, else the {@code 310} order. */
    private List<Integer> lineUpOf(TeamRecord team, int round) {
        var declared = records.getOrDefault("300", List.of()).stream()
                .filter(line -> number(PlayerRecord.columns(line, 5, 7)) == round
                        && number(PlayerRecord.columns(line, 9, 11)) == team.number())
                .findFirst();
        var order = declared.map(TeamRounds::playersOf300).orElse(team.members());
        return order.stream()
                .filter(member -> cellOf(member, round).hasOpponent())
                .toList();
    }

    private static List<Integer> playersOf300(String line) {
        var members = new ArrayList<Integer>();
        for (var start = 17; start <= line.length(); start += 5) {
            var field = PlayerRecord.columns(line, start, start + 3).trim();
            if (!field.isEmpty() && Integer.parseInt(field) > 0) {
                members.add(Integer.parseInt(field));
            }
        }
        return members;
    }

    private TeamRecord opponentOf(TeamRecord team, List<Integer> lineUp, int round) {
        var opponentPlayer =
                Integer.parseInt(cellOf(lineUp.getFirst(), round).opponent().orElseThrow());
        var opponent = teamOfPlayer.get(opponentPlayer);
        if (opponent == null) {
            throw new InvalidTrfException("Round " + round + ": player " + opponentPlayer + " belongs to no 310 team");
        }
        return opponent;
    }

    /** 1.6.1: the first board with a colour, read back to board 1 through the {@code 352} pattern. */
    private boolean hasWhiteOnBoardOne(TeamRecord team, List<Integer> lineUp, TeamRecord opponent, int round) {
        return boardOneColour(lineUp, round)
                .or(() -> boardOneColour(lineUpOf(opponent, round), round).map(Colour::opposite))
                .map(Colour.WHITE::equals)
                .orElse(team.number() < opponent.number());
    }

    private Optional<Colour> boardOneColour(List<Integer> lineUp, int round) {
        for (var board = 0; board < lineUp.size(); board++) {
            var colour = cellOf(lineUp.get(board), round).colour();
            if (colour.isPresent()) {
                return Optional.of(
                        patternColour(board) == patternColour(0)
                                ? colour.get()
                                : colour.get().opposite());
            }
        }
        return Optional.empty();
    }

    /** The {@code 352} letter of a board; without the record, colours alternate from board 1. */
    private char patternColour(int board) {
        if (board < boardColours.length()) {
            return boardColours.charAt(board);
        }
        return board % 2 == 0 ? 'W' : 'B';
    }

    /** Each game from the side of the team with White on board 1, as MatchOutcome sees it. */
    private MatchOutcome matchOutcome(List<Integer> whiteLineUp, int round) {
        var games = new ArrayList<GameOutcome>();
        for (var member : whiteLineUp) {
            var cell = cellOf(member, round);
            var opponentCell = cellOf(Integer.parseInt(cell.opponent().orElseThrow()), round);
            games.add(TrfTournament.outcomeOf(cell.result(), opponentCell.result()));
        }
        return MatchOutcome.ofGames(games);
    }

    /** A team without a match: the PAB of {@code 320} or of its members' blocks, else what they mark. */
    private Bye byeOf(TeamRecord team, int round) {
        if (pairingAllocatedByeOf(round).filter(bye -> bye == team.number()).isPresent()) {
            return Bye.PAIRING_ALLOCATED;
        }
        return team.members().stream()
                .map(member -> cellOf(member, round))
                .filter(cell -> cell.result() != ' ')
                .findFirst()
                .map(TrfTournament::byeOf)
                .orElse(Bye.ZERO_POINT);
    }

    /** {@code 320 MMMM GGGG TTT TTT …}: the PAB team of round r at columns 15+4(r−1) to 17+4(r−1). */
    private Optional<Integer> pairingAllocatedByeOf(int round) {
        return records.getOrDefault("320", List.of()).stream()
                .map(line -> PlayerRecord.columns(line, 15 + 4 * (round - 1), 17 + 4 * (round - 1))
                        .trim())
                .filter(field -> !field.isEmpty() && Integer.parseInt(field) > 0)
                .map(Integer::parseInt)
                .findFirst();
    }

    private record ForfeitedMatch(int white, int black, GameOutcome outcome) {}

    /** {@code 330 TT RRR WWW BBB}: {@code +-} White won by forfeit, {@code -+} Black did, {@code --} neither. */
    private List<ForfeitedMatch> forfeitedMatches(int round) {
        return records.getOrDefault("330", List.of()).stream()
                .filter(line -> number(PlayerRecord.columns(line, 8, 10)) == round)
                .map(line -> new ForfeitedMatch(
                        number(PlayerRecord.columns(line, 12, 14)),
                        number(PlayerRecord.columns(line, 16, 18)),
                        switch (PlayerRecord.columns(line, 5, 6)) {
                            case "+-" -> GameOutcome.WHITE_WINS_BY_FORFEIT;
                            case "-+" -> GameOutcome.BLACK_WINS_BY_FORFEIT;
                            default -> GameOutcome.DOUBLE_FORFEIT;
                        }))
                .toList();
    }

    private RoundCell cellOf(int player, int round) {
        var record = players.get(player);
        if (record == null) {
            throw new InvalidTrfException("A team lists player " + player + ", who has no 001 record");
        }
        return record.cellOf(round);
    }

    private int longestLineUp() {
        var longest = 0;
        for (var round = 1; round <= columns(); round++) {
            for (var team : teams) {
                longest = Math.max(longest, lineUpOf(team, round).size());
            }
        }
        return longest == 0 ? 4 : longest;
    }

    private static int number(String field) {
        var trimmed = field.trim();
        return trimmed.isEmpty() ? 0 : Integer.parseInt(trimmed);
    }
}
