package io.github.markdechamps.fideswiss.dubov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;

/**
 * 1.9.2: the Pairing-Allocated Bye first (3.1), then every scoregroup of the players still unpaired, in
 * descending score, with its upfloaters (3.2), then the colours (Article 5).
 */
final class DubovProcedure {

    /** What one bracket chose: its residents, upfloaters and shifted players, for the trace. */
    record BracketChoice(List<Player> residents, List<Player> upfloaters, List<Player> shifted, List<Pair> pairs) {}

    record Outcome(List<Game> games, Optional<Player> pairingAllocatedBye, List<BracketChoice> brackets) {}

    private final RoundToPair round;

    DubovProcedure(RoundToPair round) {
        this.round = round;
    }

    Outcome pair(List<Player> players) {
        var pairingAllocatedBye = new PairingAllocatedByeAssignment(round).assign(players);
        var unpaired = new ArrayList<>(players.stream()
                .filter(player ->
                        pairingAllocatedBye.filter(bye -> bye == player).isEmpty())
                .toList());
        var upfloaters = new UpfloaterSelection(round);
        var brackets = new BracketPairing(round);
        var colours = new ColourAllocation(round);
        var games = new ArrayList<Game>();
        var choices = new ArrayList<BracketChoice>();
        while (!unpaired.isEmpty()) {
            var score = unpaired.stream()
                    .map(Player::score)
                    .max(Comparable::compareTo)
                    .orElseThrow();
            var residents = unpaired.stream()
                    .filter(player -> player.score().equals(score))
                    .sorted(UpfloaterSelection.SEQUENCE)
                    .toList();
            var lower = unpaired.stream()
                    .filter(player -> score.isHigherThan(player.score()))
                    .sorted(UpfloaterSelection.SEQUENCE)
                    .toList();
            var choice = upfloaters.select(residents, lower);
            var bracket = new ArrayList<>(residents);
            bracket.addAll(choice.upfloaters());
            var result = brackets.pair(bracket);
            if (result.sameColourPairs() != choice.sameColourPairs()) {
                throw new IllegalStateException("The bracket's [C7] differs from the target: " + bracket);
            }
            result.pairs().forEach(pair -> games.add(colours.allocate(pair)));
            choices.add(new BracketChoice(residents, choice.upfloaters(), result.shifted(), result.pairs()));
            var paired = Collections.newSetFromMap(new IdentityHashMap<Player, Boolean>());
            paired.addAll(bracket);
            unpaired.removeIf(paired::contains);
        }
        return new Outcome(List.copyOf(games), pairingAllocatedBye, List.copyOf(choices));
    }
}
