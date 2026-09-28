package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cuts the least significant value, {@code count} times (Cut-1/2 and the low half of Median-1/2, C.07 14.1–14.4).
 * Under art. 16 the lowest Voluntary Unplayed Round contribution is cut instead, as long as it is not lower than the
 * least significant value (16.5.1), and so for each further cut (16.5.2).
 */
final class LowCut implements TermSource {

    private final TermSource inner;
    private final int count;
    private final String modifier;

    LowCut(TermSource inner, int count, String modifier) {
        this.inner = inner;
        this.count = count;
        this.modifier = modifier;
    }

    @Override
    public List<Term> termsOf(ParticipantId participant, TieBreakContext context) {
        var terms = new ArrayList<>(inner.termsOf(participant, context));
        for (var cut = 0; cut < count; cut++) {
            nextToCut(terms)
                    .ifPresent(index -> terms.set(index, terms.get(index).cutBy(modifier + " low cut")));
        }
        return terms;
    }

    @Override
    public boolean isUnderArticle16() {
        return inner.isUnderArticle16();
    }

    private Optional<Integer> nextToCut(List<Term> terms) {
        var leastSignificant = indexOf(terms, false);
        if (leastSignificant.isEmpty() || !inner.isUnderArticle16()) {
            return leastSignificant;
        }
        var lowestVoluntary = indexOf(terms, true);
        return lowestVoluntary
                .filter(index -> terms.get(index)
                                .value()
                                .compareTo(terms.get(leastSignificant.get()).value())
                        >= 0)
                .or(() -> leastSignificant);
    }

    private static Optional<Integer> indexOf(List<Term> terms, boolean onlyVoluntary) {
        Optional<Integer> chosen = Optional.empty();
        for (var index = 0; index < terms.size(); index++) {
            var term = terms.get(index);
            if (term.cut() || (onlyVoluntary && !term.voluntary())) {
                continue;
            }
            var comparator = onlyVoluntary ? java.util.Comparator.comparing(Term::value) : Term.LEAST_SIGNIFICANT_FIRST;
            if (chosen.isEmpty() || comparator.compare(term, terms.get(chosen.get())) < 0) {
                chosen = Optional.of(index);
            }
        }
        return chosen;
    }
}
