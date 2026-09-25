package io.github.markdechamps.fideswiss.tournament;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Who is known in advance not to be paired in which round: requested byes, withdrawals and late entries. */
final class Attendance {

    private final Map<ParticipantId, Map<RoundNumber, Bye>> requestedByes;
    private final Map<ParticipantId, RoundNumber> withdrawnFrom;
    private final Map<ParticipantId, LateEntry> lateEntries;

    private record LateEntry(RoundNumber firstRound, Bye missed) {}

    private Attendance(
            Map<ParticipantId, Map<RoundNumber, Bye>> requestedByes,
            Map<ParticipantId, RoundNumber> withdrawnFrom,
            Map<ParticipantId, LateEntry> lateEntries) {
        this.requestedByes = Map.copyOf(requestedByes);
        this.withdrawnFrom = Map.copyOf(withdrawnFrom);
        this.lateEntries = Map.copyOf(lateEntries);
    }

    static Attendance everyonePresent() {
        return new Attendance(Map.of(), Map.of(), Map.of());
    }

    Attendance withLateEntry(ParticipantId participant, RoundNumber firstRound, Bye missed) {
        var entries = new HashMap<>(lateEntries);
        entries.put(participant, new LateEntry(firstRound, missed));
        return new Attendance(requestedByes, withdrawnFrom, entries);
    }

    /** Whether the participant was on the round-1 list, not a Late Entry (GHR 2.4). */
    boolean startsInRoundOne(ParticipantId participant) {
        var entry = lateEntries.get(participant);
        return entry == null || entry.firstRound().equals(RoundNumber.FIRST);
    }

    Attendance withRequestedBye(ParticipantId participant, RoundNumber round, Bye bye) {
        var byes = new HashMap<>(requestedByes);
        var ofParticipant = new HashMap<>(byes.getOrDefault(participant, Map.of()));
        ofParticipant.put(round, bye);
        byes.put(participant, Map.copyOf(ofParticipant));
        return new Attendance(byes, withdrawnFrom, lateEntries);
    }

    Attendance withWithdrawal(ParticipantId participant, RoundNumber from) {
        var withdrawals = new HashMap<>(withdrawnFrom);
        withdrawals.merge(participant, from, (earlier, later) -> earlier.isBefore(later) ? earlier : later);
        return new Attendance(requestedByes, withdrawals, lateEntries);
    }

    /** Why the participant is not to be paired in the round, if it is not. */
    Optional<Bye> absenceIn(ParticipantId participant, RoundNumber round) {
        var notYetEntered = Optional.ofNullable(lateEntries.get(participant))
                .filter(entry -> round.isBefore(entry.firstRound()))
                .map(LateEntry::missed);
        if (notYetEntered.isPresent()) {
            return notYetEntered;
        }
        var withdrawal = Optional.ofNullable(withdrawnFrom.get(participant))
                .filter(from -> !from.isAfter(round))
                .map(from -> Bye.WITHDRAWN);
        return withdrawal.or(() -> Optional.ofNullable(
                requestedByes.getOrDefault(participant, Map.of()).get(round)));
    }
}
