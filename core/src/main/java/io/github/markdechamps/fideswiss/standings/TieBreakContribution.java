package io.github.markdechamps.fideswiss.standings;

import io.github.markdechamps.fideswiss.tournament.RoundNumber;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * One round's share in a participant's tie-break value: the counterpart (an opponent's id, or {@code dummy} for
 * the participant's own Unplayed Round, C.07 16.4), the value used, whether a modifier cut it, and the notes (with
 * articles) on how it was adjusted.
 */
public record TieBreakContribution(
        Optional<RoundNumber> round, String counterpart, BigDecimal value, boolean cut, List<String> notes) {

    public TieBreakContribution {
        Objects.requireNonNull(round, "round");
        Objects.requireNonNull(counterpart, "counterpart");
        Objects.requireNonNull(value, "value");
        notes = List.copyOf(notes);
    }

    @Override
    public String toString() {
        var where = round.map(number -> "round " + number + " ").orElse("");
        var how = notes.isEmpty() ? "" : " (" + String.join("; ", notes) + ")";
        return where + counterpart + " " + value.toPlainString() + (cut ? " cut" : "") + how;
    }
}
