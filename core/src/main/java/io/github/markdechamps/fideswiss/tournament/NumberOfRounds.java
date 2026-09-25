package io.github.markdechamps.fideswiss.tournament;

/** The number of rounds declared before the tournament starts (C.04.1 Art. 1). */
public record NumberOfRounds(int value) {

    public NumberOfRounds {
        if (value < 1) {
            throw new IllegalArgumentException("A tournament has at least one round: " + value);
        }
    }

    public static NumberOfRounds of(int value) {
        return new NumberOfRounds(value);
    }

    public boolean includes(RoundNumber round) {
        return round.value() <= value;
    }

    public boolean isLast(RoundNumber round) {
        return round.value() == value;
    }
}
