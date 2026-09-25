package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.Objects;
import java.util.Optional;

/**
 * One participant's line in the Standings: its rank, Score and tie-break values, and, when it has the same Score as
 * the participant ranked directly above it, what separates the two.
 */
public final class Standing {

    private final Rank rank;
    private final Participant participant;
    private final Score score;
    private final TieBreakValues tieBreakValues;
    private final Optional<DecidingTieBreak> decidedBy;

    public Standing(
            Rank rank,
            Participant participant,
            Score score,
            TieBreakValues tieBreakValues,
            Optional<DecidingTieBreak> decidedBy) {
        this.rank = Objects.requireNonNull(rank, "rank");
        this.participant = Objects.requireNonNull(participant, "participant");
        this.score = Objects.requireNonNull(score, "score");
        this.tieBreakValues = Objects.requireNonNull(tieBreakValues, "tieBreakValues");
        this.decidedBy = Objects.requireNonNull(decidedBy, "decidedBy");
    }

    public Rank rank() {
        return rank;
    }

    public Participant participant() {
        return participant;
    }

    public Score score() {
        return score;
    }

    public TieBreakValues tieBreakValues() {
        return tieBreakValues;
    }

    /** Against the participant ranked directly above, when both have the same Score; empty otherwise. */
    public Optional<DecidingTieBreak> decidedBy() {
        return decidedBy;
    }

    @Override
    public String toString() {
        return rank + " " + participant.id() + " " + score;
    }
}
