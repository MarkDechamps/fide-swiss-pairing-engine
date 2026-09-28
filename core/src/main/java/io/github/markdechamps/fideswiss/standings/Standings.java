package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.InvalidTournamentException;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Problem;
import java.util.List;
import java.util.Objects;

/**
 * The ranking of participants by Score, with ties broken by the Tie-break List in order, each tie-break applied to
 * the subgroups still tied (C.07 4.2). Participants equal on everything share a rank; the library never draws lots.
 */
public final class Standings {

    private final int afterRounds;
    private final List<Standing> ranked;
    private final TieBreakList tieBreakList;
    private final TieBreakEdition tieBreakEdition;

    public Standings(
            int afterRounds, List<Standing> ranked, TieBreakList tieBreakList, TieBreakEdition tieBreakEdition) {
        this.afterRounds = afterRounds;
        this.ranked = List.copyOf(ranked);
        this.tieBreakList = Objects.requireNonNull(tieBreakList, "tieBreakList");
        this.tieBreakEdition = Objects.requireNonNull(tieBreakEdition, "tieBreakEdition");
    }

    /** The number of rounds these standings count. */
    public int afterRounds() {
        return afterRounds;
    }

    public List<Standing> ranked() {
        return ranked;
    }

    public Standing standing(ParticipantId participant) {
        return ranked.stream()
                .filter(standing -> standing.participant().id().equals(participant))
                .findFirst()
                .orElseThrow(() -> new InvalidTournamentException(Problem.of("Unknown participant", participant)));
    }

    /** Who ranks higher and the Deciding Tie-break: the Score, the first tie-break that differs, or a Shared Rank. */
    public RankComparison compare(ParticipantId a, ParticipantId b) {
        var first = standing(a);
        var second = standing(b);
        if (first.score().compareTo(second.score()) != 0) {
            var aHigher = first.score().isHigherThan(second.score());
            var higher = aHigher ? first : second;
            var lower = aHigher ? second : first;
            return new RankComparison(
                    higher.participant().id(),
                    lower.participant().id(),
                    new DecidingTieBreak.ByScore(higher.score(), lower.score()));
        }
        for (var index = 0; index < tieBreakList.codes().size(); index++) {
            var difference = first.tieBreakValues()
                    .get(index)
                    .compareTo(second.tieBreakValues().get(index));
            if (difference != 0) {
                var higher = difference > 0 ? first : second;
                var lower = difference > 0 ? second : first;
                return new RankComparison(
                        higher.participant().id(),
                        lower.participant().id(),
                        new DecidingTieBreak.ByTieBreak(
                                tieBreakList.codes().get(index),
                                higher.tieBreakValues().get(index),
                                lower.tieBreakValues().get(index)));
            }
        }
        return new RankComparison(a, b, new DecidingTieBreak.SharedRank());
    }

    public TieBreakList tieBreakList() {
        return tieBreakList;
    }

    public TieBreakEdition tieBreakEdition() {
        return tieBreakEdition;
    }
}
