package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/** A tie-break read off its terms: their sum, or their average rounded half up to a whole number (ARO, 10.1). */
final class TermTieBreak implements IndividualTieBreak {

    enum Aggregation {
        SUM,
        AVERAGE_ROUNDED
    }

    private final TieBreakCode code;
    private final TermSource source;
    private final Aggregation aggregation;
    private final String article;

    TermTieBreak(TieBreakCode code, TermSource source, Aggregation aggregation, String article) {
        this.code = code;
        this.source = source;
        this.aggregation = aggregation;
        this.article = article;
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public TieBreakValue valueOf(ParticipantId participant, TieBreakContext context) {
        var terms = source.termsOf(participant, context);
        var counted =
                terms.stream().filter(term -> !term.cut()).map(Term::value).toList();
        var total = counted.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        var value = aggregation == Aggregation.SUM || counted.isEmpty()
                ? total
                : total.divide(BigDecimal.valueOf(counted.size()), 0, RoundingMode.HALF_UP);
        return new TieBreakValue(
                code,
                Optional.of(value.stripTrailingZeros().scale() < 0 ? value.setScale(0) : value.stripTrailingZeros()),
                true,
                terms.stream().map(Term::contribution).toList(),
                article);
    }
}
