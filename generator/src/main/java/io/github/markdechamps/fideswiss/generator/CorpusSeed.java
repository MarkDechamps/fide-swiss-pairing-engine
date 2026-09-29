package io.github.markdechamps.fideswiss.generator;

/** The seed of a whole corpus; tournament k's seed is SplitMix64(corpus seed, k), so any one can be redone alone. */
public record CorpusSeed(long value) {

    public static CorpusSeed of(long value) {
        return new CorpusSeed(value);
    }

    public TournamentSeed tournament(int index) {
        return TournamentSeed.of(SplitMix64.nth(value, index));
    }
}
