package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.SwissPairingException;
import java.util.List;

/** The absolute criteria cannot all be met; the Chief Arbiter has to decide (GHR 4.4, Dutch 1.9.3). */
public final class NoLegalPairingException extends SwissPairingException {

    private static final long serialVersionUID = 1L;

    private final transient PairingTrace trace;

    public NoLegalPairingException(List<Problem> problems, PairingTrace trace) {
        super(problems);
        this.trace = trace;
    }

    public PairingTrace trace() {
        return trace;
    }
}
