package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import org.junit.jupiter.api.Test;

class AccelerationRecordsTest {

    private static final String PLAYERS = """
            012 Accelerated
            142 7
            152 W
            001    1      Alpha                             2600
            001    2      Bravo                             2500
            001    3      Charlie                           2400
            001    4      Delta                             2300
            """;

    @Test
    void readsXxaAsExplicitVirtualPointsPerRound() {
        var file = TrfReader.read(PLAYERS + "XXA    2  1.0  0.5\n");
        var tournament = file.tournament();

        assertThat(file.settings().acceleration()).isInstanceOf(Acceleration.Explicit.class);
        assertThat(tournament.virtualPointsOf(ParticipantId.of("2"), RoundNumber.of(1)))
                .isEqualTo(Points.of(1));
        assertThat(tournament.virtualPointsOf(ParticipantId.of("2"), RoundNumber.of(2)))
                .isEqualTo(Points.of("0.5"));
        assertThat(tournament.virtualPointsOf(ParticipantId.of("1"), RoundNumber.of(1)))
                .isEqualTo(Points.ZERO);
    }

    @Test
    void reads250AsVirtualPointsOverARangeOfRoundsAndIds() {
        var file = TrfReader.read(PLAYERS + "250      01.0 001 002 0001 0002\n");
        var tournament = file.tournament();

        assertThat(tournament.virtualPointsOf(ParticipantId.of("2"), RoundNumber.of(2)))
                .isEqualTo(Points.of(1));
        assertThat(tournament.virtualPointsOf(ParticipantId.of("3"), RoundNumber.of(1)))
                .isEqualTo(Points.ZERO);
        assertThat(tournament.virtualPointsOf(ParticipantId.of("1"), RoundNumber.of(3)))
                .isEqualTo(Points.ZERO);
    }

    @Test
    void readsABakuCodeAsBakuAcceleration() {
        var file = TrfReader.read(PLAYERS + "192 FIDE_DUTCH_2026_BAKU\n");

        assertThat(file.settings().acceleration()).isEqualTo(Acceleration.baku());
    }

    @Test
    void letsExplicitRecordsOverrideABakuCode() {
        // TRF26: for ITDX, 250 records override any acceleration implied by the 192 code.
        var file = TrfReader.read(PLAYERS + "192 FIDE_DUTCH_2026_BAKU\nXXA    2  1.0\n");

        assertThat(file.settings().acceleration()).isInstanceOf(Acceleration.Explicit.class);
    }
}
