package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.Optional;

/** Settings given as flags, which override the file's records (Tournament profiles: profile < file < flags). */
record SettingsOverrides(
        Optional<NumberOfRounds> rounds,
        Optional<InitialColour> initialColour,
        Optional<TieBreakList> tieBreaks,
        Optional<TieBreakEdition> tieBreakEdition) {

    static final SettingsOverrides NONE =
            new SettingsOverrides(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(Optional.of(value), initialColour, tieBreaks, tieBreakEdition);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(rounds, Optional.of(value), tieBreaks, tieBreakEdition);
    }

    SettingsOverrides withTieBreaks(TieBreakList value) {
        return new SettingsOverrides(rounds, initialColour, Optional.of(value), tieBreakEdition);
    }

    SettingsOverrides withTieBreakEdition(TieBreakEdition value) {
        return new SettingsOverrides(rounds, initialColour, tieBreaks, Optional.of(value));
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var overridden = rounds.map(settings::with).orElse(settings);
        overridden = initialColour.map(overridden::with).orElse(overridden);
        overridden = tieBreaks.map(overridden::with).orElse(overridden);
        return tieBreakEdition.map(overridden::with).orElse(overridden);
    }
}
