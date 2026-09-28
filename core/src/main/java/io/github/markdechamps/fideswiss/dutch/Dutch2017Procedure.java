package io.github.markdechamps.fideswiss.dutch;

import java.util.ArrayList;
import java.util.List;

/**
 * C.04.3 (till 2026-01-31) A.9, the round-pairing outlook of Dutch 2017, as its own procedure object.
 *
 * <p>Brackets are paired from the top scoregroup down with no completion requirement, looking "just in the
 * following bracket" (C.7). After each one, if its downfloaters and every player below cannot complete the round,
 * that bracket is the Penultimate Pairing Bracket: it is paired again, choosing its downfloaters so as to complete
 * the round (C.4, above C.5) and without C.7. Its downfloaters and every lower player form the Collapsed Last
 * Bracket, paired once to complete the round (readings R1, R2). Without a PPB, the last scoregroup's bracket
 * completes it.
 */
final class Dutch2017Procedure implements DutchProcedure {

    @Override
    public List<Player> pairBrackets(List<Player> players, BracketWalk walk) {
        var scoregroups = DutchProcedure.scoregroupsFromTheTop(players);
        List<Player> movedDown = List.of();
        for (var index = 0; index < scoregroups.size(); index++) {
            var bracket = new Bracket(movedDown, scoregroups.get(index));
            var below = DutchProcedure.playersBelow(scoregroups, index);
            if (below.isEmpty()) {
                return walk.pair("last", bracket, List.of(), List.of(), CompletionScope.WHOLE_ROUND);
            }
            var ordinary = walk.tryPair(
                    bracket,
                    below,
                    DutchProcedure.residentsOfNext(scoregroups, index),
                    CompletionScope.FOLLOWING_BRACKET_ONLY);
            if (allowCompletion(walk, ordinary.candidate().downfloaters(), below)) {
                movedDown = walk.record("", bracket, ordinary);
                continue;
            }
            var penultimateDownfloaters = walk.pair("PPB", bracket, below, List.of(), CompletionScope.WHOLE_ROUND);
            var collapsedLastBracket = new Bracket(penultimateDownfloaters, below);
            return walk.pair("CLB", collapsedLastBracket, List.of(), List.of(), CompletionScope.WHOLE_ROUND);
        }
        return movedDown;
    }

    /** R1: C.1–C.3 on the downfloaters and every lower player, one PAB-eligible leftover allowed. */
    private static boolean allowCompletion(BracketWalk walk, List<Player> downfloaters, List<Player> below) {
        var notYetPaired = new ArrayList<>(downfloaters);
        notYetPaired.addAll(below);
        return walk.allowsCompletion(notYetPaired);
    }
}
