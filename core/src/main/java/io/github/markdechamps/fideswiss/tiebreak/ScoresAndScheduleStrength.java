package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Scores and Schedule Strength Combination (C.07 13.4): the secondary score of the team plus its Schedule Strength,
 * the Buchholz on the primary score (Fore Buchholz with {@code /F}) divided by a normalising factor. The factor is
 * the highest primary score achievable in the tournament over the highest secondary score achievable in one match,
 * truncated toward zero, or the {@code /Kx} the competition states (13.4.2).
 */
final class ScoresAndScheduleStrength implements IndividualTieBreak {

    private static final int SCALE = 12;

    private final TieBreakCode code;
    private final TermSource buchholz;

    ScoresAndScheduleStrength(TieBreakCode code) {
        this.code = code;
        this.buchholz = code.fore() ? new ForeBuchholz(code.forfeitsAsPlayed()) : new Buchholz(code.forfeitsAsPlayed());
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public TieBreakValue valueOf(ParticipantId participant, TieBreakContext context) {
        var terms = buchholz.termsOf(participant, context);
        var strength = terms.stream().map(Term::value).reduce(BigDecimal.ZERO, BigDecimal::add);
        var factor = normalisingFactor(context);
        var secondary =
                context.in(context.score().other()).of(participant).score().toBigDecimal();
        var value = secondary.add(strength.divide(factor, SCALE, RoundingMode.HALF_EVEN));
        return new TieBreakValue(
                code,
                Optional.of(value.stripTrailingZeros().scale() < 0 ? value.setScale(0) : value.stripTrailingZeros()),
                true,
                terms.stream().map(Term::contribution).toList(),
                "C.07 13.4: secondary score " + secondary.toPlainString() + " + Buchholz " + strength.toPlainString()
                        + " / " + factor);
    }

    /** 13.4.2, rounded to the nearest integer towards zero; never below 1. */
    private BigDecimal normalisingFactor(TieBreakContext context) {
        if (code.normalisingFactor().isPresent()) {
            return BigDecimal.valueOf(code.normalisingFactor().get());
        }
        var highestPrimary = context.winValue().toBigDecimal().multiply(BigDecimal.valueOf(context.totalRounds()));
        var highestSecondary = context.in(context.score().other()).winValue().toBigDecimal();
        var factor = highestPrimary.divide(highestSecondary, 0, RoundingMode.DOWN);
        return factor.signum() > 0 ? factor : BigDecimal.ONE;
    }
}
