package io.github.markdechamps.fideswiss.doubleswiss;

import io.github.markdechamps.fideswiss.pairing.Edition2026Only;
import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.rules.BoardOrder;
import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupProcedure;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupRound;
import io.github.markdechamps.fideswiss.tournament.BakuSecondaryScore;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.BracketSeating;
import io.github.markdechamps.fideswiss.tournament.EdebtBoardCount;
import io.github.markdechamps.fideswiss.tournament.FloatScore;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.LastRoundZeroCdTypeB;
import io.github.markdechamps.fideswiss.tournament.PabValue;
import io.github.markdechamps.fideswiss.tournament.PairingNumber;
import io.github.markdechamps.fideswiss.tournament.PairingScore;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Double-Swiss System (C.04.5, 2026): every pairing is a match of two games with alternating colours. The PAB
 * first, then the top-scoregroup with its upfloaters, bracket after bracket (the Top-Scoregroup Procedure it shares
 * word for word with the Swiss Team System), then the colours of every match (Article 4).
 *
 * @param upfloaterLookAhead the [C6] reading, as in the Swiss Team System (the same words)
 * @param floatScore the floater reading under acceleration (1.5)
 */
public record DoubleSwissSystem(UpfloaterLookAhead upfloaterLookAhead, FloatScore floatScore) implements PairingSystem {

    /** C.04.5 does not order the boards: GHR 3.6, the higher-ranked player (4.2) standing for the pair. */
    private static final Comparator<ContenderPair> BOARD_ORDER = BoardOrder.of(
            pair -> new PairingScore(higherRanked(pair).score()),
            pair -> new PairingScore(pair.other(higherRanked(pair)).score()),
            pair -> PairingNumber.of(higherRanked(pair).tpn()));

    /** Every Interpretation at its default: [C6] pass/fail and floats on the Pairing Score, as for Swiss Team. */
    public static DoubleSwissSystem withDefaults() {
        return new DoubleSwissSystem(UpfloaterLookAhead.parityMinimum(), FloatScore.pairing());
    }

    @Override
    public String name() {
        return "C.04.5 Double-Swiss System 2026";
    }

    private static DoubleSwissSystem noSuchReading(Interpretation reading) {
        throw new InvalidSettingsException(
                List.of(Problem.citing("C.04.5", "The Double-Swiss System has no Swiss Team reading " + reading)));
    }

    @Override
    public DoubleSwissSystem with(Interpretation interpretation) {
        return switch (interpretation) {
            case UpfloaterLookAhead reading -> new DoubleSwissSystem(reading, floatScore);
            case FloatScore reading -> new DoubleSwissSystem(upfloaterLookAhead, reading);
            case LastRoundZeroCdTypeB reading ->
                throw new InvalidSettingsException(List.of(Problem.citing(
                        "C.04.5", "The Double-Swiss System has no colour preferences, so no " + reading)));
            case BracketSeating reading -> noSuchReading(reading);
            case PabValue reading -> noSuchReading(reading);
            case BakuSecondaryScore reading -> noSuchReading(reading);
            case EdebtBoardCount reading -> noSuchReading(reading);
        };
    }

    /** Preface: a match of two games played in succession, with alternating colours. */
    @Override
    public int gamesInSuccession() {
        return 2;
    }

    @Override
    public List<Problem> problemsWith(TournamentSettings settings) {
        return Edition2026Only.problemsWith(this, settings);
    }

    /** 1.4: as many points as a match with one game won and the other drawn. */
    @Override
    public Points pairingAllocatedByeValue(ScoringScheme scoring) {
        return scoring.win().plus(scoring.draw());
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var settings = tournament.settings();
        var round = tournament.nextRound();
        var floatCriteriaLapse = settings.numberOfRounds().isLast(round);
        var numbers = tournament.pairingNumbers();
        var contenders = new PlayersToPair(tournament, floatScore).toBePaired(numbers);
        var criteria = DoubleSwissCriteria.of(floatCriteriaLapse);
        var trace = new ArrayList<TraceStep>();
        var pairing = TopScoregroupProcedure.pair(
                        new TopScoregroupRound(contenders, floatCriteriaLapse, upfloaterLookAhead, criteria))
                .orElseThrow(() -> noLegalPairing(trace));
        pairing.pairingAllocatedBye()
                .ifPresent(player -> trace.add(new TraceStep.ByeDecision(player.id(), "C.04.5 3.4")));
        pairing.brackets().forEach(bracket -> trace.add(bracket.traceStep(criteria)));
        var colours = new ColourAllocation(settings.initialColour());
        var boards = new ArrayList<PairedBoard>();
        var ordered = pairing.pairs().stream().sorted(BOARD_ORDER).toList();
        for (var index = 0; index < ordered.size(); index++) {
            var coloured = colours.allocate(ordered.get(index));
            var board = BoardNumber.of(index + 1);
            boards.add(new PairedBoard(
                    board, coloured.white().id(), coloured.black().id()));
            trace.add(new TraceStep.ColourDecision(board, coloured.article()));
        }
        return new RoundPairing(
                round,
                boards,
                pairing.pairingAllocatedBye().map(Contender::id),
                tournament.absencesInNextRound(),
                numbers,
                new PairingTrace(trace));
    }

    private static Contender higherRanked(ContenderPair pair) {
        return ColourAllocation.RANKING.compare(pair.top(), pair.bottom()) <= 0 ? pair.top() : pair.bottom();
    }

    private static NoLegalPairingException noLegalPairing(List<TraceStep> trace) {
        return new NoLegalPairingException(
                List.of(Problem.citing("C.04.5 3.3.3", "The round-pairing cannot be completed")),
                new PairingTrace(trace));
    }
}
