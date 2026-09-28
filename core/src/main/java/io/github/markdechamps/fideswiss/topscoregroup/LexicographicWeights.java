package io.github.markdechamps.fideswiss.topscoregroup;

import java.math.BigInteger;

/**
 * Writes a vector of counts as one number, most significant position first, so that comparing sums compares the
 * vectors lexicographically. The base must exceed any sum one position reaches.
 */
record LexicographicWeights(int base, int positions) {

    BigInteger of(int position, long count) {
        return BigInteger.valueOf(base).pow(positions - 1 - position).multiply(BigInteger.valueOf(count));
    }
}
