package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
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
        Optional<PairingSystem> system,
        MaxiTournament maxiTournament,
        Optional<NumberOfRounds> rounds,
        Optional<InitialColour> initialColour,
        Optional<SwissRulesEdition> edition,
        Optional<TieBreakList> tieBreaks,
        Optional<TieBreakEdition> tieBreakEdition,
        List<Interpretation> interpretations) {

    static final SettingsOverrides NONE = new SettingsOverrides(
            Optional.empty(),
            MaxiTournament.NOT_DECLARED,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            List.of());

    SettingsOverrides {
        interpretations = List.copyOf(interpretations);
    }

    SettingsOverrides withSystem(PairingSystem value) {
        return new SettingsOverrides(
                Optional.of(value),
                maxiTournament,
                rounds,
                initialColour,
                edition,
                tieBreaks,
                tieBreakEdition,
                interpretations);
    }

    /** {@code --maxi-tournament}: only a Lim tournament, from the file or a flag, can be one. */
    SettingsOverrides asMaxiTournament() {
        return new SettingsOverrides(
                system,
                MaxiTournament.DECLARED,
                rounds,
                initialColour,
                edition,
                tieBreaks,
                tieBreakEdition,
                interpretations);
    }

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(
                system,
                maxiTournament,
                Optional.of(value),
                initialColour,
                edition,
                tieBreaks,
                tieBreakEdition,
                interpretations);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(
                system,
                maxiTournament,
                rounds,
                Optional.of(value),
                edition,
                tieBreaks,
                tieBreakEdition,
                interpretations);
    }

    SettingsOverrides withEdition(SwissRulesEdition value) {
        return new SettingsOverrides(
                system,
                maxiTournament,
                rounds,
                initialColour,
                Optional.of(value),
                tieBreaks,
                tieBreakEdition,
                interpretations);
    }

    SettingsOverrides withTieBreaks(TieBreakList value) {
        return new SettingsOverrides(
                system,
                maxiTournament,
                rounds,
                initialColour,
                edition,
                Optional.of(value),
                tieBreakEdition,
                interpretations);
    }

    SettingsOverrides withTieBreakEdition(TieBreakEdition value) {
        return new SettingsOverrides(
                system, maxiTournament, rounds, initialColour, edition, tieBreaks, Optional.of(value), interpretations);
    }

    SettingsOverrides with(Interpretation interpretation) {
        var chosen = new ArrayList<>(interpretations);
        chosen.add(interpretation);
        return new SettingsOverrides(
                system, maxiTournament, rounds, initialColour, edition, tieBreaks, tieBreakEdition, chosen);
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var overridden = system.map(chosen -> withSystem(settings, chosen)).orElse(settings);
        overridden = declaredMaxiTournament(overridden);
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

    /**
     * A system of the file's own kind of competition. A team file keeps its own Swiss Team System, which carries
     * the file's colour preferences.
     */
    private static TournamentSettings withSystem(TournamentSettings settings, PairingSystem chosen) {
        var file = settings.pairingSystem().competitionType();
        if (chosen.competitionType() == file) {
            return file == CompetitionType.TEAM ? settings : settings.with(chosen);
        }
        throw new UsageException(
                file == CompetitionType.TEAM
                        ? chosen.name() + " cannot pair a team file (310 records)"
                        : chosen.name() + " pairs teams, and the file has no 310 records");
    }

    private TournamentSettings declaredMaxiTournament(TournamentSettings settings) {
        return maxiTournament == MaxiTournament.DECLARED
                ? settings.with(PairingSystems.asMaxiTournament(settings.pairingSystem()))
                : settings;
    }
}
