package io.github.markdechamps.fideswiss.dutch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** k-subsets of a list, in lexicographic order of position. */
final class Combinations {

    private Combinations() {}

    static <T> Stream<List<T>> of(List<T> items, int size) {
        return from(items, 0, size, List.of());
    }

    private static <T> Stream<List<T>> from(List<T> items, int start, int size, List<T> chosen) {
        if (chosen.size() == size) {
            return Stream.of(chosen);
        }
        var stillNeeded = size - chosen.size();
        return IntStream.rangeClosed(start, items.size() - stillNeeded)
                .boxed()
                .flatMap(index -> from(items, index + 1, size, appended(chosen, items.get(index))));
    }

    static <T> List<T> appended(List<T> list, T item) {
        var copy = new ArrayList<>(list);
        copy.add(item);
        return List.copyOf(copy);
    }
}
