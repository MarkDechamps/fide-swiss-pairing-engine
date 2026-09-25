package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;
import java.util.Optional;

/** The entity that gets paired: a player in an individual tournament, a team in a team tournament. */
public record Participant(ParticipantId id, Name name, Rating rating, Optional<Title> title) {

    public Participant {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(rating, "rating");
        Objects.requireNonNull(title, "title");
    }

    public static Participant of(ParticipantId id, Name name, Rating rating) {
        return new Participant(id, name, rating, Optional.empty());
    }

    public Participant withTitle(Title title) {
        return new Participant(id, name, rating, Optional.of(title));
    }

    public Participant withRating(Rating corrected) {
        return new Participant(id, name, corrected, title);
    }
}
