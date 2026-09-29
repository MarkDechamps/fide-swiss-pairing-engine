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
import io.github.markdechamps.fideswiss.tournament.BakuSecondaryScore;
import io.github.markdechamps.fideswiss.tournament.BracketSeating;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.PabValue;
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
 * are re-paired round by round by plain Gacrux, and every round must be identical under the engine's defaults, which
 * are the reference app's readings (ADR 0009). With {@code -Dfideswiss.oracle.gacrux.literal=true} the engine reads
 * the text literally ({@code bracket-seating=tpn}, {@code pab-value=draw}, {@code baku-secondary-score=real}) and is
 * compared with Gacrux patched with {@code tpn-order} ({@link OraclePrograms#GACRUX_TPN_ORDER}), from which Baku with a
 * secondary score is left out (Known Divergence KD-3). Skipped, with the way to configure it, when Gacrux is not
 * configured ({@link OraclePrograms#GACRUX}). The corpus is {@code fideswiss.oracle.tournaments} tournaments (default
 * 100) from {@code fideswiss.oracle.seed}, in every colour preference type and score format, with the events in
 * {@code fideswiss.oracle.events}.
 */
class SwissTeamOracleIT {

    private static final CorpusSeed CORPUS = CorpusSeed.of(Long.getLong("fideswiss.oracle.seed", 20260929L));
    private static final int TOURNAMENTS = Integer.getInteger("fideswiss.oracle.tournaments", 100);
    private static final String ALL_EVENTS = "forfeits,byes";

    private static final boolean LITERAL = Boolean.getBoolean("fideswiss.oracle.gacrux.literal");

    private static final Range NEVER = Range.of(1_000_000, 1_000_000);
    private static final Path FAILURES = Path.of("target", "oracle-failures");

    @Test
    void gacruxPairsSwissTeam2026AsWeDo() throws IOException {
        var program = LITERAL ? OraclePrograms.GACRUX_TPN_ORDER : OraclePrograms.GACRUX;
        var oracle = program.locate();
        assumeTrue(oracle.isPresent(), program::absence);

        var report = new OracleGate(
                        oracle.get(), SwissRulesEdition.EDITION_2026, KnownDivergences.register(), readings())
                .compare(corpus(events()));

        System.out.println(report.summary());
        for (var difference : report.differences()) {
            System.out.println("  " + difference.describe());
            save(difference);
        }
        assertThat(report.differences()).as(report.summary()).isEmpty();
    }

    /** {@code fideswiss.oracle.events} (a comma list, also {@code withdrawals} and {@code late-entries}, off by default) overrides the events played. */
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
                .withWithdrawalPercentage(events.contains("withdrawals") ? Range.of(5, 20) : Range.of(0, 0))
                .withLateEntryPercentage(events.contains("late-entries") ? Range.of(5, 20) : Range.of(0, 0))
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

    /** The literal readings (bracket seating by TPN, a drawn PAB, real secondary scores), or none: the defaults. */
    private static List<Interpretation> readings() {
        return LITERAL ? List.of(BracketSeating.tpn(), PabValue.draw(), BakuSecondaryScore.real()) : List.of();
    }

    /** KD-3, literal reading only: Gacrux accelerates the secondary score used for colours, which it then keeps real. */
    private static boolean isKnownDivergence(GeneratedTournament.Completed done) {
        var settings = done.tournament().settings();
        return LITERAL
                && settings.acceleration() instanceof Acceleration.Baku
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
