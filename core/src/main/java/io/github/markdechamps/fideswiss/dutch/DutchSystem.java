package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.history.ParticipantHistory;
import io.github.markdechamps.fideswiss.history.TournamentHistory;
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
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    @Override
    public RoundPairing pairNextRound(Tournament tournament) {
        var rules = DutchEdition.of(edition.orElse(tournament.settings().swissRulesEdition()));
        var numbers = tournament.pairingNumbers();
        var round = new RoundToPair(
                tournament.nextRound(),
                tournament.settings().numberOfRounds(),
                tournament.settings().initialColour(),
                tournament.settings().scoring().win());
        var players = playersToPair(tournament, numbers, rules);
        var pairer = new BracketPairer(new PlayerSet(players), round, rules);
        var walk = new BracketWalk(pairer);
        if (!pairer.isRoundCompletable()) {
            throw walk.noLegalPairing("No pairing complies with [C1]-[C3] for every participant");
        }
        var leftOver = rules.procedure().pairBrackets(players, walk);
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
                        history.of(id).score(),
                        history.of(id),
                        floatsOf(history.of(id), history, lossValue, rules.floatRule())))
                .sorted(PairingOrder.RANKING)
                .toList();
    }

    private static List<FloatDirection> floatsOf(
            ParticipantHistory player, TournamentHistory history, Points lossValue, FloatRule floatRule) {
        var floats = new ArrayList<FloatDirection>();
        for (var index = 0; index < player.records().size(); index++) {
            var round = RoundNumber.of(index + 1);
            var record = player.recordOf(round);
            var opponentScore = record.pairedOpponent()
                    .map(opponent -> history.of(opponent).scoreBefore(round))
                    .orElse(Score.ZERO);
            floats.add(floatRule.floatOf(record, player.scoreBefore(round), opponentScore, lossValue));
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
}
