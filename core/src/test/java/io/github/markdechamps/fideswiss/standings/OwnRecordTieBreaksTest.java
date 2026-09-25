package io.github.markdechamps.fideswiss.standings;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * The type B tie-breaks (C.07 art. 7, 10.6) on one crosstable: 1 beats 2 and 4 beats 3 with Black; 4 draws 1, 2 takes
 * a half-point bye, 3 gets the PAB; 1 wins against 3 by forfeit, 4 beats 2 with Black. Scores: 1 2½, 2 ½, 3 1, 4 2½.
 */
class OwnRecordTieBreaksTest {

    private static final int ROUNDS = 3;

    private static Standings standings(String tieBreak) {
        return CrossTable.of(4, ROUNDS, tieBreak)
                .round("1-2 1-0", "3-4 0-1")
                .round("4-1 ½", "2 HPB", "3 PAB")
                .round("1-3 +-", "2-4 0-1")
                .standings();
    }

    @ParameterizedTest(name = "{0} of {1} is {2}")
    @CsvSource({
        // 7.1: rounds scored as a win, with or without playing (a forfeit win, the PAB).
        "WIN, 1, 2",
        "WIN, 3, 1",
        // 7.2: games won over the board.
        "WON, 1, 1",
        "WON, 4, 2",
        // 7.3: games played over the board with Black.
        "BPG, 1, 1",
        "BPG, 4, 2",
        // 7.4: games won over the board with Black.
        "BWG, 4, 2",
        "BWG, 1, 0",
        // 7.5: 1 + 1½ + 2½.
        "PS, 1, 5",
        "PS/C1, 1, 4",
        // 7.6: rounds less half-point byes, zero-point byes and forfeit losses.
        "REP, 2, 2",
        "REP, 3, 2",
        "REP, 4, 3",
        // 7.7: 1 for beating the opponent's score or more than a draw without playing, ½ for equalling it.
        "STD, 1, 2.5",
        "STD, 2, 0.5",
        "STD, 3, 1",
        // 7.8: the Pairing Number, lower first.
        "TPN, 3, 3",
        // 10.6: the rating.
        "RTNG, 2, 2480",
        // 9.2: points against participants with at least half the maximum (1½ of 3): 1 and 4.
        "KS, 1, 0.5",
        "KS, 2, 0",
        "KS/L+3, 1, 0",
    })
    void countsTheParticipantsOwnRecord(String tieBreak, int participant, String expected) {
        var value = standings(tieBreak)
                .standing(id(participant))
                .tieBreakValues()
                .get(0)
                .value();

        assertThat(value)
                .hasValueSatisfying(actual -> assertThat(actual).isEqualByComparingTo(new BigDecimal(expected)));
    }

    @ParameterizedTest(name = "{0} ranks {1} above {2}")
    @CsvSource({"WON, 4, 1", "TPN, 1, 4", "RTNG, 1, 4"})
    void breaksTheTieBetweenOneAndFour(String tieBreak, int higher, int lower) {
        var comparison = standings(tieBreak).compare(id(1), id(4));

        assertThat(comparison.higher()).isEqualTo(id(higher));
        assertThat(comparison.lower()).isEqualTo(id(lower));
    }
}
