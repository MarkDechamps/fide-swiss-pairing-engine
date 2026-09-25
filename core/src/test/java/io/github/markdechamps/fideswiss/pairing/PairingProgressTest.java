package io.github.markdechamps.fideswiss.pairing;

import static io.github.markdechamps.fideswiss.tournament.TournamentMother.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.RequestedBye;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.TournamentMother;
import java.util.ArrayList;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PairingProgressTest {

    @Nested
    class GivenAListener {

        @Test
        void countsParticipantsSettledUpToEveryoneToBePaired() {
            var tournament = TournamentMother.playNextRound(
                    TournamentMother.individualSwiss(7, 5),
                    GameOutcome.WHITE_WINS,
                    GameOutcome.DRAW,
                    GameOutcome.BLACK_WINS);
            var reports = new ArrayList<Progress>();

            tournament.pairNextRound(new PairingProgress() {
                @Override
                public void advanced(Progress progress) {
                    reports.add(progress);
                }
            });

            assertThat(reports).isNotEmpty();
            assertThat(reports)
                    .allSatisfy(progress -> assertThat(progress.toPair()).isEqualTo(7));
            assertThat(reports.stream().map(Progress::settled).toList()).isSorted();
            assertThat(reports.getLast().settled()).isEqualTo(7);
        }

        @Test
        void announcesEachBracketBeforePairingIt() {
            var tournament = TournamentMother.playNextRound(
                    TournamentMother.individualSwiss(4, 5), GameOutcome.WHITE_WINS, GameOutcome.DRAW);
            var steps = new ArrayList<String>();

            tournament.pairNextRound(new PairingProgress() {
                @Override
                public void stepStarted(ProgressStep step) {
                    steps.add(step.label());
                }
            });

            assertThat(steps).containsExactly("bracket 1", "bracket 0.5", "bracket 0");
        }
    }

    @Nested
    class GivenAnInterruptedThread {

        @Test
        void stopsWithTheTraceSoFar() {
            var tournament = TournamentMother.individualSwiss(8, 5);

            Thread.currentThread().interrupt();

            assertThatThrownBy(tournament::pairNextRound).isInstanceOf(PairingCancelledException.class);
            assertThat(Thread.interrupted()).isFalse();
        }
    }

    @Nested
    class GivenAParticipantsQuestion {

        private final RoundPairing pairing = TournamentMother.playNextRound(
                        TournamentMother.individualSwiss(5, 5)
                                .requestBye(id(5), RoundNumber.of(2), RequestedBye.zero()),
                        GameOutcome.WHITE_WINS,
                        GameOutcome.DRAW)
                .pairNextRound();

        @Test
        void explainsWhereItWasPairedWithWhomAndByWhichColourRule() {
            var explanation = pairing.about(id(1));

            // 1 is alone on 1 point, floats down (1.3.2) and meets 2 in the half-point bracket.
            assertThat(explanation.bracket()).contains("0.5");
            assertThat(explanation.floatedFrom()).containsExactly("1");
            assertThat(explanation.opponent()).contains(id(2));
            assertThat(explanation.colourArticle())
                    .hasValueSatisfying(article -> assertThat(article).startsWith("C.04.3 5.2"));
            assertThat(explanation.describe()).contains("paired with 2", "floated down from bracket 1");
        }

        @Test
        void explainsAResidentPairedInItsOwnBracket() {
            var explanation = pairing.about(id(3));

            assertThat(explanation.bracket()).contains("0");
            assertThat(explanation.floatedFrom()).isEmpty();
            assertThat(explanation.opponent()).contains(id(4));
        }

        @Test
        void explainsWhyAParticipantWasNotPaired() {
            var explanation = pairing.about(id(5));

            assertThat(explanation.unpaired()).contains(Bye.ZERO_POINT);
            assertThat(explanation.describe()).contains("not paired");
        }
    }
}
