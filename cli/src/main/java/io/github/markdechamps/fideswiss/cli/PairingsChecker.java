package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.PairingCheck;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.InvalidTournamentException;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.trf.TrfTournament;
import java.io.PrintStream;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The Pairings Checker (C.02.03 §7.2.3): rebuilds the tournament round by round, pairs each recorded round
 * itself, and reports every round that breaks a rule (ILLEGAL) or is not the system's pairing (DIFFERENT).
 */
final class PairingsChecker {

    private final PrintStream out;

    PairingsChecker(PrintStream out) {
        this.out = out;
    }

    int check(TrfTournament file, Optional<Integer> onlyRound) {
        var recorded = file.recordedRounds().size();
        var rounds = onlyRound
                .map(round -> IntStream.of(requireRecorded(round, recorded)))
                .orElseGet(() -> IntStream.rangeClosed(1, recorded))
                .boxed()
                .toList();
        var inconsistent = 0;
        for (var round : rounds) {
            if (!isConsistent(file, RoundNumber.of(round))) {
                inconsistent++;
            }
        }
        out.printf(
                "checked %d rounds: %d consistent, %d not%n",
                rounds.size(), rounds.size() - inconsistent, inconsistent);
        return inconsistent == 0 ? Main.SUCCESS : Main.INCONSISTENT;
    }

    private boolean isConsistent(TrfTournament file, RoundNumber round) {
        var proposed = ProposedPairing.of(file.recordedRounds().get(round.value() - 1));
        var check = file.tournamentBefore(round).check(proposed);
        if (!check.isLegal()) {
            out.println("round " + round + ": ILLEGAL");
            check.violations().forEach(violation -> out.println("  " + violation));
            return false;
        }
        if (!check.isSystemPairing()) {
            out.println("round " + round + ": DIFFERENT");
            out.println("  file:   " + describe(proposed));
            out.println("  system: " + describe(check));
            return false;
        }
        return true;
    }

    private static String describe(PairingCheck check) {
        return check.systemPairing().map(PairingsChecker::describe).orElse("no legal pairing");
    }

    private static String describe(RoundPairing pairing) {
        return describe(ProposedPairing.of(pairing));
    }

    private static String describe(ProposedPairing pairing) {
        return Stream.concat(
                        pairing.boards().stream().map(board -> board.white() + " " + board.black()),
                        pairing.pairingAllocatedBye().stream().map(bye -> bye + " 0"))
                .collect(Collectors.joining(" | "));
    }

    private static int requireRecorded(int round, int recorded) {
        if (round < 1 || round > recorded) {
            throw new InvalidTournamentException(Problem.of("The file records no round " + round));
        }
        return round;
    }
}
