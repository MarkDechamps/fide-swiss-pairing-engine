package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;

/** Buchholz (C.07 8.1): the sum of the opponents' scores, Unplayed Rounds counted as art. 16 says. */
final class Buchholz implements TermSource {

    private final boolean forfeitsAsPlayed;

    Buchholz(boolean forfeitsAsPlayed) {
        this.forfeitsAsPlayed = forfeitsAsPlayed;
    }

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        return OpponentScores.of(participant, context, forfeitsAsPlayed).stream()
                .map(counterpart -> Term.of(
                        RoundNumber.of(counterpart.index() + 1),
                        counterpart.name(),
                        counterpart.score().toBigDecimal(),
                        counterpart.voluntary(),
                        counterpart.notes()))
                .toList();
    }

    @Override
    public boolean isUnderArticle16() {
        return true;
    }
}
