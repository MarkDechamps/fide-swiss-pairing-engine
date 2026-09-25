package io.github.markdechamps.fideswiss.tournament;

import java.util.OptionalInt;

/**
 * A participant's strength (GHR 2.1). An unrated participant ranks below every rating unless the Chief Arbiter
 * supplies an estimate, which the client then passes as an ordinary rating.
 */
public final class Rating implements Comparable<Rating> {

    private static final Rating UNRATED = new Rating(OptionalInt.empty());

    private final OptionalInt value;

    private Rating(OptionalInt value) {
        this.value = value;
    }

    public static Rating of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("A rating must not be negative: " + value);
        }
        return new Rating(OptionalInt.of(value));
    }

    public static Rating unrated() {
        return UNRATED;
    }

    public boolean isRated() {
        return value.isPresent();
    }

    /** The rating, or 0 for an unrated participant (the value rating averages use for them). */
    public int valueOrZero() {
        return value.orElse(0);
    }

    @Override
    public int compareTo(Rating other) {
        return Integer.compare(value.orElse(-1), other.value.orElse(-1));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Rating rating && value.equals(rating.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.isPresent() ? String.valueOf(value.getAsInt()) : "unrated";
    }
}
