package io.github.markdechamps.fideswiss.tournament;

/** The outcome of one game. An adjourned game counts as a draw for pairing purposes (GHR 3.1). */
public enum GameOutcome implements Outcome {
    WHITE_WINS(true, GameResult.WIN, GameResult.LOSS),
    DRAW(true, GameResult.DRAW, GameResult.DRAW),
    BLACK_WINS(true, GameResult.LOSS, GameResult.WIN),
    ADJOURNED(true, GameResult.DRAW, GameResult.DRAW),
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
}
