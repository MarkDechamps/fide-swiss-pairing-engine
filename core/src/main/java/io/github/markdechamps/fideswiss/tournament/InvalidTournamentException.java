package io.github.markdechamps.fideswiss.tournament;

import java.util.List;

/** A snapshot, or an operation on it, is structurally wrong: an unknown id, a round out of order, and the like. */
public final class InvalidTournamentException extends SwissPairingException {

    private static final long serialVersionUID = 1L;

    public InvalidTournamentException(List<Problem> problems) {
        super(problems);
    }

    public InvalidTournamentException(Problem problem) {
        super(List.of(problem));
    }
}
