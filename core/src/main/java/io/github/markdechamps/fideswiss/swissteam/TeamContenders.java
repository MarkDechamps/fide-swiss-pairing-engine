package io.github.markdechamps.fideswiss.swissteam;

import io.github.markdechamps.fideswiss.history.RoundRecord;
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
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * The teams to be paired in the next round, each as the Top-Scoregroup Procedure sees it, derived from the
 * recorded rounds: a match with at least one board played gives a colour (board 1's, 1.6.1) and counts as
 * played (rulings A3, A9); a match not forfeited is a meeting ([C1], GHR 3.5).
 */
final class TeamContenders {

    private final Tournament tournament;
    private final TournamentHistory history;
    private final FloatScore floatScore;
    private final Points secondaryPairingAllocatedByeValue;

    TeamContenders(Tournament tournament, FloatScore floatScore, Points secondaryPairingAllocatedByeValue) {
        this.tournament = tournament;
        this.history = TournamentHistory.of(tournament);
        this.floatScore = floatScore;
        this.secondaryPairingAllocatedByeValue = secondaryPairingAllocatedByeValue;
    }

    List<Contender> toBePaired(PairingNumbers numbers) {
        return tournament.participantsToBePaired().stream()
                .map(participant -> contender(participant.id(), numbers))
                .toList();
    }

    private Contender contender(ParticipantId team, PairingNumbers numbers) {
        var colours = new ArrayList<Colour>();
        var met = new HashSet<ParticipantId>();
        var matchesPlayed = 0;
        var barred = false;
        for (var round : tournament.rounds()) {
            var board = round.boardOf(team);
            if (board.isPresent()) {
                var outcome = board.get().outcome();
                var side = board.get().colourOf(team);
                if (outcome.isPlayed()) {
                    matchesPlayed++;
                    colours.add(side);
                }
                if (outcome.isMeeting()) {
                    met.add(board.get().opponentOf(team));
                }
                barred |= wonByForfeit(outcome, side);
            } else {
                barred |= round.byeOf(team)
                        .filter(TeamContenders::barsThePairingAllocatedBye)
                        .isPresent();
            }
        }
        return new Contender(
                team,
                numbers.numberOf(team).value(),
                pairingScoreBefore(team, tournament.nextRound()),
                secondaryScore(team),
                matchesPlayed,
                colours,
                met,
                barred,
                floatedInPreviousRound(team));
    }

    /** [C2] (ruling A4): a match the opponent forfeited on every board. */
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

    /** 1.5: in the previous round, the team played an opponent with a different score (ruling A8 says which). */
    private boolean floatedInPreviousRound(ParticipantId team) {
        if (tournament.rounds().isEmpty()) {
            return false;
        }
        var previous = tournament.rounds().getLast();
        return previous.boardOf(team)
                .filter(board -> board.outcome().isPlayed())
                .map(board -> !floatScoreBefore(team, previous.number())
                        .equals(floatScoreBefore(board.opponentOf(team), previous.number())))
                .orElse(false);
    }

    private Points floatScoreBefore(ParticipantId team, RoundNumber round) {
        return floatScore == FloatScore.PAIRING
                ? pairingScoreBefore(team, round)
                : history.of(team).scoreBefore(round).points();
    }

    private Points pairingScoreBefore(ParticipantId team, RoundNumber round) {
        return history.of(team).scoreBefore(round).points().plus(tournament.virtualPointsOf(team, round));
    }

    private Points secondaryScore(ParticipantId team) {
        var total = Points.ZERO;
        for (var index = 0; index < tournament.rounds().size(); index++) {
            total = total.plus(secondaryPointsIn(team, index));
        }
        return total;
    }

    private Points secondaryPointsIn(ParticipantId team, int index) {
        var scoring = tournament.settings().scoring();
        var round = tournament.rounds().get(index);
        return round.boardOf(team)
                .map(board -> scoring.secondaryPointsFor(board.outcome(), board.colourOf(team)))
                .orElseGet(
                        () -> scoring.secondaryPointsFor(byeOf(team, round, index), secondaryPairingAllocatedByeValue));
    }

    private Bye byeOf(ParticipantId team, Round round, int index) {
        return switch (history.of(team).records().get(index)) {
            case RoundRecord.NoBoard noBoard -> noBoard.bye();
            default -> round.byeOf(team).orElse(Bye.ZERO_POINT);
        };
    }
}
