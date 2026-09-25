package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.Score;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * The Lim System (C.04.4.3, 2026) for one round: the PAB (Article 1), then the scoregroups in the order of 2.2
 * (from the top down to just above the Median Scoregroup, from the bottom up to just below it, the Median
 * Scoregroup last and downward), each passing its floaters on towards the median (3.2.1); a blocked Median
 * Scoregroup cracks pairings of its neighbours (2.6); then the colours (Article 5). Round 1 is Article 7.
 */
final class LimProcedure {

    record Outcome(List<Game> games, Optional<Player> pairingAllocatedBye, int crackedPairings) {}

    /** A pairing and whether it was made in the Median Scoregroup or above (5.4). */
    private record Made(Pair pair, boolean medianOrAbove) {}

    private final RoundToPair round;
    private final Reachability reachability;
    private int crackedPairings;

    LimProcedure(RoundToPair round, Reachability reachability) {
        this.round = round;
        this.reachability = reachability;
    }

    Outcome pair(List<Player> players) {
        var bye = new PairingAllocatedByeAssignment(round, reachability).assign(players);
        var toPair = players.stream()
                .filter(player -> bye.filter(assignee -> assignee == player).isEmpty())
                .toList();
        return round.isFirstRound() ? roundOne(toPair, bye) : laterRound(toPair, bye);
    }

    /** 7.2: 1 v n/2+1, ...; #1 takes the lot colour and the odd-numbered players of the top half with it. */
    private Outcome roundOne(List<Player> players, Optional<Player> bye) {
        var list = players.stream().sorted(Comparator.comparingInt(Player::tpn)).toList();
        var half = list.size() / 2;
        var games = new ArrayList<Game>();
        for (var index = 0; index < half; index++) {
            var top = list.get(index);
            var bottom = list.get(index + half);
            var initial = round.initialColour().colour();
            var colourOfTop = index % 2 == 0 ? initial : initial.opposite();
            games.add(
                    colourOfTop == Colour.WHITE
                            ? new Game(top, bottom, "C.04.4.3 7.2")
                            : new Game(bottom, top, "C.04.4.3 7.2"));
        }
        return new Outcome(List.copyOf(games), bye, 0);
    }

    private Outcome laterRound(List<Player> players, Optional<Player> bye) {
        if (!reachability.canPairAll(players, round::compatible)) {
            throw new NoRoundPairingException("C.04.4.3 2.1", "no compatible pairing of every player exists");
        }
        var scoregroups =
                players.stream().collect(Collectors.groupingBy(Player::score, TreeMap::new, Collectors.toList()));
        var median = round.medianScore();
        var higherSide = new ArrayDeque<Made>();
        var lowerSide = new ArrayDeque<Made>();
        var downfloaters =
                pairSide(higherScores(scoregroups.keySet(), median), Direction.DOWNWARD, scoregroups, higherSide);
        var upfloaters = pairSide(lowerScores(scoregroups.keySet(), median), Direction.UPWARD, scoregroups, lowerSide);
        var medianPairs = pairMedian(
                scoregroups.getOrDefault(median, List.of()), downfloaters, upfloaters, higherSide, lowerSide);
        var made = new ArrayList<>(higherSide.reversed());
        made.addAll(lowerSide.reversed());
        medianPairs.forEach(pair -> made.add(new Made(pair, true)));
        var colours = new ColourAllocation(round);
        var games = made.stream()
                .map(pairing -> colours.allocate(pairing.pair(), pairing.medianOrAbove()))
                .toList();
        return new Outcome(games, bye, crackedPairings);
    }

    /** 2.2: the scoregroups above the median, from the top down. */
    static List<Score> higherScores(Set<Score> scores, Score median) {
        return scores.stream()
                .filter(score -> score.isHigherThan(median))
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    /** 2.2: the scoregroups below the median, from the bottom up. */
    static List<Score> lowerScores(Set<Score> scores, Score median) {
        return scores.stream().filter(median::isHigherThan).sorted().toList();
    }

    /** Pairs one side of the median in the order of 2.2 and returns the floaters it hands to the median. */
    private List<Player> pairSide(
            List<Score> scores, Direction direction, Map<Score, List<Player>> scoregroups, Deque<Made> made) {
        var incoming = new ArrayList<Floater>();
        for (var index = 0; index < scores.size(); index++) {
            var adjacent = index + 1 < scores.size()
                    ? scoregroups.get(scores.get(index + 1))
                    : scoregroups.getOrDefault(round.medianScore(), List.of());
            var result = new ScoregroupPairing(round, reachability, direction)
                    .pair(scoregroups.get(scores.get(index)), incoming, adjacent);
            var medianOrAbove = direction == Direction.DOWNWARD;
            result.pairs().forEach(pair -> made.push(new Made(pair, medianOrAbove)));
            incoming = new ArrayList<>(result.floaters().stream()
                    .map(floater -> new Floater(floater, true))
                    .toList());
        }
        return incoming.stream().map(Floater::player).toList();
    }

    /**
     * 2.2, 2.6: the Median Scoregroup, paired downward with its downfloaters first and its upfloaters next (3.6.3).
     * While it cannot be paired completely, the last pairing made on a neighbouring side is cracked: the lower side
     * when more floaters came from above than from below (2.6.1), otherwise the higher side (2.6.2), and the other
     * side when that one has none left. Both players join the median as floaters from that side.
     */
    private List<Pair> pairMedian(
            List<Player> residents,
            List<Player> downfloaters,
            List<Player> upfloaters,
            Deque<Made> higherSide,
            Deque<Made> lowerSide) {
        var fromAbove = new ArrayList<>(downfloaters);
        var fromBelow = new ArrayList<>(upfloaters);
        while (true) {
            var members = new ArrayList<>(residents);
            members.addAll(fromAbove);
            members.addAll(fromBelow);
            if (reachability.canPairAll(members, round::compatible)) {
                var incoming = new ArrayList<Floater>();
                fromAbove.forEach(player -> incoming.add(new Floater(player, true)));
                fromBelow.forEach(player -> incoming.add(new Floater(player, false)));
                return new ScoregroupPairing(round, reachability, Direction.DOWNWARD)
                        .pair(residents, incoming, List.of())
                        .pairs();
            }
            var crackLower = fromAbove.size() > fromBelow.size() && !lowerSide.isEmpty() || higherSide.isEmpty();
            var side = crackLower ? lowerSide : higherSide;
            if (side.isEmpty()) {
                throw new NoRoundPairingException("C.04.4.3 2.6", "the Median Scoregroup stays blocked");
            }
            var cracked = side.pop().pair();
            (crackLower ? fromBelow : fromAbove).addAll(List.of(cracked.first(), cracked.second()));
            crackedPairings++;
        }
    }
}
