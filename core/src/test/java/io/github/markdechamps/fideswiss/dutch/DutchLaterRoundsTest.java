package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.DRAW;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS;

import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import org.junit.jupiter.api.Test;

class DutchLaterRoundsTest {

    @Test
    void pairsScoregroupsFromTheTopDownAndAlternatesColours() {
        var tournament = TournamentMother.individualSwiss(4, 3);
        tournament = TournamentMother.playNextRound(tournament, WHITE_WINS, DRAW);

        var pairing = tournament.pairNextRound();

        // 1 (1 point) floats into the ½-point bracket and meets 2 (1.3.2, 3.3.4); 4 floats on to meet 3.
        // Both pairs alternate from their mild preferences (1.7.3, 5.2.1).
        assertThatPairing(pairing).hasBoards("2-1", "3-4").givesNoPairingAllocatedBye();
    }
}
