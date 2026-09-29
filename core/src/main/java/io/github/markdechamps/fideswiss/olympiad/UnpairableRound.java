package io.github.markdechamps.fideswiss.olympiad;

/** The round cannot be paired: no pairing of every team without a rematch exists; the Chief Arbiter decides. */
final class UnpairableRound extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String article;

    UnpairableRound(String article, String message) {
        super(message);
        this.article = article;
    }

    String article() {
        return article;
    }
}
