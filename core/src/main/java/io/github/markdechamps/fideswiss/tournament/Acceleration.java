package io.github.markdechamps.fideswiss.tournament;

import java.util.List;

/**
 * How Virtual Points are added to the Pairing Score (C.04.7): not at all, by the Baku method, or explicitly per
 * participant and round (TRF {@code 250} or {@code XXA}).
 */
public sealed interface Acceleration {

    static Acceleration none() {
        return new None();
    }

    static Acceleration baku() {
        return new Baku();
    }

    static Acceleration explicit(VirtualPoints points) {
        return new Explicit(points);
    }

    /** The Virtual Points of the participant for the pairing of the round. */
    Points virtualPointsOf(ParticipantId participant, RoundNumber round, Tournament tournament);

    /** What makes the acceleration unusable with these settings. */
    default List<Problem> problemsWith(TournamentSettings settings) {
        return List.of();
    }

    record None() implements Acceleration {
        @Override
        public Points virtualPointsOf(ParticipantId participant, RoundNumber round, Tournament tournament) {
            return Points.ZERO;
        }
    }

    record Explicit(VirtualPoints points) implements Acceleration {
        @Override
        public Points virtualPointsOf(ParticipantId participant, RoundNumber round, Tournament tournament) {
            return points.of(participant, round);
        }
    }

    /**
     * The Baku method (C.04.7 1.2–1.4): the Accelerated Group is the top 2·⌈N/4⌉ of the round-1 list; in the first
     * ⌈R/2⌉ rounds it gets a win's points for the first ⌈accelerated/2⌉ of them and half of that for the rest. A
     * Double-Swiss round is a match of two games, so its win is a match won 2-0 (Acceleration across the systems,
     * decision 3).
     */
    record Baku() implements Acceleration {
        @Override
        public Points virtualPointsOf(ParticipantId participant, RoundNumber round, Tournament tournament) {
            if (!tournament.isInAcceleratedGroup(participant)) {
                return Points.ZERO;
            }
            var settings = tournament.settings();
            var win = settings.scoring()
                    .primaryWin()
                    .times(settings.pairingSystem().gamesInSuccession());
            var accelerated = (tournament.settings().numberOfRounds().value() + 1) / 2;
            var atFullValue = (accelerated + 1) / 2;
            if (round.value() <= atFullValue) {
                return win;
            }
            return round.value() <= accelerated
                    ? Points.of(win.toBigDecimal().divide(java.math.BigDecimal.TWO))
                    : Points.ZERO;
        }

        /** C.04.7 1.1: a win equals two draws and a loss scores nothing, in the primary score. */
        @Override
        public List<Problem> problemsWith(TournamentSettings settings) {
            var scoring = settings.scoring();
            var winIsTwoDraws =
                    scoring.primaryWin().equals(scoring.primaryDraw().times(2));
            return winIsTwoDraws && scoring.primaryLoss().equals(Points.ZERO)
                    ? List.of()
                    : List.of(Problem.citing(
                            "C.04.7 1.1", "Baku acceleration needs a win worth two draws and a loss worth nothing"));
        }
    }
}
