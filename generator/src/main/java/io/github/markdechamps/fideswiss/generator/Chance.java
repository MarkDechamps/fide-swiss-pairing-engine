package io.github.markdechamps.fideswiss.generator;

import java.util.random.RandomGenerator;

/** The share of tournaments, in percent, that a variation is applied to. */
public record Chance(int percent) {

    public Chance {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("A chance is a percentage from 0 to 100: " + percent);
        }
    }

    public static Chance never() {
        return new Chance(0);
    }

    public static Chance always() {
        return new Chance(100);
    }

    public static Chance percent(int percent) {
        return new Chance(percent);
    }

    boolean happens(RandomGenerator random) {
        return percent > 0 && random.nextInt(100) < percent;
    }

    @Override
    public String toString() {
        return percent + "%";
    }
}
