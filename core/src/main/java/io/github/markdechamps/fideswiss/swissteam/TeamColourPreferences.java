package io.github.markdechamps.fideswiss.swissteam;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.ColourPreferenceType;
import io.github.markdechamps.fideswiss.tournament.LastRoundZeroCdTypeB;
import java.util.Optional;

/** 1.7: each team's colour preference in the round being paired, by the competition's preference type. */
record TeamColourPreferences(ColourPreferenceType type, LastRoundZeroCdTypeB lastRoundZeroCd, boolean isLastRound) {

    Optional<TeamColourPreference> of(Contender team) {
        return switch (type) {
            case NONE -> Optional.empty();
            case TYPE_A -> strongOrSimple(team);
            case TYPE_B -> typeB(team);
        };
    }

    /** Type B is the only type with mild preferences (4.3.4, [C9]). */
    boolean distinguishesStrength() {
        return type == ColourPreferenceType.TYPE_B;
    }

    /**
     * 1.7.2; ruling A1: in the last round, a team with CD 0 that had the same colour twice keeps its strong
     * preference unless the Interpretation says the no-preference sentence covers it too.
     */
    private Optional<TeamColourPreference> typeB(Contender team) {
        if (isLastRound && team.colourDifference() == 0 && lastRoundZeroCd == LastRoundZeroCdTypeB.NONE) {
            return Optional.empty();
        }
        return strongOrSimple(team).or(() -> mild(team));
    }

    /** The first two paragraphs of 1.7.1 and 1.7.2, worded identically; the strong clause wins (ruling A2). */
    private static Optional<TeamColourPreference> strongOrSimple(Contender team) {
        var cd = team.colourDifference();
        if (cd < -1 || (cd == 0 || cd == -1) && team.hadInLastPlayedMatches(Colour.BLACK, 2)) {
            return Optional.of(TeamColourPreference.strong(Colour.WHITE));
        }
        if (cd > 1 || (cd == 0 || cd == 1) && team.hadInLastPlayedMatches(Colour.WHITE, 2)) {
            return Optional.of(TeamColourPreference.strong(Colour.BLACK));
        }
        return Optional.empty();
    }

    /** 1.7.2, third and fourth paragraphs; none before a first match or at CD 0 in the last round. */
    private Optional<TeamColourPreference> mild(Contender team) {
        var cd = team.colourDifference();
        if (cd == -1) {
            return Optional.of(TeamColourPreference.mild(Colour.WHITE));
        }
        if (cd == 1) {
            return Optional.of(TeamColourPreference.mild(Colour.BLACK));
        }
        if (cd == 0 && !isLastRound && team.hasPlayedAMatch()) {
            return Optional.of(
                    TeamColourPreference.mild(team.colours().getLast().opposite()));
        }
        return Optional.empty();
    }
}
