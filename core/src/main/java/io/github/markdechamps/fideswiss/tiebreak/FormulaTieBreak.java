package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakContribution;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * A tie-break that is a formula rather than a per-round sum: TPR, PTP and the averages over the opponents met over
 * the board (APRO, APPO, AOB). Its contributions are the opponents' values it averages, where it has them.
 */
final class FormulaTieBreak implements IndividualTieBreak {

    private final TieBreakCode code;
    private final String article;
    private final BiFunction<ParticipantId, TieBreakContext, Optional<BigDecimal>> own;
    private final Optional<Averaging> averaging;

    /** Averages the value of each opponent met over the board, rounded half up or kept exact. */
    private record Averaging(
            BiFunction<ParticipantId, TieBreakContext, Optional<BigDecimal>> ofOpponent, boolean rounded) {}

    private FormulaTieBreak(
            TieBreakCode code,
            String article,
            BiFunction<ParticipantId, TieBreakContext, Optional<BigDecimal>> own,
            Optional<Averaging> averaging) {
        this.code = code;
        this.article = article;
        this.own = own;
        this.averaging = averaging;
    }

    static FormulaTieBreak tournamentPerformanceRating(TieBreakCode code) {
        return new FormulaTieBreak(code, "C.07 10.2", Performance::tournamentPerformanceRating, Optional.empty());
    }

    static FormulaTieBreak perfectTournamentPerformance(TieBreakCode code) {
        return new FormulaTieBreak(code, "C.07 10.3", Performance::perfectTournamentPerformance, Optional.empty());
    }

    static FormulaTieBreak averagePerformanceOfOpponents(TieBreakCode code) {
        return averaging(code, "C.07 10.4", Performance::tournamentPerformanceRating, true);
    }

    static FormulaTieBreak averagePerfectPerformanceOfOpponents(TieBreakCode code) {
        return averaging(code, "C.07 10.5", Performance::perfectTournamentPerformance, true);
    }

    /** 8.2: the average Buchholz (with {@code /F}, Fore Buchholz) of the opponents met over the board. */
    static FormulaTieBreak averageOfOpponentsBuchholz(TieBreakCode code) {
        var buchholz = code.fore() ? new ForeBuchholz(false) : new Buchholz(false);
        BiFunction<ParticipantId, TieBreakContext, Optional<BigDecimal>> ofOpponent =
                (opponent, context) -> Optional.of(buchholz.termsOf(opponent, context).stream()
                        .map(Term::value)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        return averaging(code, "C.07 8.2", ofOpponent, false);
    }

    private static FormulaTieBreak averaging(
            TieBreakCode code,
            String article,
            BiFunction<ParticipantId, TieBreakContext, Optional<BigDecimal>> ofOpponent,
            boolean rounded) {
        return new FormulaTieBreak(
                code,
                article,
                (participant, context) -> Optional.empty(),
                Optional.of(new Averaging(ofOpponent, rounded)));
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public TieBreakValue valueOf(ParticipantId participant, TieBreakContext context) {
        return averaging
                .map(average -> averaged(participant, context, average))
                .orElseGet(() -> new TieBreakValue(code, own.apply(participant, context), true, List.of(), article));
    }

    private TieBreakValue averaged(ParticipantId participant, TieBreakContext context, Averaging average) {
        var contributions = context.of(participant).games().stream()
                .flatMap(game -> {
                    var opponent = game.opponent().orElseThrow();
                    return average.ofOpponent().apply(opponent, context).stream()
                            .map(value -> new TieBreakContribution(
                                    Optional.of(game.round()), opponent.value(), value, false, List.of(article)));
                })
                .toList();
        if (contributions.isEmpty()) {
            return new TieBreakValue(code, Optional.empty(), true, List.of(), article);
        }
        var sum = contributions.stream().map(TieBreakContribution::value).reduce(BigDecimal.ZERO, BigDecimal::add);
        var count = BigDecimal.valueOf(contributions.size());
        var value = average.rounded()
                ? sum.divide(count, 0, RoundingMode.HALF_UP)
                : sum.divide(count, MathContext.DECIMAL64);
        return new TieBreakValue(code, Optional.of(value), true, contributions, article);
    }
}
