package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.PairingNumbers;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Score;
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
    public String name() {
        return "C.04.3 Dutch System 2026";
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var numbers = tournament.pairingNumbers();
        var round = new RoundToPair(
                tournament.nextRound(),
                tournament.settings().numberOfRounds(),
                tournament.settings().initialColour(),
                tournament.settings().scoring().win());
        var players = playersToPair(tournament, numbers);
        var pairer = new BracketPairer(new PlayerSet(players), round, edition);
        var trace = new ArrayList<TraceStep>();
        if (!pairer.isRoundCompletable()) {
            throw noLegalPairing("No pairing complies with [C1]-[C3] for every participant", trace);
        }
        var pairs = new ArrayList<Pair>();
        var scoregroups = List.copyOf(scoregroupsFromTheTop(players).values());
        List<Player> movedDown = List.of();
        for (var index = 0; index < scoregroups.size(); index++) {
            var bracket = new Bracket(movedDown, scoregroups.get(index));
            var outcome = pairer.pair(bracket, playersBelow(scoregroups, index), residentsOfNext(scoregroups, index))
                    .orElseThrow(() -> noLegalPairing("No candidate keeps the round completable", trace));
            trace.add(bracketStep(bracket, outcome));
            pairs.addAll(outcome.candidate().pairs());
            movedDown = outcome.candidate().downfloaters();
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
                        history.of(id).score(),
                        history.of(id),
                        floatsOf(history.of(id), history, lossValue)))
                .sorted(PairingOrder.RANKING)
                .toList();
    }

    private List<FloatDirection> floatsOf(ParticipantHistory player, TournamentHistory history, Points lossValue) {
        var floats = new ArrayList<FloatDirection>();
        for (var index = 0; index < player.records().size(); index++) {
            var round = RoundNumber.of(index + 1);
            var record = player.recordOf(round);
            var opponentScore = record.pairedOpponent()
                    .map(opponent -> history.of(opponent).scoreBefore(round))
                    .orElse(Score.ZERO);
            floats.add(edition.floatRule().floatOf(record, player.scoreBefore(round), opponentScore, lossValue));
        }
        return floats;
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
    private static final Comparator<Pair> BOARD_ORDER = Comparator.<Pair, Score>comparing(
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

    private static Map<Score, List<Player>> scoregroupsFromTheTop(List<Player> ordered) {
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
