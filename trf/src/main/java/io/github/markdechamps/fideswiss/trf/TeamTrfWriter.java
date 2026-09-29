package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Writes a team tournament as TRF26, in the layout {@link TeamRounds} and {@link TeamSettingsRecords} read: the
 * header records ({@code 142}, {@code 152}, {@code 162}, {@code 192}, {@code 212}, {@code 352}, {@code 362}), one
 * {@code 310} per team in team-number order, and per team as many {@code 001} members as there are boards. The model
 * has no players: member {@code b} of team {@code t} is start rank {@code (t-1)*boards+b}, and plays board {@code b}
 * in every round, so the line-up is the {@code 310} order and no {@code 300} is needed. A team's colour is its
 * board-1 member's (C.04.6 1.6.1); the boards then alternate as the {@code 352} pattern {@code WBWB…} says. The
 * team's PAB or requested bye is the same cell on every member's line.
 */
final class TeamTrfWriter {

    private static final String LINE_END = "\r\n";
    private static final int ROUND_COLUMN = 92;
    private static final int ROUND_WIDTH = 10;

    private final Tournament tournament;
    private final Map<ParticipantId, Integer> teamNumbers = new HashMap<>();
    private final int boards;

    private TeamTrfWriter(Tournament tournament) {
        this.tournament = tournament;
        this.boards = tournament.settings().scoring().matches().orElseThrow().boards();
        var teams = tournament.participants();
        for (var index = 0; index < teams.size(); index++) {
            teamNumbers.put(teams.get(index).id(), index + 1);
        }
    }

    static String write(Tournament tournament, TrfWriter.Options options) {
        return new TeamTrfWriter(tournament).text(options);
    }

    private String text(TrfWriter.Options options) {
        var lines = new ArrayList<String>();
        var settings = tournament.settings();
        var teams = tournament.participants();
        var rated = teams.stream().filter(team -> team.rating().isRated()).count() * boards;
        lines.add("012 " + options.name());
        lines.add("062 " + teams.size() * boards);
        lines.add("072 " + rated);
        lines.add("082 " + teams.size());
        lines.add("142 " + settings.numberOfRounds().value());
        lines.add("152 " + (settings.initialColour().colour() == Colour.WHITE ? "W" : "B"));
        lines.add(scoringRecord());
        lines.add(
                "192 " + TeamSettingsRecords.codeOf(settings) + TrfWriter.accelerationSuffix(settings.acceleration()));
        if (!settings.tieBreakList().isEmpty()) {
            lines.add("212 " + TrfWriter.tieBreakRecord(settings.tieBreakList()));
        }
        lines.add(matchScoringRecord());
        pairingAllocatedByeRecord().ifPresent(lines::add);
        lines.add("352 " + pattern());
        var standings = tournament.standings();
        teams.forEach(team ->
                lines.add(teamRecord(team, standings.standing(team.id()).rank().value())));
        teams.forEach(team -> {
            for (var board = 1; board <= boards; board++) {
                lines.add(memberRecord(team, board));
            }
        });
        return String.join(LINE_END, lines) + LINE_END;
    }

    /**
     * {@code 162}: the game points and, per board, the PAB's (C.04.6 1.4: as a drawn match, unless the scoring says
     * otherwise), always stated so that another reader never has to guess it (Known Divergence KD-2).
     */
    private String scoringRecord() {
        var scoring = tournament.settings().scoring();
        var line = new StringBuilder("162");
        var column = 6;
        var values = new String[] {
            "W" + String.format("%4s", decimal(scoring.win())),
            "D" + String.format("%4s", decimal(scoring.draw())),
            "L" + String.format("%4s", decimal(scoring.loss())),
            "P" + String.format("%4s", pabPerBoard())
        };
        for (var value : values) {
            TrfWriter.put(line, column, value);
            column += 9;
        }
        return line.toString();
    }

