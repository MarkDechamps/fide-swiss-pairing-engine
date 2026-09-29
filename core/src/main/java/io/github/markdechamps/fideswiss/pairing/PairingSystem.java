package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.rules.BasicRules;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.EdebtBoardCount;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.List;
import java.util.Optional;

/**
 * A named ruleset that decides who plays whom, and with which colour, in the next round. Obtain the FIDE systems
 * from {@link PairingSystems}; an arbiter's own authorised system (GHR 1.1) can implement this interface.
 */
public interface PairingSystem {

    RoundPairing pairNextRound(Tournament tournament);

    /** The system and edition, as the Chief Arbiter declares it (GHR 1.3). */
    default String name() {
        return getClass().getSimpleName();
    }

    /** As {@link #pairNextRound(Tournament)}, telling the listener how it advances. */
    default RoundPairing pairNextRound(Tournament tournament, PairingProgress progress) {
        return pairNextRound(tournament);
    }

    /** The rules a proposed pairing for the next round breaks: the Basic Rules, and the system's own. */
    default List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        return BasicRules.violationsOf(tournament, proposed);
    }

    /** Whether the system pairs players or teams. */
    default CompetitionType competitionType() {
        return CompetitionType.INDIVIDUAL;
    }

    /** The colour preferences of a Swiss Team competition (C.04.6 1.7); empty for every other system. */
    default Optional<ColourPreferenceType> teamColourPreferences() {
        return Optional.empty();
    }

    /** The same system with other colour preferences (C.04.6 1.7); only the Swiss Team System has them. */
    default PairingSystem withTeamColourPreferences(ColourPreferenceType preferences) {
        throw new InvalidSettingsException(
                List.of(Problem.of("The pairing system has no colour preference type " + preferences)));
    }

    /**
     * How many games each pairing plays in succession between the same two participants: two for the Double-Swiss
     * System (C.04.5 Preface), one otherwise. A team match plays its boards side by side, so it counts one.
     */
    default int gamesInSuccession() {
        return 1;
    }

    /**
     * What makes these settings unfit for the system, such as a Swiss Rules Edition it has no text for;
     * {@code Tournament.of} refuses them.
     */
    default List<Problem> problemsWith(TournamentSettings settings) {
        return List.of();
    }

    /** The system with one reading of an ambiguous article chosen; a system without it refuses. */
    default PairingSystem with(Interpretation interpretation) {
        throw new InvalidSettingsException(
                List.of(Problem.of("The pairing system has no Interpretation " + interpretation)));
    }

    /** What a Pairing-Allocated Bye is worth when the tournament does not say: a win (C.04.1 Art. 3). */
    default Points pairingAllocatedByeValue(ScoringScheme scoring) {
        return scoring.win();
    }

    /**
     * What a Pairing-Allocated Bye is worth in the score that is not the primary one, when the tournament does not
     * say: the points of a drawn match (or, in match points, a drawn match's game points).
     */
    default Points secondaryPairingAllocatedByeValue(ScoringScheme scoring) {
        return scoring.primaryScore() == PrimaryScore.MATCH_POINTS
                ? scoring.drawnMatchGamePoints()
                : scoring.matches().map(MatchScoring::draw).orElse(scoring.draw());
    }

    /** Team tie-breaks: the Board Count order of EDEBT and EDEBB (ADR 0009); systems without the reading use the default. */
    default EdebtBoardCount edebtBoardCount() {
        return EdebtBoardCount.higher();
    }
}
