package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.pairing.Violation;
import io.github.markdechamps.fideswiss.rules.BasicRules;
import io.github.markdechamps.fideswiss.rules.BoardOrder;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The Lim System (C.04.4.3, 2026), with or without the Maxi-tournament restrictions. */
public final class LimSystem implements PairingSystem {

    private static final Comparator<Player> RANKING =
            Comparator.comparing(Player::pairingScore).reversed().thenComparingInt(Player::tpn);

    private static final Comparator<Game> BOARD_ORDER = BoardOrder.of(
            game -> higherRanked(game).pairingScore(),
            game -> lowerRanked(game).pairingScore(),
            game -> higherRanked(game).pairingNumber());

    private final MaxiTournament maxiTournament;

    public LimSystem(MaxiTournament maxiTournament) {
        this.maxiTournament = maxiTournament;
    }

    @Override
    public String name() {
        return "C.04.4.3 Lim System 2026" + (maxiTournament == MaxiTournament.DECLARED ? " (Maxi-tournament)" : "");
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        try {
            var outcome = new LimProcedure(roundOf(tournament), Reachability.MATCHING).pair(playersToPair(tournament));
            return roundPairing(tournament, outcome);
        } catch (NoRoundPairingException e) {
            throw new NoLegalPairingException(
                    List.of(Problem.citing(e.article(), e.getMessage())), new PairingTrace(List.of()));
        }
    }

    /** The Basic Rules, and 2.1: the colour limits make an incompatible pair illegal outside the last round. */
    @Override
    public List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        var violations = new ArrayList<>(BasicRules.violationsOf(tournament, proposed));
        var round = roundOf(tournament);
        var players = playersToPair(tournament).stream().collect(Collectors.toMap(Player::id, Function.identity()));
        for (var board : proposed.boards()) {
            var white = players.get(board.white());
            var black = players.get(board.black());
            if (white != null && black != null && !white.hasMet(black) && !round.compatible(white, black)) {
                violations.add(Violation.of(
                        "C.04.4.3 2.1, 5.1",
                        board.white() + " and " + board.black() + " cannot meet within the colour limits",
                        board.white(),
                        board.black()));
            }
        }
        return violations;
    }

    RoundToPair roundOf(Tournament tournament) {
        var settings = tournament.settings();
        return new RoundToPair(
                tournament.nextRound(),
                settings.numberOfRounds(),
                settings.initialColour(),
                settings.scoring().win(),
                maxiTournament);
    }

    /** Scoregroups, the PAB, board order and 3.10 read the Pairing Score (Acceleration readings). */
    static List<Player> playersToPair(Tournament tournament) {
        var numbers = tournament.pairingNumbers();
        var history = TournamentHistory.of(tournament);
        return tournament.participantsToBePaired().stream()
                .map(Participant::id)
                .map(id -> {
                    var own = history.of(id);
                    return new Player(
                            id,
                            numbers.numberOf(id),
                            tournament.participant(id).rating(),
                            pairingScoreBefore(tournament, history, id, tournament.nextRound()),
                            own.playedColours(),
                            opponentsMet(own),
                            own.mayReceivePairingAllocatedBye(),
                            floatedPreviousRound(tournament, history, own));
                })
                .toList();
    }

    private static Set<ParticipantId> opponentsMet(ParticipantHistory player) {
        return player.records().stream()
                .filter(RoundRecord.Game.class::isInstance)
                .map(record -> ((RoundRecord.Game) record).opponent())
                .collect(Collectors.toSet());
    }

    /** 3.10: played the previous round against an opponent whose Pairing Score then was another. */
    private static boolean floatedPreviousRound(
            Tournament tournament, TournamentHistory history, ParticipantHistory player) {
        var played = tournament.rounds().size();
        if (played == 0) {
            return false;
        }
        var previous = RoundNumber.of(played);
        return player.recordOf(previous) instanceof RoundRecord.Game game
                && !pairingScoreBefore(tournament, history, game.opponent(), previous)
                        .equals(pairingScoreBefore(tournament, history, player.participant(), previous));
    }

    private static PairingScore pairingScoreBefore(
            Tournament tournament, TournamentHistory history, ParticipantId participant, RoundNumber round) {
        return PairingScore.of(
                history.of(participant).scoreBefore(round), tournament.virtualPointsOf(participant, round));
    }

    private static RoundPairing roundPairing(Tournament tournament, LimProcedure.Outcome outcome) {
        var trace = new ArrayList<TraceStep>();
        outcome.pairingAllocatedBye()
                .ifPresent(player -> trace.add(new TraceStep.ByeDecision(player.id(), "C.04.4.3 1.1")));
        var games = outcome.games().stream().sorted(BOARD_ORDER).toList();
        var boards = new ArrayList<PairedBoard>();
        for (var index = 0; index < games.size(); index++) {
            var game = games.get(index);
            var board = BoardNumber.of(index + 1);
            boards.add(new PairedBoard(board, game.white().id(), game.black().id()));
            trace.add(new TraceStep.ColourDecision(board, game.article()));
        }
        return new RoundPairing(
                tournament.nextRound(),
                boards,
                outcome.pairingAllocatedBye().map(Player::id),
                tournament.absencesInNextRound(),
                tournament.pairingNumbers(),
                new PairingTrace(trace));
    }

    private static Player higherRanked(Game game) {
        return RANKING.compare(game.white(), game.black()) <= 0 ? game.white() : game.black();
    }

    private static Player lowerRanked(Game game) {
        return higherRanked(game) == game.white() ? game.black() : game.white();
    }
}
