package io.github.markdechamps.fideswiss.burstein;

import io.github.markdechamps.fideswiss.tournament.PairingScore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The Literal Enumerator of C.04.4.2: for every bracket, every pairing with every number of virtual players in the
 * order of 4.3, each assessed on [C1]–[C8] by exhaustion ([C4] and [C7] by trying every pairing of the players
 * below), the first best kept. No matching anywhere; only for small fields.
 */
final class LiteralBurstein {

    /** 4.1–4.3: one candidate, for each BSN (index 0 is BSN #1) its opponent's BSN, 0 for a virtual player. */
    record Candidate(int[] opponents) {

        @Override
        public String toString() {
            var out = new ArrayList<String>();
            for (var bsn = 1; bsn <= opponents.length; bsn++) {
                var opponent = opponents[bsn - 1];
                if (opponent == 0 || opponent > bsn) {
                    out.add(bsn + "-" + opponent);
                }
            }
            return String.join(", ", out);
        }
    }

    /** Lower is better, criterion by criterion. */
    private record Quality(int c5, List<PairingScore> c6, int c7c5, List<PairingScore> c7c6, long c8)
            implements Comparable<Quality> {

        @Override
        public int compareTo(Quality other) {
            return Comparator.comparingInt(Quality::c5)
                    .thenComparing(Quality::c6, LiteralBurstein::compareScores)
                    .thenComparingInt(Quality::c7c5)
                    .thenComparing(Quality::c7c6, LiteralBurstein::compareScores)
                    .thenComparingLong(Quality::c8)
                    .compare(this, other);
        }
    }

    private record Onward(int c5, List<PairingScore> c6) {}

    private final RoundToPair round;
    private final List<Player> players = new ArrayList<>();
    private final Map<Long, Boolean> pairable = new HashMap<>();

    LiteralBurstein(RoundToPair round) {
        this.round = round;
    }

    /** 4.3 in order: BSN #1's opponent from the largest BSN down, a virtual player (BSN 0) last; then BSN #2. */
    static List<Candidate> candidates(int bracketSize, int virtualPlayers) {
        var out = new ArrayList<Candidate>();
        var opponents = new int[bracketSize];
        Arrays.fill(opponents, -1);
        each(opponents, virtualPlayers, out::add);
        return out;
    }

    BursteinProcedure.Outcome pair(List<Player> field) {
        players.clear();
        players.addAll(field);
        var bye = pairingAllocatedBye();
        var unpaired = new ArrayList<>(field.stream()
                .filter(player -> bye.filter(player::equals).isEmpty())
                .toList());
        var colours = new ColourAllocation(round);
        var games = new ArrayList<Game>();
        List<Player> incoming = List.of();
        while (!unpaired.isEmpty()) {
            var bracket = BursteinProcedure.bracket(unpaired, incoming);
            var lower = unpaired.stream()
                    .filter(player -> !bracket.contains(player))
                    .toList();
            var best = bestPairing(bracket, lower);
            var outgoing = new ArrayList<Player>();
            for (var bsn = 1; bsn <= bracket.size(); bsn++) {
                var opponent = best.opponents()[bsn - 1];
                if (opponent == 0) {
                    outgoing.add(bracket.get(bsn - 1));
                } else if (opponent > bsn) {
                    games.add(colours.allocate(Pair.of(bracket.get(bsn - 1), bracket.get(opponent - 1))));
                }
            }
            unpaired.removeIf(player -> bracket.contains(player) && !outgoing.contains(player));
            incoming = outgoing;
        }
        return new BursteinProcedure.Outcome(games, bye, List.of());
    }

    /** 3.1, with [C4] by exhaustion. */
    private Optional<Player> pairingAllocatedBye() {
        if (players.size() % 2 == 0) {
            return Optional.empty();
        }
        var order = Comparator.comparing(Player::pairingScore)
                .thenComparing(Comparator.comparingInt(Player::gamesPlayed).reversed())
                .thenComparing(Player.RANKING.reversed());
        return Optional.of(players.stream()
                .filter(Player::mayReceivePairingAllocatedBye)
                .sorted(order)
                .filter(bye -> canPairAll(
                        players.stream().filter(player -> player != bye).toList()))
                .findFirst()
                .orElseThrow(() -> new NoRoundPairingException("C.04.4.2 3.1", "no bye")));
    }

    /** 3.2.2: the first pairing in the order of 4.3 that complies best with [C1]–[C8]. */
    private Candidate bestPairing(List<Player> bracket, List<Player> lower) {
        var onwardCache = new HashMap<List<Player>, Optional<Onward>>();
        Candidate[] best = {null};
        Quality[] bestQuality = {null};
        for (var virtuals = bracket.size() % 2; virtuals <= bracket.size() && best[0] == null; virtuals += 2) {
            var opponents = new int[bracket.size()];
            Arrays.fill(opponents, -1);
            each(opponents, virtuals, candidate -> {
                var quality = assess(candidate, bracket, lower, onwardCache);
                if (quality.isPresent()
                        && (bestQuality[0] == null || quality.get().compareTo(bestQuality[0]) < 0)) {
                    best[0] = candidate;
                    bestQuality[0] = quality.get();
                }
            });
        }
        if (best[0] == null) {
            throw new NoRoundPairingException("C.04.4.2 1.9.3", "bracket cannot be paired");
        }
        return best[0];
    }

