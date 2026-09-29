package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.Outcome;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * The teams to be paired in the next round, derived from the recorded rounds. A match with at least one board
 * played is a meeting (6.1) and gives board 1 a colour (7.1, 7.7); a match won or lost by default, on every board,
 * does neither.
 */
final class OlympiadTeams {

    private OlympiadTeams() {}

    static List<Team> toBePaired(Tournament tournament) {
        var numbers = tournament.pairingNumbers();
        var history = TournamentHistory.of(tournament);
        return tournament.participantsToBePaired().stream()
                .map(participant -> {
                    var id = participant.id();
                    var board1 = new ArrayList<Optional<Colour>>();
                    var met = new HashSet<ParticipantId>();
                    var mayReceiveBye = !joinedLate(tournament, history, id);
                    for (var round : tournament.rounds()) {
                        var board = round.boardOf(id);
                        var played = board.filter(found -> found.outcome().isPlayed());
                        board1.add(played.map(found -> found.colourOf(id)));
                        played.ifPresent(found -> met.add(found.opponentOf(id)));
                        mayReceiveBye &= board.map(found -> !wonByDefault(found.outcome(), found.colourOf(id)))
                                .orElse(true);
                        mayReceiveBye &= round.byeOf(id)
                                .filter(Bye.PAIRING_ALLOCATED::equals)
                                .isEmpty();
                    }
                    return new Team(
                            id,
                            numbers.numberOf(id),
                            history.of(id).scoreBefore(tournament.nextRound()).points(),
                            board1,
                            met,
                            mayReceiveBye,
                            participant.rating());
                })
                .toList();
    }

    /** 4.2.3: joined after the round-1 pairings were published, a Late Entry. */
    private static boolean joinedLate(Tournament tournament, TournamentHistory history, ParticipantId team) {
        return tournament.isLateEntry(team)
                || !tournament.rounds().isEmpty()
                        && history.of(team).recordOf(RoundNumber.FIRST) instanceof RoundRecord.NoBoard noBoard
                        && noBoard.bye() == Bye.NOT_YET_ENTERED;
    }

    /** 4.2.2: won a match because the opposing team did not arrive, read as a forfeit on every board. */
    private static boolean wonByDefault(Outcome outcome, Colour side) {
        return switch (outcome) {
            case MatchOutcome match -> match.isForfeitedBy(side.opposite()) && !match.isForfeitedBy(side);
            case GameOutcome game -> game.isForfeitedBy(side.opposite()) && !game.isForfeitedBy(side);
        };
    }
}
