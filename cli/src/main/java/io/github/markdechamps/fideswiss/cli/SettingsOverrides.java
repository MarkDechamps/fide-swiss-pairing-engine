package io.github.markdechamps.fideswiss.cli;

import io.github.markdechamps.fideswiss.pairing.MaxiTournament;
import io.github.markdechamps.fideswiss.pairing.PairingSystem;
import io.github.markdechamps.fideswiss.pairing.PairingSystems;
import io.github.markdechamps.fideswiss.standings.TieBreakEdition;
import io.github.markdechamps.fideswiss.standings.TieBreakList;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.NumberOfRounds;
import io.github.markdechamps.fideswiss.tournament.SwissRulesEdition;
import io.github.markdechamps.fideswiss.tournament.TournamentSettings;
import java.util.Optional;

/** Settings given as flags, which override the file's records (Tournament profiles: profile < file < flags). */
record SettingsOverrides(
        Optional<PairingSystem> system,
        MaxiTournament maxiTournament,
        Optional<NumberOfRounds> rounds,
        Optional<InitialColour> initialColour,
        Optional<SwissRulesEdition> edition,
        Optional<TieBreakList> tieBreaks,
        Optional<TieBreakEdition> tieBreakEdition) {

    static final SettingsOverrides NONE = new SettingsOverrides(
            Optional.empty(),
            MaxiTournament.NOT_DECLARED,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty());

    SettingsOverrides withSystem(PairingSystem value) {
        return new SettingsOverrides(
                Optional.of(value), maxiTournament, rounds, initialColour, edition, tieBreaks, tieBreakEdition);
    }

    /** {@code --maxi-tournament}: only a Lim tournament, from the file or a flag, can be one. */
    SettingsOverrides asMaxiTournament() {
        return new SettingsOverrides(
                system, MaxiTournament.DECLARED, rounds, initialColour, edition, tieBreaks, tieBreakEdition);
    }

    SettingsOverrides withRounds(NumberOfRounds value) {
        return new SettingsOverrides(
                system, maxiTournament, Optional.of(value), initialColour, edition, tieBreaks, tieBreakEdition);
    }

    SettingsOverrides withInitialColour(InitialColour value) {
        return new SettingsOverrides(
                system, maxiTournament, rounds, Optional.of(value), edition, tieBreaks, tieBreakEdition);
    }

    SettingsOverrides withEdition(SwissRulesEdition value) {
        return new SettingsOverrides(
                system, maxiTournament, rounds, initialColour, Optional.of(value), tieBreaks, tieBreakEdition);
    }

    SettingsOverrides withTieBreaks(TieBreakList value) {
        return new SettingsOverrides(
                system, maxiTournament, rounds, initialColour, edition, Optional.of(value), tieBreakEdition);
    }

    SettingsOverrides withTieBreakEdition(TieBreakEdition value) {
        return new SettingsOverrides(
                system, maxiTournament, rounds, initialColour, edition, tieBreaks, Optional.of(value));
    }

    TournamentSettings applyTo(TournamentSettings settings) {
        var overridden = system.map(settings::with).orElse(settings);
        overridden = declaredMaxiTournament(overridden);
        overridden = rounds.map(overridden::with).orElse(overridden);
        overridden = initialColour.map(overridden::with).orElse(overridden);
        overridden = edition.map(overridden::with).orElse(overridden);
        overridden = tieBreaks.map(overridden::with).orElse(overridden);
        return tieBreakEdition.map(overridden::with).orElse(overridden);
    }

    private TournamentSettings declaredMaxiTournament(TournamentSettings settings) {
        return maxiTournament == MaxiTournament.DECLARED
                ? settings.with(PairingSystems.asMaxiTournament(settings.pairingSystem()))
                : settings;
    }
}
