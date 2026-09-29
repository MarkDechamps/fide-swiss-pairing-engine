package io.github.markdechamps.fideswiss.olympiad;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.DRAW;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS_BY_FORFEIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.Name;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OlympiadSystemTest {

    @Test
    void pairsTheTopHalfAgainstTheBottomHalfInRoundOne() {
        // 9.3 on one group of six; 7.2: 1 and 3 White by lot, 2 Black; 11.1 publishes by rating after that.
        var pairing = teams(6).pairNextRound();

        assertThatPairing(pairing).hasBoards("1-4", "5-2", "3-6").givesNoPairingAllocatedBye();
    }

    @Test
    void givesTheByeToTheLowestRankedTeamWorthOneMatchpoint() {
        // 4.1, 4.3.
        var tournament = teams(5);

        assertThatPairing(tournament.pairNextRound()).givesPairingAllocatedByeTo("5");
        assertThat(tournament.settings().pairingAllocatedByeValue()).isEqualTo(Points.of(1));
    }

    @Test
    void neitherCountsAMatchLostByDefaultAsMetNorLetsItsWinnerHaveTheBye() {
        // 6.1: only a played match counts; 4.2.2: 2 won because 4 did not arrive (every board forfeited).
        var tournament = teams(4).withRound(firstRound(match(1, 1, 3), forfeitedBy(2, 2, 4)));

        var two = teamOf(tournament, "2");

        assertThat(two.met()).isEmpty();
        assertThat(two.board1()).containsExactly(Optional.empty());
        assertThat(two.mayReceiveBye()).isFalse();
        assertThat(teamOf(tournament, "4").mayReceiveBye()).isTrue();
    }

    @Test
    void barsTheByeForALateEntry() {
        // 4.2.3: 5 joined after the round-1 pairings, so the bye passes it by.
        var tournament = teams(4).withRound(firstRound(match(1, 1, 3), match(2, 4, 2)))
                .enterLate(TournamentMother.participant(5), RoundNumber.of(2));

        assertThat(teamOf(tournament, "5").mayReceiveBye()).isFalse();
        assertThat(tournament.pairNextRound().pairingAllocatedBye()).contains(ParticipantId.of("4"));
    }

    @Test
    void pairsTeams() {
        assertThat(PairingSystems.olympiad().competitionType()).isEqualTo(CompetitionType.TEAM);
        assertThat(PairingSystems.olympiad().name()).isEqualTo("D.02 Olympiad Pairing Rules 2022");
    }

    @Test
    void refusesAcceleration() {
        var settings = Profiles.olympiad(NumberOfRounds.of(11)).with(Acceleration.baku());

        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(8)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("C.04.7");
    }

    @Test
    void refusesAScoringSchemeWithoutMatchPoints() {
        var settings = Profiles.olympiad(NumberOfRounds.of(11)).with(ScoringScheme.standard());

        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(8)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("D.02 3.2.1");
    }

    @Test
    void reportsBoardOneColoursThatBreakALimitTheOtherColoursWouldKeep() {
        // 7.3: 1 had White twice; giving it White again is reported, Black is not.
        var tournament = teams(4).withRound(firstRound(match(1, 1, 3), match(2, 4, 2)))
                .withRound(Round.of(RoundNumber.of(2), List.of(match(1, 1, 2), match(2, 3, 4)), Map.of()));

        var wrong = tournament.check(proposed("1-4", "2-3"));
        var right = tournament.check(proposed("4-1", "2-3"));

        assertThat(wrong.violations()).anyMatch(violation -> violation.article().equals("D.02 7.3"));
        assertThat(right.violations())
                .noneMatch(violation -> violation.article().equals("D.02 7.3"));
    }

    @Test
    void ranksTeamsByTheAverageOfTheirFourHighestRatingsThenTheFifth() {
        // 3.1: A and B average 2500, but B's fifth player is rated; C averages 2325.
        var a = team("1", "A");
        var b = team("2", "B");
        var c = team("3", "C");
        var key = TeamStrength.initialRanking(
                List.of(a, b, c),
                Map.of(
                        a.id(), TeamStrength.of(rating(2600), rating(2400), rating(2500), rating(2500)),
                        b.id(), TeamStrength.of(rating(2500), rating(2500), rating(2500), rating(2500), rating(2300)),
                        c.id(), TeamStrength.of(rating(2700), rating(2600), rating(2000), rating(2000))));

        var tournament = Tournament.of(Profiles.olympiad(NumberOfRounds.of(9)).with(key), List.of(a, b, c));

        assertThat(tournament.pairingNumbers().inOrder()).containsExactly(b.id(), a.id(), c.id());
        assertThat(TeamStrength.of(rating(2600), rating(2402), rating(2500), rating(2500))
                        .averageOfTheFourHighest())
                .isEqualTo(rating(2501));
    }

    private static Tournament teams(int count) {
        return Tournament.of(Profiles.olympiad(NumberOfRounds.of(11)), TournamentMother.participants(count));
    }

    private static Team teamOf(Tournament tournament, String id) {
        return OlympiadTeams.toBePaired(tournament).stream()
                .filter(team -> team.id().equals(ParticipantId.of(id)))
                .findFirst()
                .orElseThrow();
    }

    private static Participant team(String id, String name) {
        return Participant.of(ParticipantId.of(id), Name.of(name), Rating.unrated());
    }

    private static Rating rating(int value) {
        return Rating.of(value);
    }

    private static ProposedPairing proposed(String... whiteBlack) {
        var boards = new ArrayList<ProposedPairing.ProposedBoard>();
        for (var pair : whiteBlack) {
            var ids = pair.split("-");
            boards.add(new ProposedPairing.ProposedBoard(ParticipantId.of(ids[0]), ParticipantId.of(ids[1])));
        }
        return ProposedPairing.of(boards, Optional.empty());
    }

    private static Round firstRound(Board... boards) {
        return Round.of(RoundNumber.FIRST, List.of(boards), Map.of());
    }

    /** A four-board match, every board drawn, whose board-1 White is {@code white}. */
    private static Board match(int number, int white, int black) {
        return board(number, white, black, DRAW);
    }

    /** {@code black} did not arrive: every board is won by forfeit by {@code white}. */
    private static Board forfeitedBy(int number, int white, int black) {
        return board(number, white, black, WHITE_WINS_BY_FORFEIT);
    }

    private static Board board(int number, int white, int black, GameOutcome everyGame) {
        return Board.of(
                number,
                String.valueOf(white),
                String.valueOf(black),
                MatchOutcome.ofGames(Collections.nCopies(4, everyGame)));
    }
}
