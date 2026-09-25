package io.github.markdechamps.fideswiss.dubov;

import static io.github.markdechamps.fideswiss.dubov.DubovPlayerMother.fresh;
import static io.github.markdechamps.fideswiss.dubov.DubovPlayerMother.player;
import static io.github.markdechamps.fideswiss.dubov.DubovPlayerMother.withOpponentRatings;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The worked examples of C.04.4.1 (2026), and hand-made cases for criteria random rounds rarely reach. */
class DubovHandbookExamplesTest {

    @Test
    void numbersShiftersFromTheMiddleOut() {
        // 4.3.3: A-G are numbered D, C, E, B, F, A, G.
        assertThat(ShifterOrder.middleOut(List.of("A", "B", "C", "D", "E", "F", "G")))
                .containsExactly("D", "C", "E", "B", "F", "A", "G");
    }

    @Test
    void ordersTranspositionsLexicographically() {
        // 4.4.2.
        assertThat(LiteralDubov.permutations(List.of(fresh(1), fresh(2), fresh(3))).stream()
                        .map(order -> order.stream().map(Player::tpn).toList())
                        .toList())
                .containsExactly(
                        List.of(1, 2, 3),
                        List.of(1, 3, 2),
                        List.of(2, 1, 3),
                        List.of(2, 3, 1),
                        List.of(3, 1, 2),
                        List.of(3, 2, 1));
    }

    @Test
    void computesTheMaximumNumberOfUpfloatsFromThePlannedRounds() {
        // 1.8.2: MaxT = 2 + [Rnds/5].
        assertThat(List.of(maxT(4), maxT(5), maxT(9), maxT(10), maxT(11))).containsExactly(2, 3, 3, 4, 4);
    }

    @Test
    void roundsTheAverageRatingOfOpponentsHalfUp() {
        // 1.7.1.
        assertThat(withOpponentRatings(fresh(1), 1500, 1501).aro()).isEqualTo(1501);
        assertThat(withOpponentRatings(fresh(1), 1500, 1502).aro()).isEqualTo(1501);
        assertThat(fresh(1).aro()).isZero();
    }

    @Test
    void givesPlayersWithoutGamesAMildPreferenceForBlack() {
        // 1.6.4.
        assertThat(fresh(1).preference()).isEqualTo(new ColourPreference(Colour.BLACK, ColourPreference.Strength.MILD));
    }

    @Test
    void avoidsAMaximumUpfloaterBeforeCountingUpfloats() {
        // [C8] before [C9]: residents 1 and 2 have met; the upfloaters come from 3 (9 upfloats), 4 (none), 5 and
        // 6 (3 each; MaxT is 3). {4,5} and {4,6} leave 3 with an opponent it has met, so the choice is {3,4} (one
        // Maximum Upfloater, 9 upfloats) against {5,6} (two, 6 upfloats). [C8] picks {3,4}.
        var p1 = player(1, 2, List.of(Colour.WHITE), Set.of(2), 0);
        var p2 = player(2, 2, List.of(Colour.BLACK), Set.of(1), 0);
        var p3 = player(3, 0, List.of(Colour.WHITE), Set.of(5, 6), 9);
        var p4 = player(4, 0, List.of(Colour.BLACK), Set.of(), 0);
        var p5 = player(5, 0, List.of(Colour.WHITE), Set.of(3), 3);
        var p6 = player(6, 0, List.of(Colour.BLACK), Set.of(3), 3);
        var round = new RoundToPair(RoundNumber.of(2), NumberOfRounds.of(9), InitialColour.white());
        var players = List.of(p1, p2, p3, p4, p5, p6);

        var outcome = new DubovProcedure(round).pair(players);

        assertThat(outcome.brackets().getFirst().upfloaters()).containsExactlyInAnyOrder(p3, p4);
        assertThat(games(outcome)).isEqualTo(games(new LiteralDubov(round).pair(players)));
    }

    private static List<String> games(DubovProcedure.Outcome outcome) {
        return outcome.games().stream()
                .map(game -> game.white().id() + "-" + game.black().id())
                .sorted()
                .toList();
    }

    private static int maxT(int rounds) {
        return new RoundToPair(RoundNumber.FIRST, NumberOfRounds.of(rounds), InitialColour.white()).maxT();
    }
}
