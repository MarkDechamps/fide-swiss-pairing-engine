package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.PairingAllocatedByeBar;

/** Dutch 2.1: [C1]–[C3] (2017: C.1–C.3, the same three with the pre-2026 Basic Rules' PAB bar). */
final class AbsoluteCriteria {

    private final RoundToPair round;
    private final PairingAllocatedByeBar pairingAllocatedByeBar;

    AbsoluteCriteria(RoundToPair round, PairingAllocatedByeBar pairingAllocatedByeBar) {
        this.round = round;
        this.pairingAllocatedByeBar = pairingAllocatedByeBar;
    }

    boolean mayMeet(Player a, Player b) {
        return !haveMetBefore(a, b) && !areNonTopscorersWithSameAbsolutePreference(a, b);
    }

    /** [C2]. */
    boolean mayReceivePairingAllocatedBye(Player player) {
        return player.mayReceivePairingAllocatedBye(pairingAllocatedByeBar);
    }

    /** [C1]. */
    private static boolean haveMetBefore(Player a, Player b) {
        return a.hasPlayed(b);
    }

    /** [C3]. */
    boolean areNonTopscorersWithSameAbsolutePreference(Player a, Player b) {
        var preferenceA = a.colourPreference();
        var preferenceB = b.colourPreference();
        return !round.isTopscorer(a)
                && !round.isTopscorer(b)
                && preferenceA.isAbsolute()
                && preferenceB.isAbsolute()
                && preferenceA.colour().equals(preferenceB.colour());
    }
}
