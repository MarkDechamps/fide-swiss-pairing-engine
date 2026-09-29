package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.DecidingTieBreak;
import io.github.markdechamps.fideswiss.standings.Rank;
import io.github.markdechamps.fideswiss.standings.Standing;
import io.github.markdechamps.fideswiss.standings.Standings;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.standings.TieBreakValues;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Score;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ranks by Score, then applies the Tie-break List in order to each subgroup still tied (C.07 4.2). Participants
 * equal on everything share the highest place of their group; lots are never drawn.
 */
public final class StandingsCalculator {

    private final TieBreakContext context;
    private final List<TieBreak> tieBreaks;
    private final Map<ParticipantId, TieBreakValue[]> values = new HashMap<>();
    private final List<List<ParticipantId>> tiers = new ArrayList<>();

    private StandingsCalculator(TieBreakContext context, List<TieBreak> tieBreaks) {
        this.context = context;
        this.tieBreaks = tieBreaks;
    }

    /** The Standings after the tournament's first {@code rounds} rounds. */
    public static Standings standingsOf(Tournament tournament, int rounds) {
        var settings = tournament.settings();
        var tieBreaks = TieBreaks.of(
                settings.tieBreakList(), settings.tieBreakEdition(), settings.scoring(), settings.edebtBoardCount());
        var context = TieBreakContext.of(tournament, rounds, UnplayedRoundPolicy.of(settings.tieBreakEdition()));
        return new StandingsCalculator(context, tieBreaks).standings(tournament, rounds);
    }

    private Standings standings(Tournament tournament, int rounds) {
        var everyone = context.everyone();
        everyone.forEach(participant -> values.put(participant.id(), new TieBreakValue[tieBreaks.size()]));
        var byScore = everyone.stream()
                .collect(Collectors.groupingBy(
                        participant -> new Score(participant.score()),
                        () -> new java.util.TreeMap<Score, List<ParticipantId>>(Comparator.reverseOrder()),
                        Collectors.mapping(ParticipantRounds::id, Collectors.toList())));
        byScore.values().forEach(group -> rank(group, 0));
        return new Standings(
                rounds,
                ranked(),
                tournament.settings().tieBreakList(),
                tournament.settings().tieBreakEdition());
    }

    private void rank(List<ParticipantId> group, int level) {
        if (level == tieBreaks.size()) {
            tiers.add(group);
            return;
        }
        var tieBreak = tieBreaks.get(level);
        var groupValues = tieBreak.valuesFor(group, context);
        groupValues.forEach((participant, value) -> values.get(participant)[level] = value);
        if (group.size() == 1) {
            rank(group, level + 1);
            return;
        }
        var ordered = group.stream()
                .sorted((a, b) -> groupValues.get(b).compareTo(groupValues.get(a)))
                .toList();
        var subgroup = new ArrayList<ParticipantId>();
        for (var participant : ordered) {
            if (!subgroup.isEmpty()
                    && groupValues.get(subgroup.getFirst()).compareTo(groupValues.get(participant)) != 0) {
                rank(List.copyOf(subgroup), level + 1);
                subgroup.clear();
            }
            subgroup.add(participant);
        }
        rank(List.copyOf(subgroup), level + 1);
    }

    private List<Standing> ranked() {
        var standings = new ArrayList<Standing>();
        var place = 1;
        Optional<ParticipantRounds> previous = Optional.empty();
        for (var tier : tiers) {
            for (var participant : tier) {
                var rounds = context.of(participant);
                var score = new Score(rounds.score());
                var tieBreakValues = new TieBreakValues(List.of(values.get(participant)));
                var decidedBy = previous.filter(above -> above.score().equals(rounds.score()))
                        .map(above -> deciding(above.id(), participant));
                standings.add(new Standing(Rank.of(place), rounds.participant(), score, tieBreakValues, decidedBy));
                previous = Optional.of(rounds);
            }
            place += tier.size();
        }
        return standings;
    }

    private DecidingTieBreak deciding(ParticipantId above, ParticipantId below) {
        for (var level = 0; level < tieBreaks.size(); level++) {
            var higher = values.get(above)[level];
            var lower = values.get(below)[level];
            if (higher.compareTo(lower) != 0) {
                return new DecidingTieBreak.ByTieBreak(tieBreaks.get(level).code(), higher, lower);
            }
        }
        return new DecidingTieBreak.SharedRank();
    }
}
