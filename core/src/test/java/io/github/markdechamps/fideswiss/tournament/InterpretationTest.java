package io.github.markdechamps.fideswiss.tournament;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class InterpretationTest {

    @Test
    void isRefusedByASystemThatHasNoSuchReading() {
        // ADR 0003: an Interpretation belongs to the system whose article is ambiguous; the Dutch System has none.
        var dutch = Profiles.individualSwiss(NumberOfRounds.of(5));

        assertThatThrownBy(() -> dutch.with(UpfloaterLookAhead.graded()))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("GRADED");
    }

    @Test
    void ranksTheHigherBoardCountFirstInEveryTeamKindOfEventByDefault() {
        // ADR 0009: EDEBT is a tie-break reading, so it belongs to the standings settings, not to a pairing system.
        var rounds = NumberOfRounds.of(5);

        assertThat(Profiles.teamSwiss(rounds).edebtBoardCount()).isEqualTo(EdebtBoardCount.HIGHER);
        assertThat(Profiles.olympiad(rounds).edebtBoardCount()).isEqualTo(EdebtBoardCount.HIGHER);
    }

    @Test
    void takesTheLiteralBoardCountOrderForAnyTeamFileWhateverItsPairingSystem() {
        var rounds = NumberOfRounds.of(5);

        assertThat(Profiles.olympiad(rounds).with(EdebtBoardCount.lower()).edebtBoardCount())
                .isEqualTo(EdebtBoardCount.LOWER);
        assertThat(Profiles.doubleSwiss(rounds).with(EdebtBoardCount.lower()).edebtBoardCount())
                .isEqualTo(EdebtBoardCount.LOWER);
        assertThat(Profiles.teamSwiss(rounds).with(EdebtBoardCount.lower()).edebtBoardCount())
                .isEqualTo(EdebtBoardCount.LOWER);
    }

    @Test
    void keepsTheBoardCountOrderWhenThePairingSystemIsReplaced() {
        var literal = Profiles.teamSwiss(NumberOfRounds.of(5)).with(EdebtBoardCount.lower());

        assertThat(literal.with(io.github.markdechamps.fideswiss.pairing.PairingSystems.olympiad())
                        .edebtBoardCount())
                .isEqualTo(EdebtBoardCount.LOWER);
    }
}
