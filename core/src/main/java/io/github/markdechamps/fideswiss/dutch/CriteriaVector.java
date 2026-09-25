package io.github.markdechamps.fideswiss.dutch;

import java.util.List;

/** The failures of one candidate, criterion by criterion in priority order (Article 3.8.1). */
record CriteriaVector(List<Failure> failures) implements Comparable<CriteriaVector> {

    CriteriaVector {
        failures = List.copyOf(failures);
    }

    @Override
    public int compareTo(CriteriaVector other) {
        for (var index = 0; index < failures.size(); index++) {
            var difference = failures.get(index).compareTo(other.failures.get(index));
            if (difference != 0) {
                return difference;
            }
        }
        return 0;
    }
}
