package io.github.markdechamps.fideswiss.doubleswiss;

import static io.github.markdechamps.fideswiss.doubleswiss.DoubleSwissPlayerMother.withColours;
import static io.github.markdechamps.fideswiss.pairing.RoundPairingAssert.assertThatPairing;
import static io.github.markdechamps.fideswiss.tournament.Colour.BLACK;
import static io.github.markdechamps.fideswiss.tournament.Colour.WHITE;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.BLACK_WINS_BY_FORFEIT;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.DRAW;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS;
import static io.github.markdechamps.fideswiss.tournament.GameOutcome.WHITE_WINS_BY_FORFEIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.FloatScore;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.InvalidSettingsException;
import io.github.markdechamps.fideswiss.tournament.LastRoundZeroCdTypeB;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DoubleSwissSystemTest {

    @Nested
    class GivenRoundOne {

        @Test
        void pairsTheFirstIdentifierWithColoursByTheHigherRankedPlayersTpn() {
            var pairing = players(6, 5).pairNextRound();

            // C.04.5 3.6.3: the first identifier is 1 2 3 4 5 6; 4.3.1: an odd HRP gets the initial-colour.
            assertThatPairing(pairing).hasBoards("1-4", "5-2", "3-6").givesNoPairingAllocatedBye();
        }

        @Test
        void givesThePabToTheLargestTpnWhenAllScoresAreEqual() {
            var pairing = players(5, 5).pairNextRound();

            // C.04.5 3.4.4, assigned before anyone is paired (3.3.2).
            assertThatPairing(pairing).hasBoards("1-3", "4-2").givesPairingAllocatedByeTo("5");
        }
    }

    @Test
    void mayPairAgainPlayersWhoseMatchWasForfeited() {
        // C.04.5 Preface: "the same pairing may be repeated in a later round"; 2 forfeited both games. No game was
        // played, so neither has a colour (1.6) and 4.3.1 gives the odd HRP the initial-colour again.
        var tournament = players(2, 5)
                .withRound(Round.of(RoundNumber.FIRST, List.of(match(1, 1, 2, forfeitedByBlack())), Map.of()));

        assertThatPairing(tournament.pairNextRound()).hasBoards("1-2");
    }

    @Test
    void countsAMatchOfOneForfeitedGameEachAsAMeetingWithoutAColour() {
        // C.04.5 Preface and 1.6: each player forfeited one game, so the match was regularly played ([C1]) but no
        // game was actually played, so neither had a colour.
        var tournament = players(4, 5)
                .withRound(Round.of(
                        RoundNumber.FIRST,
                        List.of(
                                match(1, 1, 3, WHITE_WINS_BY_FORFEIT, BLACK_WINS_BY_FORFEIT),
                                match(2, 2, 4, DRAW, DRAW)),
                        Map.of()));

        var players = new PlayersToPair(tournament, FloatScore.pairing()).toBePaired(tournament.pairingNumbers());

        var one = players.getFirst();
        assertThat(one.met()).containsExactly(DoubleSwissPlayerMother.id(3));
        assertThat(one.colours()).isEmpty();
        assertThat(one.matchesPlayed()).isEqualTo(1);
    }

    @Test
    void barsThePabAfterAMatchWonByForfeit() {
        // C.04.5 [C2]: 2 won its match by forfeit in round 1 and 3 had the PAB; 1, who lost, is not barred.
        var tournament = players(3, 5)
                .withRound(Round.of(
                        RoundNumber.FIRST,
                        List.of(match(1, 2, 1, forfeitedByBlack())),
                        Map.of(TournamentMother.id(3), Bye.PAIRING_ALLOCATED)));

        var players = new PlayersToPair(tournament, FloatScore.pairing()).toBePaired(tournament.pairingNumbers());

        assertThat(players)
                .filteredOn(Contender::pairingAllocatedByeBarred)
                .map(Contender::tpn)
                .containsExactlyInAnyOrder(2, 3);
    }

    @Test
    void valuesThePabAtAGameWonAndAGameDrawn() {
        // C.04.5 1.4.
        assertThat(Profiles.doubleSwiss(NumberOfRounds.of(5)).pairingAllocatedByeValue())
                .isEqualTo(Points.of("1.5"));
    }

    @Test
    void valuesAByeAtAMatch() {
        // C.04.5 Preface: "Byes ... apply only to matches, never to individual games."
        var scoring = ScoringScheme.doubleSwiss();

        assertThat(scoring.pointsFor(Bye.FULL_POINT, Points.of("1.5"))).isEqualTo(Points.of(2));
        assertThat(scoring.pointsFor(Bye.HALF_POINT, Points.of("1.5"))).isEqualTo(Points.of(1));
    }

    @Test
    void scoresAMatchByItsGamePoints() {
        // C.04.5 Preface: a match ends 1½-½ when one game is won and the other drawn.
        var scoring = ScoringScheme.doubleSwiss();
        var match = MatchOutcome.ofGames(List.of(WHITE_WINS, DRAW));

        assertThat(scoring.pointsFor(match, WHITE)).isEqualTo(Points.of("1.5"));
        assertThat(scoring.pointsFor(match, BLACK)).isEqualTo(Points.of("0.5"));
    }

    @Nested
    class GivenColours {

        private final ColourAllocation colours = new ColourAllocation(InitialColour.white());

        @Test
        void givesWhiteToTheFewerWhitesNotTheColourDifference() {
            // C.04.5 4.3.2: 1 had W B B W (two Whites), 2 had W (one White), so 2 gets White.
            var pair = ContenderPair.of(withColours(1, "4", WHITE, BLACK, BLACK, WHITE), withColours(2, "4", WHITE));

            assertThat(colours.allocate(pair))
                    .extracting(c -> c.white().tpn(), c -> c.article())
                    .containsExactly(2, "C.04.5 4.3.2");
        }

        @Test
        void alternatesToTheMostRecentDifferenceAlignedAtTheEnd() {
            // C.04.5 4.3.3 with GHR 3.4: 1 had W B B and 2 had B W B; their last colours agree, the one before
            // differs (1 Black, 2 White), so 1 gets White.
            var pair = ContenderPair.of(
                    withColours(1, "4", WHITE, BLACK, BLACK), withColours(2, "4", BLACK, WHITE, BLACK));

            assertThat(colours.allocate(pair))
                    .extracting(c -> c.white().tpn(), c -> c.article())
                    .containsExactly(1, "C.04.5 4.3.3");
        }

        @Test
        void alternatesTheHigherRankedPlayer() {
            // C.04.5 4.3.4: identical histories; the HRP (the higher score, 2) had White last.
            var pair = ContenderPair.of(withColours(1, "2", BLACK, WHITE), withColours(2, "3", BLACK, WHITE));

            assertThat(colours.allocate(pair))
                    .extracting(c -> c.white().tpn(), c -> c.article())
                    .containsExactly(1, "C.04.5 4.3.4");
        }

        @Test
        void alternatesTheOpponentWhenTheHigherRankedPlayerHasNoColour() {
            // C.04.5 4.3.5: neither has a White (4.3.2) and the HRP 1 has yet to have a colour; 2 had Black.
            var pair = ContenderPair.of(withColours(1, "3"), withColours(2, "2", BLACK));

            assertThat(colours.allocate(pair))
                    .extracting(c -> c.white().tpn(), c -> c.article())
                    .containsExactly(2, "C.04.5 4.3.5");
        }
    }

    @Test
    void takesItsInterpretations() {
        var settings = Profiles.doubleSwiss(NumberOfRounds.of(5)).with(UpfloaterLookAhead.graded());

        assertThat(settings.pairingSystem())
                .isEqualTo(DoubleSwissSystem.withDefaults().with(UpfloaterLookAhead.graded()));
    }

    @Test
    void refusesAColourPreferenceInterpretation() {
        assertThatThrownBy(() -> PairingSystems.doubleSwiss().with(LastRoundZeroCdTypeB.strong()))
                .isInstanceOf(InvalidSettingsException.class);
    }

    @Test
    void refusesThePre2026Edition() {
        // GHR 1.3: C.04.5 has only its 2026 text.
        var settings = Profiles.doubleSwiss(NumberOfRounds.of(5)).with(SwissRulesEdition.PRE_2026);

        assertThatThrownBy(() -> Tournament.of(settings, TournamentMother.participants(6)))
                .isInstanceOf(InvalidSettingsException.class)
                .hasMessageContaining("GHR 1.3");
    }

    private static Tournament players(int count, int rounds) {
        return Tournament.of(Profiles.doubleSwiss(NumberOfRounds.of(rounds)), TournamentMother.participants(count));
    }

    /** The game-1 Black forfeits both games. */
    private static GameOutcome[] forfeitedByBlack() {
        return new GameOutcome[] {WHITE_WINS_BY_FORFEIT, WHITE_WINS_BY_FORFEIT};
    }

    /** A match whose game-1 White is {@code white}; each game is seen from that player's side. */
    private static Board match(int number, int white, int black, GameOutcome... games) {
        return Board.of(number, String.valueOf(white), String.valueOf(black), MatchOutcome.ofGames(List.of(games)));
    }
}
