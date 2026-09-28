package io.github.markdechamps.fideswiss.generator;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * The random draws of one tournament, each from its own substream keyed by what it is for, such as (round,
 * "byes") or (round, white, black, board). A change to one pairing then changes only the draws that depend on it.
 * The algorithm is the JDK's L64X128MixRandom, whose output is specified, so seeds reproduce across JDKs.
 */
final class Draws {

    private static final RandomGeneratorFactory<RandomGenerator> ALGORITHM =
            RandomGeneratorFactory.of("L64X128MixRandom");

    private final long seed;

    Draws(TournamentSeed seed) {
        this.seed = seed.value();
    }

    RandomGenerator stream(Object... key) {
        var state = SplitMix64.mix(seed);
        for (var part : key) {
            state = SplitMix64.mix(state ^ SplitMix64.mix(stableHash(part)));
        }
        return ALGORITHM.create(state);
    }

    /** Hashes that do not depend on the JVM: strings and numbers only. */
    private static long stableHash(Object part) {
        return switch (part) {
            case Integer number -> number;
            case Long number -> number;
            case String text -> text.hashCode() * 31L + text.length();
            default -> throw new IllegalArgumentException("Unsupported key part: " + part);
        };
    }
}
