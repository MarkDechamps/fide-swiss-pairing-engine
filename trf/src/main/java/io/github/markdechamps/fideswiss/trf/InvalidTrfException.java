package io.github.markdechamps.fideswiss.trf;

/** A TRF that cannot be read: a malformed record, an unknown result code or an unknown participant. */
public final class InvalidTrfException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTrfException(String message) {
        super(message);
    }
}
