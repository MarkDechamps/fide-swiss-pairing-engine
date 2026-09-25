package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;

/**
 * Fore Buchholz (C.07 8.3): Buchholz as if every paired game of the final round had been drawn, a forfeit known at
 * pairing time included; byes keep their points (Tie-break interpretation rulings #4).
 */
final class ForeBuchholz implements TermSource {

    private final Buchholz buchholz;

    ForeBuchholz(boolean forfeitsAsPlayed) {
        this.buchholz = new Buchholz(forfeitsAsPlayed);
    }

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        return buchholz.termsOf(participant, context.withFinalRoundDrawn());
    }

    @Override
    public boolean isUnderArticle16() {
        return true;
    }
}
