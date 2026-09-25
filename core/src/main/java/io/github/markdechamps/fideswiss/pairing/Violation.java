package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import java.util.List;

/** A rule a proposed pairing breaks: the article, the participants involved, and why. */
public record Violation(String article, List<ParticipantId> participants, String message) {

    public Violation {
        participants = List.copyOf(participants);
    }

    public static Violation of(String article, String message, ParticipantId... participants) {
        return new Violation(article, List.of(participants), message);
    }

    @Override
    public String toString() {
        return "[" + article + "] " + message;
    }
}
