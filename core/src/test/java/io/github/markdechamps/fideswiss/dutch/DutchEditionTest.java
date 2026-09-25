package io.github.markdechamps.fideswiss.dutch;

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

class DutchEditionTest {

    @Test
    void pairsRoundOneOfThePre2026EditionLikeTheOldText() {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(3)).with(SwissRulesEdition.PRE_2026);

        var pairing = Tournament.of(settings, TournamentMother.participants(5)).pairNextRound();

        // Dutch 2017 B.1-B.3 and E.5 give round 1 the same shape as 2026 3.3 and 5.2.5.
        assertThatPairing(pairing).hasBoards("1-3", "4-2").givesPairingAllocatedByeTo("5");
    }

    @Test
    void rejectsASystemPinnedToAnotherEditionThanTheSettings() {
        var settings =
                Profiles.individualSwiss(NumberOfRounds.of(3)).with(PairingSystems.dutch(SwissRulesEdition.PRE_2026));

        // GHR 1.3: the system and the edition are declared together, so they must agree.
        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(4)))
                .isInstanceOf(InvalidSettingsException.class);
    }
}
