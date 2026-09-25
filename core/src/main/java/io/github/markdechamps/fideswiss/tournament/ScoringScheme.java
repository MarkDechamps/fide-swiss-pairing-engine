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

    public ScoringScheme withPairingAllocatedBye(Points value) {
        return new ScoringScheme(win, draw, loss, Optional.of(value), matches);
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

    /** What a round without a game is worth, given the PAB value the system resolved. */
    public Points pointsFor(Bye bye, Points pairingAllocatedByeValue) {
        return switch (bye) {
            case PAIRING_ALLOCATED -> pairingAllocatedByeValue;
            case FULL_POINT ->
                primaryScore() == PrimaryScore.MATCH_POINTS ? matchScoring().win() : win;
            case HALF_POINT ->
                primaryScore() == PrimaryScore.MATCH_POINTS ? matchScoring().draw() : draw;
            case ZERO_POINT, WITHDRAWN, NOT_YET_ENTERED -> Points.ZERO;
        };
    }

    private MatchScoring matchScoring() {
        return matches.orElseThrow(() -> new IllegalStateException("This scheme scores games only"));
    }
}
