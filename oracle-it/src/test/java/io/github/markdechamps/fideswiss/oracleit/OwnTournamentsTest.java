package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OwnTournamentsTest {

    private static OracleGenerator failingOn(Set<Long> unfinishable, List<String> configurations) {
        return new OracleGenerator() {
            @Override
            public String name() {
                return "fake";
            }

            @Override
            public boolean isReproducible() {
                return true;
            }

            @Override
            public String generate(long seed, String configuration) {
                configurations.add(configuration);
                if (unfinishable.contains(seed)) {
                    throw new OracleGenerator.GenerationFailed(
                            "fake could not generate a tournament: No valid pairing");
                }
                return "trf-" + seed;
            }
        };
    }

    @Test
    void aTournamentTheGeneratorCannotFinishIsSkippedAndCounted() {
        var own = OwnTournaments.play(failingOn(Set.of(11L, 13L), new ArrayList<>()), 10, 10);

        assertThat(own.skipped()).isEqualTo(2);
        assertThat(own.generated())
                .extracting(OracleTournamentCheck.Generated::name)
                .containsExactly(
                        "seed-10", "seed-12", "seed-14", "seed-15", "seed-16", "seed-17", "seed-18", "seed-19");
    }

    @Test
    void theSameSeedsGiveTheSameTournamentsAndTheSameSkips() {
        var first = new ArrayList<String>();
        var second = new ArrayList<String>();

        var one = OwnTournaments.play(failingOn(Set.of(3L), first), 0, 8);
        var two = OwnTournaments.play(failingOn(Set.of(3L), second), 0, 8);

        assertThat(one).isEqualTo(two);
        assertThat(first).isEqualTo(second).allMatch(c -> c.contains("PlayersNumber=") && c.contains("RoundsNumber="));
    }

    @Test
    void moreThanFivePercentSkippedIsTooMany() {
        assertThat(new OwnTournaments(List.of(), 5).tooManySkipped(100)).isFalse();
        assertThat(new OwnTournaments(List.of(), 6).tooManySkipped(100)).isTrue();
        assertThat(new OwnTournaments(List.of(), 0).tooManySkipped(0)).isFalse();
    }

    @Test
    void theSummaryLineCountsTheSkippedTournaments() {
        var report = new OracleTournamentCheck(SwissRulesEdition.EDITION_2026, KnownDivergences.none())
                .check("fake", List.of(), 3);

        assertThat(report.summary()).contains("3 skipped");
    }
}
