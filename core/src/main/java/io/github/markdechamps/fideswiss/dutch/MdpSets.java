package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.PairingScore;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Article 4.4: the sets of M1 pairable MDPs, valid when the Limbo they leave complies with [C7]. "Complies" is
 * read as floating the lowest scores that can actually be achieved, not the lowest in the list, since an MDP who
 * can meet no resident stays in the Limbo whatever its score (a documented reading of 4.4.1).
 */
final class MdpSets {

    private MdpSets() {}

    /** Combinations come out in "smallest differing BSN" order (4.4.2) already. */
    static Stream<List<Player>> validInOrder(List<Player> movedDown, List<Player> bestAchievableLimbo) {
        var m1 = movedDown.size() - bestAchievableLimbo.size();
        var lowestAchievableScores = limboScores(bestAchievableLimbo);
        return Combinations.of(movedDown, m1)
                .filter(s1 -> limboScores(limboOf(movedDown, s1)).equals(lowestAchievableScores));
    }

    static List<Player> limboOf(List<Player> movedDown, List<Player> s1) {
        return movedDown.stream().filter(mdp -> !s1.contains(mdp)).toList();
    }

    private static List<PairingScore> limboScores(List<Player> limbo) {
        return limbo.stream()
                .map(Player::score)
                .sorted(Comparator.reverseOrder())
                .toList();
    }
}
