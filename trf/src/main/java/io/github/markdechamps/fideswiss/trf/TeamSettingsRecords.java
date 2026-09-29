package io.github.markdechamps.fideswiss.trf;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.MatchScoring;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.Points;
import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import io.github.markdechamps.fideswiss.tournament.Profiles;
import io.github.markdechamps.fideswiss.tournament.ScoringScheme;
import io.github.markdechamps.fideswiss.tournament.SecondaryScore;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The settings of a team file over the {@code teamSwiss} profile: {@code 192}
 * ({@code FIDE_TEAM[_TYPEA|_TYPEB][_MP|_GP][_GP|_MP][_BAKU]}), game points from {@code 162}, match points from
 * {@code 362}, the boards from {@code 352} and the PAB's points from {@code 320} (or, under game points, from
 * {@code 162}'s {@code P} on every board). The provisional {@code FIDE_OLYMPIAD} (ETT26 has no Olympiad code) takes
 * the {@code olympiad} profile instead: the Olympiad Pairing Rules, by matchpoints, with its own bye.
 */
final class TeamSettingsRecords {

    private static final Pattern SYMBOL_AND_POINTS = Pattern.compile("([A-Z]{1,2})\\s*(-?\\d+(?:\\.\\d+)?)");

    /** Provisional: ETT26 has no code for the Olympiad Pairing Rules (TRF CLI surface). */
    static final String OLYMPIAD = "FIDE_OLYMPIAD";

    private TeamSettingsRecords() {}

    static boolean declaresATeamSystem(Map<String, List<String>> records) {
        return records.containsKey("310")
                || code(records)
                        .filter(code -> code.contains("TEAM") || isOlympiad(code))
                        .isPresent();
    }

    static TournamentSettings read(Map<String, List<String>> records, NumberOfRounds rounds, int boards) {
        var code = code(records).orElse("FIDE_TEAM");
        if (isOlympiad(code)) {
            return olympiad(records, rounds, boards);
        }
        if (!code.startsWith("FIDE_TEAM")) {
            throw new InvalidTrfException("Unsupported 192 code " + code + " for a team file");
        }
        var matches = matchScoring(records, code).withBoards(boards);
        var scoring = gameScoring(records).with(matches);
        var perBoard = symbols(records, "162").get("P");
        scoring = pairingAllocatedByeValue(records, matches.primary())
                .or(() -> Optional.ofNullable(perBoard)
                        .filter(value -> matches.primary() == PrimaryScore.GAME_POINTS)
                        .map(value -> value.times(boards)))
                .map(scoring::withPairingAllocatedBye)
                .orElse(scoring);
        return Profiles.teamSwiss(rounds)
                .with(PairingSystems.swissTeam(colourPreferences(code)))
                .with(scoring);
    }

    /** {@code FIDE_OLYMPIAD}, with or without {@code _BAKU}, which the Olympiad Pairing Rules then refuse. */
    private static boolean isOlympiad(String code) {
        return code.replaceFirst("_BAKU$", "").equals(OLYMPIAD);
    }

    /** D.02: match points from {@code 362} (primary), game points from {@code 162}, the bye as 4.3 fixes it. */
    private static TournamentSettings olympiad(Map<String, List<String>> records, NumberOfRounds rounds, int boards) {
        var values = symbols(records, "362");
        var matches = new MatchScoring(
                        values.getOrDefault("TW", Points.of(2)),
                        values.getOrDefault("TD", Points.of(1)),
                        values.getOrDefault("TL", Points.ZERO),
                        PrimaryScore.MATCH_POINTS)
                .withBoards(boards);
        return Profiles.olympiad(rounds).with(gameScoring(records).with(matches));
    }

    /** TYPEA or TYPEB; a bare {@code FIDE_TEAM} is Type A; any other code uses no colour preferences. */
    private static ColourPreferenceType colourPreferences(String code) {
        var bare = code.replaceFirst("_BAKU$", "");
        if (bare.equals("FIDE_TEAM") || bare.contains("_TYPEA")) {
            return ColourPreferenceType.TYPE_A;
        }
        return bare.contains("_TYPEB") ? ColourPreferenceType.TYPE_B : ColourPreferenceType.NONE;
    }

    /**
     * The first of MP and GP is the primary score; a second one is the secondary score for colours. A code naming
     * only one has no secondary score; a code naming neither keeps the C.04.6 1.2.2 default.
     */
    private static MatchScoring matchScoring(Map<String, List<String>> records, String code) {
        var values = symbols(records, "362");
        var scoring = new MatchScoring(
                values.getOrDefault("TW", Points.of(2)),
                values.getOrDefault("TD", Points.of(1)),
                values.getOrDefault("TL", Points.ZERO),
                PrimaryScore.MATCH_POINTS);
        if (code.contains("_MP_GP")) {
            return scoring;
        }
        if (code.contains("_GP_MP")) {
            return scoring.withPrimary(PrimaryScore.GAME_POINTS);
        }
        if (code.contains("_MP")) {
            return scoring.with(SecondaryScore.NOT_USED);
        }
        if (code.contains("_GP")) {
            return scoring.withPrimary(PrimaryScore.GAME_POINTS).with(SecondaryScore.NOT_USED);
        }
        return scoring;
    }

    private static ScoringScheme gameScoring(Map<String, List<String>> records) {
        var values = symbols(records, "162");
        return new ScoringScheme(
                values.getOrDefault("W", Points.of(1)),
                values.getOrDefault("D", Points.of("0.5")),
                values.getOrDefault("L", Points.ZERO),
                Optional.empty());
    }

    /** {@code 320 MMMM GGGG …}: the PAB's match points at columns 5–8 and game points at 10–13. */
    private static Optional<Points> pairingAllocatedByeValue(Map<String, List<String>> records, PrimaryScore primary) {
        return records.getOrDefault("320", List.of()).stream().findFirst().flatMap(line -> {
            var field = primary == PrimaryScore.MATCH_POINTS
                    ? PlayerRecord.columns(line, 5, 8)
                    : PlayerRecord.columns(line, 10, 13);
            return field.isBlank() ? Optional.empty() : Optional.of(Points.of(field.trim()));
        });
    }

    private static Map<String, Points> symbols(Map<String, List<String>> records, String code) {
        var values = new HashMap<String, Points>();
        records.getOrDefault(code, List.of()).forEach(line -> {
            var matcher = SYMBOL_AND_POINTS.matcher(TrfReader.valueOf(line).toUpperCase());
            while (matcher.find()) {
                values.putIfAbsent(matcher.group(1), Points.of(matcher.group(2)));
            }
        });
        return values;
    }

    private static Optional<String> code(Map<String, List<String>> records) {
        return records.getOrDefault("192", List.of()).stream()
                .findFirst()
                .map(TrfReader::valueOf)
                .map(value -> value.trim().toUpperCase());
    }
}
