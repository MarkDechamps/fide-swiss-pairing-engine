package io.github.markdechamps.fideswiss.pairing;

/**
 * Hears how a pairing advances. Called synchronously on the pairing thread; implement only what you need. To
 * cancel, interrupt the pairing thread: the pairing then stops with a {@link PairingCancelledException}.
 */
public interface PairingProgress {

    PairingProgress NONE = new PairingProgress() {};

    default void stepStarted(ProgressStep step) {}

    default void advanced(Progress progress) {}

    /** A heartbeat, at most every 100 ms of search inside a long step. */
    default void searching(ProgressStep step, long candidatesTried) {}
}
