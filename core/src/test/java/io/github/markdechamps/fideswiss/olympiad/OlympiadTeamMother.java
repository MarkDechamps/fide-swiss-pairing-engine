package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Rating;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Olympiad teams for rule-level tests: id = initial pairing number, matchpoints, board-1 colours as "WB-" ("-" for a
 * round without a played match) and the ids of the teams met.
 */
final class OlympiadTeamMother {

    private OlympiadTeamMother() {}

    static Team team(int number, int matchPoints, String colours, int... met) {
        return new Team(
                id(number),
                PairingNumber.of(number),
                Points.of(matchPoints),
                colours.chars().mapToObj(OlympiadTeamMother::colour).toList(),
                Arrays.stream(met).mapToObj(OlympiadTeamMother::id).collect(Collectors.toSet()),
                true,
                Rating.of(2600 - number));
    }

    static Team withoutBye(Team team) {
        return new Team(
                team.id(), team.pairingNumber(), team.matchPoints(), team.board1(), team.met(), false, team.rating());
    }

    static Team rated(Team team, int rating) {
        return new Team(
                team.id(),
                team.pairingNumber(),
                team.matchPoints(),
                team.board1(),
                team.met(),
                team.mayReceiveBye(),
                Rating.of(rating));
    }

    /** The round after the teams' colours, White by lot, paired by the matchings. */
    static OlympiadProcedure.Outcome pair(List<Team> teams) {
        return pair(teams, Matchings.BLOSSOM);
    }

    static OlympiadProcedure.Outcome pair(List<Team> teams, Matchings matchings) {
        var round = teams.getFirst().board1().size() + 1;
        return new OlympiadProcedure(InitialColour.white(), round, matchings).pair(new ArrayList<>(teams));
    }

    /** The pairs as "a-b" with the smaller id first, sorted. */
    static List<String> pairs(OlympiadProcedure.Outcome outcome) {
        return outcome.games().stream()
                .map(game -> {
                    var white = Integer.parseInt(game.white().id().value());
                    var black = Integer.parseInt(game.black().id().value());
                    return Math.min(white, black) + "-" + Math.max(white, black);
                })
                .sorted()
                .toList();
    }

    /** The games as "white-black". */
    static List<String> games(OlympiadProcedure.Outcome outcome) {
        return outcome.games().stream()
                .map(game -> game.white() + "-" + game.black())
                .toList();
    }

    static List<String> sorted(String pairs) {
        return Arrays.stream(pairs.split(" ")).sorted().toList();
    }

    private static Optional<Colour> colour(int symbol) {
        return switch (symbol) {
            case 'W' -> Optional.of(Colour.WHITE);
            case 'B' -> Optional.of(Colour.BLACK);
            default -> Optional.empty();
        };
    }

    private static ParticipantId id(int number) {
        return ParticipantId.of(String.valueOf(number));
    }
}
