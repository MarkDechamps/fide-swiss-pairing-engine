package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import java.util.Objects;

/**
 * Every setting of a tournament. It always starts from a Profile; each {@code with} returns a new value with one
 * setting overridden. The Pairing System is fixed for the whole event (GHR 1.3), so it lives here.
 */
public record TournamentSettings(
        PairingSystem pairingSystem,
        SwissRulesEdition swissRulesEdition,
        ScoringScheme scoring,
        RankingKey rankingKey,
        InitialColour initialColour,
        NumberOfRounds numberOfRounds,
        TieBreakList tieBreakList,
        TieBreakEdition tieBreakEdition) {

    public TournamentSettings {
        Objects.requireNonNull(pairingSystem, "pairingSystem");
        Objects.requireNonNull(swissRulesEdition, "swissRulesEdition");
        Objects.requireNonNull(scoring, "scoring");
        Objects.requireNonNull(rankingKey, "rankingKey");
        Objects.requireNonNull(initialColour, "initialColour");
        Objects.requireNonNull(numberOfRounds, "numberOfRounds");
        Objects.requireNonNull(tieBreakList, "tieBreakList");
        Objects.requireNonNull(tieBreakEdition, "tieBreakEdition");
    }

    public TournamentSettings with(PairingSystem system) {
        return new TournamentSettings(
                system,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition);
    }

    public TournamentSettings with(ScoringScheme scheme) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scheme,
                rankingKey,
                initialColour,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition);
    }

    public TournamentSettings with(RankingKey key) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                key,
                initialColour,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition);
    }

    public TournamentSettings with(InitialColour colour) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                colour,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition);
    }

    public TournamentSettings with(NumberOfRounds rounds) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                rounds,
                tieBreakList,
                tieBreakEdition);
    }

    public TournamentSettings with(TieBreakList list) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                numberOfRounds,
                list,
                tieBreakEdition);
    }

    public TournamentSettings with(TieBreakEdition edition) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                numberOfRounds,
                tieBreakList,
                edition);
    }

    /** The value of every PAB of this event: the scheme's, or else the one the Pairing System defines. */
    public Points pairingAllocatedByeValue() {
        return scoring.pairingAllocatedBye().orElseGet(() -> pairingSystem.pairingAllocatedByeValue(scoring));
    }
}
