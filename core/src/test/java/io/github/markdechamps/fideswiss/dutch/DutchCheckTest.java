package io.github.markdechamps.fideswiss.dutch;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.ProposedPairing;
import io.github.markdechamps.fideswiss.pairing.ProposedPairing.ProposedBoard;
import io.github.markdechamps.fideswiss.pairing.Violation;
import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DutchCheckTest {

    private static final ProposedPairing WHITE_PLAYERS_MEET_AND_BLACK_PLAYERS_MEET = ProposedPairing.of(
            List.of(new ProposedBoard(id(1), id(2)), new ProposedBoard(id(3), id(4))), Optional.empty());

    @Test
    void forbidsTwoNonTopscorersWithTheSameAbsolutePreferenceFromMeeting() {
        // 1 and 2 had White twice (absolute preference for Black, 1.7.1); round 3 of 5 has no topscorers.
        var tournament = twoRoundsWhere1And2HadWhite(TournamentMother.individualSwiss(4, 5), GameOutcome.DRAW);

        var check = tournament.check(WHITE_PLAYERS_MEET_AND_BLACK_PLAYERS_MEET);

        assertThat(check.violations()).extracting(Violation::article).containsExactly("C.04.3 [C3]", "C.04.3 [C3]");
        assertThat(check.violations().getFirst().participants()).containsExactly(id(1), id(2));
    }

    @Test
    void letsTopscorersWithTheSameAbsolutePreferenceMeetInTheLastRound() {
        // 1 and 2 won twice, so in the last of 3 rounds they score over 50% of the maximum (1.8); 3 and 4 do not.
        var tournament = twoRoundsWhere1And2HadWhite(TournamentMother.individualSwiss(4, 3), GameOutcome.WHITE_WINS);

        var check = tournament.check(WHITE_PLAYERS_MEET_AND_BLACK_PLAYERS_MEET);

        assertThat(check.violations()).singleElement().satisfies(violation -> {
            assertThat(violation.article()).isEqualTo("C.04.3 [C3]");
            assertThat(violation.participants()).containsExactly(id(3), id(4));
        });
    }

    @Test
    void citesTheOldTextUnderThePre2026Edition() {
        var settings = Profiles.individualSwiss(NumberOfRounds.of(5)).with(SwissRulesEdition.PRE_2026);
        var tournament = twoRoundsWhere1And2HadWhite(
                Tournament.of(settings, TournamentMother.participants(4)), GameOutcome.DRAW);

        var check = tournament.check(WHITE_PLAYERS_MEET_AND_BLACK_PLAYERS_MEET);

        assertThat(check.violations())
                .extracting(Violation::article)
                .containsExactly("C.04.3 (2017) C.3", "C.04.3 (2017) C.3");
    }

    private static Tournament twoRoundsWhere1And2HadWhite(Tournament tournament, GameOutcome outcome) {
        return tournament
                .withRound(Round.of(
                        RoundNumber.of(1),
                        List.of(Board.of(1, "1", "3", outcome), Board.of(2, "2", "4", outcome)),
                        Map.of()))
                .withRound(Round.of(
                        RoundNumber.of(2),
                        List.of(Board.of(1, "1", "4", outcome), Board.of(2, "2", "3", outcome)),
                        Map.of()));
    }
}
