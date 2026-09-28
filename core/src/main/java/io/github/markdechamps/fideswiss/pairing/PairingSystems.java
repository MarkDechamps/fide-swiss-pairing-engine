package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.dubov.DubovSystem;
import io.github.markdechamps.fideswiss.dutch.DutchSystem;
import io.github.markdechamps.fideswiss.lim.LimSystem;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.util.List;

/** The FIDE Pairing Systems. */
public final class PairingSystems {

    private PairingSystems() {}

    /** The Dutch System (C.04.3) of the tournament's Swiss Rules Edition: 2026 unless the settings say otherwise. */
    public static PairingSystem dutch() {
        return DutchSystem.ofTheTournamentsEdition();
    }

    /**
     * The Dutch System of one Swiss Rules Edition: 2026, or pre-2026 (Dutch 2017 with the pre-2026 C.04.1/C.04.2).
     * A tournament whose settings declare another edition is rejected.
     */
    public static PairingSystem dutch(SwissRulesEdition edition) {
        return DutchSystem.of(edition);
    }

    /** The Dubov System (C.04.4.1, 2026). */
    public static PairingSystem dubov() {
        return new DubovSystem();
    }

    /** The Lim System (C.04.4.3, 2026), not declared a Maxi-tournament. */
    public static PairingSystem lim() {
        return lim(MaxiTournament.NOT_DECLARED);
    }

    /** The same Lim System declared a Maxi-tournament; only Lim knows the setting (C.04.4.3 3.2.3, 3.8, 5.7). */
    public static PairingSystem asMaxiTournament(PairingSystem system) {
        if (!(system instanceof LimSystem)) {
            throw new InvalidSettingsException(List.of(Problem.citing(
                    "C.04.4.3 3.2.3", "only the Lim System has a Maxi-tournament setting, not " + system.name())));
        }
        return lim(MaxiTournament.DECLARED);
    }

    /** The Lim System (C.04.4.3, 2026), declared a Maxi-tournament or not by the organiser. */
    public static PairingSystem lim(MaxiTournament maxiTournament) {
        return new LimSystem(maxiTournament);
    }
}
