package io.github.markdechamps.fideswiss.tournament;

import java.util.List;

/**
 * The outcome of a Match: the boards of two teams, or the two games of a Double-Swiss pairing. Each game's
 * outcome is seen from the sides of the board: WHITE is the participant with White on board 1 (in game 1),
 * whatever colour it had in that particular game.
 */
public record MatchOutcome(List<GameOutcome> games) implements Outcome {

    private static final Points WIN = Points.of(2);
    private static final Points DRAW = Points.of(1);

    public MatchOutcome {
        games = List.copyOf(games);
        if (games.isEmpty()) {
            throw new IllegalArgumentException("A match has at least one game");
        }
    }

    public static MatchOutcome ofGames(List<GameOutcome> games) {
        return new MatchOutcome(games);
    }

    /** At least one game was played over the board, so the match gives a colour (C.04.6 1.6.1, C.04.5 1.6). */
    @Override
    public boolean isPlayed() {
        return games.stream().anyMatch(GameOutcome::isPlayed);
    }

    /**
     * A match is forfeited only when a side forfeits every game (C.04.5 Preface, Swiss Team rulings A3); any other
     * match counts as a meeting, even one where each side forfeited a game.
     */
    @Override
    public boolean isMeeting() {
        return !isForfeitedBy(Colour.WHITE) && !isForfeitedBy(Colour.BLACK);
    }

    public boolean isForfeitedBy(Colour side) {
        return games.stream().allMatch(game -> game.isForfeitedBy(side));
    }

    /** The match result, by the game points of each side (at 1 / ½ / 0). */
    @Override
    public GameResult resultOf(Colour side) {
        var own = halfPointsOf(side);
        var other = halfPointsOf(side.opposite());
        if (own.isGreaterThan(other)) {
            return GameResult.WIN;
        }
        return own.equals(other) ? GameResult.DRAW : GameResult.LOSS;
    }

    @Override
    public MatchOutcome mirrored() {
        return new MatchOutcome(games.stream().map(GameOutcome::mirrored).toList());
    }

    private Points halfPointsOf(Colour side) {
        var total = Points.ZERO;
        for (var game : games) {
            total = total.plus(
                    switch (game.resultOf(side)) {
                        case WIN -> WIN;
                        case DRAW -> DRAW;
                        case LOSS -> Points.ZERO;
                    });
        }
        return total;
    }
}
