package io.github.markdechamps.fideswiss.tournament;

import java.util.List;

/** The settings combine things that cannot go together. */
public final class InvalidSettingsException extends SwissPairingException {

    private static final long serialVersionUID = 1L;

    public InvalidSettingsException(List<Problem> problems) {
        super(problems);
    }
}
