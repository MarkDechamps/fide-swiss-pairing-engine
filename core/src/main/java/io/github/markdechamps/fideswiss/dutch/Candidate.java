package io.github.markdechamps.fideswiss.dutch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/** Article 3.3: the pairs of a bracket and the players it leaves to float down. */
record Candidate(List<Pair> pairs, List<Player> downfloaters) {

    Candidate {
        pairs = List.copyOf(pairs);
        downfloaters = List.copyOf(downfloaters);
    }

    /** S1[i] against S2[i]; whatever of S2 is left over floats down. */
    static Candidate fromSubgroups(List<Player> s1, List<Player> s2InOrder) {
        var pairs = IntStream.range(0, s1.size())
                .mapToObj(index -> new Pair(s1.get(index), s2InOrder.get(index)))
                .toList();
        return new Candidate(pairs, s2InOrder.subList(s1.size(), s2InOrder.size()));
    }

    static Candidate onlyDownfloaters(List<Player> downfloaters) {
        return new Candidate(List.of(), downfloaters);
    }

    /** Article 3.3.4: an MDP-Pairing plus a candidate for its remainder; the Limbo floats too. */
    static Candidate heterogeneous(List<Pair> mdpPairing, Candidate remainder, List<Player> limbo) {
        var pairs = new ArrayList<>(mdpPairing);
        pairs.addAll(remainder.pairs());
        var downfloaters = new ArrayList<>(limbo);
        downfloaters.addAll(remainder.downfloaters());
        return new Candidate(
                pairs, downfloaters.stream().sorted(PairingOrder.RANKING).toList());
    }
}
