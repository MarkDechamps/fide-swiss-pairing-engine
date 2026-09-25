package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.Points;

/** C.07 2024-08 art. 16: the Dummy Opponent scores the participant's own final score, with no cap (16.4). */
final class UncappedDummyPolicy implements UnplayedRoundPolicy {

    @Override
    public Points adjustedScore(ParticipantRounds participant, TieBreakContext context) {
        return UnplayedRoundPolicy.adjustedScoreCountingEndByesAsDraws(participant, context);
    }

    @Override
    public Points dummyScore(ParticipantRounds participant, int index, TieBreakContext context) {
        return participant.score();
    }

    @Override
    public String dummyArticle(ParticipantRounds participant, int index) {
        return "C.07 (2024-08) 16.4";
    }
}
