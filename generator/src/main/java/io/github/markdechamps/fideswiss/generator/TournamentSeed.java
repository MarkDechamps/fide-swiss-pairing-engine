package io.github.markdechamps.fideswiss.generator;

/** The seed one generated tournament is reproduced from, given the same generator version. */
public record TournamentSeed(long value) {

    public static TournamentSeed of(long value) {
        return new TournamentSeed(value);
    }

    @Override
    public String toString() {
        return Long.toUnsignedString(value);
    }
}
