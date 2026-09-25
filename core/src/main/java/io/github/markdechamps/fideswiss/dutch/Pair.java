package io.github.markdechamps.fideswiss.dutch;

/** Article 3.3.1: an S1 player tentatively paired with an S2 player. */
record Pair(Player s1Player, Player s2Player) {

    Player higherRanked() {
        return PairingOrder.isHigherRanked(s1Player, s2Player) ? s1Player : s2Player;
    }

    Player lowerRanked() {
        return higherRanked() == s1Player ? s2Player : s1Player;
    }
}
