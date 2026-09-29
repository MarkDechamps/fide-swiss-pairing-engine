package io.github.markdechamps.fideswiss.topscoregroup;

import io.github.markdechamps.fideswiss.pairing.TraceStep;
import io.github.markdechamps.fideswiss.tournament.Points;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Top-Scoregroup Procedure shared word for word by the Swiss Team (C.04.6) and Double-Swiss (C.04.5) Systems,
 * 3.3–3.6: the PAB first, then repeatedly the top-scoregroup of those still unpaired plus the first set of
 * upfloaters, paired by the first Pairing Identifier. Colours are the system's own business, afterwards.
 */
public final class TopScoregroupProcedure {

    /** One bracket: its residents, the upfloaters chosen for it, and its pairs. */
    public record Bracket(List<Contender> residents, List<Contender> upfloaters, List<ContenderPair> pairs) {

        /** The bracket as a trace step, with how often its pairs fail each of the system's bracket criteria. */
        public TraceStep traceStep(List<PairCriterion> criteria) {
            var floating = Set.copyOf(upfloaters);
            var failed = criteria.stream()
                    .map(criterion -> failures(criterion, floating))
                    .filter(failure -> !failure.isEmpty())
                    .collect(Collectors.joining(", "));
            return new TraceStep.TopScoregroupBracket(
                    residents.getFirst().score().toString(),
                    residents.stream().map(Contender::id).toList(),
                    upfloaters.stream().map(Contender::id).toList(),
                    pairs.stream()
                            .map(pair -> List.of(pair.top().id(), pair.bottom().id()))
                            .toList(),
                    failed);
        }

        private String failures(PairCriterion criterion, Set<Contender> floating) {
            var count = pairs.stream()
                    .mapToLong(pair -> criterion.failureOf(pair, floating))
                    .sum();
            return count == 0 ? "" : "[" + criterion.article() + "] " + count;
        }
    }

    /** The round-pairing before colours; empty from {@link #pair} when it cannot be completed (3.3.3). */
    public record RoundPairing(Optional<Contender> pairingAllocatedBye, List<Bracket> brackets) {

        public List<ContenderPair> pairs() {
            return brackets.stream()
                    .flatMap(bracket -> bracket.pairs().stream())
                    .toList();
        }
    }

    private TopScoregroupProcedure() {}

    public static Optional<RoundPairing> pair(TopScoregroupRound round) {
        var unpaired = new ArrayList<>(round.contenders());
        Optional<Contender> bye = Optional.empty();
        if (unpaired.size() % 2 == 1) {
            bye = PairingAllocatedByeAssignment.assign(unpaired);
            if (bye.isEmpty()) {
                return Optional.empty();
            }
            unpaired.remove(bye.get());
        } else if (!CheapestPerfectMatching.canPairAll(unpaired)) {
            return Optional.empty();
        }
        var selection = new UpfloaterSelection(round);
        var bracketPairing = new BracketPairing(round);
        var brackets = new ArrayList<Bracket>();
        while (!unpaired.isEmpty()) {
            var topScore = unpaired.stream()
                    .map(Contender::score)
                    .max(Comparator.naturalOrder())
                    .orElseThrow();
            var residents = scoring(unpaired, topScore);
            var lower = unpaired.stream()
                    .filter(contender -> contender.score().isLessThan(topScore))
                    .toList();
            var upfloaters = selection.select(residents, lower);
            if (upfloaters.isEmpty()) {
                return Optional.empty();
            }
            var pairs = bracketPairing.pair(residents, upfloaters.get());
            brackets.add(new Bracket(residents, upfloaters.get(), pairs));
            unpaired.removeAll(residents);
            unpaired.removeAll(upfloaters.get());
        }
        return Optional.of(new RoundPairing(bye, brackets));
    }

    private static List<Contender> scoring(List<Contender> contenders, Points score) {
        return contenders.stream()
                .filter(contender -> contender.score().equals(score))
                .toList();
    }
}
