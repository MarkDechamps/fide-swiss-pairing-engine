package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakContribution;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Direct Encounter (C.07 art. 6), a type A tie-break: the separate standings of the games among the tied
 * participants. Repeated meetings count as their average (6.1.2); forfeits count only with {@code /P} (6.1.1). When
 * not everyone met (6.3), a participant is placed first when its lowest possible score beats every other's highest
 * possible one, whatever the missing games give; then the next place; and at the first place that cannot be decided
 * art. 6 is reapplied to those still tied (Tie-break interpretation rulings #10).
 *
 * <p>The value is the number of places from the bottom of the group: higher ranks higher, and equal values stay
 * tied.
 */
final class DirectEncounter implements TieBreak {

    private final TieBreakCode code;

    DirectEncounter(TieBreakCode code) {
        this.code = code;
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public Map<ParticipantId, TieBreakValue> valuesFor(List<ParticipantId> group, TieBreakContext context) {
        var tiers = order(group, context);
        var values = new LinkedHashMap<ParticipantId, TieBreakValue>();
        for (var tier = 0; tier < tiers.size(); tier++) {
            var value = BigDecimal.valueOf(tiers.size() - tier);
            for (var participant : tiers.get(tier)) {
                values.put(
                        participant,
                        new TieBreakValue(
                                code,
                                Optional.of(value),
                                true,
                                contributions(participant, group, context),
                                "C.07 6: place " + (tier + 1) + " of " + tiers.size() + " among " + group));
            }
        }
        return values;
    }

    /** The group from its top down, participants the text cannot separate sharing a tier. */
    List<List<ParticipantId>> order(List<ParticipantId> group, TieBreakContext context) {
        if (group.size() < 2) {
            return List.of(group);
        }
        var table = new SeparateStandings(group, context);
        return table.everyoneMet() ? byScores(table, group, context) : byPossibleScores(table, group, context);
    }

    /** 6.2: rank by the separate standings, and reapply art. 6 to each sub-tie. */
    private List<List<ParticipantId>> byScores(
            SeparateStandings table, List<ParticipantId> group, TieBreakContext context) {
        var tiers = new ArrayList<List<ParticipantId>>();
        var ordered = group.stream()
                .sorted((a, b) -> table.known(b).compareTo(table.known(a)))
                .toList();
        var tier = new ArrayList<ParticipantId>();
        for (var participant : ordered) {
            if (!tier.isEmpty() && table.known(tier.getFirst()).compareTo(table.known(participant)) != 0) {
                tiers.addAll(reapplied(tier, group, context));
                tier.clear();
            }
            tier.add(participant);
        }
        tiers.addAll(reapplied(tier, group, context));
        return tiers;
    }

    /** 6.3: place one participant at a time while the missing games cannot change who is alone at the top. */
    private List<List<ParticipantId>> byPossibleScores(
            SeparateStandings table, List<ParticipantId> group, TieBreakContext context) {
        var tiers = new ArrayList<List<ParticipantId>>();
        var remaining = new ArrayList<>(group);
        while (remaining.size() > 1) {
            var leader = remaining.stream()
                    .filter(candidate -> remaining.stream()
                            .filter(other -> !other.equals(candidate))
                            .allMatch(other -> table.lowest(candidate).compareTo(table.highest(other)) > 0))
                    .findFirst();
            if (leader.isEmpty()) {
                break;
            }
            tiers.add(List.of(leader.get()));
            remaining.remove(leader.get());
        }
        tiers.addAll(reapplied(remaining, group, context));
        return tiers;
    }

    private List<List<ParticipantId>> reapplied(
            List<ParticipantId> tied, List<ParticipantId> group, TieBreakContext context) {
        if (tied.size() < 2 || tied.size() == group.size()) {
            return List.of(List.copyOf(tied));
        }
        return order(List.copyOf(tied), context);
    }

    private List<TieBreakContribution> contributions(
            ParticipantId participant, List<ParticipantId> group, TieBreakContext context) {
        var contributions = new ArrayList<TieBreakContribution>();
        for (var entry : context.of(participant).entries()) {
            if (entry.opponent().filter(group::contains).isPresent() && counts(entry)) {
                contributions.add(new TieBreakContribution(
                        Optional.of(entry.round()),
                        entry.opponent().get().value(),
                        entry.points().toBigDecimal(),
                        false,
                        List.of("C.07 6.1")));
            }
        }
        return contributions;
    }

    private boolean counts(RoundEntry entry) {
        return entry.isGame() || (code.forfeitsAsPlayed() && entry.isForfeit());
    }

    /** The separate standings of one group: known scores, and the lowest and highest the missing games allow. */
    private final class SeparateStandings {

        private final Map<ParticipantId, BigDecimal> known = new LinkedHashMap<>();
        private final Map<ParticipantId, Integer> missing = new LinkedHashMap<>();
        private final BigDecimal win;
        private final BigDecimal loss;

        SeparateStandings(List<ParticipantId> group, TieBreakContext context) {
            this.win = context.winValue().toBigDecimal();
            this.loss = BigDecimal.ZERO;
            for (var participant : group) {
                var score = BigDecimal.ZERO;
                var unmet = 0;
                for (var opponent : group) {
                    if (opponent.equals(participant)) {
                        continue;
                    }
                    var encounters = encountersBetween(participant, opponent, context);
                    if (encounters.isEmpty()) {
                        unmet++;
                    } else {
                        score = score.add(average(encounters));
                    }
                }
                known.put(participant, score);
                missing.put(participant, unmet);
            }
        }

        boolean everyoneMet() {
            return missing.values().stream().allMatch(count -> count == 0);
        }

        BigDecimal known(ParticipantId participant) {
            return known.get(participant);
        }

        BigDecimal lowest(ParticipantId participant) {
            return known.get(participant).add(loss.multiply(BigDecimal.valueOf(missing.get(participant))));
        }

        BigDecimal highest(ParticipantId participant) {
            return known.get(participant).add(win.multiply(BigDecimal.valueOf(missing.get(participant))));
        }

        private List<BigDecimal> encountersBetween(
                ParticipantId participant, ParticipantId opponent, TieBreakContext context) {
            return context.of(participant).entries().stream()
                    .filter(entry -> entry.opponent().filter(opponent::equals).isPresent())
                    .filter(DirectEncounter.this::counts)
                    .map(entry -> entry.points().toBigDecimal())
                    .toList();
        }

        private static BigDecimal average(List<BigDecimal> scores) {
            return scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), MathContext.DECIMAL64);
        }
    }
}
