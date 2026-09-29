package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Burstein players for rule-level tests: id = TPN, scores and Buchholz in whole points. */
final class BursteinPlayerMother {

    private BursteinPlayerMother() {}

    static Player player(int number, int score, List<Colour> colours, Set<Integer> met, int buchholz) {
        return player(number, score, colours, met, buchholz, true);
    }

    static Player player(
            int number, int score, List<Colour> colours, Set<Integer> met, int buchholz, boolean mayHaveBye) {
        return new Player(
                ParticipantId.of(String.valueOf(number)),
                PairingNumber.of(number),
                new PairingScore(Points.of(score)),
                colours,
                met.stream().map(id -> ParticipantId.of(String.valueOf(id))).collect(Collectors.toSet()),
                mayHaveBye,
                new OppositionIndex(Points.of(buchholz), BigDecimal.ZERO));
    }
}
