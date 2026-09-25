package io.github.markdechamps.fideswiss.dutch;

import java.util.ArrayList;
import java.util.List;

/** Dutch 1.3.2: the residents of a scoregroup plus the MDPs left unpaired by the bracket above. */
record Bracket(List<Player> movedDown, List<Player> residents) {

    Bracket {
        movedDown = List.copyOf(movedDown);
        residents = List.copyOf(residents);
    }

    boolean isHomogeneous() {
        return movedDown.isEmpty();
    }

    boolean isMovedDown(Player player) {
        return movedDown.contains(player);
    }

    /** Article 4.1: BSN n is the n-th player of this list. */
    List<Player> playersInBsnOrder() {
        var players = new ArrayList<>(movedDown);
        players.addAll(residents);
        return List.copyOf(players);
    }
}
