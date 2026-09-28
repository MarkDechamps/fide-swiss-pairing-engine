package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.PairingAllocatedByeBar;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.List;

/**
 * An edition of the Dutch System as a composition of rule objects (Historic rule editions): its float rule, its
 * Basic Rules' PAB bar, its criteria list, its MDP order, and its own procedure object. Transpositions, resident
 * exchanges, colour allocation, the criteria vector and the matching mechanism are shared; nothing inherits.
 */
record DutchEdition(
        String name,
        FloatRule floatRule,
        PairingAllocatedByeBar pairingAllocatedByeBar,
        List<CandidateCriterion> criteria,
        MdpSelection mdpSelection,
        DutchProcedure procedure) {

    static DutchEdition of(SwissRulesEdition edition) {
        return switch (edition) {
            case EDITION_2026 -> edition2026();
            case PRE_2026 -> edition2017();
        };
    }

    /** C.04.3 (2026) with C.04.1/C.04.2 (2026). */
    static DutchEdition edition2026() {
        return new DutchEdition(
                "C.04.3 (2026)",
                new Dutch2026FloatRule(),
                PairingAllocatedByeBar.BASIC_RULES_2026,
                Dutch2026Criteria.inPriorityOrder(),
                MdpSets::validInOrder,
                new Dutch2026Procedure());
    }

    /** C.04.3 as in force until 2026-01-31 (Dutch 2017), with the pre-2026 C.04.1/C.04.2. */
    static DutchEdition edition2017() {
        return new DutchEdition(
                "C.04.3 (2017)",
                new Dutch2017FloatRule(),
                PairingAllocatedByeBar.BASIC_RULES_PRE_2026,
                Dutch2017Criteria.inPriorityOrder(),
                MdpExchanges::inOrder,
                new Dutch2017Procedure());
    }

    /** Whether the edition has a PAB-score criterion ([C5]) that folds into the completion check. */
    boolean foldsPairingAllocatedByeScoreIntoCompletion() {
        return criteria.stream().anyMatch(criterion -> criterion.scope() == CandidateCriterion.Scope.PAB_SCORE);
    }
}
