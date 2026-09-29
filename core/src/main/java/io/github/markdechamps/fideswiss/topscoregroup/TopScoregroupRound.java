package io.github.markdechamps.fideswiss.topscoregroup;

import io.github.markdechamps.fideswiss.tournament.BracketSeating;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

/**
 * What one system asks of the shared procedure for one round: the contenders to be paired, whether the float
 * criteria ([C7] and the system's own) lapse in this round, the [C6] Interpretation and its bracket criteria.
 */
public record TopScoregroupRound(
        List<Contender> contenders,
        boolean floatCriteriaLapse,
        UpfloaterLookAhead upfloaterLookAhead,
        List<PairCriterion> pairCriteria,
        BracketSeating seating) {

    /** Seating by TPN alone (C.04.6 3.6.1 as written), which Double-Swiss has no other reading of. */
    public TopScoregroupRound(
            List<Contender> contenders,
            boolean floatCriteriaLapse,
            UpfloaterLookAhead upfloaterLookAhead,
            List<PairCriterion> pairCriteria) {
        this(contenders, floatCriteriaLapse, upfloaterLookAhead, pairCriteria, BracketSeating.tpn());
    }

    public TopScoregroupRound {
        contenders = List.copyOf(contenders);
        pairCriteria = List.copyOf(pairCriteria);
    }

    /** The order in which a bracket's members take seats: the smaller comes first, and is a pair's top member. */
    Comparator<Contender> seatOrder() {
        var byTpn = Comparator.comparingInt(Contender::tpn);
        return seating == BracketSeating.TPN
                ? byTpn
                : Comparator.comparing(Contender::score).reversed().thenComparing(byTpn);
    }

    /**
     * The score of the scoregroup right below {@code score} in the round's standings, the PAB contender included
     * (the Following scoregroup; Readable Swiss Team pairing algorithm, decision 5).
     */
    Optional<Points> followingScore(Points score) {
        var scores = new TreeSet<Points>();
        contenders.forEach(contender -> scores.add(contender.score()));
        return Optional.ofNullable(scores.lower(score));
    }
}
