package io.github.markdechamps.fideswiss.dutch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * The exact best criteria vector any candidate of a bracket can reach (Article 3.4's "perfect"), and, for a
 * branch of the candidate sequence, whether any candidate in it still reaches that vector.
 *
 * <p>Both questions are one {@link RoundWideMatching}. The finder never picks the pairing: the literal sequence
 * does, and the finder only tells it where the target can no longer be reached.
 */
final class OptimumFinder {

    private final RoundWideMatching matching;
    private final Function<Candidate, CriteriaVector> vectorOf;
    private final List<Integer> singleDownfloaterCriteria;
    private final List<Candidate> knownOptimal = new ArrayList<>();

    OptimumFinder(
            List<CandidateCriterion> criteria,
            RoundWideMatching matching,
            Function<Candidate, CriteriaVector> vectorOf) {
        this.singleDownfloaterCriteria = IntStream.range(0, criteria.size())
                .filter(index -> criteria.get(index).scope() == CandidateCriterion.Scope.SINGLE_DOWNFLOATER)
                .boxed()
                .toList();
        this.matching = matching;
        this.vectorOf = vectorOf;
    }

    /** The target, and one candidate that reaches it. */
    record Optimum(CriteriaVector vector, Candidate candidate) {}

    /**
     * A branch of the candidate sequence: the pairs it has fixed, the bracket players still open, who of them may
     * still be paired with whom, who of them may still float, and who floats already (the Limbo).
     */
    record Branch(
            List<Pair> fixed,
            List<Player> open,
            BiPredicate<Player, Player> mayPair,
            Collection<Player> mayFloat,
            List<Player> floating) {}

    Optional<Optimum> optimum(Branch wholeBracket) {
        return best(wholeBracket);
    }

    /**
     * A candidate known to reach the target proves every branch that contains it reachable, so only branches no
     * known optimal candidate lies in need a matching of their own.
     */
    boolean canReach(Optimum optimum, Branch branch) {
        if (knownOptimal.isEmpty()) {
            knownOptimal.add(optimum.candidate());
        }
        if (knownOptimal.stream().anyMatch(known -> contains(branch, known))) {
            return true;
        }
        var found = best(branch).filter(candidate -> candidate.vector().equals(optimum.vector()));
        found.ifPresent(reached -> knownOptimal.add(reached.candidate()));
        return found.isPresent();
    }

    private static boolean contains(Branch branch, Candidate known) {
        var knownPairs = known.pairs().stream().map(OptimumFinder::players).collect(Collectors.toSet());
        var fixedPairs = branch.fixed().stream().map(OptimumFinder::players).collect(Collectors.toSet());
        if (!knownPairs.containsAll(fixedPairs)) {
            return false;
        }
        var floatingAllowed = known.downfloaters().containsAll(branch.floating())
                && known.downfloaters().stream()
                        .allMatch(player -> branch.floating().contains(player)
                                || branch.mayFloat().contains(player));
        return floatingAllowed
                && known.pairs().stream()
                        .filter(pair -> !fixedPairs.contains(players(pair)))
                        .allMatch(pair -> branch.open().contains(pair.s1Player())
                                && branch.open().contains(pair.s2Player())
                                && branch.mayPair().test(pair.s1Player(), pair.s2Player()));
    }

    /**
     * The best candidate of the branch. The matching is exact on every criterion but [C9], which counts only when
     * a single player floats and is bound to receive the PAB. If the cheapest candidate fails [C9], each possible
     * single downfloater is tried on its own. (A Limbo player floats anyway.)
     */
    private Optional<Optimum> best(Branch branch) {
        var cheapest = cheapestWith(branch, branch.mayFloat());
        if (cheapest.isEmpty() || !branch.floating().isEmpty() || !failsSingleDownfloaterCriterion(cheapest.get())) {
            return cheapest;
        }
        return branch.mayFloat().stream()
                .map(only -> cheapestWith(branch, List.of(only)))
                .flatMap(Optional::stream)
                .min(Comparator.comparing(Optimum::vector));
    }

    private boolean failsSingleDownfloaterCriterion(Optimum found) {
        return singleDownfloaterCriteria.stream()
                .anyMatch(index -> !found.vector().failures().get(index).isZero());
    }

    private static Set<Player> players(Pair pair) {
        return Set.of(pair.s1Player(), pair.s2Player());
    }

    private Optional<Optimum> cheapestWith(Branch branch, Collection<Player> mayFloat) {
        var layout =
                new RoundWideMatching.Layout(branch.open(), branch.mayPair(), mayFloat::contains, branch.floating());
        return matching.cheapest(layout).map(solution -> {
            var pairs = new ArrayList<>(branch.fixed());
            pairs.addAll(solution.pairsInBracket());
            var candidate = new Candidate(pairs, solution.downfloaters());
            return new Optimum(vectorOf.apply(candidate), candidate);
        });
    }
}
