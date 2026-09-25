package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.RankingKey;
import io.github.markdechamps.fideswiss.tournament.RequestedBye;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A TRF read into the library's model: the settings its records declare over the baseline profile, the
 * participants in starting-rank order (which is the ranking order, ADR 0006), the rounds recorded, and who is
 * marked absent for the round to be paired.
 */
public final class TrfTournament {

    private final TournamentSettings settings;
    private final List<Participant> participants;
    private final List<Round> recordedRounds;
    private final Map<ParticipantId, Bye> nextRoundAbsences;

    private TrfTournament(
            TournamentSettings settings,
            List<Participant> participants,
            List<Round> recordedRounds,
            Map<ParticipantId, Bye> nextRoundAbsences) {
        this.settings = settings;
        this.participants = List.copyOf(participants);
        this.recordedRounds = List.copyOf(recordedRounds);
        this.nextRoundAbsences = Map.copyOf(nextRoundAbsences);
    }

    static TrfTournament of(List<PlayerRecord> players, Map<String, List<String>> records) {
        var columns = players.stream()
                .mapToInt(player -> player.rounds().size())
                .max()
                .orElse(0);
        var lastColumnOnlyMarksAbsences = columns > 0 && isOnlyAbsenceMarks(players, columns);
        var completeRounds = lastColumnOnlyMarksAbsences ? columns - 1 : columns;
        var rounds = new ArrayList<Round>();
        for (var round = 1; round <= completeRounds; round++) {
            rounds.add(roundOf(players, round));
        }
        var absences = lastColumnOnlyMarksAbsences ? absenceMarks(players, columns) : Map.<ParticipantId, Bye>of();
        var participants = players.stream().map(PlayerRecord::participant).toList();
        var numberOfRounds = declaredNumberOfRounds(records).orElse(Math.max(columns, 1));
        var profile = Profiles.individualSwiss(NumberOfRounds.of(numberOfRounds));
        var settings = firstValue(records, "192")
                .flatMap(PairingSystemCode::parse)
                .map(profile::with)
                .orElse(profile)
                .with(RankingKey.asListed())
                .with(initialColour(records, players));
        return new TrfTournament(settings, participants, rounds, absences);
    }

    /** The same file under other settings, such as the command line's overrides. */
    public TrfTournament with(TournamentSettings overridden) {
        return new TrfTournament(overridden, participants, recordedRounds, nextRoundAbsences);
    }

    public TournamentSettings settings() {
        return settings;
    }

    public List<Participant> participants() {
        return participants;
    }

    /** The rounds the file records as played, in order. */
    public List<Round> recordedRounds() {
        return recordedRounds;
    }

    /** The tournament as the file leaves it: every recorded round, and the absences marked for the next. */
    public Tournament tournament() {
        var tournament = tournamentAfter(recordedRounds.size());
        for (var absence : nextRoundAbsences.entrySet()) {
            tournament = tournament.requestBye(absence.getKey(), tournament.nextRound(), requested(absence.getValue()));
        }
        return tournament;
    }

    /**
     * The tournament just before the given recorded round was paired: the earlier rounds, and everyone the round
     * left off the boards without a PAB marked absent in advance.
     */
    public Tournament tournamentBefore(RoundNumber round) {
        var tournament = tournamentAfter(round.value() - 1);
        var recorded = recordedRounds.get(round.value() - 1);
        for (var bye : recorded.byes().entrySet()) {
            if (bye.getValue() != Bye.PAIRING_ALLOCATED) {
                tournament = tournament.requestBye(bye.getKey(), round, requested(bye.getValue()));
            }
        }
        return tournament;
    }

    private Tournament tournamentAfter(int rounds) {
        var tournament = Tournament.of(settings, participants);
        for (var index = 0; index < rounds; index++) {
            tournament = tournament.withRound(recordedRounds.get(index));
        }
        return tournament;
    }

    private static RequestedBye requested(Bye bye) {
        return switch (bye) {
            case FULL_POINT -> RequestedBye.full();
            case HALF_POINT -> RequestedBye.half();
            default -> RequestedBye.zero();
        };
    }

    private static boolean isOnlyAbsenceMarks(List<PlayerRecord> players, int column) {
        return players.stream()
                .map(player -> player.cellOf(column))
                .noneMatch(cell -> cell.hasOpponent() || cell.isPairingAllocatedBye());
    }

