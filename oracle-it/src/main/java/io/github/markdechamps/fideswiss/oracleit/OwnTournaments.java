package io.github.markdechamps.fideswiss.oracleit;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * The tournaments an Oracle's own random generator plays, from consecutive seeds. The generator sometimes cannot
 * finish a tournament it made up (bbpPairings: "No valid pairing"); that is not a failure of ours, so the tournament is
 * skipped and counted. A gate fails when more than {@link #MAXIMUM_SKIPPED_PERCENT} percent are skipped, so an Oracle
 * that is broken cannot pass by generating nothing.
 *
 * @param skipped the tournaments the generator could not finish
 */
public record OwnTournaments(List<OracleTournamentCheck.Generated> generated, int skipped) {

    public static final int MAXIMUM_SKIPPED_PERCENT = 5;

    public OwnTournaments {
        generated = List.copyOf(generated);
    }

    /**
     * Plays {@code count} tournaments from the seeds {@code first}, {@code first + 1}, ...; the players (14..60) and the
     * rounds (5..11) of each are drawn from its seed.
     */
    public static OwnTournaments play(OracleGenerator generator, long first, int count) {
        var generated = new ArrayList<OracleTournamentCheck.Generated>();
        var skipped = 0;
        for (var index = 0; index < count; index++) {
            var seed = first + index;
            var random = new SplittableRandom(seed);
            var configuration =
                    "PlayersNumber=" + (14 + random.nextInt(47)) + "\nRoundsNumber=" + (5 + random.nextInt(7)) + "\n";
            try {
                generated.add(
                        new OracleTournamentCheck.Generated("seed-" + seed, generator.generate(seed, configuration)));
            } catch (OracleGenerator.GenerationFailed e) {
                skipped++;
            }
        }
        return new OwnTournaments(generated, skipped);
    }

    public boolean tooManySkipped(int requested) {
        return skipped * 100 > requested * MAXIMUM_SKIPPED_PERCENT;
    }
}
