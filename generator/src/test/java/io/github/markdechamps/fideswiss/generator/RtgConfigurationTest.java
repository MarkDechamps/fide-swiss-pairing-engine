package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RtgConfigurationTest {

    private static final GeneratorSettings BASE = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9)));

    @Test
    void fixesEveryParameterAConfigFileNames() {
        var settings = RtgConfiguration.applyTo(BASE, """
                PlayersNumber=40
                RoundsNumber=9
                ForfeitRate=12
                RetiredRate=300
                HalfPointByteRate=50
                HighestRating=2650
                LowestRating=1500
                """);

        assertThat(settings.players()).isEqualTo(Range.of(40));
        assertThat(settings.rounds()).isEqualTo(Range.of(9));
        assertThat(settings.forfeitRate()).isEqualTo(Range.of(12));
        assertThat(settings.retirementRate()).contains(Range.of(300));
        assertThat(settings.halfPointByeRate()).isEqualTo(Range.of(50));
        assertThat(settings.highestRating()).isEqualTo(Range.of(2650));
        assertThat(settings.lowestRating()).isEqualTo(Range.of(1500));
    }

    @Test
    void keepsTheRandomDefaultsForMissingKeys() {
        var settings = RtgConfiguration.applyTo(BASE, "PlayersNumber=20\n");

        assertThat(settings.rounds()).isEqualTo(BASE.rounds());
        assertThat(settings.resultModel()).isInstanceOf(MilvangModel.class);
    }

    @Test
    void switchesToAFlatDrawShareForDrawPercentage() {
        var settings = RtgConfiguration.applyTo(BASE, "DrawPercentage=30\n");

        assertThat(settings.resultModel()).isEqualTo(new FlatDrawModel(30));
    }

    @Test
    void setsTheScoringFromThePointsKeys() {
        var settings = RtgConfiguration.applyTo(BASE, "PointsForWin=3\nPointsForDraw=1\nPointsForLoss=0\n");

        assertThat(settings.tournament().scoring().win()).isEqualTo(Points.of(3));
        assertThat(settings.tournament().scoring().draw()).isEqualTo(Points.of(1));
        assertThat(settings.tournament().scoring().pairingAllocatedBye()).isEqualTo(Optional.empty());
    }

    @Test
    void rejectsAValueThatIsNotANumber() {
        assertThatThrownBy(() -> RtgConfiguration.applyTo(BASE, "PlayersNumber=many\n"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PlayersNumber");
    }
}
