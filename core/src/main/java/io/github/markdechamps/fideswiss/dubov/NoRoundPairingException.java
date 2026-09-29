package io.github.markdechamps.fideswiss.dubov;

/** 1.9.3: the round-pairing cannot be completed; the Chief Arbiter decides. */
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
