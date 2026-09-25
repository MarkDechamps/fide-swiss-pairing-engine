package io.github.markdechamps.fideswiss.dubov;

import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
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
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** The Dubov System (C.04.4.1, 2026). */
public final class DubovSystem implements PairingSystem {

    private static final Comparator<Game> BOARD_ORDER = BoardOrder.of(
            game -> higherRanked(game).score(),
            game -> lowerRanked(game).score(),
            game -> higherRanked(game).pairingNumber());

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var players = playersToPair(tournament);
        try {
            var outcome = new DubovProcedure(roundOf(tournament)).pair(players);
            return roundPairing(tournament, outcome);
        } catch (NoRoundPairingException e) {
            throw new NoLegalPairingException(
                    List.of(Problem.citing(e.article(), e.getMessage())), new PairingTrace(List.of()));
        }
    }

    /** The Basic Rules, and [C3]: no two players with the same absolute colour preference meet. */
    @Override
    public List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        var violations = new ArrayList<>(BasicRules.violationsOf(tournament, proposed));
        var players = playersToPair(tournament).stream().collect(Collectors.toMap(Player::id, Function.identity()));
        for (var board : proposed.boards()) {
            var white = players.get(board.white());
            var black = players.get(board.black());
            if (white != null
                    && black != null
                    && !white.hasMet(black)
                    && !roundOf(tournament).mayMeet(white, black)) {
                violations.add(Violation.of(
                        "C.04.4.1 [C3]",
                        board.white() + " and " + board.black() + " have the same absolute colour preference",
                        board.white(),
                        board.black()));
            }
        }
        return violations;
    }

    static RoundToPair roundOf(Tournament tournament) {
        return new RoundToPair(
                tournament.nextRound(),
                tournament.settings().numberOfRounds(),
                tournament.settings().initialColour());
    }

    static List<Player> playersToPair(Tournament tournament) {
        var numbers = tournament.pairingNumbers();
        var history = TournamentHistory.of(tournament);
        return tournament.participantsToBePaired().stream()
                .map(Participant::id)
                .map(id -> playerOf(tournament, history, id, numbers.numberOf(id)))
                .toList();
    }

    private static Player playerOf(
            Tournament tournament, TournamentHistory history, ParticipantId id, PairingNumber number) {
        var own = history.of(id);
        var met = new HashSet<ParticipantId>();
        var opponentRatings = new ArrayList<Integer>();
        for (var record : own.records()) {
            if (record instanceof RoundRecord.Game game) {
                met.add(game.opponent());
                opponentRatings.add(
                        tournament.participant(game.opponent()).rating().valueOrZero());
            }
        }
        var upfloated = upfloatedRounds(own, history);
        return new Player(
                id,
                number,
                tournament.participant(id).rating().valueOrZero(),
                own.score(),
                own.playedColours(),
                met,
                opponentRatings,
                own.mayReceivePairingAllocatedBye(),
                (int) upfloated.stream().filter(Boolean::booleanValue).count(),
                !upfloated.isEmpty() && upfloated.getLast());
    }

    /**
     * 1.8, read as: paired with an opponent who had a higher score when the round was paired, whether or not the
     * game was then played. A bye is never an upfloat.
     */
    private static List<Boolean> upfloatedRounds(ParticipantHistory own, TournamentHistory history) {
        var rounds = new ArrayList<Boolean>();
        for (var index = 0; index < own.records().size(); index++) {
            var round = RoundNumber.of(index + 1);
            rounds.add(own.recordOf(round)
                    .pairedOpponent()
                    .filter(opponent -> history.of(opponent).scoreBefore(round).isHigherThan(own.scoreBefore(round)))
                    .isPresent());
        }
        return rounds;
    }

    private static RoundPairing roundPairing(Tournament tournament, DubovProcedure.Outcome outcome) {
        var trace = new ArrayList<TraceStep>();
        outcome.pairingAllocatedBye()
                .ifPresent(player -> trace.add(new TraceStep.ByeDecision(player.id(), "C.04.4.1 3.1")));
        outcome.brackets().forEach(bracket -> trace.add(bracketStep(bracket)));
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
                Map.copyOf(tournament.absencesInNextRound()),
                tournament.pairingNumbers(),
                new PairingTrace(trace));
    }

    private static TraceStep bracketStep(DubovProcedure.BracketChoice bracket) {
        var upfloaters = ids(bracket.upfloaters());
        var shifted = ids(bracket.shifted());
        var label = bracket.residents().getFirst().score()
                + (upfloaters.isEmpty() ? "" : " + upfloaters " + upfloaters)
                + (shifted.isEmpty() ? "" : ", shifted " + shifted);
        return new TraceStep.BracketStep(
                label,
                ids(bracket.residents()),
                List.of(),
                bracket.pairs().stream()
                        .map(pair -> List.of(pair.first().id(), pair.second().id()))
                        .toList(),
                List.of(),
                "");
    }

    private static List<ParticipantId> ids(List<Player> players) {
        return players.stream().map(Player::id).toList();
    }

    private static Player higherRanked(Game game) {
        return ColourAllocation.RANKING.compare(game.white(), game.black()) <= 0 ? game.white() : game.black();
    }

    private static Player lowerRanked(Game game) {
        return higherRanked(game) == game.white() ? game.black() : game.white();
    }
}
