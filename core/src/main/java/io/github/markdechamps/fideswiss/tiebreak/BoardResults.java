package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakContribution;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The tie-breaks specific for team knock-outs (C.07 art. 12), which read a team's game points board by board over
 * all its matches, whoever played the board: Board Count (12.1), Top Board Results (12.2) and Bottom Board
 * Elimination (12.3). A forfeited game is a standard win or loss and a pairing-allocated bye gives every board a
 * win's points.
 *
 * <p>TBR and BBE reapply themselves board by board until the tie breaks, which is a lexicographic comparison of the
 * boards' results; the value packs them as {@code board 1 + board 2 / 10000 + ...} so that a plain comparison of
 * values does the same.
 */
final class BoardResults implements TieBreak {

    enum Rule {
        BOARD_COUNT("C.07 12.1"),
        TOP_BOARD_RESULTS("C.07 12.2"),
        BOTTOM_BOARD_ELIMINATION("C.07 12.3");

        private final String article;

        Rule(String article) {
            this.article = article;
        }
    }

    private static final BigDecimal STEP = new BigDecimal("0.0001");

    private final TieBreakCode code;
    private final Rule rule;

    BoardResults(TieBreakCode code, Rule rule) {
        this.code = code;
        this.rule = rule;
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public Map<ParticipantId, TieBreakValue> valuesFor(List<ParticipantId> group, TieBreakContext context) {
        var values = new LinkedHashMap<ParticipantId, TieBreakValue>();
        var comparable = rule != Rule.BOARD_COUNT || haveSameGamePoints(group, context);
        for (var team : group) {
            var perBoard = perBoard(team, context);
            var contributions = new ArrayList<TieBreakContribution>();
            for (var board = 0; board < perBoard.size(); board++) {
                contributions.add(new TieBreakContribution(
                        Optional.empty(), "board " + (board + 1), perBoard.get(board), false, List.of(rule.article)));
            }
            values.put(
                    team,
                    new TieBreakValue(
                            code,
                            comparable ? Optional.of(valueOf(perBoard)) : Optional.empty(),
                            rule != Rule.BOARD_COUNT,
                            contributions,
                            rule == Rule.BOARD_COUNT && !comparable
                                    ? rule.article + ": not usable, the tied teams scored different game points"
                                    : rule.article));
        }
        return values;
    }

    /** 12.1: the lower the sum of board number times game points, the higher the team ranks. */
    private static boolean haveSameGamePoints(List<ParticipantId> group, TieBreakContext context) {
        var gamePoints = context.in(io.github.markdechamps.fideswiss.tournament.PrimaryScore.GAME_POINTS);
        return group.stream()
                        .map(team -> gamePoints.of(team).score())
                        .distinct()
                        .count()
                <= 1;
    }

    private BigDecimal valueOf(List<BigDecimal> perBoard) {
        return switch (rule) {
            case BOARD_COUNT -> {
                var sum = BigDecimal.ZERO;
                for (var board = 0; board < perBoard.size(); board++) {
                    sum = sum.add(perBoard.get(board).multiply(BigDecimal.valueOf(board + 1L)));
                }
                yield sum;
            }
            case TOP_BOARD_RESULTS -> packed(perBoard);
            case BOTTOM_BOARD_ELIMINATION -> {
                var eliminated = new ArrayList<BigDecimal>();
                for (var last = perBoard.size() - 1; last >= 1; last--) {
                    eliminated.add(perBoard.subList(0, last).stream().reduce(BigDecimal.ZERO, BigDecimal::add));
                }
                yield packed(eliminated);
            }
        };
    }

    private static BigDecimal packed(List<BigDecimal> steps) {
        var value = BigDecimal.ZERO;
        var weight = BigDecimal.ONE;
        for (var step : steps) {
            value = value.add(step.multiply(weight));
            weight = weight.multiply(STEP);
        }
        return value.stripTrailingZeros().scale() < 0 ? value.setScale(0) : value.stripTrailingZeros();
    }

    /** The team's game points on each board, summed over its rounds. */
    private static List<BigDecimal> perBoard(ParticipantId team, TieBreakContext context) {
        var totals = new ArrayList<BigDecimal>();
        for (var round : context.boardPointsOf(team)) {
            for (var board = 0; board < round.size(); board++) {
                while (totals.size() <= board) {
                    totals.add(BigDecimal.ZERO);
                }
                totals.set(board, totals.get(board).add(round.get(board).toBigDecimal()));
            }
        }
        return totals.isEmpty() ? List.of(Points.ZERO.toBigDecimal()) : totals;
    }
}
