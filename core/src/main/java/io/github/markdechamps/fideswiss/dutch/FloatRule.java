package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Points;

/** The edition's rule that turns one round into a float. */
interface FloatRule {

    /**
     * @param opponentScoreBefore the paired opponent's score before the round, for a round with an opponent
     */
    FloatDirection floatOf(
            RoundRecord record, PairingScore scoreBefore, PairingScore opponentScoreBefore, Points lossValue);
}
