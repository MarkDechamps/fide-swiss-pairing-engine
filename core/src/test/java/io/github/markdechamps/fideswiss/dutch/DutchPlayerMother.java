package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Players as one bracket of the Dutch System sees them: a Pairing Number (the id too), a score, the colours of
 * the rounds so far ({@code "WB-B"}: White, Black, an unplayed round, Black) and the floats of those rounds. Each
 * round is drawn against a stand-in opponent numbered 50 or more, so players made here have never met.
 */
final class DutchPlayerMother {

    private static final int FIRST_STAND_IN = 50;

    private DutchPlayerMother() {}

    static Player player(int pairingNumber, String score) {
        return player(pairingNumber, score, "");
    }

    static Player player(int pairingNumber, String score, String colours) {
        return player(pairingNumber, score, colours, List.of());
    }

    static Player player(int pairingNumber, String score, String colours, List<FloatDirection> floats) {
        var tournament = TournamentMother.individualSwiss(FIRST_STAND_IN + colours.length(), colours.length() + 1);
        for (var round = 0; round < colours.length(); round++) {
            var standIn = String.valueOf(FIRST_STAND_IN + round);
            var own = String.valueOf(pairingNumber);
            tournament = switch (colours.charAt(round)) {
                case 'W' -> RecordedRoundMother.withRound(tournament, own + "-" + standIn + " ½");
                case 'B' -> RecordedRoundMother.withRound(tournament, standIn + "-" + own + " ½");
                default -> RecordedRoundMother.withRound(tournament, own + " ½bye");
            };
        }
        return new Player(
                id(pairingNumber),
                PairingNumber.of(pairingNumber),
                new PairingScore(Points.of(score)),
                TournamentHistory.of(tournament).of(id(pairingNumber)),
                floats);
    }

    /** Players 1 to {@code count}, all on the same score and with no games yet. */
    static List<Player> players(int count, String score) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(pairingNumber -> player(pairingNumber, score))
                .toList();
    }
}
