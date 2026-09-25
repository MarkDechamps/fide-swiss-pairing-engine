package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.Optional;

/** Settings given as flags, which override the file's records (Tournament profiles: profile < file < flags). */
record SettingsOverrides(
        Optional<NumberOfRounds> rounds, Optional<InitialColour> initialColour, Optional<SwissRulesEdition> edition) {

    static final SettingsOverrides NONE = new SettingsOverrides(Optional.empty(), Optional.empty(), Optional.empty());

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(Optional.of(value), initialColour, edition);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(rounds, Optional.of(value), edition);
    }

    SettingsOverrides withEdition(SwissRulesEdition value) {
        return new SettingsOverrides(rounds, initialColour, Optional.of(value));
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var withRounds = rounds.map(settings::with).orElse(settings);
        var withColour = initialColour.map(withRounds::with).orElse(withRounds);
        return edition.map(withColour::with).orElse(withColour);
    }
}
