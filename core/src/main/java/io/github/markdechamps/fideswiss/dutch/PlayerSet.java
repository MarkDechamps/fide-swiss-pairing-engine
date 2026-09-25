package io.github.markdechamps.fideswiss.dutch;

import java.util.BitSet;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** The players of one round, numbered so that a group of them is a bit set. */
final class PlayerSet {

    private final List<Player> players;
    private final Map<Player, Integer> indexes = new IdentityHashMap<>();

    PlayerSet(List<Player> playersInPairingOrder) {
        this.players = List.copyOf(playersInPairingOrder);
        for (var index = 0; index < players.size(); index++) {
            indexes.put(players.get(index), index);
        }
    }

    BitSet maskOf(Collection<Player> group) {
        var mask = new BitSet(players.size());
        for (var player : group) {
            mask.set(indexOf(player));
        }
        return mask;
    }

    static BitSet union(BitSet a, BitSet b) {
        var union = (BitSet) a.clone();
        union.or(b);
        return union;
    }

    static BitSet without(BitSet mask, int index) {
        var rest = (BitSet) mask.clone();
        rest.clear(index);
        return rest;
    }

    int indexOf(Player player) {
        return indexes.get(player);
    }

    Player at(int index) {
        return players.get(index);
    }

    int size() {
        return players.size();
    }

    List<Player> inPairingOrder() {
        return players;
    }
}
