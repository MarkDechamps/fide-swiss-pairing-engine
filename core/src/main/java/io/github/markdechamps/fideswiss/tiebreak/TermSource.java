package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;

/**
 * The values a sum-of-values tie-break is built from, one per round or opponent. The Cut and Median modifiers
 * (C.07 14.1–14.4) decorate a source by marking terms cut.
 */
interface TermSource {

    List<Term> termsOf(ParticipantId participant, TieBreakContext context);

    /** Whether art. 16 governs the source, so that a low cut prefers a Voluntary Unplayed Round (16.5). */
    default boolean isUnderArticle16() {
        return false;
    }
}
