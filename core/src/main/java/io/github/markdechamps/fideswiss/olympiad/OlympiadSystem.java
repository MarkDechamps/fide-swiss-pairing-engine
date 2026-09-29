package io.github.markdechamps.fideswiss.olympiad;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.pairing.Violation;
import io.github.markdechamps.fideswiss.rules.BasicRules;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The Olympiad Pairing Rules (D.02, effective from 1 January 2022), the only edition: a team Swiss outside C.04. It
 * pairs the groups of equal matchpoints from the top down to the Median Group, from the bottom up to it, and the
 * Median Group last, by the order of 9.3, and gives only board 1 a colour.
 */
public final class OlympiadSystem implements PairingSystem {

    private final Matchings matchings;

    public OlympiadSystem() {
        this(Matchings.BLOSSOM);
    }

    OlympiadSystem(Matchings matchings) {
        this.matchings = matchings;
    }

    @Override
    public String name() {
        return "D.02 Olympiad Pairing Rules 2022";
    }

    @Override
    public CompetitionType competitionType() {
        return CompetitionType.TEAM;
    }

    /** Matchpoints are the score (3.2.1, 6.2); the Olympiad has no acceleration (C.04.7 is for C.04 systems). */
    @Override
    public List<Problem> problemsWith(TournamentSettings settings) {
        var problems = new ArrayList<Problem>();
        if (settings.scoring().matches().isEmpty() || settings.scoring().primaryScore() != PrimaryScore.MATCH_POINTS) {
            problems.add(Problem.citing("D.02 3.2.1", "The Olympiad Pairing Rules pair by matchpoints (TRF 362)"));
        }
        if (!(settings.acceleration() instanceof Acceleration.None)) {
            problems.add(Problem.citing(
                    "C.04.7", "Acceleration is defined for the C.04 systems, not the Olympiad Pairing Rules"));
        }
        return problems;
    }

    /** 4.3: 1 matchpoint (its 2 game points are a drawn match's on the Olympiad's four boards). */
    @Override
    public Points pairingAllocatedByeValue(ScoringScheme scoring) {
        return Points.of(1);
    }

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        try {
            var outcome = procedure(tournament).pair(OlympiadTeams.toBePaired(tournament));
            return roundPairing(tournament, outcome);
        } catch (UnpairableRound e) {
            throw new NoLegalPairingException(
                    List.of(Problem.citing(e.article(), e.getMessage())), new PairingTrace(List.of()));
        }
    }

    /**
     * The Basic Rules, and 7.3 on the colours given: board 1's colours break a limit that the other colours would
     * keep. A pair that breaks 7.3 with either colour is not reported, since 7.4 may have disregarded the limits.
     */
    @Override
    public List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        var violations = new ArrayList<>(BasicRules.violationsOf(tournament, proposed));
        var teams =
                OlympiadTeams.toBePaired(tournament).stream().collect(Collectors.toMap(Team::id, Function.identity()));
        for (var board : proposed.boards()) {
            var white = teams.get(board.white());
            var black = teams.get(board.black());
            if (white != null
                    && black != null
                    && !ColourLimits.fits(white, black, Colour.WHITE)
                    && ColourLimits.fits(white, black, Colour.BLACK)) {
                violations.add(Violation.of(
                        "D.02 7.3",
                        "board 1 of " + board.white() + " - " + board.black() + " breaks the colour limits",
                        board.white(),
                        board.black()));
            }
        }
        return violations;
    }

    OlympiadProcedure procedure(Tournament tournament) {
        return new OlympiadProcedure(
                tournament.settings().initialColour(), tournament.nextRound().value(), matchings);
    }

    private static RoundPairing roundPairing(Tournament tournament, OlympiadProcedure.Outcome outcome) {
        var trace = new ArrayList<TraceStep>();
        outcome.bye().ifPresent(team -> trace.add(new TraceStep.ByeDecision(team.id(), "D.02 4.1")));
        outcome.groups().forEach(group -> trace.add(traceOf(group)));
        var boards = new ArrayList<PairedBoard>();
        for (var index = 0; index < outcome.games().size(); index++) {
            var game = outcome.games().get(index);
            var board = BoardNumber.of(index + 1);
            boards.add(new PairedBoard(board, game.white().id(), game.black().id()));
            trace.add(new TraceStep.ColourDecision(board, game.article()));
        }
        return new RoundPairing(
                tournament.nextRound(),
                boards,
                outcome.bye().map(Team::id),
                tournament.absencesInNextRound(),
                tournament.pairingNumbers(),
                new PairingTrace(trace));
    }

    private static TraceStep traceOf(OlympiadProcedure.GroupStep group) {
        var result = group.result();
        return new TraceStep.BracketStep(
                group.matchPoints() + " MP (" + group.direction().name().toLowerCase() + ")",
                group.residents().stream().map(Team::id).toList(),
                group.incoming().stream().map(Team::id).toList(),
                result.pairs().stream()
                        .map(pair -> List.of(pair.first().id(), pair.second().id()))
                        .toList(),
                result.floaters().stream().map(Team::id).toList(),
                result.limits() == ColourLimits.DISREGARDED ? "D.02 7.3 (disregarded by 7.4)" : "");
    }
}
