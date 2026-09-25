package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 4.3: the sequence numbers of the possible shifters. */
final class ShifterOrder {

    private ShifterOrder() {}

    /** 4.3.2 then 4.3.3: the players in sequence-number order, #1 first. */
    static List<Player> of(List<Player> pool, Colour seeking) {
        return middleOut(pool.stream().sorted(sorting(seeking)).toList());
    }

    /** 4.3.2: White seekers by ascending ARO, then Pairing Number; Black seekers by Pairing Number. */
    private static Comparator<Player> sorting(Colour seeking) {
        var byPairingNumber = Comparator.comparingInt(Player::tpn);
        return seeking == Colour.WHITE
                ? Comparator.comparingInt(Player::aro).thenComparing(byPairingNumber)
                : byPairingNumber;
    }

    /** 4.3.3: from the middle of what is left, the higher position first when two are in the middle. */
    static <T> List<T> middleOut(List<T> sorted) {
        var remaining = new ArrayList<>(sorted);
        var numbered = new ArrayList<T>();
        while (!remaining.isEmpty()) {
            numbered.add(remaining.remove((remaining.size() - 1) / 2));
        }
        return List.copyOf(numbered);
    }
}
