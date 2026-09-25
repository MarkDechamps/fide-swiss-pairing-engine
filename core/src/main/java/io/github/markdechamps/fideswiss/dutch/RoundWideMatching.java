package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.matching.MaximumWeightMatching;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * One bracket seen against the rest of the round: its players, the players below it and, for an odd number, a
 * seat for the PAB. A perfect matching of all of them is a candidate for the bracket (its pairs inside the
 * bracket; its downfloaters, the bracket players matched outside it) that keeps the round completable ([C4], with
 * [C5] folded in). Each edge carries one number that orders matchings as the criteria vector orders candidates,
 * so the cheapest matching is the best candidate.
 *
 * <p>Only [C9] is not a sum over edges; the optimum finder handles it by fixing the one downfloater.
 */
final class RoundWideMatching {

    /** The failures a pair inside the bracket, or a downfloater, adds to each criterion. */
    interface Contributions {
        List<Failure> ofPair(Pair pair);

        List<Failure> ofDownfloater(Player downfloater);
    }

    /** Which player of a pair is the S1 player: in a heterogeneous bracket, the MDP. */
    interface PairOrientation {
        Pair pairOf(Player a, Player b);
    }

    /**
     * A branch of the bracket: {@code open} players not yet paired, who of them may pair with whom and who may
     * float, and the players bound to float ({@code floating}, the Limbo).
     */
    record Layout(
            List<Player> open,
            BiPredicate<Player, Player> mayPairInBracket,
            Predicate<Player> mayFloat,
            List<Player> floating) {

        boolean mayDownfloat(Player player) {
            return floating.contains(player) || mayFloat.test(player);
        }
    }

    record Solution(List<Pair> pairsInBracket, List<Player> downfloaters) {}

    /** The players and rules the matching is built from. */
    record Setting(
            List<CandidateCriterion> criteria,
            Contributions contributions,
            BiPredicate<Player, Player> mayMeet,
            Predicate<Player> mayTakePairingAllocatedBye,
            PairOrientation orientation) {}

    private final Setting setting;
    private final List<Player> bracketPlayers;
    private final List<Player> below;
    private final List<Player> nextResidents;
    private final Map<Player, Integer> indexes = new IdentityHashMap<>();
    private BigInteger[][] costs;
    private BigInteger ceiling;

    RoundWideMatching(Setting setting, List<Player> bracketPlayers, List<Player> below, List<Player> nextResidents) {
        this.setting = setting;
        this.bracketPlayers = bracketPlayers;
        this.below = below;
        this.nextResidents = nextResidents;
        for (var player : bracketPlayers) {
            indexes.put(player, indexes.size());
        }
        for (var player : below) {
            indexes.put(player, indexes.size());
        }
    }

    Optional<Solution> cheapest(Layout layout) {
        writeCostsOnce();
        var players = new ArrayList<Player>(layout.open());
        players.addAll(layout.floating());
        players.addAll(below);
        var withPabSeat = players.size() % 2 == 1;
        var seat = indexes.size();
        var edges = new ArrayList<MaximumWeightMatching.Edge>();
        for (var a = 0; a < players.size(); a++) {
            var first = indexes.get(players.get(a));
            for (var b = a + 1; b < players.size(); b++) {
                if (mayBeMatched(layout, players.get(a), players.get(b))) {
                    edges.add(new MaximumWeightMatching.Edge(a, b, weightOf(first, indexes.get(players.get(b)))));
                }
            }
            if (withPabSeat && mayTakeSeat(layout, players.get(a))) {
                edges.add(new MaximumWeightMatching.Edge(a, players.size(), weightOf(first, seat)));
            }
        }
        var matching = MaximumWeightMatching.of(players.size() + (withPabSeat ? 1 : 0), edges);
        return matching.isPerfect() ? Optional.of(solutionOf(players, matching.mates())) : Optional.empty();
    }

    /** The matching maximises weight, so the cheapest edge weighs the most. */
    private BigInteger weightOf(int a, int b) {
        return ceiling.subtract(costs[a][b]);
    }

    private boolean isBracketPlayer(Player player) {
        return indexes.get(player) < bracketPlayers.size();
    }

    private boolean mayBeMatched(Layout layout, Player a, Player b) {
        var aInside = isBracketPlayer(a);
        var bInside = isBracketPlayer(b);
        if (aInside && bInside) {
            return layout.open().contains(a)
                    && layout.open().contains(b)
                    && layout.mayPairInBracket().test(a, b);
        }
        if (aInside && !layout.mayDownfloat(a) || bInside && !layout.mayDownfloat(b)) {
            return false;
        }
        return setting.mayMeet().test(a, b);
    }

    private boolean mayTakeSeat(Layout layout, Player player) {
        return setting.mayTakePairingAllocatedBye().test(player)
                && (!isBracketPlayer(player) || layout.mayDownfloat(player));
    }

    private Solution solutionOf(List<Player> players, int[] mate) {
        var pairs = new ArrayList<Pair>();
        var downfloaters = new ArrayList<Player>();
        for (var vertex = 0; vertex < players.size(); vertex++) {
            var player = players.get(vertex);
            if (!isBracketPlayer(player)) {
                continue;
            }
            var partner = mate[vertex];
            if (partner < players.size() && isBracketPlayer(players.get(partner))) {
                if (vertex < partner) {
                    pairs.add(setting.orientation().pairOf(player, players.get(partner)));
                }
            } else {
                downfloaters.add(player);
            }
        }
        downfloaters.sort(PairingOrder.RANKING);
        return new Solution(List.copyOf(pairs), List.copyOf(downfloaters));
    }

