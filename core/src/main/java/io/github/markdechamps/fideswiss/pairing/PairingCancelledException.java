package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.SwissPairingException;
import java.util.List;

/** The pairing thread was interrupted; the trace holds the steps made so far. */
public final class PairingCancelledException extends SwissPairingException {

    private static final long serialVersionUID = 1L;

    private final transient PairingTrace trace;

    public PairingCancelledException(PairingTrace trace) {
        super(List.of(Problem.of("The pairing was cancelled")));
        this.trace = trace;
    }

    public PairingTrace trace() {
        return trace;
    }
}
