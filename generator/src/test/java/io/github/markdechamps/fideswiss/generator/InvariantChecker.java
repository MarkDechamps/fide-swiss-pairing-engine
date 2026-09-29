package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Our own invariant checker (Verification strategy): on any generated tournament it asserts the absolute criteria
 * every edition shares. No rematch (C.04.1 Art. 2, GHR 3.5), at most one PAB per round and none for a participant
 * who already had one or a win's points without playing (Art. 3–4; before 2026 a requested full-point bye does not
 * bar it, Dutch 2017 reading R9), everyone in every round either on a board or
 * given a reason, nobody on a board after withdrawing (GHR 3.2), and the colour limits of Art. 6–7 in every round
 * but the last, where Dutch topscorers may break them; a Double-Swiss match gives both colours, one per game, and
 * C.04.5 has no colour criterion, and neither has C.04.6: a team's colour follows its pairing (the Olympiad's limits of
 * D.02 7.3–7.4 yield to keeping the pairings, so they are not absolute). A match is a meeting unless a side forfeited both games (C.04.5 Preface). The
 * Standings after the last round rank everyone.
 */
final class InvariantChecker {

    private InvariantChecker() {}

    static List<String> violationsOf(Tournament tournament) {
        var violations = new ArrayList<String>();
        var met = new HashSet<Set<ParticipantId>>();
        var barredFromPab = new HashSet<ParticipantId>();
        var withdrawn = new HashSet<ParticipantId>();
        var colours = new HashMap<ParticipantId, List<Colour>>();
        var lastRound = tournament.settings().numberOfRounds().value();
        var edition = tournament.settings().swissRulesEdition();
        for (var round : tournament.rounds()) {
            var number = round.number().value();
            everyoneAccountedFor(tournament, round, violations);
            nobodyPairedAfterWithdrawing(round, withdrawn, violations);
            noRematches(round, met, violations);
            pairingAllocatedByes(round, barredFromPab, violations);
            recordColours(round, colours);
            if (number < lastRound
                    && tournament.settings().pairingSystem().gamesInSuccession() == 1
                    && tournament.settings().pairingSystem().competitionType() == CompetitionType.INDIVIDUAL) {
                colourLimits(number, colours, violations);
            }
            bars(round, fullPointByeBars(tournament), barredFromPab);
        }
        everyoneRanked(tournament, violations);
        return violations;
    }

    /** The Standings after the last round rank every participant under the drawn Tie-break List (C.07). */
    private static void everyoneRanked(Tournament tournament, List<String> violations) {
        var ranked = tournament.standings().ranked().size();
        if (ranked != tournament.participants().size()) {
            violations.add("standings rank " + ranked + " of "
                    + tournament.participants().size() + " participants");
        }
    }

    private static void everyoneAccountedFor(Tournament tournament, Round round, List<String> violations) {
        for (var participant :
                tournament.participants().stream().map(Participant::id).toList()) {
            if (!round.includes(participant)) {
                violations.add("round " + round.number() + ": " + participant + " is neither paired nor given a bye");
            }
        }
    }

    private static void nobodyPairedAfterWithdrawing(
            Round round, Set<ParticipantId> withdrawn, List<String> violations) {
        round.boards().stream()
                .flatMap(Board::participants)
                .filter(withdrawn::contains)
                .forEach(participant ->
                        violations.add("round " + round.number() + ": withdrawn " + participant + " is paired"));
        round.byes().forEach((participant, bye) -> {
            if (bye == Bye.WITHDRAWN) {
                withdrawn.add(participant);
            }
        });
    }

    private static void noRematches(Round round, Set<Set<ParticipantId>> met, List<String> violations) {
        for (var board : round.boards()) {
            if (board.outcome().isMeeting() && !met.add(Set.of(board.white(), board.black()))) {
                violations.add(
                        "round " + round.number() + ": " + board.white() + " and " + board.black() + " meet again");
            }
        }
    }

    private static void pairingAllocatedByes(Round round, Set<ParticipantId> barred, List<String> violations) {
        var byes = round.byes().entrySet().stream()
                .filter(entry -> entry.getValue() == Bye.PAIRING_ALLOCATED)
                .map(Map.Entry::getKey)
                .toList();
        if (byes.size() > 1) {
            violations.add("round " + round.number() + ": " + byes.size() + " PABs");
        }
        byes.stream()
                .filter(barred::contains)
                .forEach(participant ->
                        violations.add("round " + round.number() + ": " + participant + " may not receive the PAB"));
    }

    /** From 2026 a full-point bye bars a later PAB; the Olympiad's bar is a PAB, a forfeit win or a late entry (D.02 4.3). */
    private static boolean fullPointByeBars(Tournament tournament) {
        return tournament.settings().swissRulesEdition() == SwissRulesEdition.EDITION_2026
                && !tournament.settings().pairingSystem().name().startsWith("D.02 ");
    }

    /** After this round: a PAB, a forfeit win or (from 2026) a full-point bye bars a later PAB (C.04.1 Art. 4). */
    private static void bars(Round round, boolean fullPointByeBars, Set<ParticipantId> barred) {
        round.byes().forEach((participant, bye) -> {
            if (bye == Bye.PAIRING_ALLOCATED || (bye == Bye.FULL_POINT && fullPointByeBars)) {
                barred.add(participant);
            }
        });
        for (var board : round.boards()) {
            if (!board.outcome().isPlayed()) {
                board.participants()
                        .filter(participant -> board.outcome().resultOf(board.colourOf(participant)) == GameResult.WIN)
                        .forEach(barred::add);
            }
        }
    }

    private static void recordColours(Round round, Map<ParticipantId, List<Colour>> colours) {
        for (var board : round.boards()) {
            if (board.outcome().isPlayed()) {
                board.participants()
                        .forEach(participant -> colours.computeIfAbsent(participant, key -> new ArrayList<>())
                                .add(board.colourOf(participant)));
            }
        }
    }

    private static void colourLimits(int round, Map<ParticipantId, List<Colour>> colours, List<String> violations) {
        colours.forEach((participant, played) -> {
            var whites = played.stream().filter(Colour.WHITE::equals).count();
            var difference = 2 * whites - played.size();
            if (Math.abs(difference) > 2) {
                violations.add("round " + round + ": " + participant + " has colour difference " + difference);
            }
            var size = played.size();
            if (size >= 3
                    && played.get(size - 1) == played.get(size - 2)
                    && played.get(size - 2) == played.get(size - 3)) {
                violations.add("round " + round + ": " + participant + " has the same colour three times running");
            }
        });
    }
}
