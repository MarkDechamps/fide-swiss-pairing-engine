package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.HashSet;
import java.util.List;

/**
 * A model tournament (bbpPairings' {@code model -g}, the CLI's {@code --model}): the generator replays its field
 * under its settings and number of rounds, with the forfeit, bye and withdrawal rates observed in it. A rate of an
 * event that never happened is 0, never.
 */
public final class TournamentModel {

    private TournamentModel() {}

    public static GeneratorSettings applyTo(GeneratorSettings settings, Tournament model) {
        var field = model.participants();
        var ratings = field.stream()
                .filter(participant -> participant.rating().isRated())
                .map(participant -> participant.rating().valueOrZero())
                .toList();
        var modelled = settings.with(model.settings())
                .withField(field)
                .withPlayers(Range.of(field.size()))
                .withRounds(Range.of(model.settings().numberOfRounds().value()))
                .withForfeitRate(Range.of(rate(games(model), forfeits(model))))
                .withHalfPointByeRate(Range.of(rate(playerRounds(model), byes(model, Bye.HALF_POINT))))
                .withZeroPointByeRate(Range.of(rate(playerRounds(model), byes(model, Bye.ZERO_POINT))))
                .withFullPointByeRate(Range.of(rate(playerRounds(model), byes(model, Bye.FULL_POINT))))
                .withWithdrawalPercentage(Range.of(percentage(withdrawn(model), field.size())))
                .withLateEntryPercentage(Range.of(0));
        return ratings.isEmpty()
                ? modelled
                : modelled.withHighestRating(Range.of(max(ratings))).withLowestRating(Range.of(min(ratings)));
    }

    /** Once in N: N is the number of opportunities per event, rounded; 0 when the event never happened. */
    private static int rate(int opportunities, int events) {
        return events == 0 ? 0 : Math.max(1, Math.round((float) opportunities / events));
    }

    private static int percentage(int part, int whole) {
        return whole == 0 ? 0 : Math.round(100f * part / whole);
    }

    private static int games(Tournament model) {
        return model.rounds().stream().mapToInt(round -> round.boards().size()).sum();
    }

    private static int forfeits(Tournament model) {
        return (int) model.rounds().stream()
                .flatMap(round -> round.boards().stream())
                .map(Board::outcome)
                .filter(outcome -> !outcome.isPlayed())
                .count();
    }

    private static int playerRounds(Tournament model) {
        return model.participants().size() * model.rounds().size();
    }

    private static int byes(Tournament model, Bye bye) {
        return (int) model.rounds().stream()
                .map(Round::byes)
                .flatMap(byes -> byes.values().stream())
                .filter(bye::equals)
                .count();
    }

    private static int withdrawn(Tournament model) {
        var withdrawn = new HashSet<ParticipantId>();
        model.rounds()
                .forEach(round -> round.byes().forEach((participant, bye) -> {
                    if (bye == Bye.WITHDRAWN) {
                        withdrawn.add(participant);
                    }
                }));
        return withdrawn.size();
    }

    private static int max(List<Integer> ratings) {
        return ratings.stream().mapToInt(Integer::intValue).max().orElseThrow();
    }

    private static int min(List<Integer> ratings) {
        return ratings.stream().mapToInt(Integer::intValue).min().orElseThrow();
    }
}
