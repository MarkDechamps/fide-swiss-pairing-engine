package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Diffs every TRF under {@code -Doracle.corpus=<dir>} against its recorded pairings, under the file's edition or
 * {@code -Doracle.edition=PRE_2026}; a local research aid.
 */
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
                var name = path.getFileName().toString();
                var trf = Files.readString(path, StandardCharsets.UTF_8);
                var edition = System.getProperty("oracle.edition", "");
                differences.addAll(
                        edition.isEmpty()
                                ? RecordedRoundReplay.differences(name, trf)
                                : RecordedRoundReplay.differences(name, trf, SwissRulesEdition.valueOf(edition)));
            }
        }
        differences.forEach(System.out::println);
        System.out.printf(
                "%d files, %d differing rounds, %d ms%n",
                files, differences.size(), (System.nanoTime() - started) / 1_000_000);
    }
}
