package io.github.markdechamps.fideswiss.topscoregroup;

/**
 * One of a system's bracket criteria (Swiss Team [C8]–[C10], Double-Swiss [C8]): how much one pair fails it. A
 * pairing fails the criterion by the sum over its pairs.
 */
public interface PairCriterion {

    String article();

    long failureOf(ContenderPair pair);
}
