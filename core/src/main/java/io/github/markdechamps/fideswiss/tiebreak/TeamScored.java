package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import java.util.List;
import java.util.Map;

/**
 * An individual tie-break computed in a team score, {@code :MP} or {@code :GP} (C.07 13 preamble: "using teams MP or
 * GP as the reference score"), whatever the competition's primary score is.
 */
final class TeamScored implements TieBreak {

    private final TieBreak inner;
    private final PrimaryScore score;

    TeamScored(TieBreak inner, PrimaryScore score) {
        this.inner = inner;
        this.score = score;
    }

    @Override
    public TieBreakCode code() {
        return inner.code();
    }

    @Override
    public Map<ParticipantId, TieBreakValue> valuesFor(List<ParticipantId> group, TieBreakContext context) {
        return inner.valuesFor(group, context.in(score));
    }
}
