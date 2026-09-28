package io.github.markdechamps.fideswiss.search;

import io.github.markdechamps.fideswiss.pairing.PairingProgress;
import io.github.markdechamps.fideswiss.pairing.ProgressStep;

/**
 * Counts the candidates a long step tries: every 100 ms it tells the listener, and at every beat it checks
 * whether the pairing thread was interrupted (Explaining decisions, progress).
 */
public final class SearchHeartbeat {

    private static final long INTERVAL_NANOS = 100_000_000L;
    private static final int TICKS_BETWEEN_CLOCK_READS = 256;

    private final PairingProgress progress;
    private final ProgressStep step;
    private long candidatesTried;
    private long lastBeat = System.nanoTime();

    public SearchHeartbeat(PairingProgress progress, ProgressStep step) {
        this.progress = progress;
        this.step = step;
    }

    /** Checks the interrupt flag at a step boundary. */
    public static void checkInterrupted() {
        if (Thread.interrupted()) {
            throw new SearchInterrupted();
        }
    }

    public void tick() {
        candidatesTried++;
        if (candidatesTried % TICKS_BETWEEN_CLOCK_READS != 0) {
            return;
        }
        checkInterrupted();
        var now = System.nanoTime();
        if (now - lastBeat >= INTERVAL_NANOS) {
            lastBeat = now;
            progress.searching(step, candidatesTried);
        }
    }
}
