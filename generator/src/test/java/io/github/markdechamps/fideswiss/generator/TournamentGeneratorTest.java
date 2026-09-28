package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TournamentGeneratorTest {

    private static final GeneratorSettings DUTCH = GeneratorSettings.of(Profiles.individualSwiss(NumberOfRounds.of(9)));

    @Nested
    class GivenASeed {

        @Test
        void playsEveryRoundOfATournamentWithinTheRanges() {
            var settings = DUTCH.withPlayers(Range.of(20, 30)).withRounds(Range.of(7));

            var tournament = completed(settings, 1);

            assertThat(tournament.participants()).hasSizeBetween(20, 30);
            assertThat(tournament.settings().numberOfRounds()).isEqualTo(NumberOfRounds.of(7));
            assertThat(tournament.rounds()).hasSize(7);
        }

        @Test
        void producesTheSameTournamentEveryTime() {
            var first = completed(DUTCH.withPlayers(Range.of(16)), 42);
            var again = completed(DUTCH.withPlayers(Range.of(16)), 42);

            assertThat(again.rounds()).isEqualTo(first.rounds());
            assertThat(again.participants()).isEqualTo(first.participants());
        }

        @Test
        void derivesDifferentTournamentsFromOneCorpusSeed() {
            var corpus = CorpusSeed.of(7);

            assertThat(corpus.tournament(0)).isNotEqualTo(corpus.tournament(1));
            assertThat(corpus.tournament(3)).isEqualTo(CorpusSeed.of(7).tournament(3));
        }

        @Test
        void playsTheSwissRulesEditionOfTheSettings() {
            var settings = DUTCH.with(DUTCH.tournament().with(SwissRulesEdition.PRE_2026));

            var tournament = completed(settings.withPlayers(Range.of(20)), 9);

            assertThat(tournament.settings().swissRulesEdition()).isEqualTo(SwissRulesEdition.PRE_2026);
            assertThat(tournament.rounds())
                    .hasSize(tournament.settings().numberOfRounds().value());
        }

        @Test
        void keepsTheFieldAtLeastOneLargerThanTheNumberOfRounds() {
            var tournament = completed(DUTCH.withPlayers(Range.of(6)).withRounds(Range.of(9)), 3);

            assertThat(tournament.settings().numberOfRounds().value()).isLessThanOrEqualTo(5);
        }
    }

    @Nested
    class GivenTheFieldParameters {

        @Test
        void listsParticipantsStrongestFirstWithRatingsInTheDrawnSpan() {
            var settings = DUTCH.withPlayers(Range.of(40))
                    .withHighestRating(Range.of(2600))
                    .withLowestRating(Range.of(1600))
                    .withUnratedPercentage(Range.of(0));

            var ratings = completed(settings, 5).participants().stream()
                    .map(participant -> participant.rating().valueOrZero())
                    .toList();

            assertThat(ratings).isSortedAccordingTo((a, b) -> Integer.compare(b, a));
            assertThat(ratings).allSatisfy(rating -> assertThat(rating).isBetween(1600, 2600));
        }

        @Test
        void leavesTheGivenShareOfTheFieldUnrated() {
            var settings = DUTCH.withPlayers(Range.of(50)).withUnratedPercentage(Range.of(10));

            var unrated = completed(settings, 5).participants().stream()
                    .filter(participant -> participant.rating().equals(Rating.unrated()))
                    .count();

            assertThat(unrated).isEqualTo(5);
        }
    }

    @Nested
    class GivenFrequentEvents {

        private final GeneratorSettings eventful = DUTCH.withPlayers(Range.of(40))
                .withRounds(Range.of(9))
                .withForfeitRate(Range.of(4))
                .withHalfPointByeRate(Range.of(8))
                .withZeroPointByeRate(Range.of(8))
                .withWithdrawalPercentage(Range.of(10));

        @Test
        void recordsForfeitsByesAndWithdrawals() {
            var rounds = completed(eventful, 11).rounds();

            assertThat(rounds)
                    .anySatisfy(round -> assertThat(round.boards())
                            .anySatisfy(board ->
                                    assertThat(board.outcome().isPlayed()).isFalse()));
            assertThat(rounds).anySatisfy(round -> assertThat(round.byes()).containsValue(Bye.HALF_POINT));
            assertThat(rounds).anySatisfy(round -> assertThat(round.byes()).containsValue(Bye.ZERO_POINT));
            assertThat(rounds).anySatisfy(round -> assertThat(round.byes()).containsValue(Bye.WITHDRAWN));
        }

        @Test
        void neverGivesARequestedByeInTheLastRound() {
            var rounds = completed(eventful, 11).rounds();

            assertThat(rounds.getLast().byes())
                    .doesNotContainValue(Bye.HALF_POINT)
                    .doesNotContainValue(Bye.ZERO_POINT);
        }
    }

    @Nested
    class GivenARoundWithoutALegalPairing {

        @Test
        void skipsTheTournamentAndSaysWhere() {
            var settings = GeneratorSettings.of(
                    Profiles.individualSwiss(NumberOfRounds.of(5)).with(new Unpairable()));

            var generated = TournamentGenerator.of(settings).generate(TournamentSeed.of(1));

            assertThat(generated).isInstanceOfSatisfying(GeneratedTournament.Skipped.class, skipped -> {
                assertThat(skipped.round().value()).isEqualTo(1);
                assertThat(skipped.reason()).contains("no pairing");
            });
        }
    }

    private static Tournament completed(GeneratorSettings settings, long seed) {
        var generated = TournamentGenerator.of(settings).generate(TournamentSeed.of(seed));
        assertThat(generated).isInstanceOf(GeneratedTournament.Completed.class);
        return ((GeneratedTournament.Completed) generated).tournament();
    }

    private static final class Unpairable implements PairingSystem {
        @Override
        public RoundPairing pairNextRound(Tournament tournament) {
            throw new NoLegalPairingException(List.of(Problem.of("no pairing")), new PairingTrace(List.of()));
        }
    }
}
