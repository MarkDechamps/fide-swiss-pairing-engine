package io.github.markdechamps.fideswiss.tournament;

public record BoardNumber(int value) implements Comparable<BoardNumber> {

    public BoardNumber {
        if (value < 1) {
            throw new IllegalArgumentException("Boards are numbered from 1: " + value);
        }
    }

    public static BoardNumber of(int value) {
        return new BoardNumber(value);
    }

    @Override
    public int compareTo(BoardNumber other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
