package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;
import java.util.Optional;

/**
 * Match Points or Game Points (C.07 13.1): the team's total in the score that does not decide the competition, game
 * points where match points do and the other way round.
 */
final class MatchPointsOrGamePoints implements IndividualTieBreak {

    private final TieBreakCode code;

    MatchPointsOrGamePoints(TieBreakCode code) {
        this.code = code;
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public TieBreakValue valueOf(ParticipantId participant, TieBreakContext context) {
        var secondary = context.in(context.score().other());
        return new TieBreakValue(
                code,
                Optional.of(secondary.of(participant).score().toBigDecimal()),
                true,
                List.of(),
                "C.07 13.1: the secondary score");
    }
}
