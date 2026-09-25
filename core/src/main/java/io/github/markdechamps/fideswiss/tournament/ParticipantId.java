package io.github.markdechamps.fideswiss.tournament;

import java.util.Objects;

/** The stable, opaque identity a client gives a participant; history refers to it, never to a Pairing Number. */
public record ParticipantId(String value) {

    public ParticipantId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("A participant id must not be blank");
        }
    }

    public static ParticipantId of(String value) {
        return new ParticipantId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
