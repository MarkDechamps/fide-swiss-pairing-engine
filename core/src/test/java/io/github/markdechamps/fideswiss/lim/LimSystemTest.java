package io.github.markdechamps.fideswiss.lim;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import org.junit.jupiter.api.Test;

class LimSystemTest {

    @Test
    void pairsRoundOneTopHalfAgainstBottomHalfAlternatingTheLotColour() {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5)).with(PairingSystems.lim());
        var tournament = Tournament.of(settings, TournamentMother.participants(5));

        var pairing = tournament.pairNextRound();

        // 7.1: the lowest rated player gets the PAB; 7.2: 1 v 3, 4 v 2 with #1 White.
        assertThatPairing(pairing).hasBoards("1-3", "4-2").givesPairingAllocatedByeTo("5");
    }
}
