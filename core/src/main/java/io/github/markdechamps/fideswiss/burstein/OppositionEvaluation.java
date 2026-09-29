package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;

/**
 * 1.7: Buchholz and Sonneborn-Berger over the rounds so far, with Burstein's own rules for unplayed rounds (1.7.2),
 * which are not those of C.07.
 *
 * <ul>
 *   <li>A round a player did not play over the board counts as a game against themselves with the points
 *       registered: their own current score enters their Buchholz, and those points times it their
 *       Sonneborn-Berger.
 *   <li>Exception: a run of zero-point byes up to the current round counts as draws when the player's score enters
 *       the Index of their over-the-board opponents, not their own. The zero-point byes are the requested ones (GHR 3.3)
 *       and the rounds after a Withdrawal, which the library already treats as zero-point byes (C.07 16.1).
 *   <li>Virtual points (acceleration) never enter: scores here are standings points.
 * </ul>
 */
final class OppositionEvaluation {

    private final TournamentHistory history;
    private final RoundNumber round;
    private final Points draw;

    /** @param round the round being paired; the rounds before it are the tournament so far */
    OppositionEvaluation(TournamentHistory history, RoundNumber round, Points draw) {
        this.history = history;
        this.round = round;
        this.draw = draw;
    }

    OppositionIndex indexOf(ParticipantId participant) {
        var index = OppositionIndex.NONE;
        for (var record : history.of(participant).records()) {
            var opponentScore = record instanceof RoundRecord.Game game
                    ? scoreSeenByOpponents(game.opponent())
                    : currentScore(participant);
            index = index.plus(opponentScore, record.points());
        }
        return index;
    }

    private Points currentScore(ParticipantId participant) {
        return history.of(participant).scoreBefore(round).points();
    }

    /** 1.7.2 exception: the zero-point byes of a run that reaches the current round count as draws. */
    private Points scoreSeenByOpponents(ParticipantId opponent) {
        var score = currentScore(opponent);
        var records = history.of(opponent).records();
        for (var back = records.size() - 1; back >= 0 && isZeroPointBye(records.get(back)); back--) {
            score = score.plus(draw);
        }
        return score;
    }

    private static boolean isZeroPointBye(RoundRecord record) {
        return record instanceof RoundRecord.NoBoard noBoard
                && (noBoard.bye() == Bye.ZERO_POINT || noBoard.bye() == Bye.WITHDRAWN);
    }
}
