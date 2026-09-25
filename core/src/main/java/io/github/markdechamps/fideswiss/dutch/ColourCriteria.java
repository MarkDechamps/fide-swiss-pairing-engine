package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.dutch.CandidateAssessment.PlayerInGame;
import java.util.function.Predicate;

/**
 * The four colour criteria, word for word the same in both editions: 2026 [C10]–[C13] and 2017 C.8–C.11. Each
 * edition lists them under its own article numbers.
 */
final class ColourCriteria {

    private ColourCriteria() {}

    static Failure topscorersBeyondColourDifferenceTwo(CandidateAssessment assessment) {
        return countPlayersInTopscorerGames(assessment, player -> Math.abs(player.colourDifferenceAfter()) > 2);
    }

    static Failure topscorersWithSameColourThreeTimes(CandidateAssessment assessment) {
        return countPlayersInTopscorerGames(assessment, PlayerInGame::getsSameColourThirdTimeInARow);
    }

    static Failure playersNotGettingColourPreference(CandidateAssessment assessment) {
        return Failure.count(assessment
                .playersInGames()
                .filter(player -> !player.getsPreference())
                .count());
    }

    static Failure playersNotGettingStrongColourPreference(CandidateAssessment assessment) {
        return Failure.count(assessment
                .playersInGames()
                .filter(player -> player.player().colourPreference().isStrong())
                .filter(player -> !player.getsPreference())
                .count());
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
}
