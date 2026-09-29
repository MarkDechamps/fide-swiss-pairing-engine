package io.github.markdechamps.fideswiss.olympiad;

import java.util.Comparator;

/**
 * The direction a group is paired in (6.4, 9.2). Every rule below the Median Group mirrors one above it (8.2/8.3,
 * 9.2, 9.4), so the procedure is written once and this order mirrors it: the team given priority is the highest
 * ranked one pairing downward (the Median Group and above) and the lowest ranked one pairing upward (below it).
 */
enum Direction {
    DOWNWARD,
    UPWARD;

    /** 9.2: the order in which teams are given priority; 9.3 arranges the group in it. */
    Comparator<Team> priority() {
        return this == DOWNWARD ? Team.RANKING : Team.RANKING.reversed();
    }

    /** 8.2.2, 8.3.2: the order in which teams are tried as floaters, the last in priority first. */
    Comparator<Team> floaterOrder() {
        return priority().reversed();
    }
}
