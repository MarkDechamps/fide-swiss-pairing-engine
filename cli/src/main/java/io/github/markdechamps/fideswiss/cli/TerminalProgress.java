package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.PairingProgress;
import io.github.markdechamps.fideswiss.pairing.Progress;
import io.github.markdechamps.fideswiss.pairing.ProgressStep;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.io.PrintStream;

/**
 * The one-line progress indicator on a terminal's standard error, rewritten in place. It only shows after the
 * pairing has run for 500 ms, and it is erased when the pairing ends (a {@code Progress} with nothing to pair).
 */
final class TerminalProgress implements PairingProgress {

    private static final long DELAY_NANOS = 500_000_000L;
    private static final String ERASE_LINE = "\r\033[K";

    private final PrintStream err;
    private final RoundNumber round;
    private final long started = System.nanoTime();
    private String step = "";
    private Progress progress = new Progress(0, 0);
    private boolean shown;

    private TerminalProgress(PrintStream err, RoundNumber round) {
        this.err = err;
        this.round = round;
    }

    /** A listener that shows the line only when standard error is a terminal. */
    static PairingProgress onStandardError(RoundNumber round) {
        var console = System.console();
        return console != null && console.isTerminal() ? new TerminalProgress(System.err, round) : PairingProgress.NONE;
    }

    @Override
    public void stepStarted(ProgressStep started) {
        step = started.label();
        show("");
    }

    @Override
    public void advanced(Progress advanced) {
        if (advanced.toPair() == 0) {
            erase();
            return;
        }
        progress = advanced;
        show("");
    }

    @Override
    public void searching(ProgressStep searching, long candidatesTried) {
        show(String.format(" · %,d candidates", candidatesTried));
    }

    private void show(String detail) {
        if (System.nanoTime() - started < DELAY_NANOS) {
            return;
        }
        shown = true;
        err.print(ERASE_LINE + "round " + round + ": " + progress.settled() + "/" + progress.toPair() + " settled · "
                + step + detail);
        err.flush();
    }

    private void erase() {
        if (shown) {
            err.print(ERASE_LINE);
            err.flush();
        }
    }
}