    /** Every possible edge's cost, written once per bracket so that all its branches compare alike. */
    private void writeCostsOnce() {
        if (costs != null) {
            return;
        }
        var everyone = new ArrayList<>(bracketPlayers);
        everyone.addAll(below);
        var seat = everyone.size();
        var parts = new Part[seat + 1][seat + 1][];
        for (var a = 0; a < seat; a++) {
            for (var b = a + 1; b < seat; b++) {
                parts[a][b] = parts[b][a] = partsOfMeeting(everyone.get(a), everyone.get(b));
            }
            parts[a][seat] = parts[seat][a] = partsOfSeat(everyone.get(a));
        }
        costs = new Encoding(parts).costs();
        var highest = BigInteger.ZERO;
        for (var row : costs) {
            for (var cost : row) {
                if (cost != null) {
                    highest = highest.max(cost);
                }
            }
        }
        ceiling = highest.add(BigInteger.ONE);
    }

    /** What matching a and b adds, criterion by criterion. */
    private Part[] partsOfMeeting(Player a, Player b) {
        var aInside = isBracketPlayer(a);
        var bInside = isBracketPlayer(b);
        if (aInside && bInside) {
            return partsOf(setting.contributions().ofPair(setting.orientation().pairOf(a, b)), List.of());
        }
        if (aInside || bInside) {
            var downfloater = aInside ? a : b;
            var opponent = aInside ? b : a;
            var leftByFollowing = nextResidents.contains(opponent) ? List.<Player>of() : List.of(downfloater);
            return partsOf(setting.contributions().ofDownfloater(downfloater), leftByFollowing);
        }
        var aNext = nextResidents.contains(a);
        var bNext = nextResidents.contains(b);
        return partsOf(List.of(), aNext == bNext ? List.of() : List.of(aNext ? a : b));
    }

    private Part[] partsOfSeat(Player player) {
        if (isBracketPlayer(player)) {
            return partsOf(setting.contributions().ofDownfloater(player), List.of(player));
        }
        return partsOf(List.of(), nextResidents.contains(player) ? List.of(player) : List.of());
    }

    /**
     * The parts per criterion: the given failures (none for an edge outside the bracket), and for [C8] the players
     * of the following bracket this edge leaves unpaired there ([ANN p.25], bbpPairings).
     */
    private Part[] partsOf(List<Failure> failures, List<Player> leftByFollowing) {
        var criteria = setting.criteria();
        var leftUnpairedByFollowing = nextResidents.isEmpty() ? List.<Player>of() : leftByFollowing;
        var parts = new Part[criteria.size()];
        for (var index = 0; index < criteria.size(); index++) {
            parts[index] = switch (criteria.get(index).scope()) {
                case COUNT ->
                    new Part(failures.isEmpty() ? 0 : failures.get(index).countValue(), List.of());
                case SCORES ->
                    new Part(
                            0,
                            failures.isEmpty() ? List.of() : failures.get(index).values());
                case FOLLOWING_BRACKET ->
                    new Part(
                            leftUnpairedByFollowing.size(),
                            leftUnpairedByFollowing.stream()
                                    .map(player -> player.score().points().toBigDecimal())
                                    .toList());
                case PAB_SCORE, SINGLE_DOWNFLOATER -> new Part(0, List.of());
            };
        }
        return parts;
    }

    /** One criterion's share of an edge: a count, and scores "taken in descending order". */
    private record Part(int count, List<BigDecimal> scores) {}

    /**
     * Writes each edge's parts as one number, criterion by criterion, most significant first. A count is one
     * digit. Scores are one digit per distinct score, highest first: two lists of equal length compare as their
     * counts of the highest score, then of the next, and so on. [C8] is a count, then scores. Each digit's base
     * exceeds what all the edges of one matching can add up to.
     */
    private final class Encoding {

        private final Part[][][] parts;
        private final BigInteger base;
        private final List<List<BigDecimal>> alphabets = new ArrayList<>();

        Encoding(Part[][][] parts) {
            this.parts = parts;
            this.base = BigInteger.valueOf(2L * parts.length + 1);
            for (var criterion = 0; criterion < setting.criteria().size(); criterion++) {
                var scores = new TreeSet<BigDecimal>(Comparator.reverseOrder());
                for (var row : parts) {
                    for (var edge : row) {
                        if (edge != null) {
                            scores.addAll(edge[criterion].scores());
                        }
                    }
                }
                alphabets.add(List.copyOf(scores));
            }
        }

        BigInteger[][] costs() {
            var costs = new BigInteger[parts.length][parts.length];
            for (var a = 0; a < parts.length; a++) {
                for (var b = 0; b < parts.length; b++) {
                    if (parts[a][b] != null) {
                        costs[a][b] = numberOf(parts[a][b]);
                    }
                }
            }
            return costs;
        }

        private BigInteger numberOf(Part[] edge) {
            var number = BigInteger.ZERO;
            for (var criterion = 0; criterion < setting.criteria().size(); criterion++) {
                var part = edge[criterion];
                if (hasCountDigit(criterion)) {
                    number = number.multiply(base).add(BigInteger.valueOf(part.count()));
                }
                for (var score : alphabets.get(criterion)) {
                    var count = part.scores().stream()
                            .filter(value -> value.compareTo(score) == 0)
                            .count();
                    number = number.multiply(base).add(BigInteger.valueOf(count));
                }
            }
            return number;
        }

        private boolean hasCountDigit(int criterion) {
            var scope = setting.criteria().get(criterion).scope();
            return scope == CandidateCriterion.Scope.COUNT || scope == CandidateCriterion.Scope.FOLLOWING_BRACKET;
        }
    }
}
