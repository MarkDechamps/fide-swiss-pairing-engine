package io.github.markdechamps.fideswiss.burstein;

import static io.github.markdechamps.fideswiss.burstein.BursteinPlayerMother.player;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** The worked examples of C.04.4.2 (2026), and hand-made cases for criteria random rounds rarely reach. */
class BursteinHandbookExamplesTest {

    private static final RoundToPair ROUND_FIVE_OF_NINE =
            new RoundToPair(RoundNumber.of(5), NumberOfRounds.of(9), InitialColour.white());

    @Test
    void ordersTheCandidatesOfTheNoteTo4Point3() {
        // 4.3 note: six players, two pairs, two virtual players. The text lists "1-3, 2-0, 4-5, 6-0" twice; the
        // order has it once, so 45 candidates, the first being 1-6, 2-5, 3-0, 4-0.
        var candidates = LiteralBurstein.candidates(6, 2).stream()
                .map(LiteralBurstein.Candidate::toString)
                .toList();
        assertThat(candidates).hasSize(45);
        assertThat(candidates.subList(0, 7))
                .containsExactly(
                        "1-6, 2-5, 3-0, 4-0",
                        "1-6, 2-4, 3-0, 5-0",
                        "1-6, 2-3, 4-0, 5-0",
                        "1-6, 2-0, 3-5, 4-0",
                        "1-6, 2-0, 3-4, 5-0",
                        "1-6, 2-0, 3-0, 4-5",
                        "1-5, 2-6, 3-0, 4-0");
        assertThat(candidates.subList(21, 24))
                .containsExactly("1-3, 2-0, 4-6, 5-0", "1-3, 2-0, 4-5, 6-0", "1-3, 2-0, 4-0, 5-6");
        assertThat(candidates.getLast()).isEqualTo("1-0, 2-0, 3-4, 5-6");
    }

    @Test
    void choosesTheFirstCandidateOfTheNoteWhenTheCriteriaTie() {
        // Players 3 and 4 have met everyone else in the bracket, so only two pairs can be made; all score alike.
        var met = Set.of(1, 2, 5, 6, 3, 4);
        var bracket = List.of(
                player(1, 3, List.of(), Set.of(3, 4), 60),
                player(2, 3, List.of(), Set.of(3, 4), 50),
                player(3, 3, List.of(), without(met, 3), 40),
                player(4, 3, List.of(), without(met, 4), 30),
                player(5, 3, List.of(), Set.of(3, 4), 20),
                player(6, 3, List.of(), Set.of(3, 4), 10));
        var lower = List.of(player(7, 1, List.of(), Set.of(), 5), player(8, 1, List.of(), Set.of(), 4));

        var result = new BracketPairing(ROUND_FIVE_OF_NINE).pair(bracket, lower);

        assertThat(result.pairs())
                .extracting(pair -> pair.first().tpn() + "-" + pair.second().tpn())
                .containsExactly("1-6", "2-5");
        assertThat(result.outgoing()).extracting(Player::tpn).containsExactly(3, 4);
    }

    @Test
    void seedsHalfTheRoundsRoundedDownOrFour() {
        // 1.6.2.
        assertThat(IntStream.of(3, 5, 7, 8, 9, 11, 13)
                        .mapToObj(rounds -> new RoundToPair(
                                        RoundNumber.of(1), NumberOfRounds.of(rounds), InitialColour.white())
                                .seedingRounds())
                        .toList())
                .containsExactly(1, 2, 3, 4, 4, 4, 4);
    }

    @Test
    void ranksAnIncomingFloaterWithTheResidentsByIndexAlone() {
        // 1.8 note: players' scores are not used in the ranking order.
        var floater = player(1, 6, List.of(Colour.WHITE), Set.of(), 10);
        var resident = player(2, 4, List.of(Colour.BLACK), Set.of(), 12);

        assertThat(BursteinProcedure.bracket(List.of(floater, resident), List.of(floater)))
                .containsExactly(resident, floater);
    }

    @Test
    void givesThePairingAllocatedByeToTheLowestRankedEligiblePlayer() {
        // 3.1.3–3.1.5: same score and games, so the lowest ranking (1.8) of those allowed [C2] decides.
        var a = player(1, 2, List.of(Colour.WHITE, Colour.BLACK), Set.of(), 9);
        var b = player(2, 2, List.of(Colour.WHITE, Colour.BLACK), Set.of(), 8);
        var c = player(3, 2, List.of(Colour.WHITE, Colour.BLACK), Set.of(), 7, false);

        assertThat(new PairingAllocatedByeAssignment(ROUND_FIVE_OF_NINE).assign(List.of(a, b, c)))
                .isEqualTo(Optional.of(b));
    }

    @Test
    void grantsTheOpponentsPreferenceToAPlayerWhoHasNotPlayed() {
        // 1.5.4 with 5.2.2.
        var newcomer = player(1, 0, List.of(), Set.of(), 30);
        var player = player(2, 0, List.of(Colour.WHITE), Set.of(), 10);

        var game = new ColourAllocation(ROUND_FIVE_OF_NINE).allocate(Pair.of(newcomer, player));

        assertThat(game.white()).isSameAs(newcomer);
        assertThat(game.article()).isEqualTo("C.04.4.2 5.2.2");
    }

    @Test
    void floatsTheLowerScoreRatherThanTheHigher() {
        // [C6]: with one floater unavoidable, it is taken from the lower score of a bracket with an incoming floater.
        var incoming = player(1, 4, List.of(Colour.WHITE), Set.of(), 50);
        var residentA = player(2, 3, List.of(Colour.WHITE), Set.of(), 40);
        var residentB = player(3, 3, List.of(Colour.BLACK), Set.of(), 30);
        var below = player(4, 1, List.of(Colour.BLACK), Set.of(), 20);

        var result =
                new BracketPairing(ROUND_FIVE_OF_NINE).pair(List.of(incoming, residentA, residentB), List.of(below));

        // [C6] floats a score-3 resident, not the incoming 4; [C8] then pairs 1 with 3, who wants the other colour.
        assertThat(result.outgoing()).extracting(Player::tpn).containsExactly(2);
    }

    private static Set<Integer> without(Set<Integer> numbers, int number) {
        return numbers.stream().filter(other -> other != number).collect(java.util.stream.Collectors.toSet());
    }
}
