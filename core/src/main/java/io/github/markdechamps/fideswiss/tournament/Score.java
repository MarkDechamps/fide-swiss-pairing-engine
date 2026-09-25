package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

/** The points a participant has earned from the outcomes of its rounds, as used for standings. */
public record Score(Points points) implements Comparable<Score> {

    public static final Score ZERO = new Score(Points.ZERO);

    public Score {
        Objects.requireNonNull(points, "points");
    }

    public static Score of(String points) {
        return new Score(Points.of(points));
    }

    public Score plus(Points earned) {
        return new Score(points.plus(earned));
    }

    public boolean isHigherThan(Score other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(Score other) {
        return points.compareTo(other.points);
    }

    @Override
    public String toString() {
        return points.toString();
    }
}
