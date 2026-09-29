package io.github.markdechamps.fideswiss.topscoregroup;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

/** 3.6.1: a pair of a bracket; the one with the smaller TPN is the top member. */
public record ContenderPair(Contender top, Contender bottom) {

    public static ContenderPair of(Contender a, Contender b) {
        return a.tpn() < b.tpn() ? new ContenderPair(a, b) : new ContenderPair(b, a);
    }

    /** The pair with the member that comes first in {@code seatOrder} on top. */
    static ContenderPair of(Contender a, Contender b, Comparator<Contender> seatOrder) {
        return seatOrder.compare(a, b) < 0 ? new ContenderPair(a, b) : new ContenderPair(b, a);
    }

    public boolean contains(Contender contender) {
        return top.equals(contender) || bottom.equals(contender);
    }

    public Contender other(Contender contender) {
        return top.equals(contender) ? bottom : top;
    }

    /** The resident of a pair of one resident and one upfloater: the upfloater's opponent ([C10]). */
    public Optional<Contender> upfloatersOpponent(Set<Contender> upfloaters) {
        var topFloats = upfloaters.contains(top);
        if (topFloats == upfloaters.contains(bottom)) {
            return Optional.empty();
        }
        return Optional.of(topFloats ? bottom : top);
    }
}
