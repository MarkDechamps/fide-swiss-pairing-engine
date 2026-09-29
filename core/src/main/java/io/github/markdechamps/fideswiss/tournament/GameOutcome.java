package io.github.markdechamps.fideswiss.tournament;

/**
 * The outcome of one game. An adjourned game counts as a draw for pairing purposes (GHR 3.1). The odd played
 * results ½-0, 0-½ and 0-0 occur in Double-Swiss matches (C.04.5 Preface).
 */
public enum GameOutcome implements Outcome {
    WHITE_WINS(true, GameResult.WIN, GameResult.LOSS),
    DRAW(true, GameResult.DRAW, GameResult.DRAW),
    BLACK_WINS(true, GameResult.LOSS, GameResult.WIN),
    ADJOURNED(true, GameResult.DRAW, GameResult.DRAW),
    WHITE_HALF_BLACK_ZERO(true, GameResult.DRAW, GameResult.LOSS),
    WHITE_ZERO_BLACK_HALF(true, GameResult.LOSS, GameResult.DRAW),
    BOTH_ZERO(true, GameResult.LOSS, GameResult.LOSS),
    WHITE_WINS_BY_FORFEIT(false, GameResult.WIN, GameResult.LOSS),
    BLACK_WINS_BY_FORFEIT(false, GameResult.LOSS, GameResult.WIN),
    DOUBLE_FORFEIT(false, GameResult.LOSS, GameResult.LOSS);

    private final boolean played;
    private final GameResult white;
    private final GameResult black;

    GameOutcome(boolean played, GameResult white, GameResult black) {
        this.played = played;
        this.white = white;
        this.black = black;
    }

    @Override
    public boolean isPlayed() {
        return played;
    }

    @Override
    public GameResult resultOf(Colour side) {
        return side == Colour.WHITE ? white : black;
    }

    /** Whether the side lost this game by forfeit (a double forfeit included). */
    public boolean isForfeitedBy(Colour side) {
        return !played && resultOf(side) == GameResult.LOSS;
    }

    @Override
    public GameOutcome mirrored() {
        return switch (this) {
            case WHITE_WINS -> BLACK_WINS;
            case BLACK_WINS -> WHITE_WINS;
            case WHITE_HALF_BLACK_ZERO -> WHITE_ZERO_BLACK_HALF;
            case WHITE_ZERO_BLACK_HALF -> WHITE_HALF_BLACK_ZERO;
            case WHITE_WINS_BY_FORFEIT -> BLACK_WINS_BY_FORFEIT;
            case BLACK_WINS_BY_FORFEIT -> WHITE_WINS_BY_FORFEIT;
            case DRAW, ADJOURNED, BOTH_ZERO, DOUBLE_FORFEIT -> this;
        };
    }
}
