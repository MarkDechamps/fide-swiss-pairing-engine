package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.PairingScore;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** How an edition of the Dutch System walks a round from bracket to bracket (2026 1.9, 2017 A.9). */
interface DutchProcedure {

    /**
     * Pairs every bracket of the round, the players given in pairing order (1.2), and returns the players the last
     * bracket leaves over: none, or the one who receives the PAB.
     */
    List<Player> pairBrackets(List<Player> players, BracketWalk walk);

    /** The scoregroups from the top down, each in pairing order (1.3.1). */
    static List<List<Player>> scoregroupsFromTheTop(List<Player> ordered) {
        Map<PairingScore, List<Player>> byScore = ordered.stream()
                .collect(Collectors.groupingBy(
                        Player::score, () -> new TreeMap<>(Comparator.reverseOrder()), Collectors.toList()));
        return List.copyOf(byScore.values());
    }

    static List<Player> playersBelow(List<List<Player>> scoregroups, int index) {
        return scoregroups.subList(index + 1, scoregroups.size()).stream()
                .flatMap(List::stream)
                .toList();
    }

    static List<Player> residentsOfNext(List<List<Player>> scoregroups, int index) {
        return index + 1 < scoregroups.size() ? scoregroups.get(index + 1) : List.of();
    }
}
