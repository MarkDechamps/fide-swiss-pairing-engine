package io.github.markdechamps.fideswiss.tournament;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * The Pairing Numbers of one round: the Numbered Participants in ranking order, #1 first (GHR 2.3, ADR 0006).
 * A participant never taken into account for pairing has none.
 */
public final class PairingNumbers {

    private final List<ParticipantId> inOrder;

    private PairingNumbers(List<ParticipantId> inOrder) {
        this.inOrder = List.copyOf(inOrder);
    }

    public static PairingNumbers inRankingOrder(List<ParticipantId> numberedParticipants) {
        return new PairingNumbers(numberedParticipants);
    }

    public Optional<PairingNumber> of(ParticipantId participant) {
        var index = inOrder.indexOf(participant);
        return index < 0 ? Optional.empty() : Optional.of(PairingNumber.of(index + 1));
    }

    public PairingNumber numberOf(ParticipantId participant) {
        return of(participant)
                .orElseThrow(() -> new IllegalArgumentException(participant + " holds no pairing number"));
    }

    public List<ParticipantId> inOrder() {
        return inOrder;
    }

    public int size() {
        return inOrder.size();
    }

    public List<PairingNumber> numbers() {
        return IntStream.rangeClosed(1, inOrder.size())
                .mapToObj(PairingNumber::of)
                .toList();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof PairingNumbers numbers && inOrder.equals(numbers.inOrder);
    }

    @Override
    public int hashCode() {
        return inOrder.hashCode();
    }

    @Override
    public String toString() {
        return inOrder.toString();
    }
}
