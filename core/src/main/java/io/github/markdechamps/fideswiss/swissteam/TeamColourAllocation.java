package io.github.markdechamps.fideswiss.swissteam;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.tournament.Colour;
import io.github.markdechamps.fideswiss.tournament.InitialColour;
import io.github.markdechamps.fideswiss.tournament.SecondaryScore;
import java.util.Comparator;
import java.util.Optional;

/** Article 4: the board-1 colours of a pair, once the whole round is paired (3.3.2). It always decides. */
final class TeamColourAllocation {

    /** A pair with its colours, and the rule of 4.3 that decided them. */
    record ColouredPair(Contender white, Contender black, String article) {}

    private final TeamColourPreferences preferences;
    private final InitialColour initialColour;
    private final Comparator<Contender> firstTeamOrder;

    TeamColourAllocation(TeamColourPreferences preferences, InitialColour initialColour, SecondaryScore secondary) {
        this.preferences = preferences;
        this.initialColour = initialColour;
        this.firstTeamOrder = firstTeamOrder(secondary);
    }

    /** 4.2: the higher primary score, then the higher secondary score (unless not used), then the smaller TPN. */
    private static Comparator<Contender> firstTeamOrder(SecondaryScore secondary) {
        var byScore = Comparator.comparing(Contender::score).reversed();
        var bySecondary = secondary == SecondaryScore.USED_FOR_COLOUR
                ? byScore.thenComparing(
                        Comparator.comparing(Contender::secondaryScore).reversed())
                : byScore;
        return bySecondary.thenComparingInt(Contender::tpn);
    }

    ColouredPair allocate(ContenderPair pair) {
        var first = firstTeamOrder.compare(pair.top(), pair.bottom()) <= 0 ? pair.top() : pair.bottom();
        var teams = new Teams(first, pair.other(first), preferences.of(first), preferences.of(pair.other(first)));
        return bothYetToPlay(teams)
                .or(() -> onlyOnePreference(teams))
                .or(() -> oppositePreferences(teams))
                .or(() -> onlyOneStrongPreference(teams))
                .or(() -> lowerColourDifference(teams))
                .or(() -> mostRecentDifference(teams))
                .or(() -> firstTeamsPreference(teams))
                .or(() -> alternateTheFirstTeam(teams))
                .or(() -> alternateTheOtherTeam(teams))
                .orElseThrow(() -> new IllegalStateException("Article 4.3 left " + pair + " without colours"));
    }

    private record Teams(
            Contender first,
            Contender other,
            Optional<TeamColourPreference> firstPreference,
            Optional<TeamColourPreference> otherPreference) {

        ColouredPair firstGets(Colour colour, String article) {
            return colour == Colour.WHITE
                    ? new ColouredPair(first, other, article)
                    : new ColouredPair(other, first, article);
        }
    }

    /** 4.3.1: the initial-colour to a first-team of odd TPN, the opposite to an even one. */
    private Optional<ColouredPair> bothYetToPlay(Teams teams) {
        if (teams.first().hasPlayedAMatch() || teams.other().hasPlayedAMatch()) {
            return Optional.empty();
        }
        var colour = teams.first().tpn() % 2 == 1
                ? initialColour.colour()
                : initialColour.colour().opposite();
        return Optional.of(teams.firstGets(colour, "C.04.6 4.3.1"));
    }

    private Optional<ColouredPair> onlyOnePreference(Teams teams) {
        if (teams.firstPreference().isPresent() && teams.otherPreference().isEmpty()) {
            return Optional.of(teams.firstGets(teams.firstPreference().get().colour(), "C.04.6 4.3.2"));
        }
        if (teams.firstPreference().isEmpty() && teams.otherPreference().isPresent()) {
            return Optional.of(
                    teams.firstGets(teams.otherPreference().get().colour().opposite(), "C.04.6 4.3.2"));
        }
        return Optional.empty();
    }

    private Optional<ColouredPair> oppositePreferences(Teams teams) {
        if (teams.firstPreference().isPresent()
                && teams.otherPreference().isPresent()
                && teams.firstPreference().get().colour()
                        != teams.otherPreference().get().colour()) {
            return Optional.of(teams.firstGets(teams.firstPreference().get().colour(), "C.04.6 4.3.3"));
        }
        return Optional.empty();
    }

    /** 4.3.4 (Type B only); here both want the same colour, or neither wants one. */
    private Optional<ColouredPair> onlyOneStrongPreference(Teams teams) {
        if (!preferences.distinguishesStrength()
                || teams.firstPreference().isEmpty()
                || teams.otherPreference().isEmpty()) {
            return Optional.empty();
        }
        var firstStrong = teams.firstPreference().get().isStrong();
        var otherStrong = teams.otherPreference().get().isStrong();
        if (firstStrong && !otherStrong) {
            return Optional.of(teams.firstGets(teams.firstPreference().get().colour(), "C.04.6 4.3.4"));
        }
        if (!firstStrong && otherStrong) {
            return Optional.of(
                    teams.firstGets(teams.otherPreference().get().colour().opposite(), "C.04.6 4.3.4"));
        }
        return Optional.empty();
    }

    /** 4.3.5: White to the lower colour difference. */
    private static Optional<ColouredPair> lowerColourDifference(Teams teams) {
        var first = teams.first().colourDifference();
        var other = teams.other().colourDifference();
        if (first == other) {
            return Optional.empty();
        }
        return Optional.of(teams.firstGets(first < other ? Colour.WHITE : Colour.BLACK, "C.04.6 4.3.5"));
    }

    /** 4.3.6, ruling A12: the played-only histories compared from the end (GHR 3.4). */
    private static Optional<ColouredPair> mostRecentDifference(Teams teams) {
        var first = teams.first().colours();
        var other = teams.other().colours();
        for (var back = 1; back <= Math.min(first.size(), other.size()); back++) {
            var firstColour = first.get(first.size() - back);
            if (firstColour != other.get(other.size() - back)) {
                return Optional.of(teams.firstGets(firstColour.opposite(), "C.04.6 4.3.6"));
            }
        }
        return Optional.empty();
    }

    private static Optional<ColouredPair> firstTeamsPreference(Teams teams) {
        return teams.firstPreference().map(preference -> teams.firstGets(preference.colour(), "C.04.6 4.3.7"));
    }

    private static Optional<ColouredPair> alternateTheFirstTeam(Teams teams) {
        return lastColour(teams.first()).map(colour -> teams.firstGets(colour.opposite(), "C.04.6 4.3.8"));
    }

    private static Optional<ColouredPair> alternateTheOtherTeam(Teams teams) {
        return lastColour(teams.other()).map(colour -> teams.firstGets(colour, "C.04.6 4.3.9"));
    }

    private static Optional<Colour> lastColour(Contender team) {
        return team.colours().isEmpty()
                ? Optional.empty()
                : Optional.of(team.colours().getLast());
    }
}
