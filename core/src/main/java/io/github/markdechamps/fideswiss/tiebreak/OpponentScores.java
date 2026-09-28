package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.ArrayList;
import java.util.List;

/**
 * The score each round's counterpart has as art. 16 sees it: an opponent's Adjusted Score for a game (16.3), and
 * the Dummy Opponent's capped score for an own Unplayed Round (16.4). With {@code /P} a forfeit is a game against
 * the scheduled opponent instead, and no longer a Voluntary Unplayed Round (Tie-break interpretation rulings #3).
 */
final class OpponentScores {

    /** One round's counterpart: its score, the points the participant scored, and how it was reached. */
    record Counterpart(
            int index, String name, Points score, Points pointsScored, boolean voluntary, List<String> notes) {}

    private OpponentScores() {}

    static List<Counterpart> of(ParticipantId participant, TieBreakContext context, boolean forfeitsAsPlayed) {
        var rounds = context.of(participant);
        var counterparts = new ArrayList<Counterpart>();
        for (var index = 0; index < rounds.entries().size(); index++) {
            var entry = rounds.entries().get(index);
            var category = rounds.categoryOf(index);
            if (entry.isGame() || (forfeitsAsPlayed && entry.isForfeit())) {
                var opponent = entry.opponent().orElseThrow();
                var notes = entry.isGame() ? List.of("C.07 16.3") : List.of("/P: forfeit as played", "C.07 16.3");
                counterparts.add(new Counterpart(
                        index, opponent.value(), context.adjustedScore(opponent), entry.points(), false, notes));
            } else {
                var unplayed = category.orElseThrow();
                counterparts.add(new Counterpart(
                        index,
                        "dummy",
                        context.dummyScore(rounds, index),
                        entry.points(),
                        unplayed.isVoluntary(),
                        List.of(unplayed.article(), context.dummyArticle(rounds, index))));
            }
        }
        return counterparts;
    }
}
