package io.github.markdechamps.fideswiss.tournament;

import java.util.HashMap;
import java.util.Map;

/** Points an acceleration adds to participants' scores for single rounds' pairings only (C.04.7). */
public final class VirtualPoints {

    private final Map<ParticipantId, Map<RoundNumber, Points>> points;

    private VirtualPoints(Map<ParticipantId, Map<RoundNumber, Points>> points) {
        var copy = new HashMap<ParticipantId, Map<RoundNumber, Points>>();
        points.forEach((participant, rounds) -> copy.put(participant, Map.copyOf(rounds)));
        this.points = Map.copyOf(copy);
    }

    public static VirtualPoints of(Map<ParticipantId, Map<RoundNumber, Points>> points) {
        return new VirtualPoints(points);
    }

    public Points of(ParticipantId participant, RoundNumber round) {
        return points.getOrDefault(participant, Map.of()).getOrDefault(round, Points.ZERO);
    }
}
