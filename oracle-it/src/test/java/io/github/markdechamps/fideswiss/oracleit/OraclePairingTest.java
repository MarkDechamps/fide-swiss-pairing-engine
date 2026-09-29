package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OraclePairingTest {

    @Test
    void readsBoardsAndTheByeFromAReply() {
        var pairing = OraclePairing.fromReply("3\n1 4\n5 2\n3 0\n");

        assertThat(pairing.boards()).containsExactlyInAnyOrder("1-4", "5-2");
        assertThat(pairing.bye()).contains("3");
    }

    @Test
    void theOrderOfBoardsIsNotPartOfThePairing() {
        assertThat(OraclePairing.fromReply("2\n1 4\n5 2\n")).isEqualTo(OraclePairing.fromReply("2\n5 2\n1 4\n"));
    }

    @Test
    void aReplyWithoutAByeHasNone() {
        assertThat(OraclePairing.fromReply("1\n1 2\n")).isEqualTo(new OraclePairing(Set.of("1-2"), Optional.empty()));
    }
}
