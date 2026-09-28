package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.dutch.CandidateCriterion.Scope;
import io.github.markdechamps.fideswiss.search.SearchHeartbeat;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/** Dutch Article 3: pairs one bracket by generating candidates in the order of Articles 3.6, 3.7 and 4. */
final class BracketPairer {

    /**
     * What the following bracket is paired on for [C8] / C.7: fewest unpaired, then their scores highest first.
     * For 2026 these are [C6] and [C7]; for 2017 C.7 ("maximise the number of pairs, then minimise the PSD") it is
     * both Oracles' reading: with the pairs fixed, the PSD is decided by which MDPs float on (reading R5).
     */
    private static List<CandidateCriterion> followingBracketCriteria(ToIntFunction<Player> unpairedWeight) {
        return List.of(
                CandidateCriterion.of(
                        "unpaired",
                        Scope.COUNT,
                        assessment -> Failure.count(assessment.downfloaters().stream()
                                .mapToInt(unpairedWeight)
                                .sum())),
                CandidateCriterion.of(
                        "their scores",
                        Scope.SCORES,
                        assessment -> Failure.descending(assessment.downfloaters().stream()
                                .map(BracketPairer::scoreOf)
                                .toList())));
    }

    private static BigDecimal scoreOf(Player player) {
        return player.score().points().toBigDecimal();
    }

    private final PlayerSet players;
    private final AbsoluteCriteria absolute;
    private final CompletionCriterion completion;
    private final ColourAllocation colours;
    private final RoundToPair round;
    private final DutchEdition edition;

    BracketPairer(PlayerSet players, RoundToPair round, DutchEdition edition) {
        this.players = players;
        this.round = round;
        this.edition = edition;
        this.absolute = new AbsoluteCriteria(round, edition.pairingAllocatedByeBar());
        this.completion =
                CompletionCriterion.forRound(players, absolute, edition.foldsPairingAllocatedByeScoreIntoCompletion());
        this.colours = new ColourAllocation(round);
    }

    /** Whether these players (downfloaters plus everyone below) can still complete the round ([C1]–[C3], PAB). */
    boolean allowsCompletion(List<Player> notYetPaired) {
        return completion.isSatisfiedBy(players.maskOf(notYetPaired));
    }

    boolean isRoundCompletable() {
        return completion.isRoundCompletable();
    }

    ColourAllocation colours() {
        return colours;
    }

    /** The bracket's candidate; empty only when no candidate keeps the round completable. */
    Optional<BracketOutcome> pair(
            Bracket bracket,
            List<Player> lowerPlayers,
            List<Player> nextResidents,
            CompletionScope scope,
            SearchHeartbeat heartbeat) {
        return new BracketPairing(bracket, lowerPlayers, nextResidents, edition.criteria(), scope, Optional.empty())
                .bestCandidate(heartbeat);
    }

    /** The pairing of one bracket: its optimum and its candidate sequence. */
    private final class BracketPairing {

        private final Bracket bracket;
        private final CompletionScope scope;
        private final List<Player> lowerPlayers;
        private final FollowingBrackets lookahead;
        private final List<CandidateCriterion> criteria;
        private final OptimumFinder finder;
        private final Map<Pair, List<Failure>> pairContributions = new HashMap<>();
        private final Map<Player, List<Failure>> downfloaterContributions = new IdentityHashMap<>();

        BracketPairing(
                Bracket bracket,
                List<Player> lowerPlayers,
                List<Player> nextResidents,
                List<CandidateCriterion> criteria,
                CompletionScope scope,
                Optional<ToIntFunction<Player>> unpairedWeightOverride) {
            this.bracket = bracket;
            this.scope = scope;
            this.lowerPlayers = lowerPlayers;
            this.lookahead = new FollowingBrackets(lowerPlayers, nextResidents, scope, unpairedWeightOverride);
            this.criteria = criteria;
            var setting = new RoundWideMatching.Setting(
                    criteria,
                    new SplitCriteria(),
                    absolute::mayMeet,
                    completion::mayTakePairingAllocatedBye,
                    this::pairOf,
                    scope,
                    lookahead.unpairedWeight());
            var seen = scope == CompletionScope.WHOLE_ROUND ? lowerPlayers : nextResidents;
            var matching = new RoundWideMatching(setting, bracket.playersInBsnOrder(), seen, nextResidents);
            this.finder = new OptimumFinder(criteria, matching, this::vectorOf);
        }

