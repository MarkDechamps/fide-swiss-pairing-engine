package io.github.markdechamps.fideswiss.oracleit;

import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.trf.TrfWriter;

/**
 * The TRF flavour an Oracle reads. JaVaFo reads TRF16 and needs {@code XXR}/{@code XXC}; bbpPairings reads them too.
 * The dialect writers stay here, out of {@code trf} (Random tournament generator).
 */
public enum OracleDialect {
    JAVAFO,
    BBP;

    /** The tournament as it stands, for the Oracle to pair its next round. */
    public String write(Tournament tournament, String name) {
        return TrfWriter.write(tournament, TrfWriter.Options.named(name).withJaVaFoLines());
    }
}
