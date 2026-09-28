package io.github.markdechamps.fideswiss.lim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pairs the players of a scoregroup once its floaters are out, so that every one of them can be paired: the
 * incoming floaters first (3.6–3.8), then the others by scrutiny and exchange (Article 4), each player also getting
 * the colour due to them where an exchange allows it (5.2, 5.7).
 *
 * <p>An opponent is <em>available</em> when the two are compatible (2.1) and the players left over can still all
 * be paired (4.4: "must at the same time leave Player #1 with a compatible opponent").
 */
final class Scrutiny {

    private final RoundToPair round;
    private final Reachability reachability;
    private final Direction direction;
    private final List<Player> remaining;
    private final List<Pair> pairs = new ArrayList<>();

    Scrutiny(RoundToPair round, Reachability reachability, Direction direction, List<Player> players) {
        this.round = round;
        this.reachability = reachability;
        this.direction = direction;
        this.remaining = new ArrayList<>(players);
    }

    List<Pair> pair(List<Player> floatersInOrder) {
        for (var floater : floatersInOrder) {
            if (remaining.contains(floater)) {
                pairWithFirstAvailable(floater, arranged(without(floater)));
            }
        }
        while (!remaining.isEmpty()) {
            var arranged = arranged(remaining);
            pairWithFirstAvailable(arranged.getFirst(), ExchangeOrder.forFirstOf(arranged));
        }
        return List.copyOf(pairs);
    }

    /**
     * The first available opponent in {@code candidates} due the other colour, if any, otherwise the first
     * available one. In a Maxi-tournament the exchange for colour is allowed only between opponents rated within
     * 100 points (3.8, 5.7).
     */
    private void pairWithFirstAvailable(Player player, List<Player> candidates) {
        Optional<Player> first = Optional.empty();
        Optional<Player> suited = Optional.empty();
        for (var candidate : candidates) {
            if (!isAvailable(player, candidate)) {
                continue;
            }
            if (first.isEmpty()) {
                first = Optional.of(candidate);
            }
            if (RoundToPair.dueColoursSuit(player, candidate)
                    && (!round.isMaxiTournament() || RoundToPair.withinMaxiBand(candidate, first.get()))) {
                suited = Optional.of(candidate);
                break;
            }
        }
        var firstAvailable = first;
        var opponent = suited.or(() -> firstAvailable)
                .orElseThrow(() -> new IllegalStateException("No available opponent for " + player));
        pairs.add(new Pair(player, opponent));
        remaining.remove(player);
        remaining.remove(opponent);
    }

    private boolean isAvailable(Player player, Player candidate) {
        if (!round.compatible(player, candidate)) {
            return false;
        }
        var rest = new ArrayList<>(remaining);
        rest.remove(player);
        rest.remove(candidate);
        return proposalIsCompatible(arranged(rest)) || reachability.canPairAll(rest, round::compatible);
    }

    /** A shortcut, not a rule: when the plain 2.4 proposal of the rest is compatible, the rest can be paired. */
    private boolean proposalIsCompatible(List<Player> arranged) {
        return arranged.size() % 2 == 0
                && ExchangeOrder.proposed(arranged).stream()
                        .allMatch(pair -> round.compatible(pair.first(), pair.second()));
    }

    private List<Player> without(Player player) {
        var rest = new ArrayList<>(remaining);
        rest.remove(player);
        return rest;
    }

    private List<Player> arranged(List<Player> players) {
        return players.stream().sorted(direction.order()).toList();
    }
}
