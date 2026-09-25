package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Score;

/**
 * C.04.3 (2026) 1.4.2–1.4.4: players with different scores who meet float, the higher one down; a PAB, or more
 * than a loss's points without playing, is a downfloat; nobody else floats.
 */
final class Dutch2026FloatRule implements FloatRule {

    @Override
    public FloatDirection floatOf(RoundRecord record, Score scoreBefore, Score opponentScoreBefore, Points lossValue) {
        return switch (record) {
            case RoundRecord.Game game -> floatBetween(scoreBefore, opponentScoreBefore);
            case RoundRecord.Forfeit forfeit -> downIfMoreThanALoss(forfeit.points(), lossValue);
            case RoundRecord.NoBoard noBoard ->
                noBoard.bye() == Bye.PAIRING_ALLOCATED
                        ? FloatDirection.DOWN
                        : downIfMoreThanALoss(noBoard.points(), lossValue);
        };
    }

    private static FloatDirection floatBetween(Score own, Score opponent) {
        if (own.isHigherThan(opponent)) {
            return FloatDirection.DOWN;
        }
        return opponent.isHigherThan(own) ? FloatDirection.UP : FloatDirection.NONE;
    }

    private static FloatDirection downIfMoreThanALoss(Points points, Points lossValue) {
        return points.isGreaterThan(lossValue) ? FloatDirection.DOWN : FloatDirection.NONE;
    }
}
