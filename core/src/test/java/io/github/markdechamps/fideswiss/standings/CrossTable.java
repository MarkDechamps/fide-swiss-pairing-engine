package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Builds recorded tournaments from rounds written the way a crosstable reads: {@code "1-2 1-0"} (white 1 beats
 * black 2), {@code "3-4 ½"}, {@code "5-6 0-1"}, forfeits {@code "1-2 +-"}, {@code "1-2 -+"}, {@code "1-2 --"}, and
 * byes {@code "5 PAB"}, {@code "5 HPB"}, {@code "5 FPB"}, {@code "5 ZPB"}, {@code "5 OUT"} (withdrawn).
 */
final class CrossTable {

    private Tournament tournament;

    private CrossTable(Tournament tournament) {
        this.tournament = tournament;
    }

    static CrossTable of(int participants, int rounds, String tieBreaks) {
        return of(participants, rounds, tieBreaks, TieBreakEdition.EDITION_2026_03);
    }

    static CrossTable of(int participants, int rounds, String tieBreaks, TieBreakEdition edition) {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(rounds))
                .with(TieBreakList.parse(tieBreaks))
                .with(edition);
        return new CrossTable(Tournament.of(settings, TournamentMother.participants(participants)));
    }

    CrossTable round(String... entries) {
        var boards = new ArrayList<Board>();
        var byes = new HashMap<ParticipantId, Bye>();
        for (var entry : entries) {
            var parts = entry.trim().split("\\s+");
            if (parts[0].contains("-")) {
                var ids = parts[0].split("-");
                boards.add(new Board(
                        BoardNumber.of(boards.size() + 1),
                        ParticipantId.of(ids[0]),
                        ParticipantId.of(ids[1]),
                        outcome(parts[1])));
            } else {
                byes.put(ParticipantId.of(parts[0]), bye(parts[1]));
            }
        }
        tournament = tournament.withRound(Round.of(tournament.nextRound(), boards, byes));
        return this;
    }

    Tournament tournament() {
        return tournament;
    }

    Standings standings() {
        return tournament.standings();
    }

    Standings standingsAfter(int round) {
        return tournament.standingsAfter(RoundNumber.of(round));
    }

    private static GameOutcome outcome(String code) {
        return switch (code) {
            case "1-0" -> GameOutcome.WHITE_WINS;
            case "0-1" -> GameOutcome.BLACK_WINS;
            case "½" -> GameOutcome.DRAW;
            case "+-" -> GameOutcome.WHITE_WINS_BY_FORFEIT;
            case "-+" -> GameOutcome.BLACK_WINS_BY_FORFEIT;
            case "--" -> GameOutcome.DOUBLE_FORFEIT;
            default -> throw new IllegalArgumentException(code);
        };
    }

    private static Bye bye(String code) {
        return switch (code) {
            case "PAB" -> Bye.PAIRING_ALLOCATED;
            case "HPB" -> Bye.HALF_POINT;
            case "FPB" -> Bye.FULL_POINT;
            case "ZPB" -> Bye.ZERO_POINT;
            case "OUT" -> Bye.WITHDRAWN;
            default -> throw new IllegalArgumentException(code);
        };
    }
}
