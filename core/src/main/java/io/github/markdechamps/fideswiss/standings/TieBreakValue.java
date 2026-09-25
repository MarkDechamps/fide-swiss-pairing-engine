package io.github.markdechamps.fideswiss.standings;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A participant's value for one tie-break, with the contributions it was read off (C.07 art. 5–10, 14, 16). A
 * value that is not a per-round sum carries the inputs of its formula as contributions instead. It is absent when
 * the tie-break does not apply, such as a rating tie-break with unrated participants present (art. 10).
 */
public record TieBreakValue(
        TieBreakCode code,
        Optional<BigDecimal> value,
        boolean higherIsBetter,
        List<TieBreakContribution> contributions,
        String explanation) {

    public TieBreakValue {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(value, "value");
        contributions = List.copyOf(contributions);
        Objects.requireNonNull(explanation, "explanation");
    }

    /** Positive when this value ranks above the other. */
    public int compareTo(TieBreakValue other) {
        if (value.isEmpty() || other.value.isEmpty()) {
            return 0;
        }
        var difference = value.get().compareTo(other.value.get());
        return higherIsBetter ? difference : -difference;
    }

    @Override
    public String toString() {
        return value.map(BigDecimal::toPlainString).orElse("-");
    }
}
