package io.github.markdechamps.fideswiss.standings;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DirectEncounterTest {

    @Test
    void ranksTheWinnerOfTheGameBetweenTwoTiedParticipantsFirst() {
        // 1 beats 2 and 3 beats 4; 3 beats 1 and 2 beats 4. 1 and 2 are tied on 1 point.
        var standings = CrossTable.of(4, 2, "DE")
                .round("1-2 1-0", "3-4 1-0")
                .round("1-3 0-1", "2-4 1-0")
                .standings();

        // C.07 6.1-6.2.
        assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(2)).rank()).isEqualTo(Rank.of(3));
    }

    @Test
    void leavesTiedParticipantsWhoNeverMetTied() {
        var standings = CrossTable.of(4, 2, "DE")
                .round("1-2 1-0", "3-4 ½")
                .round("1-3 ½", "2-4 1-0")
                .standings();

        assertThat(standings.standing(id(2)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(2));
    }

    @Test
    void placesAParticipantFirstWhateverTheOutcomeOfTheMissingGames() {
        // 1, 2 and 3 finish on 2 points: 1 beat both, 2 and 3 never met. 4 finishes on 3.
        var standings = CrossTable.of(6, 3, "DE")
                .round("1-2 1-0", "3-5 1-0", "4-6 1-0")
                .round("1-3 1-0", "2-6 1-0", "4-5 1-0")
                .round("4-1 1-0", "2-5 1-0", "3-6 1-0")
                .standings();

        // C.07 6.3: 1 has 2, while 2 and 3 can reach at most 1 by the missing game; 2 and 3 stay tied.
        assertThat(standings.standing(id(4)).rank()).isEqualTo(Rank.of(1));
        assertThat(standings.standing(id(1)).rank()).isEqualTo(Rank.of(2));
        assertThat(standings.standing(id(2)).rank()).isEqualTo(Rank.of(3));
        assertThat(standings.standing(id(3)).rank()).isEqualTo(Rank.of(3));
    }

    @Test
    void averagesRepeatedEncounters() {
        // 1 and 2 meet twice (1 wins, then loses) and finish tied: the average is ½ each (6.1.2), and WON is 1 each.
        var standings = CrossTable.of(4, 2, "DE, WON")
                .round("1-2 1-0", "3-4 ½")
                .round("2-1 1-0", "3-4 ½")
                .standings();

        assertThat(standings.compare(id(1), id(2)).decidedBy()).isInstanceOf(DecidingTieBreak.SharedRank.class);
    }
}
