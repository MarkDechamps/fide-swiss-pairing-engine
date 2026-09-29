package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Problem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The ordered tie-breaks a tournament declares for its Standings (C.07 4.1), for example {@code BH/C1, SB, DE}.
 * TRF record 212 lists start with {@code PTS}, the score itself, which is dropped here.
 */
public final class TieBreakList {

    /** The individual tie-breaks of C.07 art. 5. */
    private static final Set<String> INDIVIDUAL = Set.of(
            "DE", "WIN", "WON", "BPG", "BWG", "PS", "REP", "STD", "TPN", "BH", "AOB", "FB", "SB", "KS", "ARO", "TPR",
            "PTP", "APRO", "APPO", "RTNG");

    /** The tie-breaks specific for teams (C.07 art. 12–13), which need match scoring. */
    private static final Set<String> TEAM_ONLY = Set.of(
            "MPVGP", "EMMSB", "EMGSB", "EGMSB", "EGGSB", "EDE", "EDEBT", "EDEBB", "EDET", "EDEB", "SSSC", "BC", "TBR",
            "BBE");

    /** The individual tie-breaks the Technical Commission lets a team score (:MP, :GP) redefine. */
    private static final Set<String> TEAM_SCORED = Set.of("PS", "BH", "AOB", "FB", "KS");

    /** Counting matches won or games won: only :MP makes sense, so :GP is refused. */
    private static final Set<String> TEAM_COUNTED = Set.of("WIN", "WON");

    private static final Set<String> EXTENDED_SB = Set.of("EMMSB", "EMGSB", "EGMSB", "EGGSB");

    /** Every team tie-break acronym, for callers that must tell individual codes from team codes. */
    public static boolean isTeamOnly(String acronym) {
        return TEAM_ONLY.contains(acronym);
    }

    /** The tie-breaks the art. 5 table marks for Cut-1, which the Median modifiers share. */
    private static final Set<String> CUTTABLE = Set.of("BH", "FB", "SB", "PS", "ARO");

    private final List<TieBreakCode> codes;

    private TieBreakList(List<TieBreakCode> codes) {
        this.codes = List.copyOf(codes);
    }

    public static TieBreakList none() {
        return new TieBreakList(List.of());
    }

    /** Parses a comma-separated list; every unknown or unsupported code is reported at once. */
    public static TieBreakList parse(String list) {
        var codes = new ArrayList<TieBreakCode>();
        var problems = new ArrayList<Problem>();
        for (var entry : list.split(",")) {
            if (entry.isBlank() || entry.trim().equalsIgnoreCase("PTS")) {
                continue;
            }
            try {
                var code = TieBreakCode.parse(entry);
                validate(code);
                codes.add(code);
            } catch (IllegalArgumentException e) {
                problems.add(Problem.citing("C.07 5", e.getMessage()));
            }
        }
        if (!problems.isEmpty()) {
            throw new InvalidSettingsException(problems);
        }
        return new TieBreakList(codes);
    }

    private static void validate(TieBreakCode code) {
        var acronym = code.acronym();
        if (acronym.equals("ESB")) {
            throw new IllegalArgumentException("ESB is a family (C.07 13.2): name EMMSB, EMGSB, EGMSB or EGGSB");
        }
        if (acronym.startsWith("OTHER_")) {
            throw new IllegalArgumentException(acronym + " is self-defined (C.07 4.1) and has no definition here");
        }
        var team = TEAM_ONLY.contains(acronym);
        if (!team && !INDIVIDUAL.contains(acronym)) {
            throw new IllegalArgumentException("unknown tie-break " + acronym);
        }
        var scored = TEAM_SCORED.contains(acronym) || TEAM_COUNTED.contains(acronym);
        if (code.teamScore().isPresent() && !scored) {
            throw new IllegalArgumentException(acronym + " takes no team score (:MP or :GP, C.07 13)");
        }
        if (code.teamScore().filter(score -> score == PrimaryScore.GAME_POINTS).isPresent()
                && TEAM_COUNTED.contains(acronym)) {
            throw new IllegalArgumentException(acronym + " counts matches or games won: only :MP applies (C.07 7.1)");
        }
        validateModifiers(code);
    }

    private static void validateModifiers(TieBreakCode code) {
        var acronym = code.acronym();
        var cuts = code.lowCuts() > 0 || code.highCuts() > 0;
        if (cuts && EXTENDED_SB.contains(acronym)) {
            if (code.highCuts() > 0) {
                throw new IllegalArgumentException(acronym + " takes Cut-1 and Cut-2 only (C.07 14.1.2)");
            }
        } else if (cuts && !CUTTABLE.contains(acronym)) {
            throw new IllegalArgumentException(acronym + " takes no Cut or Median modifier (C.07 14)");
        }
        if (code.fore() && !acronym.equals("AOB") && !acronym.equals("SSSC")) {
            throw new IllegalArgumentException("/F applies only to AOB and SSSC (C.07 8.2, 13.4)");
        }
        if (code.limitShift() != 0 && !acronym.equals("KS")) {
            throw new IllegalArgumentException("/L applies only to KS (C.07 14.5)");
        }
        if (code.normalisingFactor().isPresent() && !acronym.equals("SSSC")) {
            throw new IllegalArgumentException("/K applies only to SSSC (C.07 13.4.2)");
        }
        if (code.forfeitsAsPlayed() && Set.of("MPVGP", "BC", "TBR", "BBE").contains(acronym)) {
            throw new IllegalArgumentException(acronym + " takes no /P (C.07 12, 13.1)");
        }
    }

    public List<TieBreakCode> codes() {
        return codes;
    }

    public boolean isEmpty() {
        return codes.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TieBreakList list && codes.equals(list.codes);
    }

    @Override
    public int hashCode() {
        return codes.hashCode();
    }

    @Override
    public String toString() {
        return String.join(", ", codes.stream().map(TieBreakCode::toString).toList());
    }
}
