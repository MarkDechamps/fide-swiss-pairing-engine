package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
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
        ResultModel resultModel) {

    public GeneratorSettings {
        Objects.requireNonNull(tournament, "tournament");
        Objects.requireNonNull(retirementRate, "retirementRate");
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
                new MilvangModel());
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
                model);
    }
}
