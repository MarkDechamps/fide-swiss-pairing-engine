package io.github.markdechamps.fideswiss.dutch;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * How badly a candidate fails one criterion: a count, or a list of scores "taken in descending order". Lower is
 * better; lists compare element by element.
 */
record Failure(List<BigDecimal> values) implements Comparable<Failure> {

    static final Failure NONE = new Failure(List.of());

    Failure {
        values = List.copyOf(values);
    }

    static Failure count(long count) {
        return new Failure(List.of(BigDecimal.valueOf(count)));
    }

    static Failure of(BigDecimal value) {
        return new Failure(List.of(value));
    }

    static Failure descending(List<BigDecimal> values) {
        return new Failure(values.stream().sorted(Comparator.reverseOrder()).toList());
    }

    @Override
    public int compareTo(Failure other) {
        for (var index = 0; index < Math.min(values.size(), other.values.size()); index++) {
            var difference = values.get(index).compareTo(other.values.get(index));
            if (difference != 0) {
                return difference;
            }
        }
        return Integer.compare(values.size(), other.values.size());
    }

    boolean isZero() {
        return values.stream().allMatch(value -> value.signum() == 0);
    }

    /** The count of a count failure, zero when there is none. */
    int countValue() {
        return values.isEmpty() ? 0 : values.getFirst().intValueExact();
    }

    @Override
    public String toString() {
        return values.stream().map(BigDecimal::toPlainString).toList().toString();
    }
}
