package io.github.markdechamps.fideswiss.swissteam;

import io.github.markdechamps.fideswiss.topscoregroup.Contender;
import io.github.markdechamps.fideswiss.topscoregroup.ContenderPair;
import io.github.markdechamps.fideswiss.topscoregroup.PairCriterion;
import java.util.List;
import java.util.Set;

/**
 * The bracket criteria of 3.6.4, in descending priority, each counted per pair. Article 4 grants every preference
 * of a pair unless both teams want the same colour, so [C8] and [C9] are known before the colours are.
 */
final class SwissTeamCriteria {

    private SwissTeamCriteria() {}

    static List<PairCriterion> of(TeamColourPreferences preferences, boolean floatCriteriaLapse) {
        return List.of(
                new UnfulfilledPreferences(preferences),
                new UnfulfilledStrongPreferences(preferences),
                new UpfloatersOpponentsThatFloated(floatCriteriaLapse));
    }

    /** 2.3.5 [C8]: two teams that want the same colour leave one of them unfulfilled. */
    private record UnfulfilledPreferences(TeamColourPreferences preferences) implements PairCriterion {
        @Override
        public String article() {
            return "C8";
        }

        @Override
        public long failureOf(ContenderPair pair, Set<Contender> upfloaters) {
            return TeamColourPreference.sameColour(preferences.of(pair.top()), preferences.of(pair.bottom())) ? 1 : 0;
        }
    }

    /** 2.3.6 [C9] (Type B only): two strong preferences for the same colour; 4.3.4 grants strong over mild. */
    private record UnfulfilledStrongPreferences(TeamColourPreferences preferences) implements PairCriterion {
        @Override
        public String article() {
            return "C9";
        }

        @Override
        public long failureOf(ContenderPair pair, Set<Contender> upfloaters) {
            var top = preferences.of(pair.top());
            var bottom = preferences.of(pair.bottom());
            var bothStrong = top.filter(TeamColourPreference::isStrong).isPresent()
                    && bottom.filter(TeamColourPreference::isStrong).isPresent();
            return preferences.distinguishesStrength() && bothStrong && TeamColourPreference.sameColour(top, bottom)
                    ? 1
                    : 0;
        }
    }

    /** 2.3.7 [C10]: with the exception of the last two rounds, an upfloater's opponent that floated last round. */
    private record UpfloatersOpponentsThatFloated(boolean lapses) implements PairCriterion {
        @Override
        public String article() {
            return "C10";
        }

        @Override
        public long failureOf(ContenderPair pair, Set<Contender> upfloaters) {
            if (lapses) {
                return 0;
            }
            return pair.upfloatersOpponent(upfloaters)
                            .filter(Contender::floatedInPreviousRound)
                            .isPresent()
                    ? 1
                    : 0;
        }
    }
}
