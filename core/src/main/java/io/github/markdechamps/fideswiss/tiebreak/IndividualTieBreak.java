package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A tie-break whose value depends on the participant alone (types B, C and D, C.07 4.3). */
interface IndividualTieBreak extends TieBreak {

    TieBreakValue valueOf(ParticipantId participant, TieBreakContext context);

    @Override
    default Map<ParticipantId, TieBreakValue> valuesFor(List<ParticipantId> group, TieBreakContext context) {
        var values = new LinkedHashMap<ParticipantId, TieBreakValue>();
        group.forEach(participant -> values.put(participant, valueOf(participant, context)));
        return values;
    }
}
