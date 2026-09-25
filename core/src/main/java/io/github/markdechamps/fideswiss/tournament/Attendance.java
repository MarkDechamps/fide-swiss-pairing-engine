package io.github.markdechamps.fideswiss.tournament;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Who is known in advance not to be paired in which round: requested byes, withdrawals and late entries. */
final class Attendance {

    private final Map<ParticipantId, Map<RoundNumber, Bye>> requestedByes;
    private final Map<ParticipantId, RoundNumber> withdrawnFrom;

    private Attendance(
            Map<ParticipantId, Map<RoundNumber, Bye>> requestedByes, Map<ParticipantId, RoundNumber> withdrawnFrom) {
        this.requestedByes = Map.copyOf(requestedByes);
        this.withdrawnFrom = Map.copyOf(withdrawnFrom);
    }

    static Attendance everyonePresent() {
        return new Attendance(Map.of(), Map.of());
    }

    Attendance withRequestedBye(ParticipantId participant, RoundNumber round, Bye bye) {
        var byes = new HashMap<>(requestedByes);
        var ofParticipant = new HashMap<>(byes.getOrDefault(participant, Map.of()));
        ofParticipant.put(round, bye);
        byes.put(participant, Map.copyOf(ofParticipant));
        return new Attendance(byes, withdrawnFrom);
    }

    Attendance withWithdrawal(ParticipantId participant, RoundNumber from) {
        var withdrawals = new HashMap<>(withdrawnFrom);
        withdrawals.merge(participant, from, (earlier, later) -> earlier.isBefore(later) ? earlier : later);
        return new Attendance(requestedByes, withdrawals);
    }

    /** Why the participant is not to be paired in the round, if it is not. */
    Optional<Bye> absenceIn(ParticipantId participant, RoundNumber round) {
        var withdrawal = Optional.ofNullable(withdrawnFrom.get(participant))
                .filter(from -> !from.isAfter(round))
                .map(from -> Bye.WITHDRAWN);
        return withdrawal.or(() -> Optional.ofNullable(
                requestedByes.getOrDefault(participant, Map.of()).get(round)));
    }
}
