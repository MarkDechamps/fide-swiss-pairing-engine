package io.github.markdechamps.fideswiss.dutch;

import java.util.Comparator;

/** Dutch 1.2: score, then Pairing Number ascending. */
final class PairingOrder {

    static final Comparator<Player> RANKING =
            Comparator.comparing(Player::score).reversed().thenComparing(Player::pairingNumber);

    private PairingOrder() {}

    static boolean isHigherRanked(Player a, Player b) {
        return RANKING.compare(a, b) < 0;
    }
}
