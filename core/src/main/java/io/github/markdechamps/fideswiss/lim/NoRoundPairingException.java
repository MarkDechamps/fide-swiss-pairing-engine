package io.github.markdechamps.fideswiss.lim;

/** The round cannot be paired: no compatible pairing of every player exists; the Chief Arbiter decides. */
final class NoRoundPairingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String article;

    NoRoundPairingException(String article, String message) {
        super(message);
        this.article = article;
    }

    String article() {
        return article;
    }
}
