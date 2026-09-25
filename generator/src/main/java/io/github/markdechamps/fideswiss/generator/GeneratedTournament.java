package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;

/** What one seed produced: a completed tournament, or the round in which no legal pairing existed. */
public sealed interface GeneratedTournament {

    TournamentSeed seed();

    /** A tournament played to its last round, with the parameters drawn for it. */
    record Completed(TournamentSeed seed, Tournament tournament, Parameters parameters)
            implements GeneratedTournament {}

    /** The tournament stopped: the round had no legal pairing, and the generator never bends a round. */
    record Skipped(TournamentSeed seed, RoundNumber round, String reason, Parameters parameters)
            implements GeneratedTournament {}

    /** The values drawn for one tournament from the settings' ranges (written to the manifest). */
    record Parameters(
            int players,
            int rounds,
            int highestRating,
            int lowestRating,
            int unrated,
            int forfeitRate,
            int halfPointByeRate,
            int zeroPointByeRate,
            int fullPointByeRate,
            int withdrawals) {}
}
