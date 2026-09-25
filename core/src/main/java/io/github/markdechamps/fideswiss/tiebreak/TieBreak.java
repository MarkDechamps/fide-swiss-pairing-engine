package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;
import java.util.Map;

/** One tie-break of a Tie-break List, as a small object (C.07 art. 5). */
interface TieBreak {

    TieBreakCode code();

    /**
     * The values of the participants of one group still tied. Only type A tie-breaks (DE) look at the group; the
     * others give each participant the same value whatever group it is in.
     */
    Map<ParticipantId, TieBreakValue> valuesFor(List<ParticipantId> group, TieBreakContext context);
}
