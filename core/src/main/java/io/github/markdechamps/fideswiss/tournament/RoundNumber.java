package io.github.markdechamps.fideswiss.tournament;

public record RoundNumber(int value) implements Comparable<RoundNumber> {

    public static final RoundNumber FIRST = new RoundNumber(1);

    public RoundNumber {
        if (value < 1) {
            throw new IllegalArgumentException("Rounds are numbered from 1: " + value);
        }
    }

    public static RoundNumber of(int value) {
        return new RoundNumber(value);
    }

    public RoundNumber next() {
        return new RoundNumber(value + 1);
    }

    public boolean isAfter(RoundNumber other) {
        return value > other.value;
    }

    public boolean isBefore(RoundNumber other) {
        return value < other.value;
    }

    @Override
    public int compareTo(RoundNumber other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
