package io.github.markdechamps.fideswiss.generator;

import io.github.markdechamps.fideswiss.pairing.NoLegalPairingException;
import io.github.markdechamps.fideswiss.pairing.PairedBoard;
import io.github.markdechamps.fideswiss.tournament.Acceleration;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.Name;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Outcome;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.RankingKey;
import io.github.markdechamps.fideswiss.tournament.Rating;
import io.github.markdechamps.fideswiss.tournament.RequestedBye;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * The Random Tournament Generator: the library itself playing a whole tournament. It builds a field and enters
 * its late participants, then for each round applies the round's withdrawals and requested byes, pairs it with {@code pairNextRound()}, draws the
 * results and records the round. Every event goes through the public API, so it can only produce valid input.
 */
public final class TournamentGenerator {

    private static final int MAXIMUM_REQUESTED_BYES = 2;

    private static final List<ScoringScheme> NON_STANDARD_SCORINGS = List.of(
            new ScoringScheme(Points.of(3), Points.of(1), Points.ZERO, Optional.empty()),
            new ScoringScheme(Points.of(2), Points.of(1), Points.ZERO, Optional.empty()));

    private final GeneratorSettings settings;

    private TournamentGenerator(GeneratorSettings settings) {
        this.settings = settings;
    }

    public static TournamentGenerator of(GeneratorSettings settings) {
        return new TournamentGenerator(settings);
    }

    public GeneratedTournament generate(TournamentSeed seed) {
        return new Run(seed).play();
    }

    /** One tournament being played from one seed. */
    private final class Run {

        private final TournamentSeed seed;
        private final Draws draws;
        private final GeneratedTournament.Parameters parameters;
        private final Map<ParticipantId, Integer> strengths = new HashMap<>();
        private final Map<ParticipantId, Integer> requestedByes = new HashMap<>();
        private final Set<ParticipantId> withdrawn = new HashSet<>();
        private final Map<Integer, List<ParticipantId>> withdrawalsByRound = new HashMap<>();
        private Tournament tournament;

        Run(TournamentSeed seed) {
            this.seed = seed;
            this.draws = new Draws(seed);
            this.parameters = drawParameters(draws.stream("parameters"));
            this.tournament = Tournament.of(tournamentSettings(), field());
            planWithdrawals();
            enterLateParticipants();
        }

        GeneratedTournament play() {
            for (var round = 1; round <= parameters.rounds(); round++) {
                applyEvents(round);
                try {
                    var pairing = tournament.pairNextRound();
                    tournament = tournament.withRound(pairing.completedWith(outcomes(round, pairing.boards())));
                } catch (NoLegalPairingException e) {
                    return new GeneratedTournament.Skipped(seed, RoundNumber.of(round), e.getMessage(), parameters);
                }
            }
            return new GeneratedTournament.Completed(seed, tournament, parameters);
        }

        private GeneratedTournament.Parameters drawParameters(RandomGenerator random) {
            var players = settings.players().draw(random);
            var rounds = Math.max(1, Math.min(settings.rounds().draw(random), players - 1));
            var highest = settings.highestRating().draw(random);
            var lowest = Math.min(settings.lowestRating().draw(random), highest);
            var unrated =
                    (int) Math.round(players * settings.unratedPercentage().draw(random) / 100.0);
            var withdrawals =
                    (int) Math.round(players * settings.withdrawalPercentage().draw(random) / 100.0);
            var variations = variations(draws.stream("variations"));
            var lateEntries = rounds < 3
                    ? 0
                    : (int) Math.round(players * settings.lateEntryPercentage().draw(random) / 100.0);
            return new GeneratedTournament.Parameters(
                    players,
                    rounds,
                    highest,
                    lowest,
                    unrated,
                    settings.forfeitRate().draw(random),
                    settings.halfPointByeRate().draw(random),
                    settings.zeroPointByeRate().draw(random),
                    settings.fullPointByeRate().draw(random),
                    withdrawals,
                    lateEntries,
                    variations.scoring(),
                    variations.acceleration(),
                    variations.tieBreakList());
        }

