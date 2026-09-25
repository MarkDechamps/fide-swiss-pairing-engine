package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakContribution;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * One value a sum-of-values tie-break adds up, with what the modifiers need: how significant it is (C.07 14.1.1:
 * SB ranks by the opponent's score first), and whether it comes from a Voluntary Unplayed Round (16.5).
 */
record Term(
        Optional<RoundNumber> round,
        String counterpart,
        BigDecimal value,
        BigDecimal significance,
        boolean voluntary,
        boolean cut,
        List<String> notes) {

    /** Least significant first: by significance, then by value. */
    static final Comparator<Term> LEAST_SIGNIFICANT_FIRST =
            Comparator.comparing(Term::significance).thenComparing(Term::value);

    Term {
        notes = List.copyOf(notes);
    }

    static Term of(RoundNumber round, String counterpart, BigDecimal value, boolean voluntary, List<String> notes) {
        return new Term(Optional.of(round), counterpart, value, value, voluntary, false, notes);
    }

    Term rankedBy(BigDecimal ranking) {
        return new Term(round, counterpart, value, ranking, voluntary, cut, notes);
    }

    Term cutBy(String modifier) {
        var withCut = new ArrayList<>(notes);
        withCut.add(modifier);
        return new Term(round, counterpart, value, significance, voluntary, true, withCut);
    }

    TieBreakContribution contribution() {
        return new TieBreakContribution(round, counterpart, value, cut, notes);
    }
}
