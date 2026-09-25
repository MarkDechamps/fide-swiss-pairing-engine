package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.dutch.CandidateAssessment.PlayerInGame;
import io.github.markdechamps.fideswiss.dutch.CandidateCriterion.Scope;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
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
                criterion("C5", Scope.PAB_SCORE, Dutch2026Criteria::minimiseScoreOfPabAssignee),
                criterion("C6", Scope.COUNT, Dutch2026Criteria::minimiseNumberOfDownfloaters),
                criterion("C7", Scope.SCORES, Dutch2026Criteria::minimiseScoresOfDownfloaters),
                criterion("C8", Scope.FOLLOWING_BRACKET, Dutch2026Criteria::complyWithC1ToC7InFollowingBracket),
                criterion("C9", Scope.SINGLE_DOWNFLOATER, Dutch2026Criteria::minimiseUnplayedGamesOfPabAssignee),
                criterion("C10", Scope.COUNT, Dutch2026Criteria::minimiseTopscorersBeyondColourDifferenceTwo),
                criterion("C11", Scope.COUNT, Dutch2026Criteria::minimiseTopscorersWithSameColourThreeTimes),
                criterion("C12", Scope.COUNT, Dutch2026Criteria::minimisePlayersNotGettingColourPreference),
                criterion("C13", Scope.COUNT, Dutch2026Criteria::minimisePlayersNotGettingStrongColourPreference),
                criterion("C14", Scope.COUNT, assessment -> residentDownfloatersWhoDownfloated(assessment, 1)),
                criterion("C15", Scope.COUNT, assessment -> mdpOpponentsWhoUpfloated(assessment, 1)),
                criterion("C16", Scope.COUNT, assessment -> residentDownfloatersWhoDownfloated(assessment, 2)),
                criterion("C17", Scope.COUNT, assessment -> mdpOpponentsWhoUpfloated(assessment, 2)),
                criterion("C18", Scope.SCORES, assessment -> scoreDifferencesOfMdpsWhoDownfloated(assessment, 1)),
                criterion("C19", Scope.SCORES, assessment -> scoreDifferencesOfMdpOpponentsWhoUpfloated(assessment, 1)),
                criterion("C20", Scope.SCORES, assessment -> scoreDifferencesOfMdpsWhoDownfloated(assessment, 2)),
                criterion(
                        "C21", Scope.SCORES, assessment -> scoreDifferencesOfMdpOpponentsWhoUpfloated(assessment, 2)));
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

    private static Failure minimiseTopscorersBeyondColourDifferenceTwo(CandidateAssessment assessment) {
        return countPlayersInTopscorerGames(assessment, player -> Math.abs(player.colourDifferenceAfter()) > 2);
    }

    private static Failure minimiseTopscorersWithSameColourThreeTimes(CandidateAssessment assessment) {
        return countPlayersInTopscorerGames(assessment, PlayerInGame::getsSameColourThirdTimeInARow);
    }

    private static Failure minimisePlayersNotGettingColourPreference(CandidateAssessment assessment) {
        return Failure.count(assessment
                .playersInGames()
                .filter(player -> !player.getsPreference())
                .count());
    }

    private static Failure minimisePlayersNotGettingStrongColourPreference(CandidateAssessment assessment) {
        return Failure.count(assessment
                .playersInGames()
                .filter(player -> player.player().colourPreference().isStrong())
                .filter(player -> !player.getsPreference())
                .count());
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

    private static Failure countPlayersInTopscorerGames(
            CandidateAssessment assessment, Predicate<PlayerInGame> failing) {
        var round = assessment.round();
        return Failure.count(assessment
                .playersInGames()
                .filter(player -> round.isTopscorer(player.player()) || round.isTopscorer(player.opponent()))
                .filter(failing)
                .count());
    }

    private static BigDecimal scoreDifference(Pair mdpPair) {
        return scoreOf(mdpPair.s1Player()).subtract(scoreOf(mdpPair.s2Player()));
    }

    private static BigDecimal scoreOf(Player player) {
        return player.score().points().toBigDecimal();
    }

    private static CandidateCriterion criterion(
            String article, Scope scope, Function<CandidateAssessment, Failure> failure) {
        return new CandidateCriterion() {
            @Override
            public String article() {
                return article;
            }

            @Override
            public Scope scope() {
                return scope;
            }

            @Override
            public Failure failureOf(CandidateAssessment candidate) {
                return failure.apply(candidate);
            }
        };
    }
}
