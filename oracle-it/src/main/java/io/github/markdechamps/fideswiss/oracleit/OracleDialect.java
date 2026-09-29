package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.trf.TrfWriter;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The TRF flavour an Oracle reads. JaVaFo reads TRF16 and needs {@code XXR}/{@code XXC}; bbpPairings reads them too.
 * Gacrux reads the TRF26 team file and accelerates only from explicit {@code 250} records, so an accelerated
 * tournament is written with its Virtual Points as {@code 250} lines (C.04.7 1.2–1.4). The dialect writers stay here,
 * out of {@code trf} (Random tournament generator).
 */
public enum OracleDialect {
    JAVAFO,
    BBP,
    GACRUX;

    /** The tournament as it stands, for the Oracle to pair its next round. */
    public String write(Tournament tournament, String name) {
        if (this == GACRUX) {
            return withVirtualPoints(
                    tournament, withoutMemberPoints(TrfWriter.write(tournament, TrfWriter.Options.named(name))));
        }
        return TrfWriter.write(tournament, TrfWriter.Options.named(name).withJaVaFoLines());
    }

    /**
     * Gacrux checks each member's points (columns 81–84) against the results in the line, including the pending
     * absence of the round to pair, and refuses the file when they differ; a member with 0.0 is not checked. The
     * team's own points are in the {@code 310} record.
     */
    private static String withoutMemberPoints(String trf) {
        var lines = new ArrayList<String>();
        for (var line : trf.split("\r\n", -1)) {
            lines.add(
                    line.startsWith("001 ") && line.length() >= 84
                            ? line.substring(0, 80) + " 0.0" + line.substring(84)
                            : line);
        }
        return String.join("\r\n", lines);
    }

    /**
     * One {@code 250} record per team and round with Virtual Points: match points at columns 5–8, game points
     * (none) at 10–13, the round twice, the team twice; placed before the first {@code 310}.
     */
    private static String withVirtualPoints(Tournament tournament, String trf) {
        if (!(tournament.settings().acceleration() instanceof Acceleration.Baku)) {
            return trf;
        }
        var records = new ArrayList<String>();
        var rounds = tournament.settings().numberOfRounds().value();
        for (var round = 1; round <= rounds; round++) {
            for (var index = 0; index < tournament.participants().size(); index++) {
                var points = tournament.virtualPointsOf(
                        tournament.participants().get(index).id(), RoundNumber.of(round));
                if (points.isGreaterThan(Points.ZERO)) {
                    records.add(String.format(
                            Locale.ROOT,
                            "250 %4.1f %4.1f %3d %3d %4d %4d",
                            points.toBigDecimal().doubleValue(),
                            0.0,
                            round,
                            round,
                            index + 1,
                            index + 1));
                }
            }
        }
        var at = trf.indexOf("310 ");
        return trf.substring(0, at)
                + String.join("\r\n", records)
                + (records.isEmpty() ? "" : "\r\n")
                + trf.substring(at);
    }
}
