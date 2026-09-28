package io.github.markdechamps.fideswiss.history;

import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;

/**
 * Which recorded rounds bar a participant from the Pairing-Allocated Bye. The two editions of the Basic Rules word
 * it differently, so it is a rule object of the Swiss Rules Edition.
 */
public enum PairingAllocatedByeBar {

    /** C.04.1 (2026) Art. 4: a PAB, or a round scored at a win's value without playing (full-point byes too). */
    BASIC_RULES_2026 {
        @Override
        boolean bars(RoundRecord record, Points winValue) {
            return switch (record) {
                case RoundRecord.Game game -> false;
                case RoundRecord.Forfeit forfeit -> !forfeit.points().isLessThan(winValue);
                case RoundRecord.NoBoard noBoard ->
                    noBoard.bye() == Bye.PAIRING_ALLOCATED || !noBoard.points().isLessThan(winValue);
            };
        }
    },

    /**
     * C.04.1 (till 2026-01-31) d: a PAB, or "a (forfeit) win due to an opponent not appearing in time". A requested
     * full-point bye does not bar (Dutch 2017 reading R9, as both Oracles do).
     */
    BASIC_RULES_PRE_2026 {
        @Override
        boolean bars(RoundRecord record, Points winValue) {
            return switch (record) {
                case RoundRecord.Game game -> false;
                case RoundRecord.Forfeit forfeit -> !forfeit.points().isLessThan(winValue);
                case RoundRecord.NoBoard noBoard -> noBoard.bye() == Bye.PAIRING_ALLOCATED;
            };
        }
    };

    public static PairingAllocatedByeBar of(SwissRulesEdition edition) {
        return edition == SwissRulesEdition.PRE_2026 ? BASIC_RULES_PRE_2026 : BASIC_RULES_2026;
    }

    abstract boolean bars(RoundRecord record, Points winValue);
}
