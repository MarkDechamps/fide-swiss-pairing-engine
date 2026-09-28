package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.Optional;

/** Settings given as flags, which override the file's records (Tournament profiles: profile < file < flags). */
record SettingsOverrides(
        Optional<PairingSystem> system,
        Optional<NumberOfRounds> rounds,
        Optional<InitialColour> initialColour,
        MaxiTournament maxiTournament) {

    static final SettingsOverrides NONE =
            new SettingsOverrides(Optional.empty(), Optional.empty(), Optional.empty(), MaxiTournament.NOT_DECLARED);

    SettingsOverrides withSystem(PairingSystem value) {
        return new SettingsOverrides(Optional.of(value), rounds, initialColour, maxiTournament);
    }

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(system, Optional.of(value), initialColour, maxiTournament);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(system, rounds, Optional.of(value), maxiTournament);
    }

    /** {@code --maxi-tournament}: only a Lim tournament, from the file or a flag, can be one. */
    SettingsOverrides asMaxiTournament() {
        return new SettingsOverrides(system, rounds, initialColour, MaxiTournament.DECLARED);
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var withSystem = system.map(settings::with).orElse(settings);
        var withMaxi = maxiTournament == MaxiTournament.DECLARED
                ? withSystem.with(PairingSystems.asMaxiTournament(withSystem.pairingSystem()))
                : withSystem;
        var withRounds = rounds.map(withMaxi::with).orElse(withMaxi);
        return initialColour.map(withRounds::with).orElse(withRounds);
    }
}
