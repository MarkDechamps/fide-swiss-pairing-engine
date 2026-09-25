package io.github.markdechamps.fideswiss.generator;

/** SplitMix64's finaliser (Steele, Lea and Flood 2014): spreads any 64-bit value over all 64 bits. */
final class SplitMix64 {

    static final long GOLDEN_GAMMA = 0x9E3779B97F4A7C15L;

    private SplitMix64() {}

    static long mix(long value) {
        var z = value;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /** The {@code index}-th output of a SplitMix64 generator started at {@code seed}. */
    static long nth(long seed, long index) {
        return mix(seed + (index + 1) * GOLDEN_GAMMA);
    }
}
