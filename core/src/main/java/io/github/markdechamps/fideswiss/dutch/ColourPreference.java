package io.github.markdechamps.fideswiss.dutch;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.List;
import java.util.Optional;

/** Dutch 1.7: the colour a player prefers, and how strongly, from the colours of its played games only. */
record ColourPreference(Optional<Colour> colour, PreferenceStrength strength) {

    static final ColourPreference NONE = new ColourPreference(Optional.empty(), PreferenceStrength.NONE);

    static ColourPreference of(Colour colour, PreferenceStrength strength) {
        return new ColourPreference(Optional.of(colour), strength);
    }

    /** 1.7.1–1.7.4 over the played-colour sequence (GHR 3.4). */
    static ColourPreference from(List<Colour> playedColours) {
        if (playedColours.isEmpty()) {
            return NONE;
        }
        var difference = colourDifference(playedColours);
        var last = playedColours.getLast();
        if (difference > 1) {
            return of(Colour.BLACK, PreferenceStrength.ABSOLUTE);
        }
        if (difference < -1) {
            return of(Colour.WHITE, PreferenceStrength.ABSOLUTE);
        }
        if (playedSameColourTwiceInARow(playedColours)) {
            return of(last.opposite(), PreferenceStrength.ABSOLUTE);
        }
        if (difference != 0) {
            return of(difference > 0 ? Colour.BLACK : Colour.WHITE, PreferenceStrength.STRONG);
        }
        return of(last.opposite(), PreferenceStrength.MILD);
    }

    /** 1.6: games with White minus games with Black. */
    static int colourDifference(List<Colour> playedColours) {
        var whites = playedColours.stream().filter(Colour.WHITE::equals).count();
        return (int) (2 * whites - playedColours.size());
    }

    boolean isAbsolute() {
        return strength == PreferenceStrength.ABSOLUTE;
    }

    boolean isStrong() {
        return strength == PreferenceStrength.STRONG;
    }

    boolean isStrongerThan(ColourPreference other) {
        return strength.compareTo(other.strength) > 0;
    }

    /** Whether the allocated colour satisfies the preference; no preference is always satisfied. */
    boolean isGrantedBy(Colour allocated) {
        return colour.map(allocated::equals).orElse(true);
    }

    private static boolean playedSameColourTwiceInARow(List<Colour> colours) {
        return colours.size() >= 2 && colours.getLast() == colours.get(colours.size() - 2);
    }
}