        /**
         * Article 3.4 accepts the first perfect candidate. The optimum finder makes "perfect" exact, so the first
         * candidate that reaches it is also the best one of 3.8, ties going to the earliest.
         */
        Optional<BracketOutcome> bestCandidate(SearchHeartbeat heartbeat) {
            return finder.optimum(wholeBracket()).map(optimum -> firstReaching(optimum, heartbeat));
        }

        private BracketOutcome firstReaching(OptimumFinder.Optimum optimum, SearchHeartbeat heartbeat) {
            var maxPairs = (bracket.playersInBsnOrder().size()
                            - optimum.candidate().downfloaters().size())
                    / 2;
            var candidates = bracket.isHomogeneous()
                    ? homogeneousCandidates(bracket.residents(), maxPairs, List.of(), List.of(), optimum)
                    : heterogeneousCandidates(optimum);
            var first = candidates
                    .peek(candidate -> heartbeat.tick())
                    .filter(this::keepsRoundCompletable)
                    .filter(candidate -> vectorOf(candidate).equals(optimum.vector()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Target unreachable in " + bracket + ": " + describe(optimum.vector())));
            return new BracketOutcome(first, describe(optimum.vector()));
        }

        /** Article 3.6: transpositions of S2, then the next resident exchange, re-sorted, and again. */
        private Stream<Candidate> homogeneousCandidates(
                List<Player> group,
                int maxPairs,
                List<Pair> fixed,
                List<Player> floating,
                OptimumFinder.Optimum optimum) {
            return Stream.concat(
                            Stream.of(ResidentExchange.NONE),
                            ResidentExchange.inOrder(maxPairs, group.size() - maxPairs))
                    .map(exchange -> exchange.applyTo(group, maxPairs))
                    .flatMap(subgroups -> Transpositions.of(
                                    subgroups.s1(),
                                    subgroups.s2(),
                                    this::mayMeetInBracket,
                                    (placed, available) -> finder.canReach(
                                            optimum,
                                            transpositionBranch(subgroups.s1(), placed, available, fixed, floating)))
                            .map(s2 -> Candidate.fromSubgroups(subgroups.s1(), s2)))
                    .map(remainder ->
                            bracket.isHomogeneous() ? remainder : Candidate.heterogeneous(fixed, remainder, floating));
        }

        /** Article 3.7: remainder first, then the next transposition of S2, then the next MDP set. */
        private Stream<Candidate> heterogeneousCandidates(OptimumFinder.Optimum optimum) {
            var bestAchievableLimbo = limboOf(optimum.candidate().downfloaters());
            return edition.mdpSelection()
                    .inOrder(bracket.movedDown(), bestAchievableLimbo)
                    .flatMap(s1 -> {
                        var limbo = MdpSets.limboOf(bracket.movedDown(), s1);
                        return Transpositions.of(
                                        s1,
                                        bracket.residents(),
                                        this::mayMeetInBracket,
                                        (placed, available) -> finder.canReach(
                                                optimum, mdpPairingBranch(s1, placed, available, limbo)))
                                .flatMap(s2 ->
                                        candidatesWithMdpPairing(Candidate.fromSubgroups(s1, s2), limbo, optimum));
                    });
        }

        private Stream<Candidate> candidatesWithMdpPairing(
                Candidate mdpPairing, List<Player> limbo, OptimumFinder.Optimum optimum) {
            var remainder = mdpPairing.downfloaters().stream()
                    .sorted(PairingOrder.RANKING)
                    .toList();
            return maxPairsOfRemainder(remainder, limbo).stream()
                    .flatMap(
                            maxPairs -> homogeneousCandidates(remainder, maxPairs, mdpPairing.pairs(), limbo, optimum));
        }

        /** Article 3.1 for the remainder: the most pairs it can make with the Limbo floating and [C4] kept. */
        private Optional<Integer> maxPairsOfRemainder(List<Player> remainder, List<Player> limbo) {
            var branch = new OptimumFinder.Branch(List.of(), remainder, this::mayMeetInBracket, remainder, limbo);
            return finder.optimum(branch).map(best -> {
                var fewest = best.candidate().downfloaters().size() - limbo.size();
                return (remainder.size() - fewest) / 2;
            });
        }

        private OptimumFinder.Branch wholeBracket() {
            var everyone = bracket.playersInBsnOrder();
            return new OptimumFinder.Branch(List.of(), everyone, this::mayMeetInBracket, everyone, List.of());
        }

        /** A partial transposition: S1 players still to place meet only S2 players still available. */
        private OptimumFinder.Branch transpositionBranch(
                List<Player> s1, List<Player> placed, List<Player> available, List<Pair> fixed, List<Player> floating) {
            var stillToPlace = s1.subList(placed.size(), s1.size());
            var open = new ArrayList<>(stillToPlace);
            open.addAll(available);
            return new OptimumFinder.Branch(
                    withPairs(fixed, s1, placed),
                    open,
                    (a, b) -> mayMeetInBracket(a, b) && stillToPlace.contains(a) != stillToPlace.contains(b),
                    available,
                    floating);
        }

        /** A partial MDP-Pairing: MDPs still to place meet residents; the residents left form the remainder. */
        private OptimumFinder.Branch mdpPairingBranch(
                List<Player> s1, List<Player> placed, List<Player> available, List<Player> limbo) {
            var open = new ArrayList<>(s1.subList(placed.size(), s1.size()));
            open.addAll(available);
            return new OptimumFinder.Branch(
                    withPairs(List.of(), s1, placed), open, this::mayMeetInBracket, available, limbo);
        }

        private List<Pair> withPairs(List<Pair> fixed, List<Player> s1, List<Player> placed) {
            var pairs = new ArrayList<>(fixed);
            for (var index = 0; index < placed.size(); index++) {
                pairs.add(new Pair(s1.get(index), placed.get(index)));
            }
            return pairs;
        }

        /** The MDP is the S1 player of an MDP pair (Article 3.3.4); otherwise the higher-ranked one. */
        private Pair pairOf(Player a, Player b) {
            if (bracket.isMovedDown(b) || (!bracket.isMovedDown(a) && PairingOrder.isHigherRanked(b, a))) {
                return new Pair(b, a);
            }
            return new Pair(a, b);
        }

        /** Each criterion evaluated on one pair, or on one downfloater, on its own. */
        private final class SplitCriteria implements RoundWideMatching.Contributions {

            @Override
            public List<Failure> ofPair(Pair pair) {
                return pairContributions.computeIfAbsent(
                        pair, key -> additiveFailures(new Candidate(List.of(key), List.of())));
            }

            @Override
            public List<Failure> ofDownfloater(Player downfloater) {
                return downfloaterContributions.computeIfAbsent(
                        downfloater, key -> additiveFailures(Candidate.onlyDownfloaters(List.of(key))));
            }

            private List<Failure> additiveFailures(Candidate part) {
                var assessment = assessmentOf(part);
                return criteria.stream()
                        .map(criterion -> isAdditive(criterion) ? criterion.failureOf(assessment) : Failure.NONE)
                        .toList();
            }

            private static boolean isAdditive(CandidateCriterion criterion) {
                return criterion.scope() == Scope.COUNT || criterion.scope() == Scope.SCORES;
            }
        }

        private List<Player> limboOf(List<Player> downfloaters) {
            return bracket.movedDown().stream().filter(downfloaters::contains).toList();
        }

        private boolean keepsRoundCompletable(Candidate candidate) {
            if (scope == CompletionScope.FOLLOWING_BRACKET_ONLY) {
                return true;
            }
            if (lookahead.isLastBracket() && candidate.downfloaters().size() > 1) {
                return false;
            }
            return completion.isSatisfiedBy(
                    PlayerSet.union(players.maskOf(candidate.downfloaters()), players.maskOf(lowerPlayers)));
        }

        private CriteriaVector vectorOf(Candidate candidate) {
            var assessment = assessmentOf(candidate);
            return new CriteriaVector(criteria.stream()
                    .map(criterion -> criterion.failureOf(assessment))
                    .toList());
        }

        private CandidateAssessment assessmentOf(Candidate candidate) {
            return new CandidateAssessment(candidate, bracket, round, lookahead, colours);
        }

        /** MDPs are paired against residents only (Article 3.3), never against each other. */
        private boolean mayMeetInBracket(Player a, Player b) {
            return absolute.mayMeet(a, b) && !(bracket.isMovedDown(a) && bracket.isMovedDown(b));
        }

        /** Only the criteria that fail, as "C12=[2]". */
        private String describe(CriteriaVector vector) {
            var failing = new ArrayList<String>();
            for (var index = 0; index < vector.failures().size(); index++) {
                var failure = vector.failures().get(index);
                if (!failure.isZero()) {
                    failing.add(criteria.get(index).article() + "=" + failure);
                }
            }
            return String.join(" ", failing);
        }
    }

    /** The brackets below the current one, as far as [C5], [C8] and [C9] (2017: C.7) look. */
    private final class FollowingBrackets implements Lookahead {

        private final List<Player> lowerPlayers;
        private final List<Player> nextResidents;
        private final CompletionScope scope;
        private final ToIntFunction<Player> unpairedWeight;
        private final Map<BitSet, Failure> followingOutcomes = new HashMap<>();
        private final Map<BitSet, Optional<Player>> pabAssignees = new HashMap<>();

        /**
         * When a 2017 following bracket is the last one, a player it leaves unpaired who may not take the PAB
         * weighs 3 instead of 1 (reading R6, a text gap: bbpPairings' {@code finalBrackets}; JaVaFo agrees).
         */
        FollowingBrackets(
                List<Player> lowerPlayers,
                List<Player> nextResidents,
                CompletionScope scope,
                Optional<ToIntFunction<Player>> unpairedWeightOverride) {
            this.lowerPlayers = lowerPlayers;
            this.nextResidents = nextResidents;
            this.scope = scope;
            var followingIsLast =
                    scope == CompletionScope.FOLLOWING_BRACKET_ONLY && lowerPlayers.size() == nextResidents.size();
            this.unpairedWeight = unpairedWeightOverride.orElse(
                    followingIsLast ? player -> completion.mayTakePairingAllocatedBye(player) ? 1 : 3 : player -> 1);
        }

        ToIntFunction<Player> unpairedWeight() {
            return unpairedWeight;
        }

        @Override
        public boolean isLastBracket() {
            return lowerPlayers.isEmpty();
        }

        @Override
        public Optional<Player> pairingAllocatedByeAssignee(List<Player> downfloaters) {
            var notYetPaired = PlayerSet.union(players.maskOf(downfloaters), players.maskOf(lowerPlayers));
            return pabAssignees.computeIfAbsent(notYetPaired, completion::lowestPossiblePairingAllocatedByeAssignee);
        }

        @Override
        public Failure followingBracketOutcome(List<Player> downfloaters) {
            if (nextResidents.isEmpty()) {
                return Failure.NONE;
            }
            return followingOutcomes.computeIfAbsent(
                    players.maskOf(downfloaters), key -> outcomeOfFollowingBracket(downfloaters));
        }

        /**
         * [C8] read as bbpPairings and [ANN p.25] do: the following bracket, with these downfloaters as its MDPs,
         * paired on the fewest unpaired, then their scores; the result is how many it leaves unpaired, then their
         * scores. 2017 C.7 asks the same "just in the following bracket", without the completion requirement.
         */
        private Failure outcomeOfFollowingBracket(List<Player> downfloaters) {
            var following = new Bracket(downfloaters, nextResidents);
            var belowFollowing = scope == CompletionScope.WHOLE_ROUND
                    ? lowerPlayers.stream()
                            .filter(player -> !nextResidents.contains(player))
                            .toList()
                    : List.<Player>of();
            var pairing = new BracketPairing(
                    following,
                    belowFollowing,
                    List.of(),
                    followingBracketCriteria(unpairedWeight),
                    scope,
                    Optional.<ToIntFunction<Player>>of(player -> 1));
            return pairing.finder
                    .optimum(pairing.wholeBracket())
                    .map(best -> downfloatersThenTheirScores(best.candidate().downfloaters()))
                    .orElseGet(() -> Failure.of(BigDecimal.valueOf(Integer.MAX_VALUE)));
        }

        private Failure downfloatersThenTheirScores(List<Player> downfloaters) {
            var values = new ArrayList<BigDecimal>();
            values.add(BigDecimal.valueOf(
                    downfloaters.stream().mapToInt(unpairedWeight).sum()));
            values.addAll(Failure.descending(downfloaters.stream()
                            .map(player -> player.score().points().toBigDecimal())
                            .toList())
                    .values());
            return new Failure(values);
        }
    }
}