    private Optional<Quality> assess(
            Candidate candidate, List<Player> bracket, List<Player> lower, Map<List<Player>, Optional<Onward>> cache) {
        var outgoing = new ArrayList<Player>();
        var c8 = 0L;
        for (var bsn = 1; bsn <= bracket.size(); bsn++) {
            var opponent = candidate.opponents()[bsn - 1];
            var player = bracket.get(bsn - 1);
            if (opponent == 0) {
                outgoing.add(player);
            } else if (opponent > bsn) {
                var other = bracket.get(opponent - 1);
                if (!round.mayMeet(player, other)) {
                    return Optional.empty();
                }
                c8 += RoundToPair.sameColourWanted(player, other);
            }
        }
        var onward = cache.computeIfAbsent(List.copyOf(outgoing), floaters -> followingBracket(floaters, lower));
        var sameColour = c8;
        return onward.map(following ->
                new Quality(outgoing.size(), descendingScores(outgoing), following.c5(), following.c6(), sameColour));
    }

    /** [C7]: the best [C5] and [C6] the following bracket reaches with these incoming floaters, [C4] kept. */
    private Optional<Onward> followingBracket(List<Player> incoming, List<Player> lower) {
        if (lower.isEmpty()) {
            return incoming.isEmpty() ? Optional.of(new Onward(0, List.of())) : Optional.empty();
        }
        var unpaired = new ArrayList<>(incoming);
        unpaired.addAll(lower);
        var next = BursteinProcedure.bracket(unpaired, incoming);
        var rest = lower.stream().filter(player -> !next.contains(player)).toList();
        Onward[] best = {null};
        for (var virtuals = next.size() % 2; virtuals <= next.size() && best[0] == null; virtuals += 2) {
            var opponents = new int[next.size()];
            Arrays.fill(opponents, -1);
            each(opponents, virtuals, candidate -> {
                var outgoing = new ArrayList<Player>();
                for (var bsn = 1; bsn <= next.size(); bsn++) {
                    var opponent = candidate.opponents()[bsn - 1];
                    if (opponent == 0) {
                        outgoing.add(next.get(bsn - 1));
                    } else if (opponent > bsn && !round.mayMeet(next.get(bsn - 1), next.get(opponent - 1))) {
                        return;
                    }
                }
                var remaining = new ArrayList<>(outgoing);
                remaining.addAll(rest);
                if (!canPairAll(remaining)) {
                    return;
                }
                var onward = new Onward(outgoing.size(), descendingScores(outgoing));
                if (best[0] == null || compareScores(onward.c6(), best[0].c6()) < 0) {
                    best[0] = onward;
                }
            });
        }
        return Optional.ofNullable(best[0]);
    }

    private static void each(int[] opponents, int virtuals, Consumer<Candidate> consumer) {
        var bsn = 1;
        while (bsn <= opponents.length && opponents[bsn - 1] != -1) {
            bsn++;
        }
        if (bsn > opponents.length) {
            if (virtuals == 0) {
                consumer.accept(new Candidate(opponents.clone()));
            }
            return;
        }
        for (var opponent = opponents.length; opponent > bsn; opponent--) {
            if (opponents[opponent - 1] == -1) {
                opponents[bsn - 1] = opponent;
                opponents[opponent - 1] = bsn;
                each(opponents, virtuals, consumer);
                opponents[bsn - 1] = -1;
                opponents[opponent - 1] = -1;
            }
        }
        if (virtuals > 0) {
            opponents[bsn - 1] = 0;
            each(opponents, virtuals - 1, consumer);
            opponents[bsn - 1] = -1;
        }
    }

    /** [C6]: the floaters' scores in descending order, compared as sequences; the smaller is better. */
    private static int compareScores(List<PairingScore> a, List<PairingScore> b) {
        for (var index = 0; index < Math.min(a.size(), b.size()); index++) {
            var byScore = a.get(index).compareTo(b.get(index));
            if (byScore != 0) {
                return byScore;
            }
        }
        return Integer.compare(a.size(), b.size());
    }

    private static List<PairingScore> descendingScores(List<Player> floaters) {
        return floaters.stream()
                .map(Player::pairingScore)
                .sorted(Collections.reverseOrder())
                .toList();
    }

    /** [C4] by exhaustion: can these players all be paired under [C1]–[C3]? */
    private boolean canPairAll(List<Player> group) {
        var index = new IdentityHashMap<Player, Integer>();
        for (var position = 0; position < players.size(); position++) {
            index.put(players.get(position), position);
        }
        var mask = 0L;
        for (var player : group) {
            mask |= 1L << index.get(player);
        }
        return canPairAll(mask);
    }

    private boolean canPairAll(long mask) {
        if (mask == 0) {
            return true;
        }
        var cached = pairable.get(mask);
        if (cached != null) {
            return cached;
        }
        var first = Long.numberOfTrailingZeros(mask);
        var result = false;
        for (var other = first + 1; other < players.size() && !result; other++) {
            if ((mask & (1L << other)) != 0 && round.mayMeet(players.get(first), players.get(other))) {
                result = canPairAll(mask & ~(1L << first) & ~(1L << other));
            }
        }
        pairable.put(mask, result);
        return result;
    }
}
