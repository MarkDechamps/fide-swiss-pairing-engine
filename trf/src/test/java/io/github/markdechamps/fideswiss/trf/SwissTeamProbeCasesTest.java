package io.github.markdechamps.fideswiss.trf;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.BracketSeating;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.LastRoundZeroCdTypeB;
import io.github.markdechamps.fideswiss.tournament.UpfloaterLookAhead;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;

/**
 * The eight Gacrux probe cases (docs/research/gacrux-team-probe), each paired as Gacrux @ 6419149 with the
 * tpn-order patch pairs it, so under the literal bracket seating ({@link BracketSeating#tpn()}); the other
 * non-default Interpretations as its alt.py counterfactual does (a Witness). The default seating, unpatched Gacrux's,
 * is checked on case 1.
 */
class SwissTeamProbeCasesTest {

    @Test
    void seatsByScoreThenTpnByDefaultAsUnpatchedGacruxDoes() {
        // ADR 0009, KD-1.
        var file = TrfReader.read(resource("case1-bracket-order.trf"));

        assertThat(boardsOf(file.tournament().pairNextRound())).containsExactly("4-1", "3-2", "7-5", "6-8");
    }

    @Test
    void seatsTheSmallerTpnAsTopMember() {
        // Ruling G1 (3.6.1), the literal reading: unpatched Gacrux gives 4-1, 3-2 here.
        assertThat(boardsOf(pair("case1-bracket-order.trf"))).containsExactly("4-2", "3-1", "7-5", "6-8");
    }

    @Test
    void readsC6AsTheParityMinimumByDefault() {
        // Ruling A6, UpfloaterLookAhead.PARITY_MINIMUM.
        assertThat(boardsOf(pair("case2-c6-graded.trf"))).containsExactly("2-1", "9-3", "10-4", "6-8", "5-7");
    }

    @Test
    void readsC6AsGradedWhenAsked() {
        // Ruling A6, alt.py c6-graded (with tpn-order).
        assertThat(boardsOf(pair("case2-c6-graded.trf", UpfloaterLookAhead.graded())))
                .containsExactly("3-1", "6-2", "9-4", "5-8", "10-7");
    }

    @Test
    void minimisesANonZeroC7() {
        // Ruling A7: "complies with" is the best value attainable.
        assertThat(boardsOf(pair("case3-c7-nonzero.trf"))).containsExactly("5-1", "2-3", "4-8", "6-9", "7-10");
    }

    @Test
    void letsTeamsWhoseMatchWasForfeitedMeetAgain() {
        // Ruling A3 (GHR 3.5): 3 and 4 did not play in round 1, so [C1] does not keep them apart.
        assertThat(boardsOf(pair("case4a-forfeit-c1.trf"))).containsExactly("5-3", "4-6", "1-2");
    }

    @Test
    void barsAForfeitWinnerAndAPabTeamFromThePab() {
        // [C2] (ruling A4) and 3.4.3 with ruling A9: forfeited matches are not played.
        var pairing = pair("case4b-forfeit-pab.trf");

        assertThat(boardsOf(pairing)).containsExactly("3-1", "5-4");
        assertThat(pairing.pairingAllocatedBye()).map(Object::toString).contains("2");
    }

    @Test
    void keepsTheStrongTypeBPreferenceInTheLastRoundByDefault() {
        // Ruling A1, LastRoundZeroCdTypeB.STRONG.
        assertThat(boardsOf(pair("case5-typeb-lastround.trf"))).containsExactly("3-6", "2-1", "4-5");
    }

    @Test
    void dropsItWhenAsked() {
        // Ruling A1, alt.py typeb-none.
        assertThat(boardsOf(pair("case5-typeb-lastround.trf", LastRoundZeroCdTypeB.none())))
                .containsExactly("3-6", "1-2", "4-5");
    }

    @Test
    void comparesColourHistoriesFromTheEnd() {
        // Ruling A12 (4.3.6 with GHR 3.4).
        assertThat(boardsOf(pair("case6-433-alignment.trf"))).containsExactly("1-2", "5-3", "6-4");
    }

    @Test
    void judgesFloatersOnThePairingScoreUnderAcceleration() {
        // Ruling A8, FloatScore.PAIRING.
        assertThat(boardsOf(pair("case7-acc-floater.trf"))).containsExactly("2-1", "3-7", "5-8", "10-6", "9-4");
    }

    @Test
    void readsATeamFileAsATeamTournament() {
        var file = TrfReader.read(resource("case1-bracket-order.trf"));

        assertThat(file.settings().pairingSystem().competitionType()).isEqualTo(CompetitionType.TEAM);
        assertThat(file.participants()).hasSize(8);
        assertThat(file.recordedRounds().getFirst().boards()).hasSize(4);
    }

    private static RoundPairing pair(String name, Interpretation... interpretations) {
        var file = TrfReader.read(resource(name));
        var settings = file.settings().with(BracketSeating.tpn());
        for (var interpretation : interpretations) {
            settings = settings.with(interpretation);
        }
        return file.with(settings).tournament().pairNextRound();
    }

    private static List<String> boardsOf(RoundPairing pairing) {
        var boards = new ArrayList<String>();
        pairing.boards().forEach(board -> boards.add(board.white() + "-" + board.black()));
        return boards;
    }

    private static String resource(String name) {
        try (var stream = Objects.requireNonNull(
                SwissTeamProbeCasesTest.class.getResourceAsStream("/swiss-team/probe/" + name), name)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
