package io.github.markdechamps.fideswiss.tournament;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** One thing wrong with a request: the article it breaks (where there is one), who is involved, and why. */
public record Problem(Optional<String> article, List<ParticipantId> participants, String message) {

    public Problem {
        Objects.requireNonNull(article, "article");
        participants = List.copyOf(participants);
        Objects.requireNonNull(message, "message");
    }

    public static Problem of(String message, ParticipantId... participants) {
        return new Problem(Optional.empty(), List.of(participants), message);
    }

    public static Problem citing(String article, String message, ParticipantId... participants) {
        return new Problem(Optional.of(article), List.of(participants), message);
    }

    @Override
    public String toString() {
        var cited = article.map(value -> "[" + value + "] ").orElse("");
        var involved = participants.isEmpty() ? "" : " " + participants;
        return cited + message + involved;
    }
}
