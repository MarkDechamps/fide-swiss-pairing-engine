package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuts the most significant value, {@code count} times: the high half of Median-1/2 (C.07 14.3–14.4), applied after
 * the low cuts ("in that order"). Art. 16.5 speaks only of the least significant value, so it has no exception here.
 */
final class HighCut implements TermSource {

    private final TermSource inner;
    private final int count;
    private final String modifier;

    HighCut(TermSource inner, int count, String modifier) {
        this.inner = inner;
        this.count = count;
        this.modifier = modifier;
    }

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        var terms = new ArrayList<>(inner.termsOf(participant, context));
        for (var cut = 0; cut < count; cut++) {
            var chosen = -1;
            for (var index = 0; index < terms.size(); index++) {
                if (!terms.get(index).cut()
                        && (chosen < 0
                                || Term.LEAST_SIGNIFICANT_FIRST.compare(terms.get(index), terms.get(chosen)) > 0)) {
                    chosen = index;
                }
            }
            if (chosen >= 0) {
                terms.set(chosen, terms.get(chosen).cutBy(modifier + " high cut"));
            }
        }
        return terms;
    }

    @Override
    public boolean isUnderArticle16() {
        return inner.isUnderArticle16();
    }
}
