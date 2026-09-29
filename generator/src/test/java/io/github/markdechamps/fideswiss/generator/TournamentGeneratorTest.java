package io.github.markdechamps.fideswiss.generator;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingTrace;
import io.github.markdechamps.fideswiss.pairing.RoundPairing;
import io.github.markdechamps.fideswiss.standings.TieBreakCode;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.MatchOutcome;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Problem;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
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
    class GivenLateEntries {

        private final GeneratorSettings late =
                DUTCH.withPlayers(Range.of(40)).withRounds(Range.of(9)).withLateEntryPercentage(Range.of(10));

        @Test
        void entersTheGivenShareOfTheFieldAfterTheFirstRound() {
            var tournament = completed(late, 13);

            assertThat(tournament.participants()).hasSize(40);
            assertThat(tournament.rounds().getFirst().byes().values())
                    .filteredOn(Bye.NOT_YET_ENTERED::equals)
                    .hasSize(4);
        }

        @Test
        void entersEveryLateParticipantByTheMiddleRound() {
            var rounds = completed(late, 13).rounds();

            assertThat(rounds.get(4).byes()).doesNotContainValue(Bye.NOT_YET_ENTERED);
        }
    }

    @Nested
    class GivenRandomVariations {

        private final GeneratorSettings small = DUTCH.withPlayers(Range.of(12)).withRounds(Range.of(5));

        @Test
        void acceleratesSomeTournamentsByBaku() {
            var accelerations = settingsOf(small.withRandomAcceleration()).stream()
                    .map(TournamentSettings::acceleration)
                    .toList();

            assertThat(accelerations).contains(Acceleration.baku(), Acceleration.none());
        }

        @Test
        void scoresSomeTournamentsOtherwiseThanOneHalfZero() {
            var scorings = settingsOf(small.withRandomScoring()).stream()
                    .map(TournamentSettings::scoring)
                    .toList();

            assertThat(scorings).contains(ScoringScheme.standard());
            assertThat(scorings).anySatisfy(scoring -> assertThat(scoring).isNotEqualTo(ScoringScheme.standard()));
        }

        @Test
        void drawsThreeToFiveTieBreaksFromTheEditionsCatalogue() {
            var old = small.with(small.tournament().with(TieBreakEdition.EDITION_2024_08));

            var lists = settingsOf(old.withRandomTieBreaks()).stream()
                    .map(TournamentSettings::tieBreakList)
                    .toList();

            assertThat(lists).allSatisfy(list -> assertThat(list.codes()).hasSizeBetween(3, 5));
            assertThat(lists)
                    .flatExtracting(TieBreakList::codes)
                    .extracting(TieBreakCode::acronym)
                    .doesNotContain("STD", "TPN", "RTNG");
            assertThat(lists).doesNotHaveDuplicates();
        }

        @Test
        void recordsTheDrawnVariationsWithTheParameters() {
            var generated = TournamentGenerator.of(small.withRandomTieBreaks()).generate(TournamentSeed.of(4));

            assertThat(generated)
                    .isInstanceOfSatisfying(
                            GeneratedTournament.Completed.class,
                            completed -> assertThat(completed.parameters().tieBreaks())
                                    .isEqualTo(completed.tournament().settings().tieBreakList()));
        }

        private static List<TournamentSettings> settingsOf(GeneratorSettings settings) {
            var corpus = CorpusSeed.of(99);
            return IntStream.range(0, 40)
                    .mapToObj(index ->
                            completed(settings, corpus.tournament(index)).settings())
                    .toList();
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

    @Nested
    class GivenATeamSystem {

        private final GeneratorSettings teams = GeneratorSettings.ofTeams(Profiles.teamSwiss(NumberOfRounds.of(9)))
                .withPlayers(Range.of(10, 14))
                .withRounds(Range.of(5))
                .withBoards(Range.of(3, 5));

        @Test
        void playsMatchesOfTheDrawnNumberOfBoardsBetweenNamedTeams() {
            var tournament = completed(teams, 3);

            var boards = tournament.settings().scoring().matches().orElseThrow().boards();
            assertThat(boards).isBetween(3, 5);
            assertThat(tournament.participants())
                    .hasSizeBetween(10, 14)
                    .allSatisfy(team -> assertThat(team.name().value()).startsWith("Team "));
            assertThat(tournament.rounds()).hasSize(5);
            tournament.rounds().stream()
                    .flatMap(round -> round.boards().stream())
                    .forEach(match ->
                            assertThat(((MatchOutcome) match.outcome()).games()).hasSize(boards));
        }

        @Test
        void recordsTheBoardsWithTheParameters() {
            var generated = TournamentGenerator.of(teams).generate(TournamentSeed.of(3));

            assertThat(((GeneratedTournament.Completed) generated).parameters().boards())
                    .isBetween(3, 5);
        }

        @Test
        void drawsTheTeamTieBreaksOfArticlesTwelveAndThirteen() {
            var drawn = IntStream.range(0, 40)
                    .mapToObj(
                            seed -> completed(teams.withRandomTieBreaks(), seed).settings())
                    .flatMap(settings -> settings.tieBreakList().codes().stream())
                    .map(TieBreakCode::acronym)
                    .toList();

            assertThat(drawn).contains("MPVGP", "EDE").anyMatch(code -> code.startsWith("EM"));
        }

        @Test
        void drawsTheFormatOfASwissTeamEventButNotOfTheOlympiad() {
            var varied = IntStream.range(0, 30)
                    .mapToObj(seed ->
                            completed(teams.withRandomTeamFormat(), seed).settings())
                    .toList();
            var olympiad = completed(
                    GeneratorSettings.ofTeams(Profiles.olympiad(NumberOfRounds.of(9)))
                            .withPlayers(Range.of(10))
                            .withRounds(Range.of(4))
                            .withRandomTeamFormat(),
                    1);

            assertThat(varied)
                    .extracting(settings -> settings.scoring().primaryScore())
                    .contains(PrimaryScore.MATCH_POINTS, PrimaryScore.GAME_POINTS);
            assertThat(varied)
                    .extracting(settings -> settings.pairingSystem().teamColourPreferences())
                    .contains(
                            Optional.of(ColourPreferenceType.TYPE_A),
                            Optional.of(ColourPreferenceType.TYPE_B),
                            Optional.of(ColourPreferenceType.NONE));
            assertThat(olympiad.settings().scoring().primaryScore()).isEqualTo(PrimaryScore.MATCH_POINTS);
        }
    }

    private static Tournament completed(GeneratorSettings settings, long seed) {
        return completed(settings, TournamentSeed.of(seed));
    }

    private static Tournament completed(GeneratorSettings settings, TournamentSeed seed) {
        var generated = TournamentGenerator.of(settings).generate(seed);
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
