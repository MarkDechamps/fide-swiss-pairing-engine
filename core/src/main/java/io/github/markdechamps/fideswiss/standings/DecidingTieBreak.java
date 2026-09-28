package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.Score;

/** What separates two participants in the Standings: their Scores, the first tie-break that differs, or nothing. */
public sealed interface DecidingTieBreak {

    /** The Scores differ. */
    record ByScore(Score higher, Score lower) implements DecidingTieBreak {
        @Override
        public String toString() {
            return "score " + higher + " > " + lower;
        }
    }

    /** The Scores and every earlier tie-break are equal; this one differs. */
    record ByTieBreak(TieBreakCode code, TieBreakValue higher, TieBreakValue lower) implements DecidingTieBreak {
        @Override
        public String toString() {
            return code + " " + higher + " " + (higher.higherIsBetter() ? ">" : "<") + " " + lower;
        }
    }

    /** The Score and every tie-break are equal: a Shared Rank, never decided by lot (C.07 4.2). */
    record SharedRank() implements DecidingTieBreak {
        @Override
        public String toString() {
            return "shared";
        }
    }
}
