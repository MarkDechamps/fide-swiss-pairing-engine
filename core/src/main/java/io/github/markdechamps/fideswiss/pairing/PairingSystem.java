package io.github.markdechamps.fideswiss.pairing;

import io.github.markdechamps.fideswiss.rules.BasicRules;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.List;

/**
 * A named ruleset that decides who plays whom, and with which colour, in the next round. Obtain the FIDE systems
 * from {@link PairingSystems}; an arbiter's own authorised system (GHR 1.1) can implement this interface.
 */
public interface PairingSystem {

    RoundPairing pairNextRound(Tournament tournament);

    /** The rules a proposed pairing for the next round breaks: the Basic Rules, and the system's own. */
    default List<Violation> violationsOf(Tournament tournament, ProposedPairing proposed) {
        return BasicRules.violationsOf(tournament, proposed);
    }

    /** Whether the system pairs players or teams. */
    default CompetitionType competitionType() {
        return CompetitionType.INDIVIDUAL;
    }

    /** What makes these settings unusable with the system; {@code Tournament.of} refuses them. */
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
}
