package io.github.markdechamps.fideswiss.dutch;

import java.util.List;

/**
 * C.04.3 (2026) 1.9, the round-pairing outlook: bracket by bracket from the top scoregroup down, every bracket
 * keeping the round completable ([C4]).
 */
final class Dutch2026Procedure implements DutchProcedure {

    @Override
    public List<Player> pairBrackets(List<Player> players, BracketWalk walk) {
        var scoregroups = DutchProcedure.scoregroupsFromTheTop(players);
        List<Player> movedDown = List.of();
        for (var index = 0; index < scoregroups.size(); index++) {
            var bracket = new Bracket(movedDown, scoregroups.get(index));
            movedDown = walk.pair(
                    "",
                    bracket,
                    DutchProcedure.playersBelow(scoregroups, index),
                    DutchProcedure.residentsOfNext(scoregroups, index),
                    CompletionScope.WHOLE_ROUND);
        }
        return movedDown;
    }
}
