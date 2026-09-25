package io.github.markdechamps.fideswiss.rules;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.Violation;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/** The checks C.04.1 and C.04.2 make of any pairing, whatever the system. */
public final class BasicRules {

    private BasicRules() {}

    public static List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        var violations = new ArrayList<Violation>();
        violations.addAll(everyoneToBePairedExactlyOnce(tournament, proposed));
        violations.addAll(pairingAllocatedByeOnlyForAnOddNumber(tournament, proposed));
        violations.addAll(noRematches(tournament, proposed));
        violations.addAll(pairingAllocatedByeOnlyForTheEligible(tournament, proposed));
        return violations;
    }

    private static List<Violation> everyoneToBePairedExactlyOnce(Tournament tournament, ProposedPairing proposed) {
        var toBePaired = tournament.participantsToBePaired().stream()
                .map(Participant::id)
                .toList();
        var absences = tournament.absencesInNextRound();
        var counts = new HashMap<ParticipantId, Integer>();
        seated(proposed).forEach(participant -> counts.merge(participant, 1, Integer::sum));
        var violations = new ArrayList<Violation>();
        counts.forEach((participant, count) -> {
            if (absences.containsKey(participant)) {
                violations.add(
                        Violation.of("C.04.2 3.2-3.3", participant + " is known not to play this round", participant));
            } else if (!toBePaired.contains(participant)) {
                violations.add(Violation.of("C.04.2 2", participant + " is not a participant", participant));
            } else if (count > 1) {
                violations.add(Violation.of("C.04.1 Art. 3", participant + " is paired more than once", participant));
            }
        });
        var missing = toBePaired.stream()
                .filter(participant -> !counts.containsKey(participant))
                .toList();
        if (!missing.isEmpty()) {
            violations.add(new Violation(
                    "C.04.1 Art. 3",
                    missing,
                    "left unpaired: "
                            + String.join(
                                    ", ", missing.stream().map(Object::toString).toList())));
        }
        return violations;
    }

    private static List<Violation> pairingAllocatedByeOnlyForAnOddNumber(
            Tournament tournament, ProposedPairing proposed) {
        var odd = tournament.participantsToBePaired().size() % 2 == 1;
        if (odd && proposed.pairingAllocatedBye().isEmpty()) {
            return List.of(Violation.of("C.04.1 Art. 3", "an odd number is to be paired but nobody gets the PAB"));
        }
        if (!odd && proposed.pairingAllocatedBye().isPresent()) {
            var participant = proposed.pairingAllocatedBye().get();
            return List.of(Violation.of(
                    "C.04.1 Art. 3",
                    "an even number is to be paired but " + participant + " gets the PAB",
                    participant));
        }
        return List.of();
    }

    private static List<Violation> noRematches(Tournament tournament, ProposedPairing proposed) {
        var history = TournamentHistory.of(tournament);
        return proposed.boards().stream()
                .filter(board -> history.of(board.white()).hasPlayedAgainst(board.black()))
                .map(board -> Violation.of(
                        "C.04.1 Art. 2",
                        board.white() + " and " + board.black() + " have already played each other",
                        board.white(),
                        board.black()))
                .toList();
    }

    private static List<Violation> pairingAllocatedByeOnlyForTheEligible(
            Tournament tournament, ProposedPairing proposed) {
        var history = TournamentHistory.of(tournament);
        return proposed.pairingAllocatedBye().stream()
                .filter(participant -> !history.of(participant).mayReceivePairingAllocatedBye())
                .map(participant -> Violation.of(
                        "C.04.1 Art. 4",
                        participant + " already had a PAB or a win's points without playing",
                        participant))
                .toList();
    }

    private static Stream<ParticipantId> seated(ProposedPairing proposed) {
        return Stream.concat(
                proposed.boards().stream().flatMap(board -> Stream.of(board.white(), board.black())),
                proposed.pairingAllocatedBye().stream());
    }
}
