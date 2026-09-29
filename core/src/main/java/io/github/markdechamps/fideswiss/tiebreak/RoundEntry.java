package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.history.RoundRecord;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.Optional;

/** One round of one participant, as the tie-breaks see it. */
record RoundEntry(
        RoundNumber round,
        Kind kind,
        Optional<ParticipantId> opponent,
        Optional<Colour> colour,
        Optional<GameResult> result,
        Points points) {

    enum Kind {
        GAME,
        FORFEIT_WIN,
        /** A forfeit loss, also each side of a double forfeit. */
        FORFEIT_LOSS,
        PAIRING_ALLOCATED_BYE,
        FULL_POINT_BYE,
        HALF_POINT_BYE,
        ZERO_POINT_BYE,
        /** Any round after a withdrawal, a zero-point bye (C.07 16.1.1). */
        WITHDRAWN,
        /** A round before a late entry arrived, a zero-point bye for the tie-breaks. */
        NOT_YET_ENTERED;

        /** Requested byes in the C.07 16.1.1 sense: half- and zero-point byes, including every round after a withdrawal. */
        boolean isRequestedBye() {
            return this == HALF_POINT_BYE || this == ZERO_POINT_BYE || this == WITHDRAWN || this == NOT_YET_ENTERED;
        }
    }

    static RoundEntry of(RoundNumber round, RoundRecord record) {
        return switch (record) {
            case RoundRecord.Game game ->
                new RoundEntry(
                        round,
                        Kind.GAME,
                        Optional.of(game.opponent()),
                        Optional.of(game.colour()),
                        Optional.of(game.result()),
                        game.points());
            case RoundRecord.Forfeit forfeit ->
                new RoundEntry(
                        round,
                        forfeit.result() == GameResult.WIN ? Kind.FORFEIT_WIN : Kind.FORFEIT_LOSS,
                        Optional.of(forfeit.opponent()),
                        Optional.empty(),
                        Optional.of(forfeit.result()),
                        forfeit.points());
            case RoundRecord.NoBoard noBoard ->
                new RoundEntry(
                        round,
                        switch (noBoard.bye()) {
                            case PAIRING_ALLOCATED -> Kind.PAIRING_ALLOCATED_BYE;
                            case FULL_POINT -> Kind.FULL_POINT_BYE;
                            case HALF_POINT -> Kind.HALF_POINT_BYE;
                            case ZERO_POINT -> Kind.ZERO_POINT_BYE;
                            case WITHDRAWN -> Kind.WITHDRAWN;
                            case NOT_YET_ENTERED -> Kind.NOT_YET_ENTERED;
                        },
                        Optional.empty(),
                        Optional.empty(),
                        Optional.empty(),
                        noBoard.points());
        };
    }

    boolean isGame() {
        return kind == Kind.GAME;
    }

    boolean isForfeit() {
        return kind == Kind.FORFEIT_WIN || kind == Kind.FORFEIT_LOSS;
    }

    /** The same round scored in the other team score (C.07 11.1). */
    RoundEntry withPoints(Points other) {
        return new RoundEntry(round, kind, opponent, colour, result, other);
    }

    /** The same pairing scored as a draw, for Fore Buchholz (C.07 8.3). */
    RoundEntry drawn(Points drawValue) {
        return new RoundEntry(round, Kind.GAME, opponent, colour, Optional.of(GameResult.DRAW), drawValue);
    }
}
