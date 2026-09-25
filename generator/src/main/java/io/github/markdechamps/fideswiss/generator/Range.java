package io.github.markdechamps.fideswiss.generator;

import java.util.random.RandomGenerator;

/** A parameter's range, drawn once per tournament; a single value fixes it. Both ends are included. */
public record Range(int min, int max) {

    public Range {
        if (min > max) {
            throw new IllegalArgumentException("Empty range " + min + ".." + max);
        }
    }

    public static Range of(int value) {
        return new Range(value, value);
    }

    public static Range of(int min, int max) {
        return new Range(min, max);
    }

    /** {@code A} or {@code A..B}, optionally followed by {@code %}. */
    public static Range parse(String text) {
        var value = text.endsWith("%") ? text.substring(0, text.length() - 1) : text;
        var dots = value.indexOf("..");
        try {
            return dots < 0
                    ? of(Integer.parseInt(value.trim()))
                    : of(
                            Integer.parseInt(value.substring(0, dots).trim()),
                            Integer.parseInt(value.substring(dots + 2).trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a number or range: " + text, e);
        }
    }

    int draw(RandomGenerator random) {
        return min == max ? min : random.nextInt(min, max + 1);
    }

    @Override
    public String toString() {
        return min == max ? String.valueOf(min) : min + ".." + max;
    }
}
