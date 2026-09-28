package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.generator.GeneratedTournament;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code <pattern>.manifest.tsv}: one line per seed with its index, TournamentSeed, status (ok, or skipped with the
 * round) and the effective parameters drawn for it (Random tournament generator).
 */
final class Manifest {

    private static final List<String> COLUMNS = List.of(
            "index",
            "seed",
            "status",
            "players",
            "rounds",
            "highest rating",
            "lowest rating",
            "unrated",
            "forfeit rate",
            "hpb rate",
            "zpb rate",
            "fpb rate",
            "withdrawals",
            "late entries",
            "scoring",
            "acceleration",
            "tiebreaks");

    private final List<String> lines = new ArrayList<>(List.of(String.join("\t", COLUMNS)));

    void add(int index, GeneratedTournament generated) {
        var status =
                switch (generated) {
                    case GeneratedTournament.Completed completed -> "ok";
                    case GeneratedTournament.Skipped skipped -> "skipped in round " + skipped.round();
                };
        var parameters =
                switch (generated) {
                    case GeneratedTournament.Completed completed -> completed.parameters();
                    case GeneratedTournament.Skipped skipped -> skipped.parameters();
                };
        lines.add(String.join(
                "\t",
                List.of(
                        String.valueOf(index),
                        generated.seed().toString(),
                        status,
                        String.valueOf(parameters.players()),
                        String.valueOf(parameters.rounds()),
                        String.valueOf(parameters.highestRating()),
                        String.valueOf(parameters.lowestRating()),
                        String.valueOf(parameters.unrated()),
                        String.valueOf(parameters.forfeitRate()),
                        String.valueOf(parameters.halfPointByeRate()),
                        String.valueOf(parameters.zeroPointByeRate()),
                        String.valueOf(parameters.fullPointByeRate()),
                        String.valueOf(parameters.withdrawals()),
                        String.valueOf(parameters.lateEntries()),
                        scoring(parameters.scoring()),
                        acceleration(parameters.acceleration()),
                        parameters.tieBreaks().toString())));
    }

    String text() {
        return String.join("\n", lines) + "\n";
    }

    private static String scoring(ScoringScheme scoring) {
        return scoring.win() + "/" + scoring.draw() + "/" + scoring.loss();
    }

    private static String acceleration(Acceleration acceleration) {
        return switch (acceleration) {
            case Acceleration.None none -> "none";
            case Acceleration.Baku baku -> "baku";
            case Acceleration.Explicit explicit -> "explicit";
        };
    }
}
