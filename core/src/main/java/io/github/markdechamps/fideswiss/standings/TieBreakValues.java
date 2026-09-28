package io.github.markdechamps.fideswiss.standings;

import java.util.List;

/** A participant's tie-break values in Tie-break List order. */
public record TieBreakValues(List<TieBreakValue> values) {

    public TieBreakValues {
        values = List.copyOf(values);
    }

    public TieBreakValue of(TieBreakCode code) {
        return values.stream()
                .filter(value -> value.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not in the Tie-break List: " + code));
    }

    public TieBreakValue get(int index) {
        return values.get(index);
    }

    public int size() {
        return values.size();
    }
}
