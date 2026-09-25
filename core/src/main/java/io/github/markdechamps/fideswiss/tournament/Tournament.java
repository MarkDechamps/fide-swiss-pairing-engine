package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private Tournament(TournamentSettings settings, List<Participant> participants, List<Round> rounds) {
        this.settings = Objects.requireNonNull(settings, "settings");
        this.participants = List.copyOf(participants);
        this.rounds = List.copyOf(rounds);
    }

    public static Tournament of(TournamentSettings settings, List<Participant> participants) {
        var problems = duplicateIds(participants);
        if (!problems.isEmpty()) {
            throw new InvalidTournamentException(problems);
        }
        return new Tournament(settings, participants, List.of());
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
        return participants;
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

    public RoundPairing pairNextRound() {
        if (!settings.numberOfRounds().includes(nextRound())) {
            throw new InvalidTournamentException(Problem.of(
                    "The tournament has only " + settings.numberOfRounds().value() + " rounds"));
        }
        return settings.pairingSystem().pairNextRound(this);
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
        return new Tournament(settings, participants, recorded);
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
