package io.github.markdechamps.fideswiss.lim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * Pairs one scoregroup (Article 2.3–2.5): its residents plus the floaters transferred into it. The players who
 * cannot be paired here, and one more if needed to make the number even, float on to the adjacent scoregroup in
 * the direction of pairing (3.2.1, 3.5).
 */
final class ScoregroupPairing {

    record Result(List<Pair> pairs, List<Player> floaters) {}

    private final RoundToPair round;
    private final Reachability reachability;
    private final Direction direction;

    ScoregroupPairing(RoundToPair round, Reachability reachability, Direction direction) {
        this.round = round;
        this.reachability = reachability;
        this.direction = direction;
    }

    /** @param adjacent the residents of the scoregroup its floaters would go to; empty for the median */
    Result pair(List<Player> residents, List<Floater> incoming, List<Player> adjacent) {
        var members = new ArrayList<Player>();
        incoming.forEach(floater -> members.add(floater.player()));
        members.addAll(residents);
        var incomingPlayers = Collections.newSetFromMap(new IdentityHashMap<Player, Boolean>());
        incoming.forEach(floater -> incomingPlayers.add(floater.player()));
        var pairings = reachability.maximumPairs(members, round::compatible);
        var floaters = new FloaterSelection(round, reachability, direction, incomingPlayers, adjacent)
                .choose(members, pairings);
        var staying = new ArrayList<>(members);
        staying.removeAll(floaters);
        var floatersInOrder = incoming.stream()
                .sorted(Floater.pairingOrder(direction))
                .map(Floater::player)
                .filter(staying::contains)
                .toList();
        var pairs = new Scrutiny(round, reachability, direction, staying).pair(floatersInOrder);
        return new Result(pairs, floaters);
    }
}
