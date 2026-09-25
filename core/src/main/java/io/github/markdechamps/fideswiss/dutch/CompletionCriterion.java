package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.PairingScore;
import java.util.BitSet;
import java.util.Optional;

/**
 * [C4]: a pairing complying with [C1]–[C3] must always exist for the players not yet paired, where one of an
 * odd number may be left over for the PAB if [C2] allows it.
 *
 * <p>[C5] outranks every quality criterion, and the lowest PAB score the round can reach is known before the
 * first bracket is paired. So [C5] folds in here: every bracket must keep that score reachable, even at the cost
 * of extra downfloaters (a documented reading, verified against bbpPairings).
 */
final class CompletionCriterion {

    private final PlayerSet players;
    private final CompletionOracle oracle;
    private final AbsoluteCriteria absolute;
    private final Optional<PairingScore> lowestReachablePabScore;

    private CompletionCriterion(
            PlayerSet players,
            AbsoluteCriteria absolute,
            CompletionOracle oracle,
            Optional<PairingScore> lowestReachablePabScore) {
        this.players = players;
        this.absolute = absolute;
        this.oracle = oracle;
        this.lowestReachablePabScore = lowestReachablePabScore;
    }

    static CompletionCriterion forRound(PlayerSet players, AbsoluteCriteria absolute) {
        var oracle = new CompletionOracle(players, absolute::mayMeet);
        var anyScore = new CompletionCriterion(players, absolute, oracle, Optional.empty());
        var everyone = players.maskOf(players.inPairingOrder());
        var lowest =
                anyScore.lowestPossiblePairingAllocatedByeAssignee(everyone).map(Player::score);
        return new CompletionCriterion(players, absolute, oracle, lowest);
    }

    boolean isSatisfiedBy(BitSet notYetPaired) {
        return notYetPaired.cardinality() % 2 == 0
                ? oracle.canPairAll(notYetPaired)
                : lowestPossiblePairingAllocatedByeAssignee(notYetPaired).isPresent();
    }

    /** Whether any complete pairing of the round exists at all. */
    boolean isRoundCompletable() {
        return isSatisfiedBy(players.maskOf(players.inPairingOrder()));
    }

    /** The lowest-ranked player who can take the PAB with everyone else still pairable ([C5] look-ahead). */
    Optional<Player> lowestPossiblePairingAllocatedByeAssignee(BitSet notYetPaired) {
        if (notYetPaired.cardinality() % 2 == 0) {
            return Optional.empty();
        }
        for (var player : players.inPairingOrder().reversed()) {
            var index = players.indexOf(player);
            if (notYetPaired.get(index)
                    && mayTakePairingAllocatedBye(player)
                    && oracle.canPairAll(PlayerSet.without(notYetPaired, index))) {
                return Optional.of(player);
            }
        }
        return Optional.empty();
    }

    /** [C2], and no higher score than the lowest the round can reach ([C5]). */
    boolean mayTakePairingAllocatedBye(Player player) {
        return absolute.mayReceivePairingAllocatedBye(player)
                && lowestReachablePabScore
                        .map(lowest -> !player.score().isHigherThan(lowest))
                        .orElse(true);
    }
}
