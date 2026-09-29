package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.EdebtBoardCount;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extended Direct Encounter for teams (C.07 13.3), a type A tie-break: Direct Encounter on the primary score and, if
 * that breaks no tie, on the secondary score (13.3.1); each new subset of tied teams restarts from the primary score
 * (13.3.3). EDEBT, EDEBB, EDET and EDEB name the knock-out tie-breaks (art. 12) that then separate exactly two teams
 * still tied in both match points and game points (13.3.2), in the order of their name. Board Count ranks the
 * higher sum first by default, as Gacrux does, and the lower one first (12.1) under {@link EdebtBoardCount#LOWER}.
 */
final class ExtendedDirectEncounter implements TieBreak {

    private final TieBreakCode code;
    private final DirectEncounter directEncounter;
    private final List<BoardResults> knockOut;

    ExtendedDirectEncounter(TieBreakCode code, EdebtBoardCount edebtBoardCount) {
        this.code = code;
        this.directEncounter = new DirectEncounter(code);
        this.knockOut = switch (code.acronym()) {
            case "EDEBT" ->
                rules(code, edebtBoardCount, BoardResults.Rule.BOARD_COUNT, BoardResults.Rule.TOP_BOARD_RESULTS);
            case "EDEBB" ->
                rules(code, edebtBoardCount, BoardResults.Rule.BOARD_COUNT, BoardResults.Rule.BOTTOM_BOARD_ELIMINATION);
            case "EDET" -> rules(code, edebtBoardCount, BoardResults.Rule.TOP_BOARD_RESULTS);
            case "EDEB" -> rules(code, edebtBoardCount, BoardResults.Rule.BOTTOM_BOARD_ELIMINATION);
            default -> List.of();
        };
    }

    private static List<BoardResults> rules(
            TieBreakCode code, EdebtBoardCount edebtBoardCount, BoardResults.Rule... rules) {
        return java.util.Arrays.stream(rules)
                .map(rule -> new BoardResults(code, rule, edebtBoardCount == EdebtBoardCount.HIGHER))
                .toList();
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
            for (var team : tiers.get(tier)) {
                values.put(
                        team,
                        new TieBreakValue(
                                code,
                                Optional.of(BigDecimal.valueOf(tiers.size() - tier)),
                                true,
                                List.of(),
                                "C.07 13.3: place " + (tier + 1) + " of " + tiers.size() + " among " + group));
            }
        }
        return values;
    }

    /** 13.3.1 and 13.3.3: primary score, then secondary score, restarting on every new subset. */
    private List<List<ParticipantId>> order(List<ParticipantId> group, TieBreakContext context) {
        if (group.size() < 2) {
            return List.of(group);
        }
        var byPrimary = directEncounter.order(group, context);
        if (byPrimary.size() > 1) {
            return restarted(byPrimary, context);
        }
        var bySecondary =
                directEncounter.order(group, context.in(context.score().other()));
        if (bySecondary.size() > 1) {
            return restarted(bySecondary, context);
        }
        return group.size() == 2 ? knockedOut(group, context) : List.of(group);
    }

    private List<List<ParticipantId>> restarted(List<List<ParticipantId>> tiers, TieBreakContext context) {
        var ordered = new ArrayList<List<ParticipantId>>();
        tiers.forEach(tier -> ordered.addAll(order(tier, context)));
        return ordered;
    }

    /** 13.3.2: exactly two teams, tied in both match points and game points, separated by art. 12 in order. */
    private List<List<ParticipantId>> knockedOut(List<ParticipantId> pair, TieBreakContext context) {
        var secondary = context.in(context.score().other());
        if (secondary
                        .of(pair.get(0))
                        .score()
                        .compareTo(secondary.of(pair.get(1)).score())
                != 0) {
            return List.of(pair);
        }
        for (var rule : knockOut) {
            var values = rule.valuesFor(pair, context);
            var difference = values.get(pair.get(0)).compareTo(values.get(pair.get(1)));
            if (difference != 0) {
                return difference > 0
                        ? List.of(List.of(pair.get(0)), List.of(pair.get(1)))
                        : List.of(List.of(pair.get(1)), List.of(pair.get(0)));
            }
        }
        return List.of(pair);
    }
}
