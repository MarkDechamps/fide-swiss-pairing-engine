package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.dubov.DubovSystem;
import io.github.markdechamps.fideswiss.dutch.DutchSystem;
import io.github.markdechamps.fideswiss.lim.LimSystem;

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

    /** The Lim System (C.04.4.3, 2026), not declared a Maxi-tournament. */
    public static PairingSystem lim() {
        return lim(MaxiTournament.NOT_DECLARED);
    }

    /** The Lim System (C.04.4.3, 2026), declared a Maxi-tournament or not by the organiser. */
    public static PairingSystem lim(MaxiTournament maxiTournament) {
        return new LimSystem(maxiTournament);
    }
}
