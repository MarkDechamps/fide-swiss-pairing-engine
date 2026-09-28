package io.github.markdechamps.fideswiss.generator;

import java.util.function.Consumer;

/**
 * A run of {@code count} tournaments from one corpus seed. A corpus skips too many when more than 0.1% of its
 * seeds end without a legal pairing, which would mean the ranges are wrong (ticket Random tournament generator).
 */
public final class Corpus {

    private final TournamentGenerator generator;
    private final CorpusSeed seed;
    private final int count;
    private int skipped;

    private Corpus(TournamentGenerator generator, CorpusSeed seed, int count) {
        this.generator = generator;
        this.seed = seed;
        this.count = count;
    }

    public static Corpus of(TournamentGenerator generator, CorpusSeed seed, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("A corpus has at least one tournament: " + count);
        }
        return new Corpus(generator, seed, count);
    }

    /** Generates every tournament in index order and hands each one, completed or skipped, to {@code sink}. */
    public Corpus generate(Consumer<GeneratedTournament> sink) {
        skipped = 0;
        for (var index = 0; index < count; index++) {
            var generated = generator.generate(seed.tournament(index));
            if (generated instanceof GeneratedTournament.Skipped) {
                skipped++;
            }
            sink.accept(generated);
        }
        return this;
    }

    public int skipped() {
        return skipped;
    }

    public boolean skipsTooMany() {
        return skipped * 1000L > count;
    }
}
