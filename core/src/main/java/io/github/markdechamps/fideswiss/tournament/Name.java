package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

public record Name(String value) implements Comparable<Name> {

    public Name {
        Objects.requireNonNull(value, "value");
    }

    public static Name of(String value) {
        return new Name(value);
    }

    @Override
    public int compareTo(Name other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
