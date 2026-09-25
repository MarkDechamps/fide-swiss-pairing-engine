package io.github.markdechamps.fideswiss.dutch;

/** One of [C5]–[C21]: the PAB criteria and the quality criteria, each citing its article. */
interface CandidateCriterion {

    String article();

    /** How the failure is built up; the optimum finder relies on it to write the criterion as matching weights. */
    Scope scope();

    Failure failureOf(CandidateAssessment candidate);

    enum Scope {
        /** A count summed over the pairs and the downfloaters ([C6], [C10]–[C17]). */
        COUNT,
        /**
         * Scores "taken in descending order", at most one per pair or downfloater ([C7], [C18]–[C21]). Wherever
         * such a criterion is compared, every higher one is equal, which fixes the list's length.
         */
        SCORES,
        /** [C8]: how well the following bracket can be paired with these downfloaters. */
        FOLLOWING_BRACKET,
        /** [C5]: the same for every candidate, since the completion check keeps the lowest reachable PAB score. */
        PAB_SCORE,
        /** [C9]: only when exactly one player floats, and that player is bound to receive the PAB. */
        SINGLE_DOWNFLOATER
    }
}
