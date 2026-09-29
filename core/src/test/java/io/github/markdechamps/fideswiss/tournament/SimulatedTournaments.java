package io.github.markdechamps.fideswiss.tournament;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Plays whole tournaments through the library's own API with random results, forfeits and requested byes, and
 * hands each snapshot to the caller just before a round is paired. The input is valid by construction.
 */
public final class SimulatedTournaments {

    private SimulatedTournaments() {}

    /** Plays until the last round or until the system finds no legal pairing (1.9.3). */
    public static void play(
            PairingSystem system, int players, int rounds, long seed, Consumer<Tournament> beforeEachRound) {
        play(Profiles.individualSwiss(NumberOfRounds.of(rounds)).with(system), players, seed, beforeEachRound);
    }

    /** As {@link #play(PairingSystem, int, int, long, Consumer)}, under the given settings (acceleration, say). */
    public static void play(TournamentSettings settings, int players, long seed, Consumer<Tournament> beforeEachRound) {
        var random = new Random(seed);
        var rounds = settings.numberOfRounds().value();
        var tournament = Tournament.of(settings, TournamentMother.participants(players));
        for (var round = 1; round <= rounds; round++) {
            tournament = withRandomAbsences(tournament, random);
            beforeEachRound.accept(tournament);
            try {
                var pairing = tournament.pairNextRound();
                var outcomes = new HashMap<BoardNumber, Outcome>();
                pairing.boards().forEach(board -> outcomes.put(board.number(), randomOutcome(settings, random)));
                tournament = tournament.withRound(pairing.completedWith(outcomes));
            } catch (NoLegalPairingException e) {
                return;
            }
        }
    }

    private static Tournament withRandomAbsences(Tournament tournament, Random random) {
        var result = tournament;
        for (var participant : tournament.participantsToBePaired()) {
            if (random.nextInt(25) == 0) {
                var bye = random.nextBoolean() ? RequestedBye.half() : RequestedBye.zero();
                result = result.requestBye(participant.id(), tournament.nextRound(), bye);
            }
        }
        return result;
    }

    /** A game, or under match scoring a match of as many games (Double-Swiss: two), each drawn independently. */
    private static Outcome randomOutcome(TournamentSettings settings, Random random) {
        var matches = settings.scoring().matches();
        if (matches.isEmpty()) {
            return randomOutcome(random);
        }
        if (random.nextInt(50) == 0) {
            var forfeited =
                    random.nextBoolean() ? GameOutcome.WHITE_WINS_BY_FORFEIT : GameOutcome.BLACK_WINS_BY_FORFEIT;
            return MatchOutcome.ofGames(
                    java.util.Collections.nCopies(matches.get().boards(), forfeited));
        }
        var games = new java.util.ArrayList<GameOutcome>();
        for (var game = 0; game < matches.get().boards(); game++) {
            games.add(random.nextInt(60) == 0 ? rareResult(random) : randomOutcome(random));
        }
        return MatchOutcome.ofGames(games);
    }

    /** The Preface's odd games: ½-0, 0-½, 0-0 and a double forfeit. */
    private static GameOutcome rareResult(Random random) {
        return switch (random.nextInt(4)) {
            case 0 -> GameOutcome.WHITE_HALF_BLACK_ZERO;
            case 1 -> GameOutcome.WHITE_ZERO_BLACK_HALF;
            case 2 -> GameOutcome.BOTH_ZERO;
            default -> GameOutcome.DOUBLE_FORFEIT;
        };
    }

    private static GameOutcome randomOutcome(Random random) {
        var draw = random.nextInt(100);
        if (draw < 4) {
            return random.nextBoolean() ? GameOutcome.WHITE_WINS_BY_FORFEIT : GameOutcome.BLACK_WINS_BY_FORFEIT;
        }
        if (draw < 34) {
            return GameOutcome.DRAW;
        }
        return draw < 70 ? GameOutcome.WHITE_WINS : GameOutcome.BLACK_WINS;
    }
}
