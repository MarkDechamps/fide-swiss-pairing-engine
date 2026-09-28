package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.List;

/** A system the library has only the 2026 text of: a tournament of another Swiss Rules Edition is rejected. */
public final class Edition2026Only {

    private Edition2026Only() {}

    public static List<Problem> problemsWith(PairingSystem system, TournamentSettings settings) {
        if (settings.swissRulesEdition() == SwissRulesEdition.EDITION_2026) {
            return List.of();
        }
        return List.of(Problem.citing(
                "GHR 1.3",
                "The library has only the 2026 text of the " + system.name() + ", not edition "
                        + settings.swissRulesEdition()));
    }
}
