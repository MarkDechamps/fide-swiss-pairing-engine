package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;
import java.util.Optional;

/**
 * A rating-based tie-break (C.07 art. 10). By default it is dropped from the list when any participant is unrated
 * (art. 10 preamble): every participant then gets no value, so it separates nobody.
 */
final class RatingTieBreak implements IndividualTieBreak {

    private final IndividualTieBreak inner;

    RatingTieBreak(IndividualTieBreak inner) {
        this.inner = inner;
    }

    @Override
    public TieBreakCode code() {
        return inner.code();
    }

    @Override
    public TieBreakValue valueOf(ParticipantId participant, TieBreakContext context) {
        if (context.anyoneUnrated()) {
            return new TieBreakValue(
                    code(), Optional.empty(), true, List.of(), "dropped: unrated participants (C.07 10)");
        }
        return inner.valueOf(participant, context);
    }
}
