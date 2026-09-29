package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PairingSystemCodeTest {

    private static final String PLAYERS =
            "001    1      Alpha                             2600                             0.0\r"
                    + "001    2      Bravo                             2500                             0.0\r";

    @ParameterizedTest
    @CsvSource({
        "FIDE_DUTCH, C.04.3 Dutch System 2026",
        "FIDE_DUTCH_2026, C.04.3 Dutch System 2026",
        "FIDE_DUTCH_2025, C.04.3 Dutch System 2026",
        "FIDE_DUBOV, C.04.4.1 Dubov System 2026",
        "FIDE_DUBOV_2026, C.04.4.1 Dubov System 2026",
        "FIDE_LIM, C.04.4.3 Lim System 2026",
        "FIDE_LIM_2026, C.04.4.3 Lim System 2026",
        "FIDE_DUTCH_BAKU, C.04.3 Dutch System 2026",
        "FIDE_LIM_BAKU, C.04.4.3 Lim System 2026"
    })
    void selectsThePairingSystemFromRecord192(String code, String system) {
        var file = TrfReader.read("012 Test\r192 " + code + "\r" + PLAYERS);

        assertThat(file.settings().pairingSystem().name()).isEqualTo(system);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUSTOM_SWISS", "FIDE_BURSTEIN", "FIDE_BURSTEIN_BAKU", "NONSENSE"})
    void rejectsACodeItCannotPair(String code) {
        assertThatThrownBy(() -> TrfReader.read("012 Test\r192 " + code + "\r" + PLAYERS))
                .isInstanceOf(InvalidTrfException.class)
                .hasMessageContaining(code);
    }

    @ParameterizedTest
    @ValueSource(strings = {"012 Test\r", "012 Test\r192 \r"})
    void defaultsToTheProfilesSystemWithoutACode(String header) {
        assertThat(TrfReader.read(header + PLAYERS).settings().pairingSystem().name())
                .isEqualTo("C.04.3 Dutch System 2026");
    }
}
