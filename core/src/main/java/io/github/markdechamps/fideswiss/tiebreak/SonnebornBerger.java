package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;

/**
 * Sonneborn-Berger (C.07 9.1): for each round, the counterpart's score times the points scored against it. Its terms
 * rank by the counterpart's score first, then by the product (14.1.1).
 */
final class SonnebornBerger implements TermSource {

    private final boolean forfeitsAsPlayed;

    SonnebornBerger(boolean forfeitsAsPlayed) {
        this.forfeitsAsPlayed = forfeitsAsPlayed;
    }

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        return OpponentScores.of(participant, context, forfeitsAsPlayed).stream()
                .map(counterpart -> Term.of(
                                RoundNumber.of(counterpart.index() + 1),
                                counterpart.name(),
                                counterpart
                                        .score()
                                        .toBigDecimal()
                                        .multiply(counterpart.pointsScored().toBigDecimal()),
                                counterpart.voluntary(),
                                counterpart.notes())
                        .rankedBy(counterpart.score().toBigDecimal()))
                .toList();
    }

    @Override
    public boolean isUnderArticle16() {
        return true;
    }
}
