package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Dubov players for rule-level tests: id = Pairing Number, rating falling with it, scores in whole points. */
final class DubovPlayerMother {

    private DubovPlayerMother() {}

    static Player fresh(int number) {
        return player(number, 0, List.of(), Set.of(), 0);
    }

    static Player player(int number, int score, List<Colour> colours, Set<Integer> met, int upfloats) {
        return new Player(
                ParticipantId.of(String.valueOf(number)),
                PairingNumber.of(number),
                2600 - number,
                new PairingScore(Points.of(score)),
                colours,
                met.stream()
                        .map(id -> ParticipantId.of(String.valueOf(id)))
                        .collect(java.util.stream.Collectors.toSet()),
                colours.stream().map(colour -> 2000).toList(),
                true,
                upfloats,
                false);
    }

    static Player withOpponentRatings(Player player, Integer... ratings) {
        return new Player(
                player.id(),
                player.pairingNumber(),
                player.rating(),
                player.pairingScore(),
                player.playedColours(),
                player.met(),
                Arrays.asList(ratings),
                player.mayReceivePairingAllocatedBye(),
                player.upfloats(),
                player.upfloatedPreviousRound());
    }
}
