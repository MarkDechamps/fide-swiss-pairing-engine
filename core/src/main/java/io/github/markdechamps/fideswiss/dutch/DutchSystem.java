package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.pairing.PairingCancelledException;
import io.github.markdechamps.fideswiss.pairing.PairingProgress;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.pairing.Violation;
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
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Dutch System (C.04.3): the edition's own procedure pairs the brackets (2026 1.9, 2017 A.9), then colours are
 * allocated. The edition is the tournament's Swiss Rules Edition, unless the system was made for one edition.
 */
public final class DutchSystem implements PairingSystem {

    private final Optional<SwissRulesEdition> edition;

    private DutchSystem(Optional<SwissRulesEdition> edition) {
        this.edition = edition;
    }

    /** The Dutch System of whichever Swiss Rules Edition the tournament declares. */
    public static DutchSystem ofTheTournamentsEdition() {
        return new DutchSystem(Optional.empty());
    }

    public static DutchSystem of(SwissRulesEdition edition) {
        return new DutchSystem(Optional.of(edition));
    }

    @Override
    public List<Problem> problemsWith(TournamentSettings settings) {
        return edition.filter(pinned -> pinned != settings.swissRulesEdition())
                .map(pinned -> List.of(Problem.citing(
                        "GHR 1.3",
                        "The Dutch System of edition " + pinned + " cannot pair a tournament of edition "
                                + settings.swissRulesEdition())))
                .orElse(List.of());
    }

    /** The Basic Rules, and [C3]: two non-topscorers with the same absolute colour preference shall not meet. */
    @Override
    public List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        var violations = new ArrayList<>(PairingSystem.super.violationsOf(tournament, proposed));
        var edition = this.edition.orElse(tournament.settings().swissRulesEdition());
        var rules = DutchEdition.of(edition);
        var players = playersToPair(tournament, tournament.pairingNumbers(), rules).stream()
                .collect(Collectors.toMap(Player::id, player -> player));
        var absolute = new AbsoluteCriteria(roundToPair(tournament), rules.pairingAllocatedByeBar());
        var article = edition == SwissRulesEdition.PRE_2026 ? "C.04.3 (2017) C.3" : "C.04.3 [C3]";
        for (var board : proposed.boards()) {
            var white = Optional.ofNullable(players.get(board.white()));
            var black = Optional.ofNullable(players.get(board.black()));
            if (white.isPresent()
                    && black.isPresent()
                    && absolute.areNonTopscorersWithSameAbsolutePreference(white.get(), black.get())) {
                violations.add(sameAbsolutePreference(article, white.get(), black.get()));
            }
        }
        return violations;
    }

    private static Violation sameAbsolutePreference(String article, Player white, Player black) {
        var colour = white.colourPreference().colour().orElseThrow().name().toLowerCase();
        return Violation.of(
                article,
                white.id() + " and " + black.id() + " both have an absolute preference for " + colour,
                white.id(),
                black.id());
    }

    private static RoundToPair roundToPair(Tournament tournament) {
        return new RoundToPair(
                tournament.nextRound(),
                tournament.settings().numberOfRounds(),
                tournament.settings().initialColour(),
                tournament.settings().scoring().win());
    }

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

    private RoundPairing pair(Tournament tournament, PairingProgress progress, List<TraceStep> cancelledTrace) {
        SearchHeartbeat.checkInterrupted();
        var rules = DutchEdition.of(edition.orElse(tournament.settings().swissRulesEdition()));
        var numbers = tournament.pairingNumbers();
        var round = roundToPair(tournament);
        var players = playersToPair(tournament, numbers, rules);
        var pairer = new BracketPairer(new PlayerSet(players), round, rules);
        var walk = new BracketWalk(pairer, progress, players.size());
        if (!pairer.isRoundCompletable()) {
            throw walk.noLegalPairing("No pairing complies with [C1]-[C3] for every participant");
        }
        List<Player> leftOver;
        try {
            leftOver = rules.procedure().pairBrackets(players, walk);
        } catch (SearchInterrupted interrupted) {
            cancelledTrace.addAll(walk.trace());
            throw interrupted;
        }
        walk.finish();
        var trace = new ArrayList<>(walk.trace());
        var pairingAllocatedBye = leftOver.stream().findFirst();
        pairingAllocatedBye.ifPresent(player -> trace.add(new TraceStep.ByeDecision(player.id(), rules.name())));
        return roundPairing(tournament, numbers, pairer.colours(), walk.pairs(), pairingAllocatedBye, trace);
    }

    private static List<Player> playersToPair(Tournament tournament, PairingNumbers numbers, DutchEdition rules) {
        var history = TournamentHistory.of(tournament);
        var lossValue = tournament.settings().scoring().loss();
        return tournament.participantsToBePaired().stream()
                .map(Participant::id)
                .map(id -> new Player(
                        id,
                        numbers.numberOf(id),
                        pairingScoreBefore(tournament, history, id, tournament.nextRound()),
                        history.of(id),
                        floatsOf(tournament, history.of(id), history, lossValue, rules.floatRule())))
                .sorted(PairingOrder.RANKING)
                .toList();
    }

    /** Floats are judged on the Pairing Scores of the round in which the game was paired (C.04.7, fixed reading). */
    private static List<FloatDirection> floatsOf(
            Tournament tournament,
            ParticipantHistory player,
            TournamentHistory history,
            Points lossValue,
            FloatRule floatRule) {
        var floats = new ArrayList<FloatDirection>();
        for (var index = 0; index < player.records().size(); index++) {
            var round = RoundNumber.of(index + 1);
            var record = player.recordOf(round);
            var opponentScore = record.pairedOpponent()
                    .map(opponent -> pairingScoreBefore(tournament, history, opponent, round))
                    .orElse(new PairingScore(Points.ZERO));
            var ownScore = pairingScoreBefore(tournament, history, player.participant(), round);
            floats.add(floatRule.floatOf(record, ownScore, opponentScore, lossValue));
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
}
