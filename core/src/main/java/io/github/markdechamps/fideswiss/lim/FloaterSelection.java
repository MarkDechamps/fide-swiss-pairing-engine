package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Article 2.3 and the Floater Selection Rules (Article 3): which players of a scoregroup are transferred to the
 * adjacent one.
 *
 * <p>The scoregroup keeps as many pairings as it can have (3.10.2), so exactly as many players float as a maximum
 * matching leaves over: those with no suitable opponent (2.3.1–2.3.3, 3.1, 4.4) and one more to make the number
 * even (2.3.4). They are chosen one at a time, each the first in the order below whose transfer still leaves that
 * many pairings:
 *
 * <ol>
 *   <li>3.9.2: avoid a floater of type a, then b, then c;
 *   <li>3.10: avoid a player who floated in the round before;
 *   <li>3.2.2: prefer a player due the colour more players are due, which tends to equalise them (in a
 *       Maxi-tournament only a player rated within 100 points of the lowest numbered one, 3.2.3);
 *   <li>3.2.4, 3.3: the lowest numbered player first.
 * </ol>
 */
final class FloaterSelection {

    /** 3.9.1, in descending order of disadvantage. */
    enum Type {
        A,
        B,
        C,
        D
    }

    private final RoundToPair round;
    private final Reachability reachability;
    private final Direction direction;
    private final Set<Player> incoming;
    private final List<Player> adjacent;

    FloaterSelection(
            RoundToPair round,
            Reachability reachability,
            Direction direction,
            Set<Player> incoming,
            List<Player> adjacent) {
        this.round = round;
        this.reachability = reachability;
        this.direction = direction;
        this.incoming = incoming;
        this.adjacent = adjacent;
    }

    List<Player> choose(List<Player> members, int pairings) {
        var floaters = new ArrayList<Player>();
        while (members.size() - floaters.size() > 2 * pairings) {
            var staying = new ArrayList<>(members);
            staying.removeAll(floaters);
            floaters.add(next(staying, floaters, pairings));
        }
        return floaters;
    }

    private Player next(List<Player> staying, List<Player> chosen, int pairings) {
        var lowestNumbered = staying.stream().max(direction.order()).orElseThrow();
        var majority = majorityDueColour(staying);
        var preference = Comparator.<Player, Type>comparing(player -> type(player, chosen), Comparator.reverseOrder())
                .thenComparing(Player::floatedPreviousRound)
                .thenComparing(player -> !tendsToEqualise(player, majority, lowestNumbered))
                .thenComparing(direction.order().reversed());
        return staying.stream()
                .sorted(preference)
                .filter(player -> leavesPairings(staying, player, pairings))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No floater keeps " + pairings + " pairings"));
    }

    private boolean leavesPairings(List<Player> staying, Player floater, int pairings) {
        var rest = new ArrayList<>(staying);
        rest.remove(floater);
        return reachability.maximumPairs(rest, round::compatible) == pairings;
    }

    /** 3.9.1: whether it already floated into this scoregroup, and whether it has a compatible opponent next door. */
    Type type(Player player, List<Player> chosen) {
        var compatible = hasCompatibleOpponentInAdjacent(player, chosen);
        if (incoming.contains(player)) {
            return compatible ? Type.B : Type.A;
        }
        return compatible ? Type.D : Type.C;
    }

    /** 3.3, 3.4: after excluding the opponents of the floaters already chosen that rank ahead of it. */
    private boolean hasCompatibleOpponentInAdjacent(Player player, List<Player> chosen) {
        var ahead = chosen.stream()
                .filter(floater -> Floater.byScoreThenNumber(direction).compare(floater, player) < 0)
                .toList();
        if (ahead.isEmpty()) {
            return adjacent.stream().anyMatch(opponent -> round.compatible(player, opponent));
        }
        var withPlayer = new ArrayList<>(ahead);
        withPlayer.add(player);
        return reachability.opponentsFound(withPlayer, adjacent, round::compatible)
                > reachability.opponentsFound(ahead, adjacent, round::compatible);
    }

    private static Optional<Colour> majorityDueColour(List<Player> players) {
        var white = players.stream()
                .filter(player -> player.dueColour().equals(Optional.of(Colour.WHITE)))
                .count();
        var black = players.stream()
                .filter(player -> player.dueColour().equals(Optional.of(Colour.BLACK)))
                .count();
        return white == black ? Optional.empty() : Optional.of(white > black ? Colour.WHITE : Colour.BLACK);
    }

    private boolean tendsToEqualise(Player player, Optional<Colour> majority, Player lowestNumbered) {
        var dueTheMajorityColour = majority.isPresent() && player.dueColour().equals(majority);
        return dueTheMajorityColour
                && (!round.isMaxiTournament() || RoundToPair.withinMaxiBand(player, lowestNumbered));
    }
}
