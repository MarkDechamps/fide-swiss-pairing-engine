package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingCancelledException;
import io.github.markdechamps.fideswiss.pairing.PairingProgress;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.Progress;
import io.github.markdechamps.fideswiss.pairing.ProgressStep;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.search.SearchHeartbeat;
import io.github.markdechamps.fideswiss.search.SearchInterrupted;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.PairingNumbers;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** The Dutch System (C.04.3): pair bracket by bracket from the top scoregroup down (1.9), then allocate colours. */
public final class DutchSystem implements PairingSystem {

    private final DutchEdition edition = DutchEdition.edition2026();

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        return pairNextRound(tournament, PairingProgress.NONE);
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament, PairingProgress progress) {
        var trace = new ArrayList<TraceStep>();
        try {
            return pair(tournament, progress, trace);
        } catch (SearchInterrupted interrupted) {
            throw new PairingCancelledException(new PairingTrace(trace));
        }
    }

    private RoundPairing pair(Tournament tournament, PairingProgress progress, List<TraceStep> trace) {
        SearchHeartbeat.checkInterrupted();
        var numbers = tournament.pairingNumbers();
        var round = new RoundToPair(
                tournament.nextRound(),
                tournament.settings().numberOfRounds(),
                tournament.settings().initialColour(),
                tournament.settings().scoring().win());
        var players = playersToPair(tournament, numbers);
        var pairer = new BracketPairer(new PlayerSet(players), round, edition);
        if (!pairer.isRoundCompletable()) {
            throw noLegalPairing("No pairing complies with [C1]-[C3] for every participant", trace);
        }
        var pairs = new ArrayList<Pair>();
        var scoregroups = List.copyOf(scoregroupsFromTheTop(players).values());
        List<Player> movedDown = List.of();
        var settled = 0;
        for (var index = 0; index < scoregroups.size(); index++) {
            var bracket = new Bracket(movedDown, scoregroups.get(index));
            var step =
                    new ProgressStep("bracket " + bracket.residents().getFirst().score());
            progress.stepStarted(step);
            var heartbeat = new SearchHeartbeat(progress, step);
            var outcome = pairer.pair(
                            bracket, playersBelow(scoregroups, index), residentsOfNext(scoregroups, index), heartbeat)
                    .orElseThrow(() -> noLegalPairing("No candidate keeps the round completable", trace));
            trace.add(bracketStep(bracket, outcome));
            pairs.addAll(outcome.candidate().pairs());
            movedDown = outcome.candidate().downfloaters();
            settled += 2 * outcome.candidate().pairs().size();
            progress.advanced(
                    new Progress(settled + (index == scoregroups.size() - 1 ? movedDown.size() : 0), players.size()));
            SearchHeartbeat.checkInterrupted();
        }
        var pairingAllocatedBye = movedDown.stream().findFirst();
        pairingAllocatedBye.ifPresent(
                player -> trace.add(new TraceStep.ByeDecision(player.id(), "C.04.3 1.9.1, [C5]")));
        return roundPairing(tournament, numbers, pairer.colours(), pairs, pairingAllocatedBye, trace);
    }

    private List<Player> playersToPair(Tournament tournament, PairingNumbers numbers) {
        var history = TournamentHistory.of(tournament);
        var lossValue = tournament.settings().scoring().loss();
        return tournament.participantsToBePaired().stream()
                .map(Participant::id)
                .map(id -> new Player(
                        id,
                        numbers.numberOf(id),
                        pairingScoreBefore(tournament, history, id, tournament.nextRound()),
                        history.of(id),
                        floatsOf(tournament, history.of(id), history, lossValue)))
                .sorted(PairingOrder.RANKING)
                .toList();
    }

    /** Floats are judged on the Pairing Scores of the round in which the game was paired (C.04.7, fixed reading). */
    private List<FloatDirection> floatsOf(
            Tournament tournament, ParticipantHistory player, TournamentHistory history, Points lossValue) {
        var floats = new ArrayList<FloatDirection>();
        for (var index = 0; index < player.records().size(); index++) {
            var round = RoundNumber.of(index + 1);
            var record = player.recordOf(round);
            var opponentScore = record.pairedOpponent()
                    .map(opponent -> pairingScoreBefore(tournament, history, opponent, round))
                    .orElse(new PairingScore(Points.ZERO));
            var ownScore = pairingScoreBefore(tournament, history, player.participant(), round);
            floats.add(edition.floatRule().floatOf(record, ownScore, opponentScore, lossValue));
        }
        return floats;
    }

    private static PairingScore pairingScoreBefore(
            Tournament tournament, TournamentHistory history, ParticipantId participant, RoundNumber round) {
        return PairingScore.of(
                history.of(participant).scoreBefore(round), tournament.virtualPointsOf(participant, round));
    }

    private static RoundPairing roundPairing(
            Tournament tournament,
            PairingNumbers numbers,
            ColourAllocation colours,
            List<Pair> pairs,
            Optional<Player> pairingAllocatedBye,
            List<TraceStep> trace) {
        var allocated =
                pairs.stream().sorted(BOARD_ORDER).map(colours::allocate).toList();
        var boards = new ArrayList<PairedBoard>();
        for (var index = 0; index < allocated.size(); index++) {
            var pair = allocated.get(index);
            var board = BoardNumber.of(index + 1);
            boards.add(new PairedBoard(board, pair.white().id(), pair.black().id()));
            trace.add(new TraceStep.ColourDecision(board, pair.article()));
        }
        return new RoundPairing(
                tournament.nextRound(),
                boards,
                pairingAllocatedBye.map(Player::id),
                tournament.absencesInNextRound(),
                numbers,
                new PairingTrace(trace));
    }

    /**
     * GHR 3.6: the higher score of the pair's higher-ranked player first, then the higher sum of scores, then the
     * smaller Pairing Number of the higher-ranked player.
     */
    private static final Comparator<Pair> BOARD_ORDER = Comparator.<Pair, PairingScore>comparing(
                    pair -> pair.higherRanked().score())
            .reversed()
            .thenComparing(Comparator.<Pair, BigDecimal>comparing(pair -> pair.s1Player()
                            .score()
                            .points()
                            .toBigDecimal()
                            .add(pair.s2Player().score().points().toBigDecimal()))
                    .reversed())
            .thenComparing(pair -> pair.higherRanked().pairingNumber());

    private static TraceStep bracketStep(Bracket bracket, BracketOutcome outcome) {
        var candidate = outcome.candidate();
        return new TraceStep.BracketStep(
                bracket.residents().getFirst().score().toString(),
                bracket.residents().stream().map(Player::id).toList(),
                bracket.movedDown().stream().map(Player::id).toList(),
                candidate.pairs().stream()
                        .map(pair ->
                                List.of(pair.s1Player().id(), pair.s2Player().id()))
                        .toList(),
                candidate.downfloaters().stream().map(Player::id).toList(),
                outcome.failedCriteria());
    }

    private static NoLegalPairingException noLegalPairing(String message, List<TraceStep> trace) {
        return new NoLegalPairingException(List.of(Problem.citing("C.04.3 1.9.3", message)), new PairingTrace(trace));
    }

    private static Map<PairingScore, List<Player>> scoregroupsFromTheTop(List<Player> ordered) {
        return ordered.stream()
                .collect(Collectors.groupingBy(
                        Player::score, () -> new TreeMap<>(Comparator.reverseOrder()), Collectors.toList()));
    }

    private static List<Player> playersBelow(List<List<Player>> scoregroups, int index) {
        return scoregroups.subList(index + 1, scoregroups.size()).stream()
                .flatMap(List::stream)
                .toList();
    }

    private static List<Player> residentsOfNext(List<List<Player>> scoregroups, int index) {
        return index + 1 < scoregroups.size() ? scoregroups.get(index + 1) : List.of();
    }
}
