package io.github.markdechamps.fideswiss.tournament;

/** The Tournament Pairing Number (TPN) a Numbered Participant carries in one round; #1 ranks highest (GHR 2.3). */
public record PairingNumber(int value) implements Comparable<PairingNumber> {

    public PairingNumber {
        if (value < 1) {
            throw new IllegalArgumentException("Pairing numbers start at 1: " + value);
        }
    }

    public static PairingNumber of(int value) {
        return new PairingNumber(value);
    }

    public boolean isOdd() {
        return value % 2 == 1;
    }

    @Override
    public int compareTo(PairingNumber other) {
        return Integer.compare(value, other.value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
