package io.github.markdechamps.fideswiss.dubov;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import org.junit.jupiter.api.Test;

class DubovSystemTest {

    @Test
    void pairsRoundOneByPairingNumberHalvesWithColoursByParity() {
        var tournament = dubov(4);

        var pairing = tournament.pairNextRound();

        // 3.2.3: nobody has played, so G1 is the first half by TPN; 5.2.1: the odd TPN gets the initial colour.
        assertThatPairing(pairing).hasBoards("1-3", "4-2").givesNoPairingAllocatedBye();
    }

    @Test
    void givesTheByeBeforePairingToTheLowestScoreWithTheLargestPairingNumber() {
        var tournament = TournamentMother.playNextRound(dubov(5), GameOutcome.WHITE_WINS, GameOutcome.BLACK_WINS);

        var pairing = tournament.pairNextRound();

        // Round 1: 1-3, 4-2 and the PAB to 5. Round 2: 3 and 4 have no points; 5 had the PAB ([C2], 3.1.1), so
        // the bye goes to the one with the largest TPN (3.1.5).
        assertThatPairing(pairing).givesPairingAllocatedByeTo("4");
        assertThat(pairing.boards()).hasSize(2);
    }

    @Test
    void pairsTheAcceleratedGroupAsItsOwnBracketInRoundOne() {
        var settings = Profiles.acceleratedOpen(NumberOfRounds.of(9)).with(PairingSystems.dubov());
        var tournament = Tournament.of(settings, TournamentMother.participants(8));

        var pairing = tournament.pairNextRound();

        // Acceleration readings: #1-#4 have a Pairing Score of 1 and form the first bracket, #5-#8 the second;
        // 3.2.3 pairs each by TPN halves and 5.2.1 gives the odd TPN the initial colour.
        assertThatPairing(pairing).hasBoards("1-3", "4-2", "5-7", "8-6").givesNoPairingAllocatedBye();
    }

    @Test
    void rejectsThePre2026Edition() {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5))
                .with(PairingSystems.dubov())
                .with(SwissRulesEdition.PRE_2026);

        // GHR 1.3: the library has only the 2026 text of this system.
        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(4)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("2026");
    }

    private static Tournament dubov(int players) {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5)).with(PairingSystems.dubov());
        return Tournament.of(settings, TournamentMother.participants(players));
    }
}
