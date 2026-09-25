package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import java.util.List;
import java.util.stream.Collectors;

/** Every failure the library reports. Each one carries all the problems found, not only the first. */
public abstract sealed class SwissPairingException extends RuntimeException
        permits InvalidSettingsException, InvalidTournamentException, NoLegalPairingException {

    private static final long serialVersionUID = 1L;

    private final transient List<Problem> problems;

    protected SwissPairingException(List<Problem> problems) {
        super(problems.stream().map(Problem::toString).collect(Collectors.joining("; ")));
        this.problems = List.copyOf(problems);
    }

    public List<Problem> problems() {
        return problems;
    }
}
