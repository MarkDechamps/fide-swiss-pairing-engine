package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * The individual tie-breaks a Tie-break Edition defines, with the modifiers the art. 5 table allows, from which a
 * random Tie-break List of three to five entries is drawn (ticket Random tournament generator). A team competition has its
 * own catalogue, with the team tie-breaks of C.07 articles 12 and 13.
 */
final class TieBreakCatalogue {

    private static final int FEWEST = 3;
    private static final int MOST = 5;

    private static final List<String> SINCE_2024_08 = List.of(
            "BH", "BH/C1", "BH/C2", "BH/M1", "BH/C1/P", "FB", "FB/C1", "SB", "SB/C1", "SB/M1", "PS", "PS/C1", "ARO",
            "ARO/C1", "AOB", "DE", "WIN", "WON", "BPG", "BWG", "REP", "KS", "KS/L+2", "TPR", "PTP", "APRO", "APPO");

    private static final List<String> NEW_IN_2026_03 = List.of("STD", "TPN", "RTNG", "AOB/F");

    /** C.07 articles 12 and 13, only for teams; {@code EDE*} and the board codes need games on boards (art. 12). */
    private static final List<String> TEAM_ONLY = List.of(
            "MPvGP",
            "EMMSB",
            "EMMSB/C1",
            "EMGSB",
            "EMGSB/C1",
            "EGMSB",
            "EGMSB/C1",
            "EGGSB",
            "EGGSB/C1",
            "EDE",
            "EDEBT",
            "EDEBB",
            "EDET",
            "EDEB",
            "SSSC",
            "BC",
            "TBR",
            "BBE");

    /** The individual tie-breaks computed over a team score, {@code :MP} or {@code :GP} (C.07 13, the manual, 6.4). */
    private static final List<String> TEAM_SCORED =
            List.of("WIN:MP", "WON:MP", "BH:MP", "BH:GP/C1", "PS:GP", "PS:MP", "FB:GP", "AOB:MP", "KS:GP");

    private static final List<String> TEAM_PLAIN = List.of("BH", "BH/C1", "SB", "DE", "PS", "KS");

    private final List<String> entries;

    private TieBreakCatalogue(List<String> entries) {
        this.entries = List.copyOf(entries);
    }

    static TieBreakCatalogue of(TieBreakEdition edition) {
        var entries = new ArrayList<>(SINCE_2024_08);
        if (edition == TieBreakEdition.EDITION_2026_03) {
            entries.addAll(NEW_IN_2026_03);
        }
        return new TieBreakCatalogue(entries);
    }

    /** The catalogue of a team competition: the team-only codes of art. 12–13 and the individual ones over a team score. */
    static TieBreakCatalogue ofTeams() {
        var entries = new ArrayList<>(TEAM_ONLY);
        entries.addAll(TEAM_SCORED);
        entries.addAll(TEAM_PLAIN);
        return new TieBreakCatalogue(entries);
    }

    /** Three to five different entries, in the order drawn. */
    TieBreakList draw(RandomGenerator random) {
        var remaining = new ArrayList<>(entries);
        var chosen = new ArrayList<String>();
        var size = random.nextInt(FEWEST, MOST + 1);
        while (chosen.size() < size) {
            chosen.add(remaining.remove(random.nextInt(remaining.size())));
        }
        return TieBreakList.parse(String.join(", ", chosen));
    }
}
