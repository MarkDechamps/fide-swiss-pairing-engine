package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class KnownDivergencesTest {

    private static final String HALF_POINT_BYE = "001    4      Player 0004    2413   2.0   13 b 1  0000 - H\r\n";

    private final KnownDivergences register = KnownDivergences.register();

    @Test
    void javafoWithAHalfPointByeIsKd4() {
        assertThat(register.classify("JaVaFo 2.2", HALF_POINT_BYE)).contains("KD-4");
    }

    @Test
    void javafoWithoutAHalfPointByeIsUnregistered() {
        assertThat(register.classify("JaVaFo 2.2", "001    4      Player 0004    2413   2.0   13 b 1\r\n"))
                .isEmpty();
    }

    @Test
    void anotherOracleWithAHalfPointByeIsUnregistered() {
        assertThat(register.classify("bbpPairings v5.0.1", HALF_POINT_BYE)).isEmpty();
    }

    @Test
    void gacruxWithVirtualPointsIsKd3() {
        assertThat(register.classify("Gacrux @ 6419149 (tpn-order)", "250  2.0  0.0   1   2    1    4\r\n"))
                .contains("KD-3");
    }

    @Test
    void anEmptyRegisterRecognisesNothing() {
        assertThat(KnownDivergences.none().classify("JaVaFo 2.2", HALF_POINT_BYE))
                .isEmpty();
    }

    @Test
    void parsesTabSeparatedLinesAndSkipsComments() {
        var parsed = KnownDivergences.parse("# note\n\nKD-9\tX\tfoo\n");

        assertThat(parsed.classify("X program", "a foo b")).contains("KD-9");
    }

    @Test
    void refusesAMalformedLine() {
        assertThatThrownBy(() -> KnownDivergences.parse("KD-9\tX")).isInstanceOf(IllegalArgumentException.class);
    }
}
