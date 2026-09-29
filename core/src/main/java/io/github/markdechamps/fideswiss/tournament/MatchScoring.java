package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

/**
 * How a match is scored on top of its games: match points for a won, drawn and lost match (TRF 362), which of
 * match points and game points is the primary score, whether the other one is used for colours (C.04.6 1.2), and
 * how many games a match has (TRF 352), which a bye's game points need.
 */
public record MatchScoring(
        Points win, Points draw, Points loss, PrimaryScore primary, SecondaryScore secondary, int boards) {

    public MatchScoring {
        Objects.requireNonNull(win, "win");
        Objects.requireNonNull(draw, "draw");
        Objects.requireNonNull(loss, "loss");
        Objects.requireNonNull(primary, "primary");
        Objects.requireNonNull(secondary, "secondary");
        if (boards < 1) {
            throw new IllegalArgumentException("A match has at least one board, not " + boards);
        }
    }

    public MatchScoring(Points win, Points draw, Points loss, PrimaryScore primary) {
        this(win, draw, loss, primary, SecondaryScore.USED_FOR_COLOUR, 4);
    }

    /** 2 / 1 / 0 match points over four boards, match points primary, game points for colours (C.04.6 1.2.2). */
    public static MatchScoring standard() {
        return new MatchScoring(Points.of(2), Points.of(1), Points.ZERO, PrimaryScore.MATCH_POINTS);
    }

    public MatchScoring withPrimary(PrimaryScore score) {
        return new MatchScoring(win, draw, loss, score, secondary, boards);
    }

    public MatchScoring with(SecondaryScore use) {
        return new MatchScoring(win, draw, loss, primary, use, boards);
    }

    public MatchScoring withBoards(int count) {
        return new MatchScoring(win, draw, loss, primary, secondary, count);
    }

    public Points pointsFor(GameResult result) {
        return switch (result) {
            case WIN -> win;
            case DRAW -> draw;
            case LOSS -> loss;
        };
    }
}
