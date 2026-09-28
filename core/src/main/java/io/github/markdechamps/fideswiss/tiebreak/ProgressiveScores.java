package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.ArrayList;
import java.util.List;

/**
 * (Sum of) Progressive Scores (C.07 7.5): the cumulative score after each round. PS-C1 drops the score after round
 * 1 even when round 1 was a Voluntary Unplayed Round: art. 16 does not govern PS (Tie-break interpretation rulings
 * #2).
 */
final class ProgressiveScores implements TermSource {

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        var terms = new ArrayList<Term>();
        var cumulative = Points.ZERO;
        for (var entry : context.of(participant).entries()) {
            cumulative = cumulative.plus(entry.points());
            terms.add(Term.of(
                    entry.round(), "after round " + entry.round(), cumulative.toBigDecimal(), false, List.of()));
        }
        return terms;
    }
}
