package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.tournament.Points;

/**
 * How Unplayed Rounds count (C.07 art. 16): the score a participant's opponents see (16.3), and the score of the
 * Dummy Opponent each of its own Unplayed Rounds is scored against (16.4). The Tie-break Edition fixes the default;
 * a system such as Burstein composes its own.
 */
interface UnplayedRoundPolicy {

    /** 16.3: the Adjusted Score, used only in the opponents' tie-breaks. */
    Points adjustedScore(ParticipantRounds participant, TieBreakContext context);

    /** 16.4: the Dummy Opponent's score for the participant's own Unplayed Round {@code index}. */
    Points dummyScore(ParticipantRounds participant, int index, TieBreakContext context);

    /** The article the dummy's score comes from, for the contribution's note. */
    String dummyArticle(ParticipantRounds participant, int index);

    static UnplayedRoundPolicy of(TieBreakEdition edition) {
        return switch (edition) {
            case EDITION_2026_03 -> new CappedDummyPolicy();
            case EDITION_2024_08 -> new UncappedDummyPolicy();
        };
    }

    /** 16.3.1–16.3.2: rounds of category .5 count as draws, every other round at the points awarded. */
    static Points adjustedScoreCountingEndByesAsDraws(ParticipantRounds participant, TieBreakContext context) {
        var adjusted = Points.ZERO;
        for (var index = 0; index < participant.entries().size(); index++) {
            var entry = participant.entries().get(index);
            var toTheEnd = participant
                    .categoryOf(index)
                    .filter(UnplayedCategory.REQUESTED_BYE_TO_THE_END::equals)
                    .isPresent();
            adjusted = adjusted.plus(toTheEnd ? context.drawValue() : entry.points());
        }
        return adjusted;
    }
}
