package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

/**
 * How a team match is scored on top of its games: match points for a won, drawn and lost match (TRF 362), and
 * which of match points and game points is the primary score (C.04.6 1.2).
 */
public record MatchScoring(Points win, Points draw, Points loss, PrimaryScore primary) {

    public MatchScoring {
        Objects.requireNonNull(win, "win");
        Objects.requireNonNull(draw, "draw");
        Objects.requireNonNull(loss, "loss");
        Objects.requireNonNull(primary, "primary");
    }

    /** 2 / 1 / 0 match points, match points primary (C.04.6 1.2.2, TRF 362 defaults). */
    public static MatchScoring standard() {
        return new MatchScoring(Points.of(2), Points.of(1), Points.ZERO, PrimaryScore.MATCH_POINTS);
    }

    public MatchScoring withPrimary(PrimaryScore score) {
        return new MatchScoring(win, draw, loss, score);
    }

    public Points pointsFor(GameResult result) {
        return switch (result) {
            case WIN -> win;
            case DRAW -> draw;
            case LOSS -> loss;
        };
    }
}