        /**
         * The tournament-level variations: a non-standard scoring, then Baku where that scoring allows it (C.04.7
         * 1.1), then a Tie-break List drawn from the Tie-break Edition's catalogue.
         */
        private TournamentSettings variations(RandomGenerator random) {
            var variations = settings.tournament();
            if (settings.nonStandardScoring().happens(random)) {
                variations = variations.with(NON_STANDARD_SCORINGS.get(random.nextInt(NON_STANDARD_SCORINGS.size())));
            }
            if (settings.bakuAcceleration().happens(random)
                    && Acceleration.baku().problemsWith(variations).isEmpty()) {
                variations = variations.with(Acceleration.baku());
            }
            if (settings.drawnTieBreaks().happens(random)) {
                variations = variations.with(
                        TieBreakCatalogue.of(variations.tieBreakEdition()).draw(random));
            }
            return variations;
        }

        private TournamentSettings tournamentSettings() {
            var initialColour =
                    draws.stream("initial colour").nextBoolean() ? InitialColour.white() : InitialColour.black();
            return settings.tournament()
                    .with(parameters.scoring())
                    .with(parameters.acceleration())
                    .with(parameters.tieBreaks())
                    .with(NumberOfRounds.of(parameters.rounds()))
                    .with(RankingKey.asListed())
                    .with(initialColour);
        }

        /**
         * Ratings uniform between the drawn lowest and highest, strongest first. The unrated participants keep a
         * hidden strength, used only by the result model, and are listed after the rated ones (GHR 2.1).
         */
        private List<Participant> field() {
            var random = draws.stream("field");
            var strengthsDrawn = new ArrayList<Integer>();
            for (var index = 0; index < parameters.players() - parameters.lateEntries(); index++) {
                strengthsDrawn.add(random.nextInt(parameters.lowestRating(), parameters.highestRating() + 1));
            }
            strengthsDrawn.sort((a, b) -> Integer.compare(b, a));
            var unrated = chosenIndexes(random, strengthsDrawn.size(), parameters.unrated());
            var rated = new ArrayList<Integer>();
            var hidden = new ArrayList<Integer>();
            for (var index = 0; index < strengthsDrawn.size(); index++) {
                (unrated.contains(index) ? hidden : rated).add(strengthsDrawn.get(index));
            }
            var participants = new ArrayList<Participant>();
            for (var strength : rated) {
                participants.add(participant(participants.size() + 1, Rating.of(strength), strength));
            }
            for (var strength : hidden) {
                participants.add(participant(participants.size() + 1, Rating.unrated(), strength));
            }
            return participants;
        }

        private Participant participant(int startRank, Rating rating, int strength) {
            var id = ParticipantId.of(String.valueOf(startRank));
            strengths.put(id, strength);
            return Participant.of(id, Name.of(String.format("Player %04d", startRank)), rating);
        }

        /** Each chosen participant withdraws after a uniformly chosen round, so it is unpaired from the next. */
        private void planWithdrawals() {
            if (parameters.rounds() < 2 || settings.retirementRate().isPresent()) {
                return;
            }
            var random = draws.stream("withdrawals");
            var ids = tournament.participants().stream().map(Participant::id).toList();
            for (var index : chosenIndexes(random, ids.size(), parameters.withdrawals())) {
                var from = random.nextInt(2, parameters.rounds() + 1);
                withdrawalsByRound
                        .computeIfAbsent(from, key -> new ArrayList<>())
                        .add(ids.get(index));
            }
        }

        /**
         * The late participants register before the first round with the round they enter in, from 2 to
         * ⌈rounds/2⌉, so every round records their absence; they are rated within the span and listed last.
         */
        private void enterLateParticipants() {
            var random = draws.stream("late entries");
            var lastEntryRound = (parameters.rounds() + 1) / 2;
            for (var entrant = 0; entrant < parameters.lateEntries(); entrant++) {
                var strength = random.nextInt(parameters.lowestRating(), parameters.highestRating() + 1);
                var firstRound = RoundNumber.of(random.nextInt(2, lastEntryRound + 1));
                var startRank = tournament.participants().size() + 1;
                tournament = tournament.enterLate(participant(startRank, Rating.of(strength), strength), firstRound);
            }
        }

