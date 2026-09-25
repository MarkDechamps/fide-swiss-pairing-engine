package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakValue;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/** A type B tie-break read off the participant's own record (C.07 art. 7, 10.6), one rule per factory. */
final class OwnRecordTieBreak implements IndividualTieBreak {

    private static final BigDecimal HALF = new BigDecimal("0.5");

    private final TieBreakCode code;
    private final String article;
    private final boolean higherIsBetter;
    private final BiFunction<ParticipantRounds, TieBreakContext, BigDecimal> rule;

    private OwnRecordTieBreak(
            TieBreakCode code,
            String article,
            boolean higherIsBetter,
            BiFunction<ParticipantRounds, TieBreakContext, BigDecimal> rule) {
        this.code = code;
        this.article = article;
        this.higherIsBetter = higherIsBetter;
        this.rule = rule;
    }

    /** 7.1: rounds in which the participant got a win's points, with or without playing. */
    static OwnRecordTieBreak wins(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.1",
                true,
                (rounds, context) -> count(rounds.entries().stream()
                        .filter(entry -> entry.points().equals(context.winValue()))
                        .count()));
    }

    /** 7.2: games won over the board. */
    static OwnRecordTieBreak gamesWon(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.2",
                true,
                (rounds, context) -> count(
                        rounds.games().stream().filter(OwnRecordTieBreak::isWin).count()));
    }

    /** 7.3: games played over the board with Black. */
    static OwnRecordTieBreak gamesWithBlack(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.3",
                true,
                (rounds, context) -> count(rounds.games().stream()
                        .filter(OwnRecordTieBreak::isBlack)
                        .count()));
    }

    /** 7.4: games won over the board with Black. */
    static OwnRecordTieBreak winsWithBlack(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.4",
                true,
                (rounds, context) -> count(rounds.games().stream()
                        .filter(game -> isBlack(game) && isWin(game))
                        .count()));
    }

    /**
     * 7.6: the rounds less the half-point byes, zero-point byes (every round after a withdrawal among them, 16.1.1)
     * and forfeit losses, a double forfeit included (Tie-break interpretation rulings #11).
     */
    static OwnRecordTieBreak roundsElectedToPlay(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.6",
                true,
                (rounds, context) -> count(rounds.entries().stream()
                        .filter(entry -> !entry.kind().isRequestedBye() && entry.kind() != RoundEntry.Kind.FORFEIT_LOSS)
                        .count()));
    }

    /**
     * 7.7: 1 for each round scored above the scheduled opponent or above a draw without playing, and ½ for each
     * round level with the opponent or exactly a draw without playing.
     */
    static OwnRecordTieBreak standardPoints(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.7",
                true,
                (rounds, context) -> rounds.entries().stream()
                        .map(entry -> {
                            var par = entry.opponent()
                                    .map(opponent -> opponentPointsIn(opponent, entry, context))
                                    .orElse(context.drawValue());
                            var difference = entry.points().compareTo(par);
                            if (difference > 0) {
                                return BigDecimal.ONE;
                            }
                            return difference == 0 ? HALF : BigDecimal.ZERO;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    /** 7.8: the Pairing Number of the latest paired round, ascending (ADR 0006). */
    static OwnRecordTieBreak pairingNumber(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 7.8",
                false,
                (rounds, context) -> count(context.pairingNumberOrder().indexOf(rounds.id()) + 1L));
    }

    /** 10.6: the rating, highest first. */
    static OwnRecordTieBreak rating(TieBreakCode code) {
        return new OwnRecordTieBreak(
                code,
                "C.07 10.6",
                true,
                (rounds, context) -> count(rounds.participant().rating().valueOrZero()));
    }

    /**
     * 9.2 in a Swiss: points scored against every participant with at least half the maximum possible score, the
     * limit moved by {@code /L} in half points (14.5). It uses raw final scores and every paired round, forfeits at
     * their awarded points (Tie-break interpretation rulings #2).
     */
    static OwnRecordTieBreak koya(TieBreakCode code) {
        return new OwnRecordTieBreak(code, "C.07 9.2", true, (rounds, context) -> {
            var limit = context.winValue()
                    .toBigDecimal()
                    .multiply(BigDecimal.valueOf(context.roundsCounted()))
                    .multiply(HALF)
                    .add(context.drawValue().toBigDecimal().multiply(BigDecimal.valueOf(code.limitShift())));
            return rounds.entries().stream()
                    .filter(entry -> entry.opponent()
                            .filter(opponent ->
                                    context.of(opponent).score().toBigDecimal().compareTo(limit) >= 0)
                            .isPresent())
                    .map(entry -> entry.points().toBigDecimal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        });
    }

    @Override
    public TieBreakCode code() {
        return code;
    }

    @Override
    public TieBreakValue valueOf(ParticipantId participant, TieBreakContext context) {
        var value = rule.apply(context.of(participant), context);
        return new TieBreakValue(code, Optional.of(value), higherIsBetter, List.of(), article);
    }

    private static io.github.markdechamps.fideswiss.tournament.Points opponentPointsIn(
            ParticipantId opponent, RoundEntry entry, TieBreakContext context) {
        return context.of(opponent).entries().get(entry.round().value() - 1).points();
    }

    private static boolean isWin(RoundEntry game) {
        return game.result().filter(GameResult.WIN::equals).isPresent();
    }

    private static boolean isBlack(RoundEntry game) {
        return game.colour().filter(Colour.BLACK::equals).isPresent();
    }

    private static BigDecimal count(long count) {
        return BigDecimal.valueOf(count);
    }
}
