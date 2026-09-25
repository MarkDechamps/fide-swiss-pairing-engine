package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import java.util.function.UnaryOperator;

/**
 * JaVaFo's and bbpPairings' generator configuration: {@code Key=Value} lines whose keys fix a parameter; a
 * missing key keeps its random default. Unknown keys are ignored, as both programs do.
 */
public final class RtgConfiguration {

    private RtgConfiguration() {}

    public static GeneratorSettings applyTo(GeneratorSettings settings, String configuration) {
        var result = settings;
        for (var line : configuration.split("\r\n|\r|\n")) {
            var equals = line.indexOf('=');
            if (line.isBlank() || line.startsWith("#") || equals < 0) {
                continue;
            }
            result = applied(
                    result,
                    line.substring(0, equals).trim(),
                    line.substring(equals + 1).trim());
        }
        return result;
    }

    private static GeneratorSettings applied(GeneratorSettings settings, String key, String value) {
        return switch (key) {
            case "PlayersNumber" -> settings.withPlayers(range(key, value));
            case "RoundsNumber" -> settings.withRounds(range(key, value));
            case "ForfeitRate" -> settings.withForfeitRate(range(key, value));
            case "RetiredRate" -> settings.withRetirementRate(range(key, value));
            case "HalfPointByeRate", "HalfPointByteRate" -> settings.withHalfPointByeRate(range(key, value));
            case "HighestRating" -> settings.withHighestRating(range(key, value));
            case "LowestRating" -> settings.withLowestRating(range(key, value));
            case "DrawPercentage" ->
                settings.with(new FlatDrawModel(range(key, value).min()));
            case "PointsForWin" ->
                withScoring(
                        settings,
                        scoring -> new ScoringScheme(
                                points(key, value), scoring.draw(), scoring.loss(), scoring.pairingAllocatedBye()));
            case "PointsForDraw" ->
                withScoring(
                        settings,
                        scoring -> new ScoringScheme(
                                scoring.win(), points(key, value), scoring.loss(), scoring.pairingAllocatedBye()));
            case "PointsForLoss" ->
                withScoring(
                        settings,
                        scoring -> new ScoringScheme(
                                scoring.win(), scoring.draw(), points(key, value), scoring.pairingAllocatedBye()));
            default -> settings;
        };
    }

    private static GeneratorSettings withScoring(GeneratorSettings settings, UnaryOperator<ScoringScheme> change) {
        return settings.with(
                settings.tournament().with(change.apply(settings.tournament().scoring())));
    }

    private static Range range(String key, String value) {
        try {
            return Range.parse(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(key + " must be a number: " + value, e);
        }
    }

    private static Points points(String key, String value) {
        try {
            return Points.of(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a number: " + value, e);
        }
    }
}
