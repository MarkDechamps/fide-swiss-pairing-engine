package io.github.markdechamps.fideswiss.doubleswiss;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.topscoregroup.PairCriterion;
import java.util.List;
import java.util.Set;

/**
 * The bracket criteria of 3.6.4 beyond [C1]: only [C8]. Double-Swiss has no colour criterion; Article 4 allocates
 * the colours once the whole round is paired.
 */
final class DoubleSwissCriteria {

    private DoubleSwissCriteria() {}

    static List<PairCriterion> of(boolean floatCriteriaLapse) {
        return List.of(new UpfloatersOpponentsThatFloated(floatCriteriaLapse));
    }

    /**
     * 2.3.5 [C8]: with the exception of the last round, an upfloater's opponent that was a floater in the previous
     * round. The opponent is the resident of a resident-upfloater pair, as in the Swiss Team build's [C10], the same
     * words.
     */
    private record UpfloatersOpponentsThatFloated(boolean lapses) implements PairCriterion {
        @Override
        public String article() {
            return "C8";
        }

        @Override
        public long failureOf(ContenderPair pair, Set<Contender> upfloaters) {
            if (lapses) {
                return 0;
            }
            return pair.upfloatersOpponent(upfloaters)
                            .filter(Contender::floatedInPreviousRound)
                            .isPresent()
                    ? 1
                    : 0;
        }
    }
}
