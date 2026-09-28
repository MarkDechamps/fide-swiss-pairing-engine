package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Points;

/**
 * C.04.3 (till 2026-01-31) A.4.b: after a game between different scores the higher-ranked player gets a downfloat
 * and the other an upfloat; "a player who, for whatever reason, does not play in a round, also receives a
 * downfloat": the PAB, forfeits either way, any bye, an absence (reading R8).
 */
final class Dutch2017FloatRule implements FloatRule {

    @Override
    public FloatDirection floatOf(
            RoundRecord record, PairingScore scoreBefore, PairingScore opponentScoreBefore, Points lossValue) {
        if (!record.isPlayed()) {
            return FloatDirection.DOWN;
        }
        if (scoreBefore.isHigherThan(opponentScoreBefore)) {
            return FloatDirection.DOWN;
        }
        return opponentScoreBefore.isHigherThan(scoreBefore) ? FloatDirection.UP : FloatDirection.NONE;
    }
}