    private static Map<ParticipantId, Bye> absenceMarks(List<PlayerRecord> players, int column) {
        var absences = new HashMap<ParticipantId, Bye>();
        for (var player : players) {
            var cell = player.cellOf(column);
            if (cell.result() != ' ') {
                absences.put(player.participant().id(), byeOf(cell));
            }
        }
        return absences;
    }

    private static Round roundOf(List<PlayerRecord> players, int round) {
        var boards = new ArrayList<Board>();
        var byes = new HashMap<ParticipantId, Bye>();
        for (var player : players) {
            var cell = player.cellOf(round);
            var id = player.participant().id();
            if (!cell.hasOpponent()) {
                byes.put(id, byeOf(cell));
            } else if (isWhiteOfItsBoard(player, cell)) {
                var opponent = ParticipantId.of(cell.opponent().orElseThrow());
                var opponentCell = cellOfPlayer(players, opponent, round);
                boards.add(new Board(
                        BoardNumber.of(boards.size() + 1),
                        id,
                        opponent,
                        outcomeOf(cell.result(), opponentCell.result())));
            }
        }
        return Round.of(RoundNumber.of(round), boards, byes);
    }

    /** The player with {@code w} sits White; without colours (a forfeit), the lower starting rank does. */
    private static boolean isWhiteOfItsBoard(PlayerRecord player, RoundCell cell) {
        return cell.colour()
                .map(Colour.WHITE::equals)
                .orElseGet(() ->
                        player.startRank() < Integer.parseInt(cell.opponent().orElseThrow()));
    }

    private static RoundCell cellOfPlayer(List<PlayerRecord> players, ParticipantId id, int round) {
        return players.stream()
                .filter(player -> player.participant().id().equals(id))
                .findFirst()
                .map(player -> player.cellOf(round))
                .orElseThrow(() -> new InvalidTrfException("Round " + round + " refers to unknown player " + id));
    }

    private static GameOutcome outcomeOf(char white, char black) {
        return switch (white) {
            case '1', 'W' -> GameOutcome.WHITE_WINS;
            case '=', 'D' -> GameOutcome.DRAW;
            case '0', 'L' -> GameOutcome.BLACK_WINS;
            case '+' -> GameOutcome.WHITE_WINS_BY_FORFEIT;
            case '-' -> black == '+' ? GameOutcome.BLACK_WINS_BY_FORFEIT : GameOutcome.DOUBLE_FORFEIT;
            default -> throw new InvalidTrfException("Unknown result code '" + white + "'");
        };
    }

    private static Bye byeOf(RoundCell cell) {
        return switch (cell.result()) {
            case 'U' -> Bye.PAIRING_ALLOCATED;
            case 'F', '+' -> Bye.FULL_POINT;
            case 'H' -> Bye.HALF_POINT;
            default -> Bye.ZERO_POINT;
        };
    }

    private static Optional<Integer> declaredNumberOfRounds(Map<String, List<String>> records) {
        return firstValue(records, "142")
                .or(() -> firstValue(records, "XXR"))
                .map(value -> Integer.parseInt(TrfReader.words(value).getFirst()));
    }

    /**
     * 152 (TRF26) or XXC (JaVaFo); without either, the colour the top participant paired in round 1 had, as
     * TRF26 prescribes for 152 and bbpPairings infers.
     */
    private static InitialColour initialColour(Map<String, List<String>> records, List<PlayerRecord> players) {
        var declared = firstValue(records, "152")
                .map(value ->
                        value.trim().toUpperCase().startsWith("B") ? InitialColour.black() : InitialColour.white())
                .or(() -> records.getOrDefault("XXC", List.of()).stream()
                        .flatMap(value -> TrfReader.words(value).stream())
                        .filter(word -> word.equals("white1") || word.equals("black1"))
                        .reduce((first, second) -> second)
                        .map(word -> word.equals("black1") ? InitialColour.black() : InitialColour.white()));
        return declared.orElseGet(() -> inferredInitialColour(players));
    }

    private static InitialColour inferredInitialColour(List<PlayerRecord> players) {
        var pairingNumber = 0;
        for (var player : players) {
            var cell = player.cellOf(1);
            if (cell.hasOpponent() || cell.isPairingAllocatedBye()) {
                pairingNumber++;
                if (cell.colour().isPresent()) {
                    var colour = cell.colour().get();
                    var initial = pairingNumber % 2 == 1 ? colour : colour.opposite();
                    return new InitialColour(initial);
                }
            }
        }
        return InitialColour.white();
    }

    private static Optional<String> firstValue(Map<String, List<String>> records, String code) {
        return records.getOrDefault(code, List.of()).stream().findFirst();
    }
}
