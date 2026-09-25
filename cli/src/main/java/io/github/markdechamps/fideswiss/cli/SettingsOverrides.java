package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.Optional;

/** Settings given as flags, which override the file's records (Tournament profiles: profile < file < flags). */
record SettingsOverrides(
        Optional<PairingSystem> system, Optional<NumberOfRounds> rounds, Optional<InitialColour> initialColour) {

    static final SettingsOverrides NONE = new SettingsOverrides(Optional.empty(), Optional.empty(), Optional.empty());

    SettingsOverrides withSystem(PairingSystem value) {
        return new SettingsOverrides(Optional.of(value), rounds, initialColour);
    }

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(system, Optional.of(value), initialColour);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(system, rounds, Optional.of(value));
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var withSystem = system.map(settings::with).orElse(settings);
        var withRounds = rounds.map(withSystem::with).orElse(withSystem);
        return initialColour.map(withRounds::with).orElse(withRounds);
    }
}
