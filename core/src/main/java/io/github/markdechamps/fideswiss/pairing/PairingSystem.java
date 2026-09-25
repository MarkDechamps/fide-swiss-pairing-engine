package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;

/**
 * A named ruleset that decides who plays whom, and with which colour, in the next round. Obtain the FIDE systems
 * from {@link PairingSystems}; an arbiter's own authorised system (GHR 1.1) can implement this interface.
 */
public interface PairingSystem {

    RoundPairing pairNextRound(Tournament tournament);

    /** What a Pairing-Allocated Bye is worth when the tournament does not say: a win (C.04.1 Art. 3). */
    default Points pairingAllocatedByeValue(ScoringScheme scoring) {
        return scoring.win();
    }
}