    /** The PAB's game points per board: its game points spread over the boards (a win's or a draw's, by the system). */
    private String pabPerBoard() {
        var scoring = tournament.settings().scoring();
        var byeGamePoints = scoring.matches().orElseThrow().primary() == PrimaryScore.GAME_POINTS
                ? tournament.settings().pairingAllocatedByeValue()
                : tournament.settings().pairingSystem().secondaryPairingAllocatedByeValue(scoring);
        var perBoard =
                byeGamePoints.toBigDecimal().divide(java.math.BigDecimal.valueOf(boards), 4, RoundingMode.HALF_UP);
        return perBoard.setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /** {@code 362}: match points of a win, draw and loss; {@code 320} the PAB's when the scoring states it. */
    private String matchScoringRecord() {
        MatchScoring matches = tournament.settings().scoring().matches().orElseThrow();
        return "362 TW" + String.format("%4s", decimal(matches.win())) + "   TD"
                + String.format("%4s", decimal(matches.draw())) + "   TL"
                + String.format("%4s", decimal(matches.loss()));
    }

    /** {@code 320}: only when the scoring states the PAB's match points, which {@code 162}'s per-board value cannot. */
    private java.util.Optional<String> pairingAllocatedByeRecord() {
        var scoring = tournament.settings().scoring();
        var matches = scoring.matches().orElseThrow();
        if (matches.primary() != PrimaryScore.MATCH_POINTS
                || scoring.pairingAllocatedBye().isEmpty()) {
            return java.util.Optional.empty();
        }
        var line = new StringBuilder("320");
        TrfWriter.put(
                line,
                5,
                String.format("%4s", decimal(scoring.pairingAllocatedBye().get())));
        TrfWriter.put(line, 10, String.format("%4s", decimal(scoring.drawnMatchGamePoints())));
        return java.util.Optional.of(line.toString());
    }

    /** Board 1 has the team's colour, then the boards alternate (C.04.6 1.6.1). */
    private String pattern() {
        var pattern = new StringBuilder();
        for (var board = 0; board < boards; board++) {
            pattern.append(board % 2 == 0 ? 'W' : 'B');
        }
        return pattern.toString();
    }

    /** Columns 5–7 team number, 9–40 name, 48–53 strength, 55–60 match points, 62–67 game points, 69–71 rank. */
    private String teamRecord(Participant team, int rank) {
        var line = new StringBuilder();
        TrfWriter.put(line, 1, "310");
        TrfWriter.put(line, 5, String.format("%3d", teamNumbers.get(team.id())));
        TrfWriter.put(line, 9, TrfWriter.truncated(team.name().value(), 32));
        TrfWriter.put(line, 48, String.format("%6d", team.rating().valueOrZero()));
        TrfWriter.put(line, 55, String.format("%6s", decimal(total(team.id(), PrimaryScore.MATCH_POINTS))));
        TrfWriter.put(line, 62, String.format("%6s", decimal(total(team.id(), PrimaryScore.GAME_POINTS))));
        TrfWriter.put(line, 69, String.format("%3d", rank));
        for (var board = 1; board <= boards; board++) {
            TrfWriter.put(line, 74 + 5 * (board - 1), String.format("%4d", memberRank(team.id(), board)));
        }
        return line.toString();
    }

    private String memberRecord(Participant team, int board) {
        var line = new StringBuilder();
        TrfWriter.put(line, 1, "001");
        TrfWriter.put(line, 5, String.format("%4d", memberRank(team.id(), board)));
        TrfWriter.put(line, 15, TrfWriter.truncated(memberName(team, board), 33));
        if (team.rating().isRated()) {
            TrfWriter.put(line, 49, String.format("%4d", team.rating().valueOrZero()));
        }
        var column = ROUND_COLUMN;
        var gamePoints = Points.ZERO;
        for (var round : tournament.rounds()) {
            TrfWriter.put(line, column, cellOf(round, team.id(), board));
            gamePoints = gamePoints.plus(gamePointsOf(round, team.id(), board));
            column += ROUND_WIDTH;
        }
        TrfWriter.put(line, 81, String.format("%4s", decimal(gamePoints)));
        var absence = tournament.absencesInNextRound().get(team.id());
        if (absence != null && tournament.settings().numberOfRounds().includes(tournament.nextRound())) {
            TrfWriter.put(line, column, "0000 - " + TrfWriter.byeCode(absence));
        }
        return line.toString().stripTrailing();
    }

    private static String memberName(Participant team, int board) {
        return team.name().value() + " B" + board;
    }

    private int memberRank(ParticipantId team, int board) {
        return (teamNumbers.get(team) - 1) * boards + board;
    }

    private String cellOf(Round round, ParticipantId team, int board) {
        return round.boardOf(team)
                .map(match -> gameCell(match, team, board))
                .orElseGet(() -> "0000 - " + TrfWriter.byeCode(round.byeOf(team).orElse(Bye.ZERO_POINT)));
    }

    /** The colour of board {@code b} for a team: the pattern for the side with White on board 1, else the reverse. */
    private String gameCell(Board match, ParticipantId team, int board) {
        var side = match.colourOf(team);
        var game = gameOf(match, board);
        var colour = board % 2 == 1 ? side : side.opposite();
        var result = game.resultOf(side);
        var code = game.isPlayed() ? TrfWriter.playedCode(result) : (result == GameResult.WIN ? '+' : '-');
        return String.format(
                "%4d %c %c", memberRank(match.opponentOf(team), board), colour == Colour.WHITE ? 'w' : 'b', code);
    }

    private GameOutcome gameOf(Board match, int board) {
        if (!(match.outcome() instanceof MatchOutcome outcome)
                || outcome.games().size() != boards) {
            throw new IllegalArgumentException(
                    "Board " + match.number() + " is not a match of " + boards + " games: " + match.outcome());
        }
        return outcome.games().get(board - 1);
    }

    /** A member's points of a round: the game, or a bye's per-board points (the PAB's are the {@code 162} {@code P}). */
    private Points gamePointsOf(Round round, ParticipantId team, int board) {
        var scoring = tournament.settings().scoring();
        return round.boardOf(team)
                .map(match -> scoring.pointsFor(gameOf(match, board).resultOf(match.colourOf(team))))
                .orElseGet(() -> switch (round.byeOf(team).orElse(Bye.ZERO_POINT)) {
                    case PAIRING_ALLOCATED -> Points.of(pabPerBoard());
                    case FULL_POINT -> scoring.win();
                    case HALF_POINT -> scoring.draw();
                    case ZERO_POINT, WITHDRAWN, NOT_YET_ENTERED -> Points.ZERO;
                });
    }

    /** The team's match points or game points over the recorded rounds, byes included (C.04.6 1.4). */
    private Points total(ParticipantId team, PrimaryScore score) {
        var scoring = tournament.settings().scoring();
        var matches = scoring.matches().orElseThrow();
        var primary = scoring.primaryScore() == score;
        var pab = tournament.settings().pairingAllocatedByeValue();
        var secondaryPab = tournament.settings().pairingSystem().secondaryPairingAllocatedByeValue(scoring);
        var total = Points.ZERO;
        for (var round : tournament.rounds()) {
            var points = round.boardOf(team)
                    .map(match -> score == PrimaryScore.MATCH_POINTS
                            ? scoring.matchPointsFor(match.outcome(), match.colourOf(team))
                            : scoring.gamePointsFor(match.outcome(), match.colourOf(team)))
                    .orElseGet(() -> {
                        var bye = round.byeOf(team).orElse(Bye.ZERO_POINT);
                        return primary ? scoring.pointsFor(bye, pab) : scoring.secondaryPointsFor(bye, secondaryPab);
                    });
            total = total.plus(points);
        }
        return total;
    }

    private static String decimal(Points points) {
        return points.toBigDecimal().setScale(1, RoundingMode.HALF_UP).toPlainString();
    }
}
