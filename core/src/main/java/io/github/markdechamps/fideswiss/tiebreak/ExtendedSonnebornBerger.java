package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;

/**
 * Extended Sonneborn-Berger for teams (C.07 13.2): for each opponent, its total in one team score times the points
 * scored against it in one team score, the four combinations being EMMSB, EMGSB, EGMSB and EGGSB. Unplayed Rounds are
 * managed by art. 16 (the dummy's total is in the opponent's score), and Cut-1 ranks the terms by the opponent's total
 * first (14.1.2).
 */
final class ExtendedSonnebornBerger implements TermSource {

    private final PrimaryScore opponentTotal;
    private final PrimaryScore scored;
    private final boolean forfeitsAsPlayed;

    ExtendedSonnebornBerger(PrimaryScore opponentTotal, PrimaryScore scored, boolean forfeitsAsPlayed) {
        this.opponentTotal = opponentTotal;
        this.scored = scored;
        this.forfeitsAsPlayed = forfeitsAsPlayed;
    }

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        var totals = OpponentScores.of(participant, context.in(opponentTotal), forfeitsAsPlayed);
        var points = OpponentScores.of(participant, context.in(scored), forfeitsAsPlayed);
        var terms = new java.util.ArrayList<Term>();
        for (var index = 0; index < totals.size(); index++) {
            var total = totals.get(index);
            terms.add(Term.of(
                            RoundNumber.of(total.index() + 1),
                            total.name(),
                            total.score()
                                    .toBigDecimal()
                                    .multiply(points.get(index).pointsScored().toBigDecimal()),
                            total.voluntary(),
                            total.notes())
                    .rankedBy(total.score().toBigDecimal()));
        }
        return terms;
    }

    @Override
    public boolean isUnderArticle16() {
        return true;
    }
}
