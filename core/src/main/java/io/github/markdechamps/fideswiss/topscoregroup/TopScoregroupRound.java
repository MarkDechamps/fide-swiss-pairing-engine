package io.github.markdechamps.fideswiss.topscoregroup;

import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
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
        List<PairCriterion> pairCriteria) {

    public TopScoregroupRound {
        contenders = List.copyOf(contenders);
        pairCriteria = List.copyOf(pairCriteria);
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
