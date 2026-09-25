package io.github.markdechamps.fideswiss.history;

import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameResult;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.Optional;

/** What one round was for one participant, as every system and tie-break sees it. */
public sealed interface RoundRecord {

    Points points();

    /** Whether a game was played over the board (GHR 3.4, 3.5). */
    default boolean isPlayed() {
        return false;
    }

    /** The participant it was paired against, played or not. */
    default Optional<ParticipantId> pairedOpponent() {
        return Optional.empty();
    }

    /** A game played over the board. */
    record Game(ParticipantId opponent, Colour colour, GameResult result, Points points) implements RoundRecord {
        @Override
        public boolean isPlayed() {
            return true;
        }

        @Override
        public Optional<ParticipantId> pairedOpponent() {
            return Optional.of(opponent);
        }
    }

    /** A pairing whose game was not played: a forfeit win or loss, or a double forfeit. It has no colour. */
    record Forfeit(ParticipantId opponent, GameResult result, Points points) implements RoundRecord {
        @Override
        public Optional<ParticipantId> pairedOpponent() {
            return Optional.of(opponent);
        }
    }

    /** No board: a bye of some kind, a round after a withdrawal, or a round before a late entry arrived. */
    record NoBoard(Bye bye, Points points) implements RoundRecord {}
}
