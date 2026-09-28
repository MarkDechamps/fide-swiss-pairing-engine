package io.github.markdechamps.fideswiss.topscoregroup;

import java.util.Optional;

/** 3.6.1: a pair of a bracket; the one with the smaller TPN is the top member. */
public record ContenderPair(Contender top, Contender bottom) {

    public static ContenderPair of(Contender a, Contender b) {
        return a.tpn() < b.tpn() ? new ContenderPair(a, b) : new ContenderPair(b, a);
    }

    public boolean contains(Contender contender) {
        return top.equals(contender) || bottom.equals(contender);
    }

    public Contender other(Contender contender) {
        return top.equals(contender) ? bottom : top;
    }

    /** The member with the higher score, when the scores differ: the upfloater's opponent (Gacrux's [C10]). */
    public Optional<Contender> higherScored() {
        var comparison = top.score().compareTo(bottom.score());
        if (comparison == 0) {
            return Optional.empty();
        }
        return Optional.of(comparison > 0 ? top : bottom);
    }
}
