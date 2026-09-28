package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
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

    /** The team tie-breaks of C.07 art. 12–13, not implemented yet. */
    private static final Set<String> TEAM = Set.of(
            "MPVGP", "ESB", "EMMSB", "EMGSB", "EGMSB", "EGGSB", "EDE", "EDEBT", "EDEBB", "EDET", "EDEB", "SSSC", "BC",
            "TBR", "BBE");

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
        if (TEAM.contains(acronym)) {
            throw new IllegalArgumentException(acronym + " is a team tie-break, which is not implemented yet");
        }
        if (acronym.startsWith("OTHER_")) {
            throw new IllegalArgumentException(acronym + " is self-defined (C.07 4.1) and has no definition here");
        }
        if (!INDIVIDUAL.contains(acronym)) {
            throw new IllegalArgumentException("unknown tie-break " + acronym);
        }
        if ((code.lowCuts() > 0 || code.highCuts() > 0) && !CUTTABLE.contains(acronym)) {
            throw new IllegalArgumentException(acronym + " takes no Cut or Median modifier (C.07 14)");
        }
        if (code.fore() && !acronym.equals("AOB")) {
            throw new IllegalArgumentException("/F applies only to AOB (C.07 8.2)");
        }
        if (code.limitShift() != 0 && !acronym.equals("KS")) {
            throw new IllegalArgumentException("/L applies only to KS (C.07 14.5)");
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
