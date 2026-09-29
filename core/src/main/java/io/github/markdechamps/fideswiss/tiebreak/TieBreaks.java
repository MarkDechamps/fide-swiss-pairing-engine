package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Builds each code of a Tie-break List into its tie-break object, modifiers as decorators. */
final class TieBreaks {

    /** New in C.07 2026-03. */
    private static final Set<String> NEW_IN_2026 = Set.of("STD", "TPN", "RTNG");

    private TieBreaks() {}

    static List<TieBreak> of(TieBreakList list, TieBreakEdition edition, ScoringScheme scoring) {
        var problems = new ArrayList<Problem>();
        var tieBreaks = new ArrayList<TieBreak>();
        for (var code : list.codes()) {
            if (scoring.matches().isEmpty()
                    && (TieBreakList.isTeamOnly(code.acronym())
                            || code.teamScore().isPresent())) {
                problems.add(Problem.citing("C.07 13", code + " needs a team competition (match scoring, TRF 362)"));
            } else if (edition == TieBreakEdition.EDITION_2024_08
                    && (NEW_IN_2026.contains(code.acronym()) || code.fore())) {
                problems.add(Problem.citing("C.07 5", code + " does not exist in Tie-break Edition 2024-08"));
            } else {
                tieBreaks.add(of(code));
            }
        }
        if (!problems.isEmpty()) {
            throw new InvalidSettingsException(problems);
        }
        return tieBreaks;
    }

    private static TieBreak of(TieBreakCode code) {
        var tieBreak = built(code);
        return code.teamScore()
                .map(score -> (TieBreak) new TeamScored(tieBreak, score))
                .orElse(tieBreak);
    }

    private static TieBreak built(TieBreakCode code) {
        return switch (code.acronym()) {
            case "MPVGP" -> new MatchPointsOrGamePoints(code);
            case "EMMSB" -> extended(code, PrimaryScore.MATCH_POINTS, PrimaryScore.MATCH_POINTS, "C.07 13.2.1");
            case "EMGSB" -> extended(code, PrimaryScore.MATCH_POINTS, PrimaryScore.GAME_POINTS, "C.07 13.2.2");
            case "EGMSB" -> extended(code, PrimaryScore.GAME_POINTS, PrimaryScore.MATCH_POINTS, "C.07 13.2.3");
            case "EGGSB" -> extended(code, PrimaryScore.GAME_POINTS, PrimaryScore.GAME_POINTS, "C.07 13.2.4");
            case "EDE", "EDEBT", "EDEBB", "EDET", "EDEB" -> new ExtendedDirectEncounter(code);
            case "SSSC" -> new ScoresAndScheduleStrength(code);
            case "BC" -> new BoardResults(code, BoardResults.Rule.BOARD_COUNT);
            case "TBR" -> new BoardResults(code, BoardResults.Rule.TOP_BOARD_RESULTS);
            case "BBE" -> new BoardResults(code, BoardResults.Rule.BOTTOM_BOARD_ELIMINATION);
            case "BH" -> sum(code, new Buchholz(code.forfeitsAsPlayed()), "C.07 8.1");
            case "FB" -> sum(code, new ForeBuchholz(code.forfeitsAsPlayed()), "C.07 8.3");
            case "SB" -> sum(code, new SonnebornBerger(code.forfeitsAsPlayed()), "C.07 9.1");
            case "PS" -> sum(code, new ProgressiveScores(), "C.07 7.5");
            case "DE" -> new DirectEncounter(code);
            case "WIN" -> OwnRecordTieBreak.wins(code);
            case "WON" -> OwnRecordTieBreak.gamesWon(code);
            case "BPG" -> OwnRecordTieBreak.gamesWithBlack(code);
            case "BWG" -> OwnRecordTieBreak.winsWithBlack(code);
            case "REP" -> OwnRecordTieBreak.roundsElectedToPlay(code);
            case "STD" -> OwnRecordTieBreak.standardPoints(code);
            case "TPN" -> OwnRecordTieBreak.pairingNumber(code);
            case "RTNG" -> new RatingTieBreak(OwnRecordTieBreak.rating(code));
            case "KS" -> OwnRecordTieBreak.koya(code);
            case "AOB" -> FormulaTieBreak.averageOfOpponentsBuchholz(code);
            case "TPR" -> new RatingTieBreak(FormulaTieBreak.tournamentPerformanceRating(code));
            case "PTP" -> new RatingTieBreak(FormulaTieBreak.perfectTournamentPerformance(code));
            case "APRO" -> new RatingTieBreak(FormulaTieBreak.averagePerformanceOfOpponents(code));
            case "APPO" -> new RatingTieBreak(FormulaTieBreak.averagePerfectPerformanceOfOpponents(code));
            case "ARO" ->
                new RatingTieBreak(new TermTieBreak(
                        code,
                        modified(code, new OpponentRatings()),
                        TermTieBreak.Aggregation.AVERAGE_ROUNDED,
                        "C.07 10.1"));
            default ->
                throw new InvalidSettingsException(List.of(Problem.citing("C.07 5", code + " is not implemented yet")));
        };
    }

    private static TieBreak extended(
            TieBreakCode code, PrimaryScore opponentTotal, PrimaryScore scored, String article) {
        return sum(code, new ExtendedSonnebornBerger(opponentTotal, scored, code.forfeitsAsPlayed()), article);
    }

    private static TieBreak sum(TieBreakCode code, TermSource source, String article) {
        return new TermTieBreak(code, modified(code, source), TermTieBreak.Aggregation.SUM, article);
    }

    /** Cut-n, or Median-n as its low cuts followed by its high cuts (C.07 14.3: "in that order"). */
    private static TermSource modified(TieBreakCode code, TermSource source) {
        var modifier = "/" + (code.highCuts() > 0 ? "M" + code.highCuts() : "C" + code.lowCuts());
        var modified = code.lowCuts() > 0 ? new LowCut(source, code.lowCuts(), modifier + " (C.07 14)") : source;
        return code.highCuts() > 0 ? new HighCut(modified, code.highCuts(), modifier + " (C.07 14)") : modified;
    }
}
