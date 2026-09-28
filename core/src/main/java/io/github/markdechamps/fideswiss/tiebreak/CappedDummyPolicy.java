package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.Points;

/**
 * C.07 2026-03 art. 16: the Dummy Opponent scores the participant's own score, capped for a forfeit at the scheduled
 * opponent's Adjusted Score (16.4.1) and otherwise at a draw's points times the number of rounds (16.4.2). The
 * number of rounds is the rounds the Standings count (Tie-break interpretation rulings #9).
 */
final class CappedDummyPolicy implements UnplayedRoundPolicy {

    @Override
    public Points adjustedScore(ParticipantRounds participant, TieBreakContext context) {
        return UnplayedRoundPolicy.adjustedScoreCountingEndByesAsDraws(participant, context);
    }

    @Override
    public Points dummyScore(ParticipantRounds participant, int index, TieBreakContext context) {
        var own = participant.score();
        var entry = participant.entries().get(index);
        var cap = entry.isForfeit()
                ? context.adjustedScore(entry.opponent().orElseThrow())
                : context.drawValue().times(context.roundsCounted());
        return own.isGreaterThan(cap) ? cap : own;
    }

    @Override
    public String dummyArticle(ParticipantRounds participant, int index) {
        return participant.entries().get(index).isForfeit() ? "C.07 16.4.1" : "C.07 16.4.2";
    }
}
