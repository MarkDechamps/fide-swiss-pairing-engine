package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.PrimaryScore;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

/**
 * One entry of a Tie-break List in the Technical Commission's code syntax: an acronym and its modifiers, such as
 * {@code BH/C1}, {@code SB/M1/P}, {@code KS/L+2} or {@code AOB/F}. The Handbook's hyphen form ({@code BH-C1}) is
 * read too, and codes are case-insensitive.
 *
 * @param lowCuts the least significant values cut (Cut-1/2, and the low half of Median-1/2, C.07 14.1–14.4)
 * @param highCuts the most significant values cut (the high half of Median-1/2)
 * @param forfeitsAsPlayed {@code /P}: forfeits count as games against the scheduled opponent
 * @param fore {@code /F}: AOB over the opponents' Fore Buchholz (8.2)
 * @param limitShift {@code /L±n}: the Koya limit moved by n half points (14.5)
 * @param teamScore {@code :MP} or {@code :GP}: the team score the tie-break is computed in (C.07 13); empty is the
 *     primary score
 * @param normalisingFactor {@code /Kx}: SSSC's own normalising factor (13.4.2)
 */
public record TieBreakCode(
        String acronym,
        int lowCuts,
        int highCuts,
        boolean forfeitsAsPlayed,
        boolean fore,
        int limitShift,
        Optional<PrimaryScore> teamScore,
        Optional<Integer> normalisingFactor) {

    public TieBreakCode {
        teamScore = teamScore == null ? Optional.empty() : teamScore;
        normalisingFactor = normalisingFactor == null ? Optional.empty() : normalisingFactor;
    }

    public TieBreakCode(
            String acronym, int lowCuts, int highCuts, boolean forfeitsAsPlayed, boolean fore, int limitShift) {
        this(acronym, lowCuts, highCuts, forfeitsAsPlayed, fore, limitShift, Optional.empty(), Optional.empty());
    }

    static TieBreakCode parse(String text) {
        var parts = text.trim().toUpperCase(Locale.ROOT).split("[/-]");
        var head = parts[0].trim();
        var acronym = head.contains(":") ? head.substring(0, head.indexOf(':')).trim() : head;
        if (acronym.isEmpty()) {
            throw new IllegalArgumentException("empty tie-break code");
        }
        var teamScore = head.contains(":")
                ? teamScore(head.substring(head.indexOf(':') + 1).trim(), head)
                : null;
        Integer normalisingFactor = null;
        var lowCuts = 0;
        var highCuts = 0;
        var forfeitsAsPlayed = false;
        var fore = false;
        var limitShift = 0;
        for (var index = 1; index < parts.length; index++) {
            var modifier = parts[index].trim();
            switch (modifier) {
                case "C1" -> lowCuts += 1;
                case "C2" -> lowCuts += 2;
                case "M1" -> {
                    lowCuts += 1;
                    highCuts += 1;
                }
                case "M2" -> {
                    lowCuts += 2;
                    highCuts += 2;
                }
                case "P" -> forfeitsAsPlayed = true;
                case "F" -> fore = true;
                default -> {
                    if (modifier.startsWith("K")) {
                        normalisingFactor = factor(modifier);
                    } else {
                        limitShift = limit(modifier, text);
                    }
                }
            }
        }
        return new TieBreakCode(
                acronym,
                lowCuts,
                highCuts,
                forfeitsAsPlayed,
                fore,
                limitShift,
                Optional.ofNullable(teamScore),
                Optional.ofNullable(normalisingFactor));
    }

    private static PrimaryScore teamScore(String score, String head) {
        return switch (score) {
            case "MP" -> PrimaryScore.MATCH_POINTS;
            case "GP" -> PrimaryScore.GAME_POINTS;
            default -> throw new IllegalArgumentException("the team score of " + head + " is :MP or :GP");
        };
    }

    private static int factor(String modifier) {
        try {
            var factor = Integer.parseInt(modifier.substring(1));
            if (factor < 1) {
                throw new NumberFormatException();
            }
            return factor;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("the SSSC normalising factor is a whole number from 1: /" + modifier);
        }
    }

    private static int limit(String modifier, String text) {
        if (!modifier.startsWith("L")) {
            throw new IllegalArgumentException("unknown modifier /" + modifier + " in " + text.trim());
        }
        try {
            return Integer.parseInt(modifier.substring(1).replace("+", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("the Koya limit takes a whole number of half points: /" + modifier);
        }
    }

    /** The code as the TEC syntax writes it, for example {@code BH/C1/P}. */
    @Override
    public String toString() {
        var modifiers = new ArrayList<String>();
        normalisingFactor.ifPresent(factor -> modifiers.add("K" + factor));
        if (highCuts > 0) {
            modifiers.add("M" + highCuts);
        } else if (lowCuts > 0) {
            modifiers.add("C" + lowCuts);
        }
        if (limitShift != 0) {
            modifiers.add("L" + (limitShift > 0 ? "+" : "") + limitShift);
        }
        if (fore) {
            modifiers.add("F");
        }
        if (forfeitsAsPlayed) {
            modifiers.add("P");
        }
        var name = acronym.equals("MPVGP") ? "MPvGP" : acronym;
        var head = teamScore
                .map(score -> name + (score == PrimaryScore.MATCH_POINTS ? ":MP" : ":GP"))
                .orElse(name);
        return modifiers.isEmpty() ? head : head + "/" + String.join("/", modifiers);
    }
}
