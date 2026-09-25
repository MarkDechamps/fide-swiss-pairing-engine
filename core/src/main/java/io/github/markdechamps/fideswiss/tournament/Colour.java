package io.github.markdechamps.fideswiss.tournament;

public enum Colour {
    WHITE,
    BLACK;

    public Colour opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
