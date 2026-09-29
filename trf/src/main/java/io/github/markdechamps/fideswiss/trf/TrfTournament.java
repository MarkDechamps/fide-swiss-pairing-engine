package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.Board;
import io.github.markdechamps.fideswiss.tournament.BoardNumber;
import io.github.markdechamps.fideswiss.tournament.Bye;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.GameOutcome;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Participant;
import io.github.markdechamps.fideswiss.tournament.ParticipantId;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.RankingKey;
import io.github.markdechamps.fideswiss.tournament.RequestedBye;
import io.github.markdechamps.fideswiss.tournament.Round;
import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.Tournament;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A TRF read into the library's model: the settings its records declare over the baseline profile, the
 * participants in starting-rank order (which is the ranking order, ADR 0006), the rounds recorded, and who is
 * marked absent for the round to be paired.
 */
public final class TrfTournament {

    private static final Pattern SCORING_PAIR = Pattern.compile("([WDLAPX])\\s*(\\d+(?:\\.\\d+)?)");

    private final TournamentSettings settings;
    private final List<Participant> participants;
    private final List<Round> recordedRounds;
    private final Map<ParticipantId, Bye> nextRoundAbsences;
    private final Map<ParticipantId, Points> declaredPoints;
    private final Map<ParticipantId, Integer> declaredRanks;
    private final boolean declaresTieBreaks;

