package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairingCheck;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An immutable snapshot of a tournament: its settings, its participants and the rounds recorded so far (ADR
 * 0002). Every change returns a new snapshot; the same snapshot always gives the same pairing.
 */
public final class Tournament {

    private final TournamentSettings settings;
    private final List<Participant> participants;
    private final List<Round> rounds;
    private final Attendance attendance;

    private Tournament(
            TournamentSettings settings, List<Participant> participants, List<Round> rounds, Attendance attendance) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.participants = List.copyOf(participants);
        this.rounds = List.copyOf(rounds);
        this.attendance = attendance;
    }

    public static Tournament of(TournamentSettings settings, List<Participant> participants) {
        var settingsProblems = settings.acceleration().problemsWith(settings);
        if (!settingsProblems.isEmpty()) {
            throw new InvalidSettingsException(settingsProblems);
        }
        var problems = duplicateIds(participants);
        if (!problems.isEmpty()) {
            throw new InvalidTournamentException(problems);
        }
        return new Tournament(settings, participants, List.of(), Attendance.everyonePresent());
    }

    /** The simplest tournament: the baseline profile ({@link Profiles#individualSwiss}). */
    public static Tournament of(List<Participant> participants, NumberOfRounds rounds) {
        return of(Profiles.individualSwiss(rounds), participants);
    }

    public TournamentSettings settings() {
        return settings;
    }

    public List<Participant> participants() {
        return participants;
    }

    public List<Round> rounds() {
        return rounds;
    }

    public Participant participant(ParticipantId id) {
        return findParticipant(id)
                .orElseThrow(() -> new InvalidTournamentException(Problem.of("Unknown participant", id)));
    }

    public RoundNumber nextRound() {
        return RoundNumber.of(rounds.size() + 1);
    }

    /** The participants to be paired in the next round, in registration order. */
    public List<Participant> participantsToBePaired() {
        return participants.stream()
                .filter(participant -> absenceInNextRound(participant.id()).isEmpty())
                .toList();
    }

    /** Everyone known in advance not to be paired in the next round, with why (GHR 3.2, 3.3). */
    public Map<ParticipantId, Bye> absencesInNextRound() {
        var absences = new HashMap<ParticipantId, Bye>();
        participants.forEach(participant ->
                absenceInNextRound(participant.id()).ifPresent(bye -> absences.put(participant.id(), bye)));
        return Map.copyOf(absences);
    }

    /** A Requested Bye for a round not yet recorded (GHR 3.3). */
    public Tournament requestBye(ParticipantId participant, RoundNumber round, RequestedBye bye) {
        requireFutureRound(participant, round);
        return new Tournament(
                settings, participants, rounds, attendance.withRequestedBye(participant, round, bye.bye()));
    }

    /** A Withdrawal: the participant is no longer paired from that round on (GHR 3.2). */
    public Tournament withdraw(ParticipantId participant, RoundNumber from) {
        requireFutureRound(participant, from);
        return new Tournament(settings, participants, rounds, attendance.withWithdrawal(participant, from));
    }

    private Optional<Bye> absenceInNextRound(ParticipantId participant) {
        return attendance.absenceIn(participant, nextRound());
    }

    private void requireFutureRound(ParticipantId participant, RoundNumber round) {
        participant(participant);
        if (round.isBefore(nextRound())) {
            throw new InvalidTournamentException(Problem.of("Round " + round + " is already recorded", participant));
        }
    }

    /**
     * The Pairing Numbers for the next round: the Numbered Participants (taken into account for this round's
     * pairing or an earlier one) in the order of the settings' ranking key (GHR 2.2–2.4, ADR 0006).
     */
    public PairingNumbers pairingNumbers() {
        var toBePaired = new HashSet<>(participantsToBePaired());
        var numbered = participants.stream()
                .filter(participant -> toBePaired.contains(participant) || wasEverPaired(participant.id()))
                .sorted(settings.rankingKey().order(participants))
                .map(Participant::id)
                .toList();
        return PairingNumbers.inRankingOrder(numbered);
    }

    /** The Virtual Points the settings' acceleration adds to the participant's score for the round's pairing. */
    public Points virtualPointsOf(ParticipantId participant, RoundNumber round) {
        return settings.acceleration().virtualPointsOf(participant, round, this);
    }

    /** C.04.7 1.2: the top 2·⌈N/4⌉ of the round-1 list, in ranking-key order. */
    boolean isInAcceleratedGroup(ParticipantId participant) {
        var ranked = participants.stream()
                .sorted(settings.rankingKey().order(participants))
                .map(Participant::id)
                .toList();
        var groupSize = 2 * ((ranked.size() + 3) / 4);
        return ranked.indexOf(participant) < groupSize;
    }

    public RoundPairing pairNextRound() {
        if (!settings.numberOfRounds().includes(nextRound())) {
            throw new InvalidTournamentException(Problem.of(
                    "The tournament has only " + settings.numberOfRounds().value() + " rounds"));
        }
        return settings.pairingSystem().pairNextRound(this);
    }

    /**
     * Checks a pairing for the next round that did not come from the library (GHR 4.4, a manual pairing, another
     * program's): the rules it breaks and how it differs from the system's own pairing (ADR 0004).
     */
    public PairingCheck check(ProposedPairing proposed) {
        var violations = settings.pairingSystem().violationsOf(this, proposed);
        try {
            var system = pairNextRound();
            return new PairingCheck(violations, Optional.of(system), differencesBetween(proposed, system));
        } catch (NoLegalPairingException e) {
            return new PairingCheck(violations, Optional.empty(), List.of("the system finds no legal pairing"));
        }
    }

    private static List<String> differencesBetween(ProposedPairing proposed, RoundPairing system) {
        var proposedBoards = proposed.boards().stream()
                .map(board -> board.white() + "-" + board.black())
                .collect(Collectors.toSet());
        var systemBoards = system.boards().stream()
                .map(board -> board.white() + "-" + board.black())
                .collect(Collectors.toSet());
        var differences = new ArrayList<String>();
        proposedBoards.stream()
                .filter(board -> !systemBoards.contains(board))
                .sorted()
                .forEach(board -> differences.add("proposed " + board + " is not the system's"));
        systemBoards.stream()
                .filter(board -> !proposedBoards.contains(board))
                .sorted()
                .forEach(board -> differences.add("the system pairs " + board));
        if (!proposed.pairingAllocatedBye().equals(system.pairingAllocatedBye())) {
            differences.add("the system gives the PAB to "
                    + system.pairingAllocatedBye().map(Object::toString).orElse("nobody"));
        }
        return differences;
    }

    public Tournament withRound(Round round) {
        var problems = new ArrayList<Problem>();
        if (!round.number().equals(nextRound())) {
            problems.add(Problem.of("Expected round " + nextRound() + " but got round " + round.number()));
        }
        round.mentions()
                .filter(id -> findParticipant(id).isEmpty())
                .distinct()
                .forEach(id -> problems.add(Problem.of("Unknown participant in round " + round.number(), id)));
        round.mentions()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .forEach((id, count) -> {
                    if (count > 1) {
                        problems.add(Problem.of("Participant appears more than once in round " + round.number(), id));
                    }
                });
        if (!problems.isEmpty()) {
            throw new InvalidTournamentException(problems);
        }
        var recorded = new ArrayList<>(rounds);
        recorded.add(round);
        return new Tournament(settings, participants, recorded, attendance);
    }

    private boolean wasEverPaired(ParticipantId participant) {
        return rounds.stream().anyMatch(round -> round.pairs(participant));
    }

    private Optional<Participant> findParticipant(ParticipantId id) {
        return participants.stream()
                .filter(participant -> participant.id().equals(id))
                .findFirst();
    }

    private static List<Problem> duplicateIds(List<Participant> participants) {
        var seen = new HashSet<ParticipantId>();
        return participants.stream()
                .map(Participant::id)
                .filter(id -> !seen.add(id))
                .distinct()
                .map(id -> Problem.of("Two participants share an id", id))
                .toList();
    }
}