        private void applyEvents(int round) {
            var number = RoundNumber.of(round);
            var retiring = new ArrayList<>(withdrawalsByRound.getOrDefault(round, List.of()));
            settings.retirementRate().ifPresent(rate -> retiring.addAll(retirements(round, rate)));
            for (var participant : retiring) {
                if (withdrawn.add(participant)) {
                    tournament = tournament.withdraw(participant, number);
                }
            }
            if (round < parameters.rounds()) {
                requestByes(round);
            }
        }

        private List<ParticipantId> retirements(int round, Range rate) {
            if (round < 2) {
                return List.of();
            }
            var random = draws.stream(round, "retirements");
            var drawn = rate.draw(draws.stream("retirement rate"));
            return tournament.participants().stream()
                    .map(Participant::id)
                    .filter(participant -> !withdrawn.contains(participant))
                    .filter(participant -> happens(random, drawn))
                    .toList();
        }

        /** Never in the last round, at most two per participant (ticket Random tournament generator). */
        private void requestByes(int round) {
            var random = draws.stream(round, "requested byes");
            for (var participant : tournament.participants()) {
                var id = participant.id();
                var bye = requestedBye(random);
                if (bye.isEmpty() || withdrawn.contains(id) || isAbsent(id, round)) {
                    continue;
                }
                if (requestedByes.getOrDefault(id, 0) < MAXIMUM_REQUESTED_BYES) {
                    requestedByes.merge(id, 1, Integer::sum);
                    tournament = tournament.requestBye(id, RoundNumber.of(round), bye.get());
                }
            }
        }

        private boolean isAbsent(ParticipantId participant, int round) {
            return tournament.absenceIn(participant, RoundNumber.of(round)).isPresent();
        }

        private Optional<RequestedBye> requestedBye(RandomGenerator random) {
            var half = happens(random, parameters.halfPointByeRate());
            var zero = happens(random, parameters.zeroPointByeRate());
            var full = happens(random, parameters.fullPointByeRate());
            if (half) {
                return Optional.of(RequestedBye.half());
            }
            if (zero) {
                return Optional.of(RequestedBye.zero());
            }
            return full ? Optional.of(RequestedBye.full()) : Optional.empty();
        }

        private Map<BoardNumber, Outcome> outcomes(int round, List<PairedBoard> boards) {
            var outcomes = new HashMap<BoardNumber, Outcome>();
            for (var board : boards) {
                var random = draws.stream(
                        round,
                        board.white().value(),
                        board.black().value(),
                        board.number().value());
                outcomes.put(board.number(), outcomeOf(board, random));
            }
            return outcomes;
        }

        /** Each side is absent independently, as in bbp, so that 1 in N games is forfeited. */
        private Outcome outcomeOf(PairedBoard board, RandomGenerator random) {
            var absence = parameters.forfeitRate() == 0 ? 0 : 1 - Math.sqrt(1 - 1.0 / parameters.forfeitRate());
            var whiteAbsent = random.nextDouble() < absence;
            var blackAbsent = random.nextDouble() < absence;
            if (whiteAbsent && blackAbsent) {
                return GameOutcome.DOUBLE_FORFEIT;
            }
            if (whiteAbsent || blackAbsent) {
                return whiteAbsent ? GameOutcome.BLACK_WINS_BY_FORFEIT : GameOutcome.WHITE_WINS_BY_FORFEIT;
            }
            return settings.resultModel().outcome(strengths.get(board.white()), strengths.get(board.black()), random);
        }

        private static boolean happens(RandomGenerator random, int rate) {
            return rate > 0 && random.nextInt(rate) == 0;
        }

        private static Set<Integer> chosenIndexes(RandomGenerator random, int size, int count) {
            var indexes = new ArrayList<Integer>();
            for (var index = 0; index < size; index++) {
                indexes.add(index);
            }
            var chosen = new HashSet<Integer>();
            for (var pick = 0; pick < Math.min(count, size); pick++) {
                chosen.add(indexes.remove(random.nextInt(indexes.size())));
            }
            return chosen;
        }
    }
}
