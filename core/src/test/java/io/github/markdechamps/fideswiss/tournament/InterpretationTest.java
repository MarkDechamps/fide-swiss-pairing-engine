package io.github.markdechamps.fideswiss.tournament;

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
}
