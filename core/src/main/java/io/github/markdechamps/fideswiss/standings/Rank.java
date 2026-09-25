package io.github.markdechamps.fideswiss.standings;

/** A place in the Standings, from 1. Participants who share a rank all hold the highest place of their group. */
public record Rank(int value) implements Comparable<Rank> {

    public Rank {
        if (value < 1) {
            throw new IllegalArgumentException("Ranks start at 1: " + value);
        }
    }

    public static Rank of(int value) {
        return new Rank(value);
    }

    @Override
    public int compareTo(Rank other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
