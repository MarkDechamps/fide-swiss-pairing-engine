package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.CompetitionType;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.Interpretation;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Settings given as flags, which override the file's records (Tournament profiles: profile < file < flags). */
record SettingsOverrides(
        Optional<NumberOfRounds> rounds,
        Optional<InitialColour> initialColour,
        Optional<SwissRulesEdition> edition,
        Optional<TieBreakList> tieBreaks,
        Optional<TieBreakEdition> tieBreakEdition,
        Optional<String> system,
        List<Interpretation> interpretations) {

    static final SettingsOverrides NONE = new SettingsOverrides(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            List.of());

    SettingsOverrides {
        interpretations = List.copyOf(interpretations);
    }

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(
                Optional.of(value), initialColour, edition, tieBreaks, tieBreakEdition, system, interpretations);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(
                rounds, Optional.of(value), edition, tieBreaks, tieBreakEdition, system, interpretations);
    }

    SettingsOverrides withEdition(SwissRulesEdition value) {
        return new SettingsOverrides(
                rounds, initialColour, Optional.of(value), tieBreaks, tieBreakEdition, system, interpretations);
    }

    SettingsOverrides withTieBreaks(TieBreakList value) {
        return new SettingsOverrides(
                rounds, initialColour, edition, Optional.of(value), tieBreakEdition, system, interpretations);
    }

    SettingsOverrides withTieBreakEdition(TieBreakEdition value) {
        return new SettingsOverrides(
                rounds, initialColour, edition, tieBreaks, Optional.of(value), system, interpretations);
    }

    SettingsOverrides withSystem(String name) {
        return new SettingsOverrides(
                rounds, initialColour, edition, tieBreaks, tieBreakEdition, Optional.of(name), interpretations);
    }

    SettingsOverrides with(Interpretation interpretation) {
        var chosen = new ArrayList<>(interpretations);
        chosen.add(interpretation);
        return new SettingsOverrides(rounds, initialColour, edition, tieBreaks, tieBreakEdition, system, chosen);
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var overridden = system.map(name -> withSystem(settings, name)).orElse(settings);
        overridden = rounds.map(overridden::with).orElse(overridden);
        overridden = initialColour.map(overridden::with).orElse(overridden);
        overridden = edition.map(overridden::with).orElse(overridden);
        overridden = tieBreaks.map(overridden::with).orElse(overridden);
        overridden = tieBreakEdition.map(overridden::with).orElse(overridden);
        for (var interpretation : interpretations) {
            overridden = overridden.with(interpretation);
        }
        return overridden;
    }

    /** A system of the file's own kind of competition: a team file keeps its colour preferences. */
    private static TournamentSettings withSystem(TournamentSettings settings, String name) {
        var current = settings.pairingSystem().competitionType();
        return switch (name) {
            case "dutch" -> {
                if (current == CompetitionType.TEAM) {
                    throw new UsageException("--dutch cannot pair a team file (310 records)");
                }
                yield settings.with(PairingSystems.dutch());
            }
            case "swiss-team" -> {
                if (current != CompetitionType.TEAM) {
                    throw new UsageException("--swiss-team pairs teams, and the file has no 310 records");
                }
                yield settings;
            }
            default -> throw new UsageException("unsupported pairing system " + name);
        };
    }
}
