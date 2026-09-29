package io.github.markdechamps.fideswiss.burstein;

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

class BursteinSystemTest {

    @Test
    void pairsTheSeedingRoundsByTheDutchSystem() {
        // 1.6: five rounds give two seeding rounds, paired following the Dutch System.
        var tournament = burstein(7);
        for (var round = 1; round <= 2; round++) {
            var pairing = tournament.pairNextRound();
            assertThat(pairing.boards())
                    .isEqualTo(PairingSystems.dutch().pairNextRound(tournament).boards());
            tournament = TournamentMother.playNextRound(
                    tournament, GameOutcome.WHITE_WINS, GameOutcome.DRAW, GameOutcome.BLACK_WINS);
        }
        assertThat(BursteinSystem.roundOf(tournament).isSeedingRound()).isFalse();
        assertThat(tournament.pairNextRound().trace().steps())
                .anySatisfy(step -> assertThat(step.toString()).contains("C.04.4.2"));
    }

    @Test
    void rejectsThePre2026Edition() {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5))
                .with(PairingSystems.burstein())
                .with(SwissRulesEdition.PRE_2026);

        // GHR 1.3: the library has only the 2026 text of this system.
        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(4)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("2026");
    }

    private static Tournament burstein(int players) {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5)).with(PairingSystems.burstein());
        return Tournament.of(settings, TournamentMother.participants(players));
    }
}
