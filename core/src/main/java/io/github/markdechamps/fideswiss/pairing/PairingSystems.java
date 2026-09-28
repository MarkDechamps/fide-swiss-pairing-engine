package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.dutch.DutchSystem;
import io.github.markdechamps.fideswiss.swissteam.SwissTeamSystem;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;

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

    /** The Swiss Team Pairing System (C.04.6, 2026) with Type A colour preferences and default Interpretations. */
    public static PairingSystem swissTeam() {
        return swissTeam(ColourPreferenceType.TYPE_A);
    }

    /** The Swiss Team Pairing System with the competition's colour preferences (1.7). */
    public static PairingSystem swissTeam(ColourPreferenceType colourPreferences) {
        return SwissTeamSystem.of(colourPreferences);
    }
}