    private TrfTournament(
            TournamentSettings settings,
            List<Participant> participants,
            List<Round> recordedRounds,
            Map<ParticipantId, Bye> nextRoundAbsences,
            Map<ParticipantId, Points> declaredPoints,
            Map<ParticipantId, Integer> declaredRanks,
            boolean declaresTieBreaks) {
        this.settings = settings;
        this.declaredPoints = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(declaredPoints));
        this.declaredRanks = java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(declaredRanks));
        this.declaresTieBreaks = declaresTieBreaks;
        this.participants = List.copyOf(participants);
        this.recordedRounds = List.copyOf(recordedRounds);
        this.nextRoundAbsences = Map.copyOf(nextRoundAbsences);
    }

    static TrfTournament of(List<PlayerRecord> players, Map<String, List<String>> records) {
        return of(players, records, Optional.empty());
    }

    /**
     * As declared, or with the Double-Swiss encoding (ADR 0007) switched on or off by the command line's system,
     * which overrides {@code 192}.
     */
    static TrfTournament of(
            List<PlayerRecord> players, Map<String, List<String>> records, Optional<Boolean> doubleSwissOverride) {
        if (TeamSettingsRecords.declaresATeamSystem(records)) {
            return ofTeams(players, records);
        }
        var declaredSystem = firstValue(records, "192").flatMap(PairingSystemCode::parse);
        var doubleSwiss = doubleSwissOverride.orElseGet(() ->
                declaredSystem.filter(system -> system.gamesInSuccession() == 2).isPresent());
        var columns = players.stream()
                .mapToInt(player -> player.rounds().size())
                .max()
                .orElse(0);
        var rounds = new ArrayList<Round>();
        Map<ParticipantId, Bye> absences;
        int recordedRounds;
        if (doubleSwiss) {
            var matches = new MatchColumns(players).read();
            rounds.addAll(matches.rounds());
            absences = matches.absences();
            recordedRounds = (columns + 1) / 2;
        } else {
            var lastColumnOnlyMarksAbsences = columns > 0 && isOnlyAbsenceMarks(players, columns);
            var completeRounds = lastColumnOnlyMarksAbsences ? columns - 1 : columns;
            for (var round = 1; round <= completeRounds; round++) {
                rounds.add(roundOf(players, round));
            }
            absences = lastColumnOnlyMarksAbsences ? absenceMarks(players, columns) : Map.<ParticipantId, Bye>of();
            recordedRounds = columns;
        }
        var participants = players.stream().map(PlayerRecord::participant).toList();
        var numberOfRounds = declaredNumberOfRounds(records, doubleSwiss).orElse(Math.max(recordedRounds, 1));
        var profile = doubleSwiss
                ? Profiles.doubleSwiss(NumberOfRounds.of(numberOfRounds))
                : Profiles.individualSwiss(NumberOfRounds.of(numberOfRounds));
        var withSystem = declaredSystem
                .filter(system -> (system.gamesInSuccession() == 2) == doubleSwiss)
                .map(profile::with)
                .orElse(profile)
                .with(RankingKey.asListed())
                .with(initialColour(records, players))
                .with(scoring(records, profile.scoring()))
                .with(swissRulesEdition(records))
                .with(AccelerationRecords.read(records, numberOfRounds, false, doubleSwiss));
        var settings = declaredTieBreaks(records).map(withSystem::with).orElse(withSystem);
        return new TrfTournament(
                settings,
                participants,
                rounds,
                absences,
                declared(players, PlayerRecord::declaredPoints),
                declared(players, PlayerRecord::declaredRank),
                declaredTieBreaks(records).isPresent());
    }

    /**
     * A team file: the {@code 310} teams in team-number order are the participants, their matches the rounds;
     * the declared points are the teams' primary scores.
     */
    private static TrfTournament ofTeams(List<PlayerRecord> players, Map<String, List<String>> records) {
        var teams = records.getOrDefault("310", List.of()).stream()
                .map(TeamRecord::parse)
                .sorted(java.util.Comparator.comparingInt(TeamRecord::number))
                .toList();
        var teamRounds = new TeamRounds(teams, players, records);
        var columns = teamRounds.columns();
        var lastColumnOnlyMarksAbsences = columns > 0 && teamRounds.isOnlyAbsenceMarks(columns);
        var completeRounds = lastColumnOnlyMarksAbsences ? columns - 1 : columns;
        var rounds = new ArrayList<Round>();
        for (var round = 1; round <= completeRounds; round++) {
            rounds.add(teamRounds.roundOf(round));
        }
        var absences = lastColumnOnlyMarksAbsences ? teamRounds.absenceMarks(columns) : Map.<ParticipantId, Bye>of();
        var numberOfRounds = declaredNumberOfRounds(records).orElse(Math.max(columns, 1));
        var declared = TeamSettingsRecords.read(records, NumberOfRounds.of(numberOfRounds), teamRounds.boards());
        var inMatchPoints = declared.scoring().primaryScore() == PrimaryScore.MATCH_POINTS;
        var profile = declared.with(RankingKey.asListed())
                .with(declaredInitialColour(records).orElseGet(() -> inferredInitialColour(rounds, teams)))
                .with(AccelerationRecords.read(records, numberOfRounds, inMatchPoints));
        var settings = declaredTieBreaks(records).map(profile::with).orElse(profile);
        var points = new java.util.LinkedHashMap<ParticipantId, Points>();
        var ranks = new java.util.LinkedHashMap<ParticipantId, Integer>();
        for (var team : teams) {
            (inMatchPoints ? team.declaredMatchPoints() : team.declaredGamePoints())
                    .ifPresent(value -> points.put(team.id(), value));
            team.declaredRank().ifPresent(rank -> ranks.put(team.id(), rank));
        }
        return new TrfTournament(
                settings,
                teams.stream().map(TeamRecord::participant).toList(),
                rounds,
                absences,
                points,
                ranks,
                declaredTieBreaks(records).isPresent());
    }

    private static <T> Map<ParticipantId, T> declared(
            List<PlayerRecord> players, java.util.function.Function<PlayerRecord, Optional<T>> value) {
        var declared = new java.util.LinkedHashMap<ParticipantId, T>();
        players.forEach(player -> value.apply(player)
                .ifPresent(found -> declared.put(player.participant().id(), found)));
        return declared;
    }

    /**
     * The edition the {@code 192} code declares (TRF CLI surface): {@code FIDE_DUTCH_2017} is pre-2026, a bare or
     * 2025/2026 Dutch code is 2026. bbpPairings writes the code into {@code 092}, read the same way without a 192.
     */
    private static SwissRulesEdition swissRulesEdition(Map<String, List<String>> records) {
        var declared = firstValue(records, "192")
                .or(() ->
                        firstValue(records, "092").filter(value -> value.trim().startsWith("FIDE_DUTCH")));
        return declared.map(value -> editionOf(value.trim())).orElse(SwissRulesEdition.EDITION_2026);
    }

    /** A {@code _BAKU} suffix adds acceleration (read by AccelerationRecords) and leaves the edition alone. */
    private static SwissRulesEdition editionOf(String code) {
        var system = code.toUpperCase().replaceFirst("_BAKU$", "");
        return system.equals("FIDE_DUTCH_2017") ? SwissRulesEdition.PRE_2026 : SwissRulesEdition.EDITION_2026;
    }

    /** The same file under other settings, such as the command line's overrides. */
    public TrfTournament with(TournamentSettings overridden) {
        return new TrfTournament(
                overridden,
                participants,
                recordedRounds,
                nextRoundAbsences,
                declaredPoints,
                declaredRanks,
                declaresTieBreaks);
    }

    /** The Points each {@code 001} record states (columns 81–84), where it states them. */
    public Map<ParticipantId, Points> declaredPoints() {
        return declaredPoints;
    }

    /** The rank each {@code 001} record states (columns 86–89), where it states one. */
    public Map<ParticipantId, Integer> declaredRanks() {
        return declaredRanks;
    }

    /** Whether the file names its own Tie-break List (202 or 212), so that its ranks can be held to it. */
    public boolean declaresTieBreaks() {
        return declaresTieBreaks;
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

    static boolean isOnlyAbsenceMarks(List<PlayerRecord> players, int column) {
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

    static RoundCell cellOfPlayer(List<PlayerRecord> players, ParticipantId id, int round) {
        return players.stream()
                .filter(player -> player.participant().id().equals(id))
                .findFirst()
                .map(player -> player.cellOf(round))
                .orElseThrow(() -> new InvalidTrfException("Round " + round + " refers to unknown player " + id));
    }

    static GameOutcome outcomeOf(char white, char black) {
        return switch (white) {
            case '1', 'W' -> GameOutcome.WHITE_WINS;
            case '=', 'D' -> GameOutcome.DRAW;
            case '0', 'L' -> GameOutcome.BLACK_WINS;
            case '+' -> GameOutcome.WHITE_WINS_BY_FORFEIT;
            case '-' -> black == '+' ? GameOutcome.BLACK_WINS_BY_FORFEIT : GameOutcome.DOUBLE_FORFEIT;
            default -> throw new InvalidTrfException("Unknown result code '" + white + "'");
        };
    }

    static Bye byeOf(RoundCell cell) {
        return switch (cell.result()) {
            case 'U' -> Bye.PAIRING_ALLOCATED;
            case 'F', '+' -> Bye.FULL_POINT;
            case 'H' -> Bye.HALF_POINT;
            default -> Bye.ZERO_POINT;
        };
    }

    /**
     * {@code 162}: pairs of a symbol and its points; {@code W} win, {@code D} draw, {@code L} loss, {@code P} the
     * PAB. Values not given keep their defaults. Under Double-Swiss the bye symbols are values per match (ADR 0007);
     * {@code F} and {@code H}, which the writer adds, are read as two games won and two drawn.
     */
    private static ScoringScheme scoring(Map<String, List<String>> records, ScoringScheme baseline) {
        var scoring = baseline;
        for (var value : records.getOrDefault("162", List.of())) {
            var matcher = SCORING_PAIR.matcher(value);
            while (matcher.find()) {
                var points = Points.of(matcher.group(2));
                scoring = switch (matcher.group(1).charAt(0)) {
                    case 'W' ->
                        new ScoringScheme(
                                points,
                                scoring.draw(),
                                scoring.loss(),
                                scoring.pairingAllocatedBye(),
                                scoring.matches());
                    case 'D' ->
                        new ScoringScheme(
                                scoring.win(),
                                points,
                                scoring.loss(),
                                scoring.pairingAllocatedBye(),
                                scoring.matches());
                    case 'L' ->
                        new ScoringScheme(
                                scoring.win(),
                                scoring.draw(),
                                points,
                                scoring.pairingAllocatedBye(),
                                scoring.matches());
                    case 'P' -> scoring.withPairingAllocatedBye(points);
                    default -> scoring;
                };
            }
        }
        return scoring;
    }

    private static Optional<Integer> declaredNumberOfRounds(Map<String, List<String>> records) {
        return firstValue(records, "142")
                .or(() -> firstValue(records, "XXR"))
                .map(value -> Integer.parseInt(TrfReader.words(value).getFirst()));
    }

    /** Under Double-Swiss, {@code 142} counts TRF rounds, two per match, so it must be even (ADR 0007). */
    private static Optional<Integer> declaredNumberOfRounds(Map<String, List<String>> records, boolean doubleSwiss) {
        var declared = declaredNumberOfRounds(records);
        if (!doubleSwiss) {
            return declared;
        }
        declared.filter(rounds -> rounds % 2 == 1).ifPresent(rounds -> {
            throw new InvalidTrfException(
                    "Record 142 declares " + rounds + " rounds: a Double-Swiss file has two per match (ADR 0007)");
        });
        return declared.map(rounds -> rounds / 2);
    }

    /**
     * 152 (TRF26) or XXC (JaVaFo); without either, the colour the top participant paired in round 1 had, as
     * TRF26 prescribes for 152 and bbpPairings infers.
     */
    private static InitialColour initialColour(Map<String, List<String>> records, List<PlayerRecord> players) {
        return declaredInitialColour(records).orElseGet(() -> inferredInitialColour(players));
    }

    private static Optional<InitialColour> declaredInitialColour(Map<String, List<String>> records) {
        return firstValue(records, "152")
                .map(value ->
                        value.trim().toUpperCase().startsWith("B") ? InitialColour.black() : InitialColour.white())
                .or(() -> records.getOrDefault("XXC", List.of()).stream()
                        .flatMap(line -> TrfReader.words(TrfReader.valueOf(line)).stream())
                        .filter(word -> word.equals("white1") || word.equals("black1"))
                        .reduce((first, second) -> second)
                        .map(word -> word.equals("black1") ? InitialColour.black() : InitialColour.white()));
    }

    /** Without 152, the board-1 colour the first team had in round 1 (as TPN 1, 4.3.1 gives it the initial colour). */
    private static InitialColour inferredInitialColour(List<Round> rounds, List<TeamRecord> teams) {
        if (rounds.isEmpty() || teams.isEmpty()) {
            return InitialColour.white();
        }
        var first = teams.getFirst().id();
        return rounds.getFirst()
                .boardOf(first)
                .filter(board -> board.outcome().isPlayed())
                .map(board -> new InitialColour(board.colourOf(first)))
                .orElse(InitialColour.white());
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

    /** 212 (the full standings order, from {@code PTS}) or 202 (the tie-breaks among equal points). */
    private static Optional<TieBreakList> declaredTieBreaks(Map<String, List<String>> records) {
        return firstValue(records, "212").or(() -> firstValue(records, "202")).map(TieBreakList::parse);
    }

    private static Optional<String> firstValue(Map<String, List<String>> records, String code) {
        return records.getOrDefault(code, List.of()).stream().findFirst().map(TrfReader::valueOf);
    }
}
