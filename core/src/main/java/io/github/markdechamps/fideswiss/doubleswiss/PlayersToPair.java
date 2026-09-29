package io.github.markdechamps.fideswiss.doubleswiss;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.FloatScore;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.Outcome;
import io.github.markdechamps.fideswiss.tournament.PairingNumbers;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The players to be paired in the next round, each as the Top-Scoregroup Procedure sees it, derived from the
 * recorded matches. A match is forfeited only when a player forfeited both games (Preface); any other match is a
 * meeting ([C1]), counts as played (3.4.3) and can make a floater (1.5). It gives a colour, the one scheduled for
 * game 1, only if at least one game was actually played (1.6).
 */
final class PlayersToPair {

    private final Tournament tournament;
    private final TournamentHistory history;
    private final FloatScore floatScore;

    PlayersToPair(Tournament tournament, FloatScore floatScore) {
        this.tournament = tournament;
        this.history = TournamentHistory.of(tournament);
        this.floatScore = floatScore;
    }

    List<Contender> toBePaired(PairingNumbers numbers) {
        return tournament.participantsToBePaired().stream()
                .map(participant -> contender(participant.id(), numbers))
                .toList();
    }

    private Contender contender(ParticipantId player, PairingNumbers numbers) {
        var colours = new ArrayList<Colour>();
        var met = new HashSet<ParticipantId>();
        var matchesPlayed = 0;
        var barred = false;
        for (var round : tournament.rounds()) {
            var board = round.boardOf(player);
            if (board.isPresent()) {
                var outcome = board.get().outcome();
                var side = board.get().colourOf(player);
                if (outcome.isPlayed()) {
                    colours.add(side);
                }
                if (outcome.isMeeting()) {
                    matchesPlayed++;
                    met.add(board.get().opponentOf(player));
                }
                barred |= wonByForfeit(outcome, side);
            } else {
                barred |= round.byeOf(player)
                        .filter(PlayersToPair::barsThePairingAllocatedBye)
                        .isPresent();
            }
        }
        var score = pairingScoreBefore(player, tournament.nextRound());
        return new Contender(
                player,
                numbers.numberOf(player).value(),
                score,
                score,
                matchesPlayed,
                colours,
                met,
                barred,
                floatedInPreviousRound(player));
    }

    /** [C2]: a match the opponent forfeited (both games), while this player did not. */
    private static boolean wonByForfeit(Outcome outcome, Colour side) {
        return switch (outcome) {
            case MatchOutcome match -> match.isForfeitedBy(side.opposite()) && !match.isForfeitedBy(side);
            case GameOutcome game -> game.isForfeitedBy(side.opposite()) && !game.isForfeitedBy(side);
        };
    }

    /** [C2]: a PAB, or a FIDE-deprecated full-point bye; a half-point bye does not bar it. */
    private static boolean barsThePairingAllocatedBye(Bye bye) {
        return bye == Bye.PAIRING_ALLOCATED || bye == Bye.FULL_POINT;
    }

    /** 1.5: in the previous round, the player met an opponent with a different score before that round. */
    private boolean floatedInPreviousRound(ParticipantId player) {
        if (tournament.rounds().isEmpty()) {
            return false;
        }
        var previous = tournament.rounds().getLast();
        return previous.boardOf(player)
                .filter(board -> board.outcome().isMeeting())
                .map(board -> !floatScoreBefore(player, previous.number())
                        .equals(floatScoreBefore(board.opponentOf(player), previous.number())))
                .orElse(false);
    }

    private Points floatScoreBefore(ParticipantId player, RoundNumber round) {
        return floatScore == FloatScore.PAIRING
                ? pairingScoreBefore(player, round)
                : history.of(player).scoreBefore(round).points();
    }

    private Points pairingScoreBefore(ParticipantId player, RoundNumber round) {
        return history.of(player).scoreBefore(round).points().plus(tournament.virtualPointsOf(player, round));
    }
}
