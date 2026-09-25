package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.matching.CheapestPerfectMatching;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** 3.1: the Pairing-Allocated Bye, assigned before any bracket is paired. */
final class PairingAllocatedByeAssignment {

    /** 3.1.3 lowest score, 3.1.4 most games played over the board, 3.1.5 largest Pairing Number. */
    private static final Comparator<Player> PREFERENCE = Comparator.comparing(Player::score)
            .thenComparing(Comparator.comparingInt(Player::gamesPlayed).reversed())
            .thenComparing(Comparator.comparingInt(Player::tpn).reversed());

    private final RoundToPair round;

    PairingAllocatedByeAssignment(RoundToPair round) {
        this.round = round;
    }

    /** Nobody for an even number of players; for an odd number, the first eligible player of 3.1.1–3.1.5. */
    Optional<Player> assign(List<Player> players) {
        if (players.size() % 2 == 0) {
            return Optional.empty();
        }
        return Optional.of(players.stream()
                .filter(Player::mayReceivePairingAllocatedBye)
                .sorted(PREFERENCE)
                .filter(bye -> leavesOthersPairable(players, bye))
                .findFirst()
                .orElseThrow(() -> new NoRoundPairingException(
                        "C.04.4.1 3.1", "no Pairing-Allocated Bye leaves the others pairable")));
    }

    /** 3.1.2 with [C4]. */
    private boolean leavesOthersPairable(List<Player> players, Player bye) {
        var rest = players.stream().filter(player -> player != bye).toList();
        return CheapestPerfectMatching.canPairAll(rest, round::mayMeet);
    }
}
