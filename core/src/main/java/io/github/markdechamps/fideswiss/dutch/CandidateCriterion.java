package io.github.markdechamps.fideswiss.dutch;

import java.util.function.Function;

/** One of [C5]–[C21] (2017: C.5–C.19): the PAB criteria and the quality criteria, each citing its article. */
interface CandidateCriterion {

    String article();

    /** How the failure is built up; the optimum finder relies on it to write the criterion as matching weights. */
    Scope scope();

    Failure failureOf(CandidateAssessment candidate);

    static CandidateCriterion of(String article, Scope scope, Function<CandidateAssessment, Failure> failure) {
        return new CandidateCriterion() {
            @Override
            public String article() {
                return article;
            }

            @Override
            public Scope scope() {
                return scope;
            }

            @Override
            public Failure failureOf(CandidateAssessment candidate) {
                return failure.apply(candidate);
            }
        };
    }

    enum Scope {
        /** A count summed over the pairs and the downfloaters ([C6], [C10]–[C17]). */
        COUNT,
        /**
         * Scores "taken in descending order", at most one per pair or downfloater ([C7], [C18]–[C21]). Wherever
         * such a criterion is compared, every higher one is equal, which fixes the list's length.
         */
        SCORES,
        /** [C8] (2017: C.7): how well the following bracket can be paired with these downfloaters. */
        FOLLOWING_BRACKET,
        /** [C5]: the same for every candidate, since the completion check keeps the lowest reachable PAB score. */
        PAB_SCORE,
        /** [C9]: only when exactly one player floats, and that player is bound to receive the PAB. */
        SINGLE_DOWNFLOATER
    }
}
