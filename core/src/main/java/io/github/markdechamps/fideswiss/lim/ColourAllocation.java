package io.github.markdechamps.fideswiss.lim;

import io.github.markdechamps.fideswiss.tournament.Colour;
import java.util.Optional;

/**
 * Article 5 (and 6, 7.2): the colours of a pair once the pairing is made.
 *
 * <ol>
 *   <li>5.1.1, 5.1.2, 5.3: a colour that would be a third in a row or three more of one colour is never given;
 *   <li>5.1, 5.5, 5.6: each player gets the colour due to them (equalising, else alternating) when the two differ;
 *   <li>5.4: otherwise the earlier colours, going back, decide: at the latest round where they differ each gets the
 *       colour the other had then;
 *   <li>5.4: with identical histories, in the Median Scoregroup or above the higher ranked player gets the colour
 *       due to them, below it the lower ranked one.
 * </ol>
 */
final class ColourAllocation {

    private final RoundToPair round;

    ColourAllocation(RoundToPair round) {
        this.round = round;
    }

    Game allocate(Pair pair, boolean medianOrAbove) {
        var a = pair.first();
        var b = pair.second();
        var decision = byLimits(a, b)
                .map(colour -> new Decision(colour, "C.04.4.3 5.3"))
                .or(() -> byDueColours(a, b).map(colour -> new Decision(colour, "C.04.4.3 5.1")))
                .or(() -> byHistory(a, b).map(colour -> new Decision(colour, "C.04.4.3 5.4")))
                .orElseGet(() -> new Decision(byRank(a, b, medianOrAbove), "C.04.4.3 5.4"));
        return decision.colour() == Colour.WHITE
                ? new Game(a, b, decision.article())
                : new Game(b, a, decision.article());
    }

    private record Decision(Colour colour, String article) {}

    private static Optional<Colour> byLimits(Player a, Player b) {
        var aWhite = a.mayReceive(Colour.WHITE) && b.mayReceive(Colour.BLACK);
        var aBlack = a.mayReceive(Colour.BLACK) && b.mayReceive(Colour.WHITE);
        if (aWhite == aBlack) {
            return Optional.empty();
        }
        return Optional.of(aWhite ? Colour.WHITE : Colour.BLACK);
    }

    private static Optional<Colour> byDueColours(Player a, Player b) {
        var dueA = a.dueColour();
        var dueB = b.dueColour();
        if (dueA.isPresent() && dueB.isPresent()) {
            return dueA.get() != dueB.get() ? dueA : Optional.empty();
        }
        return dueA.or(() -> dueB.map(Colour::opposite));
    }

    private static Optional<Colour> byHistory(Player a, Player b) {
        var coloursA = a.playedColours();
        var coloursB = b.playedColours();
        for (var back = 1; back <= Math.min(coloursA.size(), coloursB.size()); back++) {
            var colourA = coloursA.get(coloursA.size() - back);
            if (colourA != coloursB.get(coloursB.size() - back)) {
                return Optional.of(colourA.opposite());
            }
        }
        return Optional.empty();
    }

    /** Players with no games take the lot colour of 7.2. */
    private Colour byRank(Player a, Player b, boolean medianOrAbove) {
        var higher = a.tpn() < b.tpn() ? a : b;
        var decides = medianOrAbove ? higher : (higher == a ? b : a);
        var colour = decides.dueColour().orElse(round.initialColour().colour());
        return decides == a ? colour : colour.opposite();
    }
}
