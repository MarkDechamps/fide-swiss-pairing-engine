package io.github.markdechamps.fideswiss.tournament;

import java.util.Comparator;
import java.util.List;

/**
 * How participants are ranked into Pairing Numbers (GHR 2.2): by strength, title and name for individuals; by
 * an order announced beforehand ({@link #declared}); or in the order they were registered ({@link #asListed}).
 */
public sealed interface RankingKey {

    static RankingKey strengthTitleName() {
        return new StrengthTitleName();
    }

    static RankingKey declared(List<ParticipantId> order) {
        return new Declared(List.copyOf(order));
    }

    static RankingKey asListed() {
        return new AsListed();
    }

    /** The ranking order, strongest first, for participants registered in {@code registrationOrder}. */
    Comparator<Participant> order(List<Participant> registrationOrder);

    /** GHR 2.2.1–2.2.3. */
    record StrengthTitleName() implements RankingKey {
        @Override
        public Comparator<Participant> order(List<Participant> registrationOrder) {
            return Comparator.comparing(Participant::rating)
                    .reversed()
                    .thenComparing(participant ->
                            participant.title().map(Title::ordinal).orElse(Title.values().length))
                    .thenComparing(Participant::name);
        }
    }

    record Declared(List<ParticipantId> ids) implements RankingKey {
        @Override
        public Comparator<Participant> order(List<Participant> registrationOrder) {
            return Comparator.comparingInt(participant -> positionOf(participant.id()));
        }

        private int positionOf(ParticipantId id) {
            var position = ids.indexOf(id);
            return position < 0 ? Integer.MAX_VALUE : position;
        }
    }

    record AsListed() implements RankingKey {
        @Override
        public Comparator<Participant> order(List<Participant> registrationOrder) {
            return Comparator.comparingInt(registrationOrder::indexOf);
        }
    }
}
