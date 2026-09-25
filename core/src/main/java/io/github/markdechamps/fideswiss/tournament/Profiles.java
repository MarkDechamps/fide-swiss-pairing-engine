package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;

/** Ready-made settings for common kinds of event; only the number of rounds is left to the client. */
public final class Profiles {

    private Profiles() {}

    /** The baseline: Dutch System, 2026 rules, 1 / ½ / 0, the PAB worth a win, White for the top participant. */
    public static TournamentSettings individualSwiss(NumberOfRounds rounds) {
        return new TournamentSettings(
                PairingSystems.dutch(),
                SwissRulesEdition.EDITION_2026,
                ScoringScheme.standard(),
                RankingKey.strengthTitleName(),
                InitialColour.white(),
                rounds);
    }
}
