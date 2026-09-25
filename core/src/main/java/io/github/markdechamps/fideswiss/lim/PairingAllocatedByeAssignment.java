package io.github.markdechamps.fideswiss.lim;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Article 1.1 and 7.1, on Basic Rules 3 and 4: with an odd number to pair, the bye goes to the eligible player with
 * the lowest rank in the lowest scoregroup (in round 1: the lowest rated player, unrated counting as the lowest and
 * ties going to the lowest rank). "Eligible" is read to include that the bye leaves the others pairable, as Swiss
 * Team and Double-Swiss 3.4.1 say outright.
 */
final class PairingAllocatedByeAssignment {

    private static final Comparator<Player> LOWEST_RANK_FIRST =
            Comparator.comparingInt(Player::tpn).reversed();

    private final RoundToPair round;
    private final Reachability reachability;

    PairingAllocatedByeAssignment(RoundToPair round, Reachability reachability) {
        this.round = round;
        this.reachability = reachability;
    }

    Optional<Player> assign(List<Player> players) {
        var candidates = candidates(players);
        return candidates.stream()
                .filter(bye -> othersPairable(players, bye))
                .findFirst()
                .or(() -> candidates.stream().findFirst());
    }

    private boolean othersPairable(List<Player> players, Player bye) {
        var rest = new ArrayList<>(players);
        rest.remove(bye);
        return round.isFirstRound() || reachability.canPairAll(rest, round::compatible);
    }

    private List<Player> candidates(List<Player> players) {
        if (players.size() % 2 == 0) {
            return List.of();
        }
        var order = round.isFirstRound()
                ? Comparator.comparing(Player::rating).thenComparing(LOWEST_RANK_FIRST)
                : Comparator.comparing(Player::score).thenComparing(LOWEST_RANK_FIRST);
        return players.stream()
                .filter(Player::mayReceivePairingAllocatedBye)
                .sorted(order)
                .toList();
    }
}
