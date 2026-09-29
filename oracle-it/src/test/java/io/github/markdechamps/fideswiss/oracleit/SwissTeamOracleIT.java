package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.markdechamps.fideswiss.generator.Corpus;
import io.github.markdechamps.fideswiss.generator.CorpusSeed;
import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.generator.TournamentGenerator;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.SecondaryScore;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * The nightly Oracle gate for the Swiss Team System (Verification strategy): the team tournaments our generator plays
 * are re-paired round by round by Gacrux, with Ruling G1 applied ({@link GacruxProgram}), and every round must be
 * identical. Skipped, with the way to configure it, when Gacrux is not configured ({@link OraclePrograms#GACRUX}).
 * The corpus is {@code fideswiss.oracle.tournaments} tournaments (default 100) from {@code fideswiss.oracle.seed}, in
 * every colour preference type and score format, with the events in {@code fideswiss.oracle.events}. Baku is played
 * only where the secondary score is not used for colours: Gacrux adds Virtual Points to it (Known Divergence KD-3).
 */
class SwissTeamOracleIT {

    private static final CorpusSeed CORPUS = CorpusSeed.of(Long.getLong("fideswiss.oracle.seed", 20260929L));
    private static final int TOURNAMENTS = Integer.getInteger("fideswiss.oracle.tournaments", 100);
    private static final String ALL_EVENTS = "forfeits,byes";

    private static final Range NEVER = Range.of(1_000_000, 1_000_000);
    private static final Path FAILURES = Path.of("target", "oracle-failures");

    @Test
    void gacruxPairsSwissTeam2026AsWeDo() throws IOException {
        var oracle = OraclePrograms.GACRUX.locate();
        assumeTrue(oracle.isPresent(), OraclePrograms.GACRUX::absence);

        var report = new OracleGate(oracle.get(), SwissRulesEdition.EDITION_2026).compare(corpus(events()));

        System.out.println(report.summary());
        for (var difference : report.differences()) {
            System.out.println("  " + difference.describe());
            save(difference);
        }
        assertThat(report.differences()).as(report.summary()).isEmpty();
    }

    /** {@code fideswiss.oracle.events} (a comma list) overrides the events played. */
    private static Set<String> events() {
        return Set.of(System.getProperty("fideswiss.oracle.events", ALL_EVENTS).split(","));
    }

    private static List<GeneratedTournament.Completed> corpus(Set<String> events) {
        var settings = GeneratorSettings.ofTeams(Profiles.teamSwiss(NumberOfRounds.of(9)))
                .withPlayers(Range.of(6, 24))
                .withRounds(Range.of(4, 11))
                .withBoards(Range.of(2, 6))
                .withForfeitRate(events.contains("forfeits") ? Range.of(30, 200) : NEVER)
                .withHalfPointByeRate(events.contains("byes") ? Range.of(40, 400) : NEVER)
                .withZeroPointByeRate(events.contains("byes") ? Range.of(40, 400) : NEVER)
                .withRandomTeamFormat()
                .withRandomAcceleration();
        var completed = new ArrayList<GeneratedTournament.Completed>();
        Corpus.of(TournamentGenerator.of(settings), CORPUS, TOURNAMENTS).generate(generated -> {
            if (generated instanceof GeneratedTournament.Completed done && !isKnownDivergence(done)) {
                completed.add(done);
            }
        });
        return completed;
    }

    /** KD-3: Gacrux accelerates the secondary score used for colours, which the library keeps real. */
    private static boolean isKnownDivergence(GeneratedTournament.Completed done) {
        var settings = done.tournament().settings();
        return settings.acceleration() instanceof Acceleration.Baku
                && settings.scoring().matches().map(MatchScoring::secondary).orElse(SecondaryScore.NOT_USED)
                        == SecondaryScore.USED_FOR_COLOUR;
    }

    private static void save(OracleGate.Difference difference) throws IOException {
        Files.createDirectories(FAILURES);
        Files.writeString(
                FAILURES.resolve("gacrux-" + difference.tournament() + "-round" + difference.round() + ".trf"),
                difference.input(),
                StandardCharsets.UTF_8);
    }
}
