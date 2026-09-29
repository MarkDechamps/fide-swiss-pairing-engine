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
        Acceleration acceleration,
        NumberOfRounds numberOfRounds,
        TieBreakList tieBreakList,
        TieBreakEdition tieBreakEdition,
        EdebtBoardCount edebtBoardCount) {

    public TournamentSettings {
        Objects.requireNonNull(pairingSystem, "pairingSystem");
        Objects.requireNonNull(swissRulesEdition, "swissRulesEdition");
        Objects.requireNonNull(scoring, "scoring");
        Objects.requireNonNull(rankingKey, "rankingKey");
        Objects.requireNonNull(initialColour, "initialColour");
        Objects.requireNonNull(acceleration, "acceleration");
        Objects.requireNonNull(numberOfRounds, "numberOfRounds");
        Objects.requireNonNull(tieBreakList, "tieBreakList");
        Objects.requireNonNull(tieBreakEdition, "tieBreakEdition");
        Objects.requireNonNull(edebtBoardCount, "edebtBoardCount");
    }

    public TournamentSettings with(PairingSystem system) {
        return new TournamentSettings(
                system,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(SwissRulesEdition edition) {
        return new TournamentSettings(
                pairingSystem,
                edition,
                scoring,
                rankingKey,
                initialColour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(ScoringScheme scheme) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scheme,
                rankingKey,
                initialColour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(RankingKey key) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                key,
                initialColour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(InitialColour colour) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                colour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(Acceleration method) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                method,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(NumberOfRounds rounds) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                acceleration,
                rounds,
                tieBreakList,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(TieBreakList list) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                acceleration,
                numberOfRounds,
                list,
                tieBreakEdition,
                edebtBoardCount);
    }

    public TournamentSettings with(TieBreakEdition edition) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                edition,
                edebtBoardCount);
    }

    /** The Board Count order of EDEBT and EDEBB (ADR 0009): a tie-break reading, whatever the team system. */
    public TournamentSettings with(EdebtBoardCount order) {
        return new TournamentSettings(
                pairingSystem,
                swissRulesEdition,
                scoring,
                rankingKey,
                initialColour,
                acceleration,
                numberOfRounds,
                tieBreakList,
                tieBreakEdition,
                order);
    }

    /**
     * One reading of an ambiguous article (ADR 0003): a tie-break reading is a standings setting, any other belongs
     * to the Pairing System. Under game points, choosing the PAB's value replaces a value the file stated, which
     * would else leave the reading without effect.
     */
    public TournamentSettings with(Interpretation interpretation) {
        return switch (interpretation) {
            case EdebtBoardCount order -> with(order);
            case PabValue reading -> withPabValue(reading);
            default -> with(pairingSystem.with(interpretation));
        };
    }

    private TournamentSettings withPabValue(PabValue reading) {
        var chosen = with(pairingSystem.with(reading));
        return scoring.primaryScore() == PrimaryScore.GAME_POINTS
                ? chosen.with(scoring.withoutPairingAllocatedBye())
                : chosen;
    }

    /** The value of every PAB of this event: the scheme's, or else the one the Pairing System defines. */
    public Points pairingAllocatedByeValue() {
        return scoring.pairingAllocatedBye().orElseGet(() -> pairingSystem.pairingAllocatedByeValue(scoring));
    }
}
