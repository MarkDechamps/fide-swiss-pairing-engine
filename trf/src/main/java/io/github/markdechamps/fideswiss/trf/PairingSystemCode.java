package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Record 192, the Encoded Type of Tournament (ETT26), for the systems the library pairs. A bare code takes the
 * 2026 rules, never the tournament date; {@code FIDE_DUTCH_2025} is an alias of {@code _2026}. {@code FIDE_LIM}
 * is provisional: ETT26 has no Lim code (TRF CLI surface).
 */
final class PairingSystemCode {

    private static final Map<String, Supplier<PairingSystem>> SYSTEMS = Map.of(
            "FIDE_DUTCH", PairingSystems::dutch,
            "FIDE_DUTCH_2026", PairingSystems::dutch,
            "FIDE_DUTCH_2025", PairingSystems::dutch,
            "FIDE_DUBOV", PairingSystems::dubov,
            "FIDE_DUBOV_2026", PairingSystems::dubov,
            "FIDE_LIM", PairingSystems::lim,
            "FIDE_LIM_2026", PairingSystems::lim);

    private PairingSystemCode() {}

    /** The system a 192 value names; empty for a blank value; an error for any code the library cannot pair. */
    static Optional<PairingSystem> parse(String value) {
        var code = value.trim().toUpperCase();
        if (code.isEmpty()) {
            return Optional.empty();
        }
        var system = SYSTEMS.get(code);
        if (system == null) {
            throw new InvalidTrfException("Unsupported pairing system in record 192: " + code);
        }
        return Optional.of(system.get());
    }
}
