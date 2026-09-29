package io.github.markdechamps.fideswiss.topscoregroup;

import java.util.Set;

/**
 * One of a system's bracket criteria (Swiss Team [C8]-[C10], Double-Swiss [C8]): how much one pair of a bracket
 * fails it, given the bracket's upfloaters. A pairing fails the criterion by the sum over its pairs.
 */
public interface PairCriterion {

    String article();

    long failureOf(ContenderPair pair, Set<Contender> upfloaters);
}
