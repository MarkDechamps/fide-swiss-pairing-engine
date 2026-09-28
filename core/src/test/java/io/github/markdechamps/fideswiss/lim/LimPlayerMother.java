package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/** Lim players for rule-level tests: id = Pairing Number, scores given in half points, colours as "WB…". */
final class LimPlayerMother {

    private LimPlayerMother() {}

    static Player player(int number, int halfPoints, String colours, int rating, int... met) {
        return player(number, halfPoints, colours, rating, false, met);
    }

    static Player player(
            int number, int halfPoints, String colours, int rating, boolean floatedPreviousRound, int... met) {
        return new Player(
                id(number),
                PairingNumber.of(number),
                Rating.of(rating),
                score(halfPoints),
                colours.chars()
                        .mapToObj(c -> c == 'W' ? Colour.WHITE : Colour.BLACK)
                        .toList(),
                Arrays.stream(met).mapToObj(LimPlayerMother::id).collect(Collectors.toSet()),
                true,
                floatedPreviousRound);
    }

    static Player withOpponents(Player player, Set<Integer> met) {
        return new Player(
                player.id(),
                player.pairingNumber(),
                player.rating(),
                player.pairingScore(),
                player.playedColours(),
                met.stream().map(LimPlayerMother::id).collect(Collectors.toSet()),
                player.mayReceivePairingAllocatedBye(),
                player.floatedPreviousRound());
    }

    static RoundToPair round(int round, boolean maxi) {
        return round(round, maxi, 9);
    }

    static RoundToPair round(int round, boolean maxi, int rounds) {
        return round(round, maxi, rounds, InitialColour.white());
    }

    static RoundToPair round(int round, boolean maxi, int rounds, InitialColour colour) {
        return new RoundToPair(
                RoundNumber.of(round),
                NumberOfRounds.of(rounds),
                colour,
                Points.of(1),
                maxi ? MaxiTournament.DECLARED : MaxiTournament.NOT_DECLARED);
    }

    static PairingScore score(int halfPoints) {
        return new PairingScore(Points.of(BigDecimal.valueOf(halfPoints, 0).divide(BigDecimal.valueOf(2))));
    }

    private static ParticipantId id(int number) {
        return ParticipantId.of(String.valueOf(number));
    }
}
