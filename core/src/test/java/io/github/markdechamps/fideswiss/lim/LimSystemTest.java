package io.github.markdechamps.fideswiss.lim;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
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

    @Test
    void pairsTheAcceleratedGroupAboveTheMedianInRoundOne() {
        var settings = Profiles.acceleratedOpen(NumberOfRounds.of(9)).with(PairingSystems.lim());
        var tournament = Tournament.of(settings, TournamentMother.participants(8));

        var pairing = tournament.pairNextRound();

        // Acceleration readings: #1-#4 have a Pairing Score of 1 over a median of 0 and pair inside their
        // scoregroup (Article 4); #5-#8 form the median. Nobody has played, so 5.4 gives the lot colour.
        assertThatPairing(pairing).hasBoards("1-3", "2-4", "5-7", "6-8").givesNoPairingAllocatedBye();
    }

    @Test
    void rejectsThePre2026Edition() {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5))
                .with(PairingSystems.lim())
                .with(SwissRulesEdition.PRE_2026);

        // GHR 1.3: the library has only the 2026 text of this system.
        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(4)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("2026");
    }
}
