package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Everything the tie-breaks read: every participant's counted rounds, the scoring and the Unplayed Round policy. */
final class TieBreakContext {

    private final Map<ParticipantId, ParticipantRounds> participants;
    private final ScoringScheme scoring;
    private final UnplayedRoundPolicy policy;
    private final int roundsCounted;
    private final List<ParticipantId> pairingNumberOrder;
    private final Map<ParticipantId, Points> adjustedScores = new HashMap<>();

    private TieBreakContext(
            Map<ParticipantId, ParticipantRounds> participants,
            ScoringScheme scoring,
            UnplayedRoundPolicy policy,
            int roundsCounted,
            List<ParticipantId> pairingNumberOrder) {
        this.participants = participants;
        this.scoring = scoring;
        this.policy = policy;
        this.roundsCounted = roundsCounted;
        this.pairingNumberOrder = List.copyOf(pairingNumberOrder);
    }

    /** The tournament's first {@code rounds} rounds, under the edition's policy. */
    static TieBreakContext of(Tournament tournament, int rounds, UnplayedRoundPolicy policy) {
        var history = TournamentHistory.of(tournament);
        var participants = new LinkedHashMap<ParticipantId, ParticipantRounds>();
        for (var participant : tournament.participants()) {
            var records = history.of(participant.id()).records();
            var entries = new ArrayList<RoundEntry>();
            for (var index = 0; index < rounds; index++) {
                entries.add(RoundEntry.of(RoundNumber.of(index + 1), records.get(index)));
            }
            participants.put(participant.id(), new ParticipantRounds(participant, entries));
        }
        return new TieBreakContext(
                participants, tournament.settings().scoring(), policy, rounds, pairingNumberOrder(tournament, rounds));
    }

    /** C.07 8.3: the same rounds with every paired game of the final round scored as a draw. */
    TieBreakContext withFinalRoundDrawn() {
        var drawn = new LinkedHashMap<ParticipantId, ParticipantRounds>();
        participants.forEach((id, rounds) -> {
            var entries = new ArrayList<>(rounds.entries());
            if (!entries.isEmpty() && entries.getLast().opponent().isPresent()) {
                entries.set(entries.size() - 1, entries.getLast().drawn(drawValue()));
            }
            drawn.put(id, new ParticipantRounds(rounds.participant(), entries));
        });
        return new TieBreakContext(drawn, scoring, policy, roundsCounted, pairingNumberOrder);
    }

    ParticipantRounds of(ParticipantId participant) {
        return participants.get(participant);
    }

    List<ParticipantRounds> everyone() {
        return List.copyOf(participants.values());
    }

    List<Participant> participantList() {
        return participants.values().stream()
                .map(ParticipantRounds::participant)
                .toList();
    }

    Points adjustedScore(ParticipantId participant) {
        return adjustedScores.computeIfAbsent(participant, id -> policy.adjustedScore(of(id), this));
    }

    Points dummyScore(ParticipantRounds participant, int index) {
        return policy.dummyScore(participant, index, this);
    }

    String dummyArticle(ParticipantRounds participant, int index) {
        return policy.dummyArticle(participant, index);
    }

    int roundsCounted() {
        return roundsCounted;
    }

    Points winValue() {
        return scoring.win();
    }

    Points drawValue() {
        return scoring.draw();
    }

    /** Numbered Participants by their latest Pairing Number, then the never-numbered ones (ADR 0006). */
    List<ParticipantId> pairingNumberOrder() {
        return pairingNumberOrder;
    }

    boolean anyoneUnrated() {
        return participants.values().stream()
                .anyMatch(rounds -> !rounds.participant().rating().isRated());
    }

    private static List<ParticipantId> pairingNumberOrder(Tournament tournament, int rounds) {
        var ranked = tournament.participants().stream()
                .sorted(tournament.settings().rankingKey().order(tournament.participants()))
                .map(Participant::id)
                .toList();
        var counted = tournament.rounds().subList(0, rounds);
        var numbered = ranked.stream()
                .filter(id -> counted.stream().anyMatch(round -> round.pairs(id)))
                .toList();
        var order = new ArrayList<>(numbered);
        ranked.stream().filter(id -> !numbered.contains(id)).forEach(order::add);
        return order;
    }
}
