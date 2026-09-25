package io.github.markdechamps.fideswiss.tournament;

import java.util.List;
import java.util.stream.IntStream;

/** Tournaments of participants "1", "2", … whose ratings fall with their number, so the id is the start rank. */
public final class TournamentMother {

    private static final int TOP_RATING = 2500;

    private TournamentMother() {}

    public static Tournament individualSwiss(int participants, int rounds) {
        return Tournament.of(participants(participants), NumberOfRounds.of(rounds));
    }

    public static List<Participant> participants(int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(TournamentMother::participant)
                .toList();
    }

    public static Participant participant(int startRank) {
        return Participant.of(
                ParticipantId.of(String.valueOf(startRank)),
                Name.of("Player " + startRank),
                Rating.of(TOP_RATING - 10 * startRank));
    }

    public static ParticipantId id(int startRank) {
        return ParticipantId.of(String.valueOf(startRank));
    }
}
