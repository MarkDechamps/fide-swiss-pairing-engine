package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

/**
 * The score a Pairing System pairs on: the Score plus the Virtual Points for the round being paired (C.04.7 1.5).
 * Standings and tie-breaks never see it.
 */
public record PairingScore(Points points) implements Comparable<PairingScore> {

    public PairingScore {
        Objects.requireNonNull(points, "points");
    }

    public static PairingScore of(Score score, Points virtualPoints) {
        return new PairingScore(score.points().plus(virtualPoints));
    }

    public boolean isHigherThan(PairingScore other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(PairingScore other) {
        return points.compareTo(other.points);
    }

    @Override
    public String toString() {
        return points.toString();
    }
}
