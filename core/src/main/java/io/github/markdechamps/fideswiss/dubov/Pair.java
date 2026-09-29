package io.github.markdechamps.fideswiss.dubov;

/** Two players paired with each other, the smaller Pairing Number first. */
record Pair(Player first, Player second) {

    static Pair of(Player a, Player b) {
        return a.tpn() < b.tpn() ? new Pair(a, b) : new Pair(b, a);
    }

    Player other(Player player) {
        return first == player ? second : first;
    }
}
