package io.github.markdechamps.fideswiss.trf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Diffs every TRF under {@code -Doracle.corpus=<dir>} against its recorded pairings; a local research aid. */
@EnabledIfSystemProperty(named = "oracle.corpus", matches = ".+")
class OracleCorpusRun {

    @Test
    void replaysTheCorpus() throws IOException {
        var differences = new ArrayList<String>();
        var files = 0;
        var started = System.nanoTime();
        try (Stream<Path> paths = Files.walk(Path.of(System.getProperty("oracle.corpus")))) {
            for (var path :
                    paths.filter(p -> p.toString().endsWith(".trf")).sorted().toList()) {
                files++;
                differences.addAll(RecordedRoundReplay.differences(
                        path.getFileName().toString(), Files.readString(path, StandardCharsets.UTF_8)));
            }
        }
        differences.forEach(System.out::println);
        System.out.printf(
                "%d files, %d differing rounds, %d ms%n",
                files, differences.size(), (System.nanoTime() - started) / 1_000_000);
    }
}
