package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** The slowest round of a simulated tournament per system: {@code -Dtiming=dubov|lim -Dplayers=250}. */
@EnabledIfSystemProperty(named = "timing", matches = ".+")
class TimingRun {

    private static final Map<String, Supplier<PairingSystem>> SYSTEMS =
            Map.of("dutch", PairingSystems::dutch, "dubov", PairingSystems::dubov);

    @Test
    void reportsTheSlowestRound() {
        var system = SYSTEMS.get(System.getProperty("timing")).get();
        var players = Integer.getInteger("players", 250);
        var slowest = new long[1];
        SimulatedTournaments.play(system, players, Integer.getInteger("rounds", 9), 7L, tournament -> {
            var started = System.nanoTime();
            try {
                tournament.pairNextRound();
            } catch (RuntimeException ignored) {
                // the simulation reports it too
            }
            slowest[0] = Math.max(slowest[0], System.nanoTime() - started);
        });
        System.out.printf(
                "%s, %d players: slowest round %d ms%n", System.getProperty("timing"), players, slowest[0] / 1_000_000);
    }
}
