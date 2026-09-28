package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.CandidateCriterion.of;

import io.github.markdechamps.fideswiss.dutch.CandidateCriterion.Scope;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * C.04.3 (till 2026-01-31) C.5–C.19 in descending priority. C.1–C.3 are the {@link AbsoluteCriteria}, and C.4
 * belongs to the {@link Dutch2017Procedure} (it applies in the Penultimate Pairing Bracket only).
 *
 * <p>C.12–C.19 speak of "players who receive the same downfloat/upfloat": whoever gets a float in this round
 * (reading R7). By A.4.b, in a pair of different scores the higher gets a downfloat and the lower an upfloat, and
 * every downfloater gets a downfloat. That covers MDP-Pairings, Limbo MDPs and, in the Collapsed Last Bracket,
 * remainder pairs.
 */
final class Dutch2017Criteria {

    /** A.8: a downfloater's SD is taken against "one point less than" the bracket's lowest score. */
    private static final BigDecimal ONE_POINT = BigDecimal.ONE;

    private Dutch2017Criteria() {}

    static List<CandidateCriterion> inPriorityOrder() {
        return List.of(
                of(
                        "C.5",
                        Scope.COUNT,
                        assessment -> Failure.count(assessment.downfloaters().size())),
                of("C.6", Scope.SCORES, Dutch2017Criteria::pairingScoreDifference),
                of(
                        "C.7",
                        Scope.FOLLOWING_BRACKET,
                        assessment -> assessment.lookahead().followingBracketOutcome(assessment.downfloaters())),
                of("C.8", Scope.COUNT, ColourCriteria::topscorersBeyondColourDifferenceTwo),
                of("C.9", Scope.COUNT, ColourCriteria::topscorersWithSameColourThreeTimes),
                of("C.10", Scope.COUNT, ColourCriteria::playersNotGettingColourPreference),
                of("C.11", Scope.COUNT, ColourCriteria::playersNotGettingStrongColourPreference),
                of("C.12", Scope.COUNT, assessment -> sameFloatCount(assessment, FloatDirection.DOWN, 1)),
                of("C.13", Scope.COUNT, assessment -> sameFloatCount(assessment, FloatDirection.UP, 1)),
                of("C.14", Scope.COUNT, assessment -> sameFloatCount(assessment, FloatDirection.DOWN, 2)),
                of("C.15", Scope.COUNT, assessment -> sameFloatCount(assessment, FloatDirection.UP, 2)),
                of("C.16", Scope.SCORES, assessment -> sameFloatScoreDifferences(assessment, FloatDirection.DOWN, 1)),
                of("C.17", Scope.SCORES, assessment -> sameFloatScoreDifferences(assessment, FloatDirection.UP, 1)),
                of("C.18", Scope.SCORES, assessment -> sameFloatScoreDifferences(assessment, FloatDirection.DOWN, 2)),
                of("C.19", Scope.SCORES, assessment -> sameFloatScoreDifferences(assessment, FloatDirection.UP, 2)));
    }

    /**
     * A.8, the Pairing Score Difference: one SD per pair (the absolute score difference) and one per downfloater
     * (its score minus one point less than the bracket's lowest score), highest first. With C.5 equal the list's
     * length is fixed. In the Collapsed Last Bracket each player keeps its own score (reading R4).
     */
    private static Failure pairingScoreDifference(CandidateAssessment assessment) {
        var pairs = assessment.pairs().stream().map(Dutch2017Criteria::scoreDifference);
        var downfloaters = assessment.downfloaters().stream()
                .map(downfloater -> downfloaterScoreDifference(assessment, downfloater));
        return Failure.descending(Stream.concat(pairs, downfloaters).toList());
    }

    private static Failure sameFloatCount(CandidateAssessment assessment, FloatDirection direction, int roundsAgo) {
        return Failure.count(floatsReceived(assessment, direction)
                .filter(received -> received.player().floatRoundsAgo(roundsAgo) == direction)
                .count());
    }

    private static Failure sameFloatScoreDifferences(
            CandidateAssessment assessment, FloatDirection direction, int roundsAgo) {
        return Failure.descending(floatsReceived(assessment, direction)
                .filter(received -> received.player().floatRoundsAgo(roundsAgo) == direction)
                .map(FloatReceived::scoreDifference)
                .toList());
    }

    /** A float this candidate gives a player, with the SD of A.8 that goes with it. */
    private record FloatReceived(Player player, BigDecimal scoreDifference) {}

    private static Stream<FloatReceived> floatsReceived(CandidateAssessment assessment, FloatDirection direction) {
        var inPairs = assessment.pairs().stream()
                .flatMap(pair -> receiverIn(pair, direction).stream()
                        .map(player -> new FloatReceived(player, scoreDifference(pair))));
        if (direction == FloatDirection.UP) {
            return inPairs;
        }
        var downfloaters = assessment.downfloaters().stream()
                .map(player -> new FloatReceived(player, downfloaterScoreDifference(assessment, player)));
        return Stream.concat(inPairs, downfloaters);
    }

    private static Optional<Player> receiverIn(Pair pair, FloatDirection direction) {
        var a = pair.s1Player();
        var b = pair.s2Player();
        if (a.score().equals(b.score())) {
            return Optional.empty();
        }
        var higher = a.score().isHigherThan(b.score()) ? a : b;
        var lower = higher == a ? b : a;
        return Optional.of(direction == FloatDirection.DOWN ? higher : lower);
    }

    private static BigDecimal scoreDifference(Pair pair) {
        return scoreOf(pair.s1Player()).subtract(scoreOf(pair.s2Player())).abs();
    }

    private static BigDecimal downfloaterScoreDifference(CandidateAssessment assessment, Player downfloater) {
        var artificial =
                assessment.lowestScoreInBracket().points().toBigDecimal().subtract(ONE_POINT);
        return scoreOf(downfloater).subtract(artificial);
    }

    private static BigDecimal scoreOf(Player player) {
        return player.score().points().toBigDecimal();
    }
}
