package io.github.markdechamps.fideswiss.oracleit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.markdechamps.fideswiss.generator.Corpus;
import io.github.markdechamps.fideswiss.generator.CorpusSeed;
import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.generator.TournamentGenerator;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
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
 * The nightly Oracle gates for the Dutch System (Verification strategy): the tournaments our generator plays are
 * re-paired round by round by the Oracle, and every round must be identical. Each gate is skipped, with the way to
 * configure it, when its program is not configured ({@link OraclePrograms}). The corpus is {@code
 * fideswiss.oracle.tournaments} tournaments (default 100) from {@code fideswiss.oracle.seed}, with the events in {@code fideswiss.oracle.events};
 * every difference is
 * written under {@code target/oracle-failures} as the Oracle's exact input, for CI to keep.
 */
class DutchOracleIT {

    private static final CorpusSeed CORPUS = CorpusSeed.of(Long.getLong("fideswiss.oracle.seed", 20260929L));
    private static final int TOURNAMENTS = Integer.getInteger("fideswiss.oracle.tournaments", 100);
    private static final String ALL_EVENTS = "forfeits,byes,withdrawals,late-entries";

    private static final Range NEVER = Range.of(1_000_000, 1_000_000);
    private static final Path FAILURES = Path.of("target", "oracle-failures");

    @Test
    void bbpPairingsV6PairsDutch2026AsWeDo() throws IOException {
        gate(OraclePrograms.BBP_V6, SwissRulesEdition.EDITION_2026, ALL_EVENTS);
    }

    @Test
    void bbpPairingsV5PairsDutch2017AsWeDo() throws IOException {
        gate(OraclePrograms.BBP_V5, SwissRulesEdition.PRE_2026, ALL_EVENTS);
    }

    @Test
    void jaVaFoPairsDutch2017AsWeDo() throws IOException {
        gate(OraclePrograms.JAVAFO, SwissRulesEdition.PRE_2026, ALL_EVENTS);
    }

    private static void gate(OraclePrograms program, SwissRulesEdition edition, String defaultEvents)
            throws IOException {
        var oracle = program.locate();
        assumeTrue(oracle.isPresent(), program::absence);
        var report = new OracleGate(oracle.get(), edition).compare(corpus(edition, events(defaultEvents)));
        System.out.println(report.summary());
        for (var difference : report.differences()) {
            System.out.println("  " + difference.describe());
            save(program, difference);
        }
        assertThat(report.differences()).as(report.summary()).isEmpty();
    }

    /** {@code fideswiss.oracle.events} (a comma list) overrides the events a gate plays. */
    private static Set<String> events(String defaultEvents) {
        return Set.of(
                System.getProperty("fideswiss.oracle.events", defaultEvents).split(","));
    }

    private static List<GeneratedTournament.Completed> corpus(SwissRulesEdition edition, Set<String> events) {
        var settings = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9))
                        .with(PairingSystems.dutch())
                        .with(edition))
                .withPlayers(Range.of(14, 60))
                .withRounds(Range.of(5, 11))
                .withForfeitRate(events.contains("forfeits") ? Range.of(8, 30) : NEVER)
                .withHalfPointByeRate(events.contains("byes") ? Range.of(15, 200) : NEVER)
                .withZeroPointByeRate(events.contains("byes") ? Range.of(15, 200) : NEVER)
                .withWithdrawalPercentage(events.contains("withdrawals") ? Range.of(0, 5) : Range.of(0, 0))
                .withLateEntryPercentage(events.contains("late-entries") ? Range.of(0, 5) : Range.of(0, 0));
        var completed = new ArrayList<GeneratedTournament.Completed>();
        Corpus.of(TournamentGenerator.of(settings), CORPUS, TOURNAMENTS).generate(generated -> {
            if (generated instanceof GeneratedTournament.Completed done) {
                completed.add(done);
            }
        });
        return completed;
    }

    private static void save(OraclePrograms program, OracleGate.Difference difference) throws IOException {
        Files.createDirectories(FAILURES);
        Files.writeString(
                FAILURES.resolve(
                        program.name() + "-" + difference.tournament() + "-round" + difference.round() + ".trf"),
                difference.input(),
                StandardCharsets.UTF_8);
    }
}
