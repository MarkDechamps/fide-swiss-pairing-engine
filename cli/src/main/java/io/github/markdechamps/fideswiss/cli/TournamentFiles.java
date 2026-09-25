package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.generator.Corpus;
import io.github.markdechamps.fideswiss.generator.CorpusSeed;
import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.generator.GeneratorSettings;
import io.github.markdechamps.fideswiss.generator.RtgConfiguration;
import io.github.markdechamps.fideswiss.generator.TournamentGenerator;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.trf.TrfWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@code generate} and {@code -g}: plays a corpus of tournaments and writes each as a TRF26 file named from the
 * output pattern, with a manifest of every seed when there is more than one. Precedence: the generator's
 * defaults, then the configuration file, then the flags.
 */
final class TournamentFiles {

    private static final String INDEX = "%d";

    private final PrintStream err;

    TournamentFiles(PrintStream err) {
        this.err = err;
    }

    int generate(Command.Generate command) {
        if (command.count() > 1 && !command.output().contains(INDEX)) {
            throw new UsageException("-o needs %d in it to name " + command.count() + " files");
        }
        var corpusSeed = CorpusSeed.of(command.seed().orElseGet(System::nanoTime));
        if (command.seed().isEmpty()) {
            err.println("seed " + Long.toUnsignedString(corpusSeed.value()));
        }
        var corpus = Corpus.of(TournamentGenerator.of(settings(command)), corpusSeed, command.count());
        var manifest = new ArrayList<String>();
        manifest.add("index\tseed\tstatus\tplayers\trounds");
        var index = new AtomicInteger();
        corpus.generate(generated -> manifest.add(written(command.output(), index.getAndIncrement(), generated)));
        if (command.count() > 1) {
            write(command.output() + ".manifest.tsv", String.join("\n", manifest) + "\n");
        }
        if (corpus.skipped() > 0) {
            err.println("skipped " + corpus.skipped() + " of " + command.count() + " seeds");
        }
        return corpus.skipsTooMany() ? Main.TOO_MANY_SKIPS : Main.SUCCESS;
    }

    private GeneratorSettings settings(Command.Generate command) {
        var settings = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9)));
        if (command.configuration().isPresent()) {
            settings = RtgConfiguration.applyTo(
                    settings, read(command.configuration().get()));
        }
        for (var override : command.overrides()) {
            settings = override.apply(settings);
        }
        return settings;
    }

    private static String written(String pattern, int index, GeneratedTournament generated) {
        return switch (generated) {
            case GeneratedTournament.Completed completed -> {
                var name = "RTG " + Version.number() + " seed " + completed.seed();
                write(
                        pattern.replace(INDEX, String.valueOf(index)),
                        TrfWriter.write(completed.tournament(), TrfWriter.Options.named(name)));
                yield manifestLine(index, generated, "ok", completed.parameters());
            }
            case GeneratedTournament.Skipped skipped ->
                manifestLine(index, generated, "skipped in round " + skipped.round(), skipped.parameters());
        };
    }

    private static String manifestLine(
            int index, GeneratedTournament generated, String status, GeneratedTournament.Parameters parameters) {
        return String.join(
                "\t",
                List.of(
                        String.valueOf(index),
                        generated.seed().toString(),
                        status,
                        String.valueOf(parameters.players()),
                        String.valueOf(parameters.rounds())));
    }

    private static String read(String file) {
        try {
            return Files.readString(Path.of(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(String file, String text) {
        try {
            Files.writeString(Path.of(file), text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
