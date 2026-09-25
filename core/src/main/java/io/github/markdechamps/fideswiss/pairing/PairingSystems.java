package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.dubov.DubovSystem;
import io.github.markdechamps.fideswiss.dutch.DutchSystem;

/** The FIDE Pairing Systems. */
public final class PairingSystems {

    private PairingSystems() {}

    /** The Dutch System (C.04.3) of the tournament's Swiss Rules Edition. */
    public static PairingSystem dutch() {
        return new DutchSystem();
    }

    /** The Dubov System (C.04.4.1, 2026). */
    public static PairingSystem dubov() {
        return new DubovSystem();
    }
}
