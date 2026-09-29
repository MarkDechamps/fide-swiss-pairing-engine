package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Record 192, the Encoded Type of Tournament (ETT26), for the systems the library pairs. A bare code takes the
 * 2026 rules, never the tournament date; {@code FIDE_DUTCH_2025} is an alias of {@code _2026}, and
 * {@code FIDE_DUTCH_2017} is the Dutch System of the pre-2026 edition. {@code FIDE_LIM} is provisional: ETT26 has no
 * Lim code (TRF CLI surface). A {@code _BAKU} suffix adds acceleration and leaves the system alone.
 */
final class PairingSystemCode {

    private static final Map<String, Supplier<PairingSystem>> SYSTEMS = Map.of(
            "FIDE_DUTCH", PairingSystems::dutch,
            "FIDE_DUTCH_2026", PairingSystems::dutch,
            "FIDE_DUTCH_2025", PairingSystems::dutch,
            "FIDE_DUTCH_2017", PairingSystems::dutch,
            "FIDE_DUBOV", PairingSystems::dubov,
            "FIDE_DUBOV_2026", PairingSystems::dubov,
            "FIDE_BURSTEIN", PairingSystems::burstein,
            "FIDE_BURSTEIN_2026", PairingSystems::burstein,
            "FIDE_LIM", PairingSystems::lim,
            "FIDE_LIM_2026", PairingSystems::lim);

    private PairingSystemCode() {}

    /**
     * The code a writer gives the settings' system, known by the Handbook number its name starts with (GHR 1.3): the
     * bare code for the 2026 rules (bbpPairings v6 rejects {@code FIDE_DUTCH_2026}), {@code FIDE_DUTCH_2017} for the
     * pre-2026 Dutch System. A system the Handbook does not define has no code.
     */
    static String of(TournamentSettings settings) {
        var name = settings.pairingSystem().name();
        if (name.startsWith("C.04.3 ")) {
            return settings.swissRulesEdition() == SwissRulesEdition.PRE_2026 ? "FIDE_DUTCH_2017" : "FIDE_DUTCH";
        }
        if (name.startsWith("C.04.4.1 ")) {
            return "FIDE_DUBOV";
        }
        if (name.startsWith("C.04.4.2 ")) {
            return "FIDE_BURSTEIN";
        }
        if (name.startsWith("C.04.4.3 ")) {
            return "FIDE_LIM";
        }
        throw new IllegalArgumentException("TRF26 has no 192 code for the pairing system " + name);
    }

    /** The system a 192 value names; empty for a blank value; an error for any code the library cannot pair. */
    static Optional<PairingSystem> parse(String value) {
        var code = value.trim().toUpperCase();
        if (code.isEmpty()) {
            return Optional.empty();
        }
        var system = SYSTEMS.get(code.replaceFirst("_BAKU$", ""));
        if (system == null) {
            throw new InvalidTrfException("Unsupported pairing system in record 192: " + code);
        }
        return Optional.of(system.get());
    }
}
