package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;

/** Ready-made settings for common kinds of event; only the number of rounds is left to the client. */
public final class Profiles {

    private Profiles() {}

    /**
     * The baseline: Dutch System, 2026 rules, 1 / ½ / 0, the PAB worth a win, White for the top participant, and
     * the tie-breaks {@code BH/C1, BH, SB, DE} under C.07 2026-03 (our choice; C.07 2.1 leaves it open).
     */
    public static TournamentSettings individualSwiss(NumberOfRounds rounds) {
        return new TournamentSettings(
                PairingSystems.dutch(),
                SwissRulesEdition.EDITION_2026,
                ScoringScheme.standard(),
                RankingKey.strengthTitleName(),
                InitialColour.white(),
                Acceleration.none(),
                rounds,
                TieBreakList.parse("BH/C1, BH, SB, DE"),
                TieBreakEdition.EDITION_2026_03);
    }

    /** As {@link #individualSwiss}, with Baku acceleration (C.04.7). */
    public static TournamentSettings acceleratedOpen(NumberOfRounds rounds) {
        return individualSwiss(rounds).with(Acceleration.baku());
    }

    /**
     * The Swiss Team System (C.04.6, 2026): match points 2 / 1 / 0 primary over four boards, game points for
     * colours, a PAB worth a drawn match, Type A colour preferences and every Interpretation at its default. The
     * team tie-breaks ({@code MPvGP, EDE, EMGSB/C1}) are not implemented yet, so the list is empty for now.
     */
    public static TournamentSettings teamSwiss(NumberOfRounds rounds) {
        return individualSwiss(rounds)
                .with(PairingSystems.swissTeam())
                .with(ScoringScheme.teams())
                .with(TieBreakList.none());
    }

    /**
     * The Double-Swiss System (C.04.5, 2026): two games per match at 1 / ½ / 0, the PAB worth a game won and a game
     * drawn (1.4), and the individual tie-breaks of {@link #individualSwiss} over each player's game points.
     */
    public static TournamentSettings doubleSwiss(NumberOfRounds rounds) {
        return individualSwiss(rounds).with(PairingSystems.doubleSwiss()).with(ScoringScheme.doubleSwiss());
    }
}
