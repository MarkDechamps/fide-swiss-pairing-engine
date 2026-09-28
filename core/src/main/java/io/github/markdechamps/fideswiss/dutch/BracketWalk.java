package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairingProgress;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.Progress;
import io.github.markdechamps.fideswiss.pairing.ProgressStep;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.search.SearchHeartbeat;
import io.github.markdechamps.fideswiss.tournament.Problem;
import java.util.ArrayList;
import java.util.List;

/** A procedure's walk through a round: the pairs fixed so far and the trace, bracket by bracket. */
final class BracketWalk {

    private final BracketPairer pairer;
    private final PairingProgress progress;
    private final int toPair;
    private final List<Pair> pairs = new ArrayList<>();
    private final List<TraceStep> trace = new ArrayList<>();

    BracketWalk(BracketPairer pairer, PairingProgress progress, int toPair) {
        this.pairer = pairer;
        this.progress = progress;
        this.toPair = toPair;
    }

    /** Pairs the bracket, records its pairs and trace, and returns its downfloaters. */
    List<Player> pair(
            String kind, Bracket bracket, List<Player> below, List<Player> nextResidents, CompletionScope scope) {
        return record(kind, bracket, tryPair(bracket, below, nextResidents, scope));
    }

    /** The bracket's chosen candidate, not yet recorded; the walk fails when no candidate exists. */
    BracketOutcome tryPair(Bracket bracket, List<Player> below, List<Player> nextResidents, CompletionScope scope) {
        SearchHeartbeat.checkInterrupted();
        var step = new ProgressStep("bracket " + bracket.residents().getFirst().score());
        progress.stepStarted(step);
        return pairer.pair(bracket, below, nextResidents, scope, new SearchHeartbeat(progress, step))
                .orElseThrow(() -> noLegalPairing("No candidate keeps the round completable"));
    }

    /** Records a final bracket: its pairs settle their participants for good. */
    List<Player> record(String kind, Bracket bracket, BracketOutcome outcome) {
        pairs.addAll(outcome.candidate().pairs());
        trace.add(stepOf(kind, bracket, outcome));
        progress.advanced(new Progress(2 * pairs.size(), toPair));
        return outcome.candidate().downfloaters();
    }

    /** Everyone is settled once the PAB, if any, is given. */
    void finish() {
        progress.advanced(new Progress(toPair, toPair));
    }

    boolean allowsCompletion(List<Player> notYetPaired) {
        return pairer.allowsCompletion(notYetPaired);
    }

    List<Pair> pairs() {
        return List.copyOf(pairs);
    }

    List<TraceStep> trace() {
        return trace;
    }

    NoLegalPairingException noLegalPairing(String message) {
        return new NoLegalPairingException(List.of(Problem.citing("C.04.3 1.9.3", message)), new PairingTrace(trace));
    }

    private static TraceStep stepOf(String kind, Bracket bracket, BracketOutcome outcome) {
        var candidate = outcome.candidate();
        var score = bracket.residents().getFirst().score().toString();
        return new TraceStep.BracketStep(
                kind.isEmpty() ? score : kind + " " + score,
                bracket.residents().stream().map(Player::id).toList(),
                bracket.movedDown().stream().map(Player::id).toList(),
                candidate.pairs().stream()
                        .map(pair ->
                                List.of(pair.s1Player().id(), pair.s2Player().id()))
                        .toList(),
                candidate.downfloaters().stream().map(Player::id).toList(),
                outcome.failedCriteria());
    }
}
