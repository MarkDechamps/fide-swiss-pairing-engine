package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * What the generator plays: the tournament settings (system, editions, scoring; the number of rounds is drawn),
 * and per-tournament ranges for the field and its events. A rate N means once in N games or player-rounds; 0
 * means never. The defaults follow bbpPairings v6's generator (ticket Random tournament generator).
 */
public record GeneratorSettings(
        TournamentSettings tournament,
        Range players,
        Range rounds,
        Range highestRating,
        Range lowestRating,
        Range unratedPercentage,
        Range forfeitRate,
        Range halfPointByeRate,
        Range zeroPointByeRate,
        Range fullPointByeRate,
        Range withdrawalPercentage,
        Optional<Range> retirementRate,
        Range lateEntryPercentage,
        Chance bakuAcceleration,
        Chance nonStandardScoring,
        Chance drawnTieBreaks,
        Optional<List<Participant>> field,
        ResultModel resultModel) {

    public GeneratorSettings {
        Objects.requireNonNull(tournament, "tournament");
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(rounds, "rounds");
        Objects.requireNonNull(highestRating, "highestRating");
        Objects.requireNonNull(lowestRating, "lowestRating");
        Objects.requireNonNull(unratedPercentage, "unratedPercentage");
        Objects.requireNonNull(forfeitRate, "forfeitRate");
        Objects.requireNonNull(halfPointByeRate, "halfPointByeRate");
        Objects.requireNonNull(zeroPointByeRate, "zeroPointByeRate");
        Objects.requireNonNull(fullPointByeRate, "fullPointByeRate");
        Objects.requireNonNull(withdrawalPercentage, "withdrawalPercentage");
        Objects.requireNonNull(retirementRate, "retirementRate");
        Objects.requireNonNull(lateEntryPercentage, "lateEntryPercentage");
        Objects.requireNonNull(bakuAcceleration, "bakuAcceleration");
        Objects.requireNonNull(nonStandardScoring, "nonStandardScoring");
        Objects.requireNonNull(drawnTieBreaks, "drawnTieBreaks");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(resultModel, "resultModel");
    }

    public static GeneratorSettings of(TournamentSettings tournament) {
        return new GeneratorSettings(
                tournament,
                Range.of(15, 215),
                Range.of(5, 15),
                Range.of(2400, 2800),
                Range.of(1400, 2300),
                Range.of(0, 10),
                Range.of(6, 30),
                Range.of(15, 3225),
                Range.of(15, 3225),
                Range.of(0),
                Range.of(0, 5),
                Optional.empty(),
                Range.of(0, 5),
                Chance.never(),
                Chance.never(),
                Chance.never(),
                Optional.empty(),
                new MilvangModel());
    }

    /** {@code --acceleration random}: Baku for 20% of the tournaments. */
    public GeneratorSettings withRandomAcceleration() {
        return withBakuAcceleration(Chance.percent(20));
    }

    /** {@code --random-scoring}: a non-standard scoring for 10% of the tournaments. */
    public GeneratorSettings withRandomScoring() {
        return withNonStandardScoring(Chance.percent(10));
    }

    /** {@code --tiebreaks random}: every tournament draws its own Tie-break List. */
    public GeneratorSettings withRandomTieBreaks() {
        return withDrawnTieBreaks(Chance.always());
    }

    public GeneratorSettings with(TournamentSettings settings) {
        return new GeneratorSettings(
                settings,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withPlayers(Range range) {
        return new GeneratorSettings(
                tournament,
                range,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withRounds(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                range,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withHighestRating(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                range,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withLowestRating(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                range,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withUnratedPercentage(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                range,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withForfeitRate(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                range,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withHalfPointByeRate(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                range,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withZeroPointByeRate(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                range,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withFullPointByeRate(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                range,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    public GeneratorSettings withWithdrawalPercentage(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                range,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    /** bbp/JaVaFo's {@code RetiredRate}: once in N player-rounds a player retires; replaces the percentage. */
    public GeneratorSettings withRetirementRate(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                Optional.of(range),
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    /** The share of the field entering late (GHR 2.4), each in a round from 2 to the middle round. */
    public GeneratorSettings withLateEntryPercentage(Range range) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                range,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    /** The share of tournaments accelerated by Baku (C.04.7) when their scoring allows it. */
    public GeneratorSettings withBakuAcceleration(Chance chance) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                chance,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                resultModel);
    }

    /** The share of tournaments scored 3/1/0 or 2/1/0 instead of the settings' scoring. */
    public GeneratorSettings withNonStandardScoring(Chance chance) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                chance,
                drawnTieBreaks,
                field,
                resultModel);
    }

    /** The share of tournaments whose Tie-break List is drawn from the Tie-break Edition's catalogue. */
    public GeneratorSettings withDrawnTieBreaks(Chance chance) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                chance,
                field,
                resultModel);
    }

    /** A fixed field, in starting-rank order, in place of a drawn one (a model TRF); nobody enters late. */
    public GeneratorSettings withField(List<Participant> participants) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                Optional.of(List.copyOf(participants)),
                resultModel);
    }

    public GeneratorSettings with(ResultModel model) {
        return new GeneratorSettings(
                tournament,
                players,
                rounds,
                highestRating,
                lowestRating,
                unratedPercentage,
                forfeitRate,
                halfPointByeRate,
                zeroPointByeRate,
                fullPointByeRate,
                withdrawalPercentage,
                retirementRate,
                lateEntryPercentage,
                bakuAcceleration,
                nonStandardScoring,
                drawnTieBreaks,
                field,
                model);
    }
}
