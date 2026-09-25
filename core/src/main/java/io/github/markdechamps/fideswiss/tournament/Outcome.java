package io.github.markdechamps.fideswiss.tournament;

/** What happened on one board: a single game, or later a match of several games. */
public sealed interface Outcome permits GameOutcome {

    /** Whether a game was actually played over the board, so that it counts as a meeting and for colours. */
    boolean isPlayed();

    GameResult resultOf(Colour side);
}
