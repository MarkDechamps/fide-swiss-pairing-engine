package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.tournament.PairingScore;
import java.util.Comparator;

/**
 * The direction a scoregroup is paired in (2.2). Every upward rule of the text mirrors a downward one (3.2.1/3.2.3/
 * 3.2.4, 3.3/3.4, 3.6/3.7, 3.8, 4.1.1/4.1.2, 5.4), so the procedure is written once in the downward frame and this
 * order mirrors it: the "first" player is the highest numbered (#1) downward and the lowest numbered upward.
 */
enum Direction {
    DOWNWARD,
    UPWARD;

    /** The order of Pairing Numbers as the direction reads them: scrutiny starts with the first (4.1). */
    Comparator<Player> order() {
        var byNumber = Comparator.comparingInt(Player::tpn);
        return this == DOWNWARD ? byNumber : byNumber.reversed();
    }

    /** 3.6/3.7: floaters from the side the pairing started at come first, the higher (lower) score first. */
    Comparator<PairingScore> scoresFromAhead() {
        return this == DOWNWARD ? Comparator.<PairingScore>reverseOrder() : Comparator.<PairingScore>naturalOrder();
    }

    Direction mirrored() {
        return this == DOWNWARD ? UPWARD : DOWNWARD;
    }
}
