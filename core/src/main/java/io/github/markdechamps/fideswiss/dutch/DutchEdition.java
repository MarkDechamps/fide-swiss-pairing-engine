package io.github.markdechamps.fideswiss.dutch;

import java.util.List;

/**
 * An edition of the Dutch System as a composition of rule objects (Historic rule editions): what differs between
 * editions is listed here, line by line.
 */
record DutchEdition(String name, FloatRule floatRule, List<CandidateCriterion> criteria) {

    static DutchEdition edition2026() {
        return new DutchEdition("C.04.3 (2026)", new Dutch2026FloatRule(), Dutch2026Criteria.inPriorityOrder());
    }
}
