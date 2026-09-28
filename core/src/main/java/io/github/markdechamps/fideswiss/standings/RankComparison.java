package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.ParticipantId;

/** Who of two participants ranks higher, and why. With a Shared Rank the order of the two is the one asked. */
public record RankComparison(ParticipantId higher, ParticipantId lower, DecidingTieBreak decidedBy) {}
