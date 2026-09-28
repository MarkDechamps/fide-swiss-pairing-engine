package io.github.markdechamps.fideswiss.swissteam;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.topscoregroup.PairCriterion;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupProcedure;
import io.github.markdechamps.fideswiss.topscoregroup.TopScoregroupRound;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.FloatScore;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.LastRoundZeroCdTypeB;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SecondaryScore;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Swiss Team Pairing System (C.04.6, 2026): the PAB first, then the top-scoregroup with its upfloaters,
 * bracket after bracket (the Top-Scoregroup Procedure), then the board-1 colours of every match (Article 4).
 *
 * @param colourPreferences Type A (the default), Type B or none (1.7)
 * @param upfloaterLookAhead the [C6] reading (ruling A6)
 * @param lastRoundZeroCd the Type B last-round reading (ruling A1)
 * @param floatScore the floater reading under acceleration (ruling A8)
 */
public record SwissTeamSystem(
        ColourPreferenceType colourPreferences,
        UpfloaterLookAhead upfloaterLookAhead,
        LastRoundZeroCdTypeB lastRoundZeroCd,
        FloatScore floatScore)
        implements PairingSystem {

    /** Type A colour preferences and every Interpretation at its default (ADR 0003). */
    public static SwissTeamSystem of(ColourPreferenceType colourPreferences) {
        return new SwissTeamSystem(
                colourPreferences,
                UpfloaterLookAhead.parityMinimum(),
                LastRoundZeroCdTypeB.strong(),
                FloatScore.pairing());
    }

    @Override
    public SwissTeamSystem with(Interpretation interpretation) {
        return switch (interpretation) {
            case UpfloaterLookAhead reading ->
                new SwissTeamSystem(colourPreferences, reading, lastRoundZeroCd, floatScore);
            case LastRoundZeroCdTypeB reading ->
                new SwissTeamSystem(colourPreferences, upfloaterLookAhead, reading, floatScore);
            case FloatScore reading ->
                new SwissTeamSystem(colourPreferences, upfloaterLookAhead, lastRoundZeroCd, reading);
        };
    }

    @Override
    public CompetitionType competitionType() {
        return CompetitionType.TEAM;
    }

    /** The 2026 text only, a scheme with match scoring, and no Baku over game points (C.04.7 1.4.4). */
    @Override
    public List<Problem> problemsWith(TournamentSettings settings) {
        var problems = new ArrayList<Problem>();
        if (settings.swissRulesEdition() != SwissRulesEdition.EDITION_2026) {
            problems.add(Problem.citing("C.04.6", "The Swiss Team System is implemented in its 2026 edition only"));
        }
        if (settings.scoring().matches().isEmpty()) {
            problems.add(Problem.citing("C.04.6 1.2", "The Swiss Team System needs match scoring (TRF 362)"));
        }
        if (settings.acceleration() instanceof Acceleration.Baku
                && settings.scoring().primaryScore() == PrimaryScore.GAME_POINTS) {
            problems.add(Problem.citing(
                    "C.04.7 1.4.4", "Baku acceleration cannot be used when game points are the primary score"));
        }
        return problems;
    }

    /** 1.4: as many points as a drawn match, in the primary score. */
    @Override
    public Points pairingAllocatedByeValue(ScoringScheme scoring) {
        return drawnMatch(scoring, scoring.primaryScore());
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var settings = tournament.settings();
        var round = tournament.nextRound();
        var floatCriteriaLapse = round.value() > settings.numberOfRounds().value() - 2;
        var preferences = new TeamColourPreferences(
                colourPreferences, lastRoundZeroCd, settings.numberOfRounds().isLast(round));
        var numbers = tournament.pairingNumbers();
        var secondaryByeValue =
                drawnMatch(settings.scoring(), settings.scoring().primaryScore().other());
        var contenders = new TeamContenders(tournament, floatScore, secondaryByeValue).toBePaired(numbers);
        var criteria = SwissTeamCriteria.of(preferences, floatCriteriaLapse);
        var trace = new ArrayList<TraceStep>();
        var pairing = TopScoregroupProcedure.pair(
                        new TopScoregroupRound(contenders, floatCriteriaLapse, upfloaterLookAhead, criteria))
                .orElseThrow(() -> noLegalPairing(trace));
        pairing.pairingAllocatedBye().ifPresent(team -> trace.add(new TraceStep.ByeDecision(team.id(), "C.04.6 3.4")));
        pairing.brackets().forEach(bracket -> trace.add(bracketStep(bracket, criteria)));
        var colours = new TeamColourAllocation(preferences, settings.initialColour(), secondaryScore(settings));
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

    /**
     * GHR 3.6 on the pair's scores (TRF CLI surface, Gacrux's order): the higher score first, then the higher sum
     * of scores, then the smaller TPN.
     */
    private static final Comparator<ContenderPair> BOARD_ORDER = Comparator.<ContenderPair, Points>comparing(
                    pair -> higherScore(pair))
            .reversed()
            .thenComparing(Comparator.<ContenderPair, BigDecimal>comparing(pair ->
                            pair.top().score().plus(pair.bottom().score()).toBigDecimal())
                    .reversed())
            .thenComparingInt(pair -> pair.top().tpn());

    private static Points higherScore(ContenderPair pair) {
        return pair.top().score().isLessThan(pair.bottom().score())
                ? pair.bottom().score()
                : pair.top().score();
    }

    private static Points drawnMatch(ScoringScheme scoring, PrimaryScore score) {
        return score == PrimaryScore.MATCH_POINTS
                ? scoring.matches().map(MatchScoring::draw).orElse(scoring.draw())
                : scoring.drawnMatchGamePoints();
    }

    private static SecondaryScore secondaryScore(TournamentSettings settings) {
        return settings.scoring().matches().map(MatchScoring::secondary).orElse(SecondaryScore.USED_FOR_COLOUR);
    }

    private static TraceStep bracketStep(TopScoregroupProcedure.Bracket bracket, List<PairCriterion> criteria) {
        var upfloaters = Set.copyOf(bracket.upfloaters());
        var failed = criteria.stream()
                .map(criterion -> failures(criterion, bracket, upfloaters))
                .filter(failure -> !failure.isEmpty())
                .collect(Collectors.joining(", "));
        return new TraceStep.TopScoregroupBracket(
                bracket.residents().getFirst().score().toString(),
                bracket.residents().stream().map(Contender::id).toList(),
                bracket.upfloaters().stream().map(Contender::id).toList(),
                bracket.pairs().stream()
                        .map(pair -> List.of(pair.top().id(), pair.bottom().id()))
                        .toList(),
                failed);
    }

    private static String failures(
            PairCriterion criterion, TopScoregroupProcedure.Bracket bracket, Set<Contender> upfloaters) {
        var count = bracket.pairs().stream()
                .mapToLong(pair -> criterion.failureOf(pair, upfloaters))
                .sum();
        return count == 0 ? "" : "[" + criterion.article() + "] " + count;
    }

    private static NoLegalPairingException noLegalPairing(List<TraceStep> trace) {
        return new NoLegalPairingException(
                List.of(Problem.citing("C.04.6 3.3.3", "The round-pairing cannot be completed")),
                new PairingTrace(trace));
    }
}
