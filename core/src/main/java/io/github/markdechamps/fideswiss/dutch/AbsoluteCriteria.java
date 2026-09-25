package io.github.markdechamps.fideswiss.dutch;

/** Dutch 2.1: [C1]–[C3]. */
final class AbsoluteCriteria {

    private final RoundToPair round;

    AbsoluteCriteria(RoundToPair round) {
        this.round = round;
    }

    boolean mayMeet(Player a, Player b) {
        return !haveMetBefore(a, b) && !areNonTopscorersWithSameAbsolutePreference(a, b);
    }

    /** [C2]. */
    boolean mayReceivePairingAllocatedBye(Player player) {
        return player.mayReceivePairingAllocatedBye();
    }

    /** [C1]. */
    private static boolean haveMetBefore(Player a, Player b) {
        return a.hasPlayed(b);
    }

    /** [C3]. */
    private boolean areNonTopscorersWithSameAbsolutePreference(Player a, Player b) {
        var preferenceA = a.colourPreference();
        var preferenceB = b.colourPreference();
        return !round.isTopscorer(a)
                && !round.isTopscorer(b)
                && preferenceA.isAbsolute()
                && preferenceB.isAbsolute()
                && preferenceA.colour().equals(preferenceB.colour());
    }
}
