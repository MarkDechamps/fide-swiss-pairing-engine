package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.dutch.DutchSystem;
import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.pairing.Edition2026Only;
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
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Burstein System (C.04.4.2, 2026). Its seeding rounds are paired by the Dutch System 2026 (1.6.1); every later
 * round by the Opposition Evaluation Index (1.7–1.8) and the order of Article 4.
 */
public final class BursteinSystem implements PairingSystem {

    /** C.04.4.2 does not order the boards; they follow the Pairing Scores, then the higher-ranked TPN. */
    private static final Comparator<Game> BOARD_ORDER = BoardOrder.of(
            game -> higherScore(game),
            game -> lowerScore(game),
            game -> higherRanked(game).pairingNumber());

    private final PairingSystem seeding = DutchSystem.of(SwissRulesEdition.EDITION_2026);

    @Override
    public String name() {
        return "C.04.4.2 Burstein System 2026";
    }

    @Override
    public List<Problem> problemsWith(TournamentSettings settings) {
        return Edition2026Only.problemsWith(this, settings);
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var round = roundOf(tournament);
        if (round.isSeedingRound()) {
            return seeding.pairNextRound(tournament);
        }
        try {
            var outcome = new BursteinProcedure(round).pair(playersToPair(tournament));
            return roundPairing(tournament, outcome);
        } catch (NoRoundPairingException e) {
            throw new NoLegalPairingException(
                    List.of(Problem.citing(e.article(), e.getMessage())), new PairingTrace(List.of()));
        }
    }

    /** A seeding round by the Dutch System's rules; any later round by the Basic Rules and [C3]. */
    @Override
    public List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        var round = roundOf(tournament);
        if (round.isSeedingRound()) {
            return seeding.violationsOf(tournament, proposed);
        }
        var violations = new ArrayList<>(BasicRules.violationsOf(tournament, proposed));
        var players = playersToPair(tournament).stream().collect(Collectors.toMap(Player::id, Function.identity()));
        for (var board : proposed.boards()) {
            var white = players.get(board.white());
            var black = players.get(board.black());
            if (white != null && black != null && !white.hasMet(black) && !round.mayMeet(white, black)) {
                violations.add(Violation.of(
                        "C.04.4.2 [C3]",
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
        var evaluation = new OppositionEvaluation(
                history, tournament.nextRound(), tournament.settings().scoring().draw());
        return tournament.participantsToBePaired().stream()
                .map(Participant::id)
                .map(id -> {
                    var own = history.of(id);
                    var met = new HashSet<ParticipantId>();
                    own.records().stream()
                            .filter(RoundRecord.Game.class::isInstance)
                            .map(record -> ((RoundRecord.Game) record).opponent())
                            .forEach(met::add);
                    return new Player(
                            id,
                            numbers.numberOf(id),
                            PairingScore.of(
                                    own.scoreBefore(tournament.nextRound()),
                                    tournament.virtualPointsOf(id, tournament.nextRound())),
                            own.playedColours(),
                            met,
                            own.mayReceivePairingAllocatedBye(),
                            evaluation.indexOf(id));
                })
                .toList();
    }

    private static RoundPairing roundPairing(Tournament tournament, BursteinProcedure.Outcome outcome) {
        var trace = new ArrayList<TraceStep>();
        outcome.pairingAllocatedBye()
                .ifPresent(player -> trace.add(new TraceStep.ByeDecision(player.id(), "C.04.4.2 3.1")));
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

    private static TraceStep bracketStep(BursteinProcedure.BracketChoice bracket) {
        var incoming = ids(bracket.incoming());
        var label = bracket.residents().isEmpty()
                ? "incoming floaters"
                : bracket.residents().getFirst().pairingScore()
                        + (incoming.isEmpty() ? "" : " + incoming floaters " + incoming);
        return new TraceStep.BracketStep(
                label,
                ids(bracket.residents()),
                incoming,
                bracket.pairs().stream()
                        .map(pair -> List.of(pair.first().id(), pair.second().id()))
                        .toList(),
                ids(bracket.outgoing()),
                "");
    }

    private static List<ParticipantId> ids(List<Player> players) {
        return players.stream().map(Player::id).toList();
    }

    private static Player higherRanked(Game game) {
        return game.white().ranksAbove(game.black()) ? game.white() : game.black();
    }

    private static PairingScore higherScore(Game game) {
        var white = game.white().pairingScore();
        var black = game.black().pairingScore();
        return white.compareTo(black) >= 0 ? white : black;
    }

    private static PairingScore lowerScore(Game game) {
        var white = game.white().pairingScore();
        var black = game.black().pairingScore();
        return white.compareTo(black) >= 0 ? black : white;
    }
}
