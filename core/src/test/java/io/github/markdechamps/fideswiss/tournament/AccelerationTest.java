package io.github.markdechamps.fideswiss.tournament;

import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AccelerationTest {

    @Nested
    class GivenBakuAcceleration {

        private final Tournament tournament =
                Tournament.of(Profiles.acceleratedOpen(NumberOfRounds.of(9)), TournamentMother.participants(12));

        @Test
        void givesTheAcceleratedGroupAWinsPointsInTheFirstHalfOfTheAcceleratedRounds() {
            // C.04.7 1.4: 9 rounds accelerate the first 5, three at a win's points and two at half of it.
            var virtualPoints = IntStream.rangeClosed(1, 7)
                    .mapToObj(round -> tournament
                            .virtualPointsOf(id(1), RoundNumber.of(round))
                            .toString())
                    .toList();

            assertThat(virtualPoints).containsExactly("1", "1", "1", "0.5", "0.5", "0", "0");
        }

        @Test
        void accelerates2TimesNOver4RoundedUpOfTheRoundOneList() {
            // C.04.7 1.2: GA is the first 2·⌈12/4⌉ = 6 participants.
            assertThat(tournament.virtualPointsOf(id(6), RoundNumber.FIRST)).isEqualTo(Points.of(1));
            assertThat(tournament.virtualPointsOf(id(7), RoundNumber.FIRST)).isEqualTo(Points.ZERO);
        }

        @Test
        void sizesTheAcceleratedGroupOf161At82() {
            var large =
                    Tournament.of(Profiles.acceleratedOpen(NumberOfRounds.of(9)), TournamentMother.participants(161));

            assertThat(large.virtualPointsOf(id(82), RoundNumber.FIRST)).isEqualTo(Points.of(1));
            assertThat(large.virtualPointsOf(id(83), RoundNumber.FIRST)).isEqualTo(Points.ZERO);
        }

        @Test
        void usesThePointsForAWinOfTheScoringScheme() {
            var threePoints = new ScoringScheme(Points.of(2), Points.of(1), Points.ZERO, java.util.Optional.empty());
            var tournament = Tournament.of(
                    Profiles.acceleratedOpen(NumberOfRounds.of(9)).with(threePoints), TournamentMother.participants(8));

            assertThat(tournament.virtualPointsOf(id(1), RoundNumber.of(4))).isEqualTo(Points.of(1));
        }

        @Test
        void isRejectedWhenAWinIsNotWorthTwoDraws() {
            var football = new ScoringScheme(Points.of(3), Points.of(1), Points.ZERO, java.util.Optional.empty());

            // C.04.7 1.1.
            assertThatThrownBy(() -> Tournament.of(
                            Profiles.acceleratedOpen(NumberOfRounds.of(9)).with(football),
                            TournamentMother.participants(8)))
                    .isInstanceOf(InvalidSettingsException.class);
        }

        @Test
        void pairsRoundOneWithinEachGroup() {
            var pairing = Tournament.of(
                            Profiles.acceleratedOpen(NumberOfRounds.of(9)), TournamentMother.participants(8))
                    .pairNextRound();

            // GA = 1-4 on a Pairing Score of 1, GB = 5-8 on 0: 1-3, 4-2 and 5-7, 8-6.
            assertThatPairing(pairing).hasBoards("1-3", "4-2", "5-7", "8-6");
        }
    }

    @Nested
    class GivenExplicitVirtualPoints {

        @Test
        void addsThemToThePairingScoreOfThatRoundOnly() {
            var virtualPoints = VirtualPoints.of(Map.of(id(4), Map.of(RoundNumber.FIRST, Points.of(1))));
            var tournament = Tournament.of(
                    Profiles.individualSwiss(NumberOfRounds.of(5)).with(Acceleration.explicit(virtualPoints)),
                    TournamentMother.participants(4));

            var pairing = tournament.pairNextRound();

            // 4 pairs on 1 point alone and floats down to the top of 0: 4 meets 1, then 2 meets 3.
            assertThatPairing(pairing).hasBoardsInAnyOrder("1-4", "3-2");
            assertThat(tournament.virtualPointsOf(id(4), RoundNumber.of(2))).isEqualTo(Points.ZERO);
        }
    }
}
