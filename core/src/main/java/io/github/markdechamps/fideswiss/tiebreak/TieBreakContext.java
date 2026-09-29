package io.github.markdechamps.fideswiss.tiebreak;

import io.github.markdechamps.fideswiss.history.TournamentHistory;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything the tie-breaks read: every participant's counted rounds, the scoring and the Unplayed Round policy. In a
 * team competition a context reads one team score, match points or game points (C.07 11.1, 13); {@link #in} gives the
 * context of the other one, which has the same rounds with that score's points.
 */
final class TieBreakContext {

    private final Map<ParticipantId, ParticipantRounds> participants;
    private final ScoringScheme scoring;
    private final UnplayedRoundPolicy policy;
    private final int roundsCounted;
    private final List<ParticipantId> pairingNumberOrder;
    private final Map<ParticipantId, Points> adjustedScores = new HashMap<>();
    private final PrimaryScore score;
    private final int totalRounds;
    private final Map<ParticipantId, List<List<Points>>> boardPoints;
    private TieBreakContext other = this;

    private TieBreakContext(
            Map<ParticipantId, ParticipantRounds> participants,
            ScoringScheme scoring,
            UnplayedRoundPolicy policy,
            int roundsCounted,
            List<ParticipantId> pairingNumberOrder,
            PrimaryScore score,
            int totalRounds,
            Map<ParticipantId, List<List<Points>>> boardPoints) {
        this.participants = participants;
        this.scoring = scoring;
        this.policy = policy;
        this.roundsCounted = roundsCounted;
        this.pairingNumberOrder = List.copyOf(pairingNumberOrder);
        this.score = score;
        this.totalRounds = totalRounds;
        this.boardPoints = boardPoints;
    }

    /** The tournament's first {@code rounds} rounds, under the edition's policy. */
    static TieBreakContext of(Tournament tournament, int rounds, UnplayedRoundPolicy policy) {
        var history = TournamentHistory.of(tournament);
        var participants = new LinkedHashMap<ParticipantId, ParticipantRounds>();
        for (var participant : tournament.participants()) {
            var records = history.of(participant.id()).records();
            var entries = new ArrayList<RoundEntry>();
            for (var index = 0; index < rounds; index++) {
                entries.add(RoundEntry.of(RoundNumber.of(index + 1), records.get(index)));
            }
            participants.put(participant.id(), new ParticipantRounds(participant, entries));
        }
        var scoring = tournament.settings().scoring();
        var primary = new TieBreakContext(
                participants,
                scoring,
                policy,
                rounds,
                pairingNumberOrder(tournament, rounds),
                scoring.primaryScore(),
                tournament.settings().numberOfRounds().value(),
                boardPointsOf(tournament, rounds, history));
        if (scoring.matches().isPresent()) {
            primary.other = new TieBreakContext(
                    otherScoreRounds(tournament, rounds, participants),
                    scoring,
                    policy,
                    rounds,
                    primary.pairingNumberOrder,
                    scoring.primaryScore().other(),
                    primary.totalRounds,
                    primary.boardPoints);
            primary.other.other = primary;
        }
        return primary;
    }

    /** The rounds again, each scored in the other team score (C.07 11.1); a bye is worth a drawn match's points. */
    private static Map<ParticipantId, ParticipantRounds> otherScoreRounds(
            Tournament tournament, int rounds, Map<ParticipantId, ParticipantRounds> primary) {
        var scoring = tournament.settings().scoring();
        var byeValue = scoring.primaryScore() == PrimaryScore.MATCH_POINTS
                ? scoring.drawnMatchGamePoints()
                : scoring.matches().map(MatchScoring::draw).orElse(scoring.draw());
        var view = new LinkedHashMap<ParticipantId, ParticipantRounds>();
        primary.forEach((id, own) -> {
            var entries = new ArrayList<RoundEntry>();
            for (var index = 0; index < rounds; index++) {
                var entry = own.entries().get(index);
                var round = tournament.rounds().get(index);
                var points = round.boardOf(id)
                        .map(board -> scoring.secondaryPointsFor(board.outcome(), board.colourOf(id)))
                        .orElseGet(() -> scoring.secondaryPointsFor(byeOf(entry), byeValue));
                entries.add(entry.withPoints(points));
            }
            view.put(id, new ParticipantRounds(own.participant(), entries));
        });
        return view;
    }

    private static Bye byeOf(RoundEntry entry) {
        return switch (entry.kind()) {
            case PAIRING_ALLOCATED_BYE -> Bye.PAIRING_ALLOCATED;
            case FULL_POINT_BYE -> Bye.FULL_POINT;
            case HALF_POINT_BYE -> Bye.HALF_POINT;
            case WITHDRAWN -> Bye.WITHDRAWN;
            case NOT_YET_ENTERED -> Bye.NOT_YET_ENTERED;
            default -> Bye.ZERO_POINT;
        };
    }

    /**
     * Each team's game points board by board, round by round (C.07 12): a forfeited game is a standard win or loss,
     * and a pairing-allocated bye gives every board a win's points. Empty without match scoring.
     */
    private static Map<ParticipantId, List<List<Points>>> boardPointsOf(
            Tournament tournament, int rounds, TournamentHistory history) {
        var scoring = tournament.settings().scoring();
        var perParticipant = new LinkedHashMap<ParticipantId, List<List<Points>>>();
        if (scoring.matches().isEmpty()) {
            return perParticipant;
        }
        var boards = scoring.matches().orElseThrow().boards();
        for (var participant : tournament.participants()) {
            var id = participant.id();
            var perRound = new ArrayList<List<Points>>();
            for (var index = 0; index < rounds; index++) {
                var round = tournament.rounds().get(index);
                var board = round.boardOf(id);
                if (board.isPresent()
                        && board.get().outcome()
                                instanceof io.github.markdechamps.fideswiss.tournament.MatchOutcome match) {
                    var colour = board.get().colourOf(id);
                    perRound.add(match.games().stream()
                            .map(game -> scoring.pointsFor(game.resultOf(colour)))
                            .toList());
                } else {
                    var bye = history.of(id).records().get(index)
                                    instanceof io.github.markdechamps.fideswiss.history.RoundRecord.NoBoard noBoard
                            ? noBoard.bye()
                            : Bye.ZERO_POINT;
                    var each =
                            switch (bye) {
                                case PAIRING_ALLOCATED, FULL_POINT -> scoring.win();
                                case HALF_POINT -> scoring.draw();
                                default -> Points.ZERO;
                            };
                    perRound.add(java.util.Collections.nCopies(boards, each));
                }
            }
            perParticipant.put(id, perRound);
        }
        return perParticipant;
    }

    /** C.07 8.3: the same rounds with every paired game of the final round scored as a draw. */
    TieBreakContext withFinalRoundDrawn() {
        var view = drawnCopy();
        if (other != this) {
            view.other = other.drawnCopy();
            view.other.other = view;
        }
        return view;
    }

    private TieBreakContext drawnCopy() {
        var drawn = new LinkedHashMap<ParticipantId, ParticipantRounds>();
        participants.forEach((id, rounds) -> {
            var entries = new ArrayList<>(rounds.entries());
            if (!entries.isEmpty() && entries.getLast().opponent().isPresent()) {
                entries.set(entries.size() - 1, entries.getLast().drawn(drawValue()));
            }
            drawn.put(id, new ParticipantRounds(rounds.participant(), entries));
        });
        return new TieBreakContext(
                drawn, scoring, policy, roundsCounted, pairingNumberOrder, score, totalRounds, boardPoints);
    }

    /** The context reading the given team score; a competition without match scoring has only its own. */
    TieBreakContext in(PrimaryScore wanted) {
        return wanted == score || other == this ? this : other;
    }

    /** The team score this context reads. */
    PrimaryScore score() {
        return score;
    }

    boolean hasMatches() {
        return scoring.matches().isPresent();
    }

    int boards() {
        return scoring.matches().map(MatchScoring::boards).orElse(1);
    }

    /** The rounds of the tournament, which SSSC's normalising factor is defined over (13.4.2). */
    int totalRounds() {
        return totalRounds;
    }

    /** The highest score a match gives in the score this context reads. */
    Points winValue() {
        return valueOf(true);
    }

    Points drawValue() {
        return valueOf(false);
    }

    /** Game points board by board for each round of the team (C.07 12); one list per round. */
    List<List<Points>> boardPointsOf(ParticipantId team) {
        return boardPoints.get(team);
    }

    private Points valueOf(boolean win) {
        var single = win ? scoring.win() : scoring.draw();
        return scoring.matches()
                .map(matches -> score == PrimaryScore.MATCH_POINTS
                        ? (win ? matches.win() : matches.draw())
                        : single.times(matches.boards()))
                .orElse(single);
    }

    ParticipantRounds of(ParticipantId participant) {
        return participants.get(participant);
    }

    List<ParticipantRounds> everyone() {
        return List.copyOf(participants.values());
    }

    List<Participant> participantList() {
        return participants.values().stream()
                .map(ParticipantRounds::participant)
                .toList();
    }

    Points adjustedScore(ParticipantId participant) {
        return adjustedScores.computeIfAbsent(participant, id -> policy.adjustedScore(of(id), this));
    }

    Points dummyScore(ParticipantRounds participant, int index) {
        return policy.dummyScore(participant, index, this);
    }

    String dummyArticle(ParticipantRounds participant, int index) {
        return policy.dummyArticle(participant, index);
    }

    int roundsCounted() {
        return roundsCounted;
    }

    /** Numbered Participants by their latest Pairing Number, then the never-numbered ones (ADR 0006). */
    List<ParticipantId> pairingNumberOrder() {
        return pairingNumberOrder;
    }

    boolean anyoneUnrated() {
        return participants.values().stream()
                .anyMatch(rounds -> !rounds.participant().rating().isRated());
    }

    private static List<ParticipantId> pairingNumberOrder(Tournament tournament, int rounds) {
        var ranked = tournament.participants().stream()
                .sorted(tournament.settings().rankingKey().order(tournament.participants()))
                .map(Participant::id)
                .toList();
        var counted = tournament.rounds().subList(0, rounds);
        var numbered = ranked.stream()
                .filter(id -> counted.stream().anyMatch(round -> round.pairs(id)))
                .toList();
        var order = new ArrayList<>(numbered);
        ranked.stream().filter(id -> !numbered.contains(id)).forEach(order::add);
        return order;
    }
}
