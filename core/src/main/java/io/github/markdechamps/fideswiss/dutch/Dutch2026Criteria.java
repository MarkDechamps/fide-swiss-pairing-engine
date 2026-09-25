package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.dutch.CandidateCriterion.of;

import io.github.markdechamps.fideswiss.dutch.CandidateCriterion.Scope;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/** C.04.3 (2026) Articles 2.3–2.4: [C5]–[C21] in descending priority. */
final class Dutch2026Criteria {

    /**
     * [ANN p.31]: a downfloater's score difference is greater than any resident's. Between two Limbo MDPs, the one
     * with the higher score has the greater difference, as it will face someone lower still (a documented reading
     * of [C18]/[C20], verified against bbpPairings).
     */
    private static final BigDecimal DOWNFLOATER_SCORE_DIFFERENCE = BigDecimal.valueOf(1_000);

    private Dutch2026Criteria() {}

    static List<CandidateCriterion> inPriorityOrder() {
        return List.of(
                of("C5", Scope.PAB_SCORE, Dutch2026Criteria::minimiseScoreOfPabAssignee),
                of("C6", Scope.COUNT, Dutch2026Criteria::minimiseNumberOfDownfloaters),
                of("C7", Scope.SCORES, Dutch2026Criteria::minimiseScoresOfDownfloaters),
                of("C8", Scope.FOLLOWING_BRACKET, Dutch2026Criteria::complyWithC1ToC7InFollowingBracket),
                of("C9", Scope.SINGLE_DOWNFLOATER, Dutch2026Criteria::minimiseUnplayedGamesOfPabAssignee),
                of("C10", Scope.COUNT, ColourCriteria::topscorersBeyondColourDifferenceTwo),
                of("C11", Scope.COUNT, ColourCriteria::topscorersWithSameColourThreeTimes),
                of("C12", Scope.COUNT, ColourCriteria::playersNotGettingColourPreference),
                of("C13", Scope.COUNT, ColourCriteria::playersNotGettingStrongColourPreference),
                of("C14", Scope.COUNT, assessment -> residentDownfloatersWhoDownfloated(assessment, 1)),
                of("C15", Scope.COUNT, assessment -> mdpOpponentsWhoUpfloated(assessment, 1)),
                of("C16", Scope.COUNT, assessment -> residentDownfloatersWhoDownfloated(assessment, 2)),
                of("C17", Scope.COUNT, assessment -> mdpOpponentsWhoUpfloated(assessment, 2)),
                of("C18", Scope.SCORES, assessment -> scoreDifferencesOfMdpsWhoDownfloated(assessment, 1)),
                of("C19", Scope.SCORES, assessment -> scoreDifferencesOfMdpOpponentsWhoUpfloated(assessment, 1)),
                of("C20", Scope.SCORES, assessment -> scoreDifferencesOfMdpsWhoDownfloated(assessment, 2)),
                of("C21", Scope.SCORES, assessment -> scoreDifferencesOfMdpOpponentsWhoUpfloated(assessment, 2)));
    }

    private static Failure minimiseScoreOfPabAssignee(CandidateAssessment assessment) {
        return assessment
                .lookahead()
                .pairingAllocatedByeAssignee(assessment.downfloaters())
                .map(assignee -> Failure.of(scoreOf(assignee)))
                .orElse(Failure.NONE);
    }

    private static Failure minimiseNumberOfDownfloaters(CandidateAssessment assessment) {
        return Failure.count(assessment.downfloaters().size());
    }

    private static Failure minimiseScoresOfDownfloaters(CandidateAssessment assessment) {
        return Failure.descending(assessment.downfloaters().stream()
                .map(Dutch2026Criteria::scoreOf)
                .toList());
    }

    private static Failure complyWithC1ToC7InFollowingBracket(CandidateAssessment assessment) {
        return assessment.lookahead().followingBracketOutcome(assessment.downfloaters());
    }

    /**
     * The note to [C9]: only in brackets that downfloat exactly one player, "who will end up receiving the PAB".
     * Read as: nobody below can take the PAB instead, so that downfloater is bound to get it (a documented
     * reading, verified against bbpPairings).
     */
    private static Failure minimiseUnplayedGamesOfPabAssignee(CandidateAssessment assessment) {
        var downfloaters = assessment.downfloaters();
        if (downfloaters.size() != 1) {
            return Failure.NONE;
        }
        var downfloater = downfloaters.getFirst();
        var boundToGetPab = assessment
                .lookahead()
                .pairingAllocatedByeAssignee(downfloaters)
                .filter(assignee -> assignee == downfloater)
                .isPresent();
        return boundToGetPab ? Failure.count(downfloater.unplayedRounds()) : Failure.NONE;
    }

    private static Failure residentDownfloatersWhoDownfloated(CandidateAssessment assessment, int roundsAgo) {
        return Failure.count(assessment.residentDownfloaters().stream()
                .filter(player -> player.floatRoundsAgo(roundsAgo) == FloatDirection.DOWN)
                .count());
    }

    private static Failure mdpOpponentsWhoUpfloated(CandidateAssessment assessment, int roundsAgo) {
        return Failure.count(assessment.mdpPairs().stream()
                .filter(pair -> pair.s2Player().floatRoundsAgo(roundsAgo) == FloatDirection.UP)
                .count());
    }

    private static Failure scoreDifferencesOfMdpsWhoDownfloated(CandidateAssessment assessment, int roundsAgo) {
        var paired = assessment.mdpPairs().stream()
                .filter(pair -> pair.s1Player().floatRoundsAgo(roundsAgo) == FloatDirection.DOWN)
                .map(Dutch2026Criteria::scoreDifference);
        var floatingAgain = assessment.limbo().stream()
                .filter(mdp -> mdp.floatRoundsAgo(roundsAgo) == FloatDirection.DOWN)
                .map(mdp -> DOWNFLOATER_SCORE_DIFFERENCE.add(scoreOf(mdp)));
        return Failure.descending(Stream.concat(paired, floatingAgain).toList());
    }

    private static Failure scoreDifferencesOfMdpOpponentsWhoUpfloated(CandidateAssessment assessment, int roundsAgo) {
        return Failure.descending(assessment.mdpPairs().stream()
                .filter(pair -> pair.s2Player().floatRoundsAgo(roundsAgo) == FloatDirection.UP)
                .map(Dutch2026Criteria::scoreDifference)
                .toList());
    }

    private static BigDecimal scoreDifference(Pair mdpPair) {
        return scoreOf(mdpPair.s1Player()).subtract(scoreOf(mdpPair.s2Player()));
    }

    private static BigDecimal scoreOf(Player player) {
        return player.score().points().toBigDecimal();
    }
}
