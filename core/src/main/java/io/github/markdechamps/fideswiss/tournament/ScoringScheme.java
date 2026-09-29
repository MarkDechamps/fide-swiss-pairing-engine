package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;
import java.util.Optional;

/**
 * The points each result is worth (C.04.1 Art. 3–4 are written in terms of these values, so they are
 * configuration). {@code win}, {@code draw} and {@code loss} score a game; team competitions add
 * {@link MatchScoring} for the match as a whole. The Pairing-Allocated Bye's value is the same for every PAB of
 * an event; left unset, it is whatever the Pairing System defines.
 */
public record ScoringScheme(
        Points win, Points draw, Points loss, Optional<Points> pairingAllocatedBye, Optional<MatchScoring> matches) {

    public ScoringScheme {
        Objects.requireNonNull(win, "win");
        Objects.requireNonNull(draw, "draw");
        Objects.requireNonNull(loss, "loss");
        Objects.requireNonNull(pairingAllocatedBye, "pairingAllocatedBye");
        Objects.requireNonNull(matches, "matches");
    }

    public ScoringScheme(Points win, Points draw, Points loss, Optional<Points> pairingAllocatedBye) {
        this(win, draw, loss, pairingAllocatedBye, Optional.empty());
    }

    /** 1 / ½ / 0, with the PAB as the system defines. */
    public static ScoringScheme standard() {
        return new ScoringScheme(Points.of(1), Points.of("0.5"), Points.ZERO, Optional.empty());
    }

    /** Game points 1 / ½ / 0 per board and match points 2 / 1 / 0, match points primary (C.04.6 1.2.2). */
    public static ScoringScheme teams() {
        return standard().with(MatchScoring.standard());
    }

    /**
     * Double-Swiss (C.04.5 Preface): 1 / ½ / 0 per game, a match of two games scored by its game points, and a bye
     * worth the points of a match (a full-point bye two games won, a half-point bye two drawn).
     */
    public static ScoringScheme doubleSwiss() {
        return standard()
                .with(MatchScoring.standard()
                        .withPrimary(PrimaryScore.GAME_POINTS)
                        .withBoards(2));
    }

    public ScoringScheme withPairingAllocatedBye(Points value) {
        return new ScoringScheme(win, draw, loss, Optional.of(value), matches);
    }

    public ScoringScheme withoutPairingAllocatedBye() {
        return new ScoringScheme(win, draw, loss, Optional.empty(), matches);
    }

    public ScoringScheme with(MatchScoring scoring) {
        return new ScoringScheme(win, draw, loss, pairingAllocatedBye, Optional.of(scoring));
    }

    /** Only for a scheme with match scoring. */
    public ScoringScheme withPrimaryScore(PrimaryScore primary) {
        return with(matchScoring().withPrimary(primary));
    }

    /** The primary score: match points, unless game points are primary or the event scores games only. */
    public PrimaryScore primaryScore() {
        return matches.map(MatchScoring::primary).orElse(PrimaryScore.GAME_POINTS);
    }

    /** What a win is worth in the primary score: a match win's match points, else a game win. */
    public Points primaryWin() {
        return inMatchPoints() ? matchScoring().win() : win;
    }

    public Points primaryDraw() {
        return inMatchPoints() ? matchScoring().draw() : draw;
    }

    public Points primaryLoss() {
        return inMatchPoints() ? matchScoring().loss() : loss;
    }

    private boolean inMatchPoints() {
        return matches.isPresent() && primaryScore() == PrimaryScore.MATCH_POINTS;
    }

    public Points pointsFor(GameResult result) {
        return switch (result) {
            case WIN -> win;
            case DRAW -> draw;
            case LOSS -> loss;
        };
    }

    /** What the outcome scores for one side, in the primary score. */
    public Points pointsFor(Outcome outcome, Colour side) {
        return switch (outcome) {
            case GameOutcome game -> pointsFor(game.resultOf(side));
            case MatchOutcome match ->
                primaryScore() == PrimaryScore.MATCH_POINTS
                        ? matchScoring().pointsFor(match.resultOf(side))
                        : gamePointsFor(match, side);
        };
    }

    /** The game points of one side: its result in every game added up. */
    public Points gamePointsFor(Outcome outcome, Colour side) {
        return switch (outcome) {
            case GameOutcome game -> pointsFor(game.resultOf(side));
            case MatchOutcome match -> {
                var total = Points.ZERO;
                for (var game : match.games()) {
                    total = total.plus(pointsFor(game.resultOf(side)));
                }
                yield total;
            }
        };
    }

    /** The match points of one side; a plain game scores as a match of one board. */
    public Points matchPointsFor(Outcome outcome, Colour side) {
        return matches.map(scoring -> scoring.pointsFor(outcome.resultOf(side)))
                .orElseGet(() -> pointsFor(outcome, side));
    }

    /** What the outcome scores for one side in the secondary score: the other of match and game points. */
    public Points secondaryPointsFor(Outcome outcome, Colour side) {
        return primaryScore() == PrimaryScore.MATCH_POINTS
                ? gamePointsFor(outcome, side)
                : matchScoring().pointsFor(outcome.resultOf(side));
    }

    /** What a round without a game is worth, given the PAB value the system resolved. */
    public Points pointsFor(Bye bye, Points pairingAllocatedByeValue) {
        return pointsFor(bye, pairingAllocatedByeValue, primaryScore());
    }

    /** What a round without a game is worth in the secondary score, given the PAB's secondary value. */
    public Points secondaryPointsFor(Bye bye, Points pairingAllocatedByeValue) {
        return pointsFor(bye, pairingAllocatedByeValue, primaryScore().other());
    }

    /** The game points of a drawn match: every board drawn (C.04.6 1.4, a team PAB's game points). */
    public Points drawnMatchGamePoints() {
        return draw.times(matches.map(MatchScoring::boards).orElse(1));
    }

    private Points pointsFor(Bye bye, Points pairingAllocatedByeValue, PrimaryScore score) {
        var inMatchPoints = score == PrimaryScore.MATCH_POINTS && matches.isPresent();
        var boards = matches.map(MatchScoring::boards).orElse(1);
        return switch (bye) {
            case PAIRING_ALLOCATED -> pairingAllocatedByeValue;
            case FULL_POINT -> inMatchPoints ? matchScoring().win() : win.times(boards);
            case HALF_POINT -> inMatchPoints ? matchScoring().draw() : draw.times(boards);
            case ZERO_POINT, WITHDRAWN, NOT_YET_ENTERED -> Points.ZERO;
        };
    }

    private MatchScoring matchScoring() {
        return matches.orElseThrow(() -> new IllegalStateException("This scheme scores games only"));
    }
}
