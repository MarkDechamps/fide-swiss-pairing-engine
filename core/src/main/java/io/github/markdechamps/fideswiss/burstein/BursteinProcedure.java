package io.github.markdechamps.fideswiss.burstein;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;

/**
 * 1.9.2: the Pairing-Allocated Bye first (3.1), then the top scoregroup and bracket by bracket down (3.2), then the
 * colours (Article 5).
 */
final class BursteinProcedure {

    /** What one bracket chose: its incoming floaters, residents, pairs and outgoing floaters, for the trace. */
    record BracketChoice(List<Player> incoming, List<Player> residents, List<Pair> pairs, List<Player> outgoing) {}

    record Outcome(List<Game> games, Optional<Player> pairingAllocatedBye, List<BracketChoice> brackets) {}

    private final RoundToPair round;

    BursteinProcedure(RoundToPair round) {
        this.round = round;
    }

    Outcome pair(List<Player> players) {
        var pairingAllocatedBye = new PairingAllocatedByeAssignment(round).assign(players);
        var unpaired = new ArrayList<>(players.stream()
                .filter(player ->
                        pairingAllocatedBye.filter(bye -> bye == player).isEmpty())
                .toList());
        var brackets = new BracketPairing(round);
        var colours = new ColourAllocation(round);
        var games = new ArrayList<Game>();
        var choices = new ArrayList<BracketChoice>();
        List<Player> incoming = List.of();
        while (!unpaired.isEmpty()) {
            var arriving = incoming;
            var bracket = bracket(unpaired, arriving);
            var residents = bracket.stream()
                    .filter(player -> !arriving.contains(player))
                    .toList();
            var lower = unpaired.stream()
                    .filter(player -> !bracket.contains(player))
                    .toList();
            var result = brackets.pair(bracket, lower);
            result.pairs().forEach(pair -> games.add(colours.allocate(pair)));
            choices.add(new BracketChoice(arriving, residents, result.pairs(), result.outgoing()));
            var paired = Collections.newSetFromMap(new IdentityHashMap<Player, Boolean>());
            result.pairs().forEach(pair -> {
                paired.add(pair.first());
                paired.add(pair.second());
            });
            unpaired.removeIf(paired::contains);
            incoming = result.outgoing();
        }
        return new Outcome(List.copyOf(games), pairingAllocatedBye, List.copyOf(choices));
    }

    /** 1.2.2 and 4.1: the incoming floaters and the highest scoregroup left, ranked by 1.8. */
    static List<Player> bracket(List<Player> unpaired, List<Player> incoming) {
        var score = unpaired.stream()
                .filter(player -> !incoming.contains(player))
                .map(Player::pairingScore)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("Incoming floaters with no scoregroup left"));
        var bracket = new ArrayList<>(incoming);
        unpaired.stream()
                .filter(player ->
                        !incoming.contains(player) && player.pairingScore().equals(score))
                .forEach(bracket::add);
        bracket.sort(Player.RANKING);
        return List.copyOf(bracket);
    }
}
