package io.github.markdechamps.fideswiss.search;

/** The pairing thread was interrupted; the system turns this into a cancellation carrying its trace. */
public final class SearchInterrupted extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SearchInterrupted() {
        super("interrupted", null, false, false);
    }
}
