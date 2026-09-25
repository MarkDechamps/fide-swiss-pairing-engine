package io.github.markdechamps.fideswiss.tournament;

import java.math.BigDecimal;
import java.util.Objects;

/** An exact number of points, such as a win's value or a participant's total. */
public final class Points implements Comparable<Points> {

    public static final Points ZERO = new Points(BigDecimal.ZERO);

    private final BigDecimal value;

    private Points(BigDecimal value) {
        this.value = normalised(value);
    }

    public static Points of(String value) {
        return new Points(new BigDecimal(value));
    }

    public static Points of(BigDecimal value) {
        return new Points(Objects.requireNonNull(value, "value"));
    }

    public static Points of(int value) {
        return new Points(BigDecimal.valueOf(value));
    }

    public Points plus(Points other) {
        return new Points(value.add(other.value));
    }

    public Points minus(Points other) {
        return new Points(value.subtract(other.value));
    }

    public Points times(int factor) {
        return new Points(value.multiply(BigDecimal.valueOf(factor)));
    }

    public boolean isGreaterThan(Points other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(Points other) {
        return compareTo(other) < 0;
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    private static BigDecimal normalised(BigDecimal value) {
        var stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    @Override
    public int compareTo(Points other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Points points && value.equals(points.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
