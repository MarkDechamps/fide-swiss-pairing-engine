package io.github.markdechamps.fideswiss.dutch;

import java.util.List;
import java.util.stream.Stream;

/**
 * The order in which a heterogeneous bracket tries its S1, the MDPs it pairs: 2026 Article 4.4 (valid MDP sets) or
 * 2017 D.3 (MDP-exchanges between S1 and the Limbo). An edition rule; the sequence around it is shared.
 */
interface MdpSelection {

    /** Every S1 of M1 MDPs in the edition's order; M1 is read off the optimum's Limbo. */
    Stream<List<Player>> inOrder(List<Player> movedDown, List<Player> bestAchievableLimbo);
}
