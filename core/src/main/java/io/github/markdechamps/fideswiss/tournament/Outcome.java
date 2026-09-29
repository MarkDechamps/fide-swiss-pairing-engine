package io.github.markdechamps.fideswiss.tournament;

/** What happened on one board: a single game, or a match of several games. */
public sealed interface Outcome permits GameOutcome, MatchOutcome {

    /** Whether a game was actually played over the board, so that it counts for colours (GHR 3.4). */
    boolean isPlayed();

    /**
     * Whether the two sides count as having met (C.04.1 Art. 2, GHR 3.5): a game played, or a match that was
     * not forfeited.
     */
    default boolean isMeeting() {
        return isPlayed();
    }

    GameResult resultOf(Colour side);

    /** The same outcome with the sides' colours swapped: the same side still wins. */
    Outcome mirrored();
}
