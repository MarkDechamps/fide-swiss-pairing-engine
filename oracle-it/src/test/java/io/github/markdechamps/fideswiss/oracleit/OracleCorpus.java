package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.generator.Chance;
import io.github.markdechamps.fideswiss.generator.Corpus;
import io.github.markdechamps.fideswiss.generator.CorpusSeed;
import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.Range;
import io.github.markdechamps.fideswiss.generator.TournamentGenerator;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
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

/**
 * The corpus every Dutch Oracle gate plays, sized by system properties: {@code fideswiss.oracle.tournaments} (default
 * 100; the 50,000-tournament release gate sets it, see the release-gate workflow), {@code fideswiss.oracle.seed}, {@code
 * fideswiss.oracle.baku} (percent accelerated, default 20) and {@code fideswiss.oracle.events}.
 */
final class OracleCorpus {

    static final CorpusSeed CORPUS = CorpusSeed.of(Long.getLong("fideswiss.oracle.seed", 20260929L));
    static final int TOURNAMENTS = Integer.getInteger("fideswiss.oracle.tournaments", 100);
    /** The share of tournaments accelerated by Baku, {@code fideswiss.oracle.baku} percent (default 20). */
    static final int BAKU_PERCENT = Integer.getInteger("fideswiss.oracle.baku", 20);

    static final String ALL_EVENTS = "forfeits,byes,withdrawals,late-entries";

    private static final Range NEVER = Range.of(1_000_000, 1_000_000);
    static final Path FAILURES = Path.of("target", "oracle-failures");

    private OracleCorpus() {}

    /** {@code fideswiss.oracle.events} (a comma list) overrides the events a gate plays. */
    static Set<String> events(String defaultEvents) {
        return Set.of(
                System.getProperty("fideswiss.oracle.events", defaultEvents).split(","));
    }

    static long accelerated(List<GeneratedTournament.Completed> tournaments) {
        return tournaments.stream()
                .filter(done -> done.tournament().settings().acceleration() instanceof Acceleration.Baku)
                .count();
    }

    static List<GeneratedTournament.Completed> corpus(SwissRulesEdition edition, Set<String> events) {
        var settings = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9))
                        .with(PairingSystems.dutch())
                        .with(edition))
                .withBakuAcceleration(Chance.percent(BAKU_PERCENT))
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

    static void save(String name, String text) throws IOException {
        Files.createDirectories(FAILURES);
        Files.writeString(FAILURES.resolve(name), text, StandardCharsets.UTF_8);
    }
}
