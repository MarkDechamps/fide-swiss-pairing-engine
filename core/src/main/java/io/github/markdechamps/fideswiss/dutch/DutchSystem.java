package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The Dutch System (C.04.3). */
public final class DutchSystem implements PairingSystem {

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var numbers = tournament.pairingNumbers();
        var ranked = numbers.inOrder();
        var half = ranked.size() / 2;
        var boards = new ArrayList<PairedBoard>();
        for (var index = 0; index < half; index++) {
            var higher = ranked.get(index);
            var lower = ranked.get(half + index);
            var colour = tournament.settings().initialColour().forPairingNumber(numbers.numberOf(higher));
            var board = BoardNumber.of(index + 1);
            boards.add(
                    colour == Colour.WHITE
                            ? new PairedBoard(board, higher, lower)
                            : new PairedBoard(board, lower, higher));
        }
        return new RoundPairing(
                tournament.nextRound(),
                List.copyOf(boards),
                Optional.empty(),
                Map.of(),
                numbers,
                new PairingTrace(List.of()));
    }
}
