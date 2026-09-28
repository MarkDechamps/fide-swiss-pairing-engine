package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;

import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import org.junit.jupiter.api.Test;

class DutchRoundOneTest {

    @Test
    void pairsTheTopHalfAgainstTheBottomHalfWithColoursByPairingNumberParity() {
        var tournament = TournamentMother.individualSwiss(4, 3);

        var pairing = tournament.pairNextRound();

        // C.04.3 1.1 (Pairing Numbers by rating), 3.2 (S1 = first half, S2 = second half), 3.3.1 (S1[i] meets
        // S2[i]), 5.2.5 (odd TPN gets the initial colour, White by default).
        assertThatPairing(pairing).hasBoards("1-3", "4-2").givesNoPairingAllocatedBye();
    }

    @Test
    void givesThePairingAllocatedByeToTheLowestRankedWhenTheNumberIsOdd() {
        var tournament = TournamentMother.individualSwiss(5, 3);

        var pairing = tournament.pairNextRound();

        // C.04.1 Art. 3; C.04.3 1.9.1: the one left over downfloats from the last bracket and receives the PAB.
        assertThatPairing(pairing).hasBoards("1-3", "4-2").givesPairingAllocatedByeTo("5");
    }
}
