package io.github.markdechamps.fideswiss.topscoregroup;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** 3.4: the Pairing-Allocated Bye of an odd number of contenders, the first step of the round-pairing (3.3.2). */
final class PairingAllocatedByeAssignment {

    /** 3.4.2 the lowest score, 3.4.3 the most matches played, 3.4.4 the largest TPN. */
    private static final Comparator<Contender> BYE_ORDER = Comparator.comparing(Contender::score)
            .thenComparing(Comparator.comparingInt(Contender::matchesPlayed).reversed())
            .thenComparing(Comparator.comparingInt(Contender::tpn).reversed());

    private PairingAllocatedByeAssignment() {}

    /** The first contender in bye order that [C2] and 3.4.1 allow; empty when there is none. */
    static Optional<Contender> assign(List<Contender> contenders) {
        return contenders.stream()
                .filter(contender -> !contender.pairingAllocatedByeBarred())
                .sorted(BYE_ORDER)
                .filter(bye -> leavesALegalPairing(contenders, bye))
                .findFirst();
    }

    /** 3.4.1, with [C3] (Readable Double-Swiss pairing algorithm, decision 3). */
    private static boolean leavesALegalPairing(List<Contender> contenders, Contender bye) {
        return CheapestPerfectMatching.canPairAll(
                contenders.stream().filter(contender -> !contender.equals(bye)).toList());
    }
}
