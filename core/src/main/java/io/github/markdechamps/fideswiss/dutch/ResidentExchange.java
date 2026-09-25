package io.github.markdechamps.fideswiss.dutch;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Article 4.3: a swap of equally sized groups of BSNs between the original S1 and the original S2. */
record ResidentExchange(List<Integer> movedFromS1, List<Integer> movedFromS2) {

    static final ResidentExchange NONE = new ResidentExchange(List.of(), List.of());

    /** Article 4.3.2, the four comparison rules in their given order. */
    static final Comparator<ResidentExchange> PRIORITY = Comparator.comparingInt(ResidentExchange::size)
            .thenComparingInt(ResidentExchange::sumDifference)
            .thenComparing(ResidentExchange::largestMovedFromS1First)
            .thenComparing(ResidentExchange::smallestMovedFromS2First);

    /** Every exchange between the original subgroups, in priority order. Each size is sorted on its own. */
    static Stream<ResidentExchange> inOrder(int s1Size, int s2Size) {
        var s1Bsns = bsns(1, s1Size);
        var s2Bsns = bsns(s1Size + 1, s1Size + s2Size);
        return Stream.iterate(1, size -> size <= Math.min(s1Size, s2Size), size -> size + 1)
                .flatMap(size -> Combinations.of(s1Bsns, size)
                        .flatMap(fromS1 ->
                                Combinations.of(s2Bsns, size).map(fromS2 -> new ResidentExchange(fromS1, fromS2)))
                        .sorted(PRIORITY));
    }

    Subgroups applyTo(List<Player> bracketInBsnOrder, int s1Size) {
        var s1 = bracketInBsnOrder.stream()
                .filter(player -> isInNewS1(bracketInBsnOrder.indexOf(player) + 1, s1Size))
                .toList();
        var s2 = bracketInBsnOrder.stream()
                .filter(player -> !s1.contains(player))
                .toList();
        return new Subgroups(s1, s2);
    }

    private boolean isInNewS1(int bsn, int s1Size) {
        return bsn <= s1Size ? !movedFromS1.contains(bsn) : movedFromS2.contains(bsn);
    }

    private int size() {
        return movedFromS1.size();
    }

    private int sumDifference() {
        return sum(movedFromS2) - sum(movedFromS1);
    }

    private static int sum(List<Integer> bsns) {
        return bsns.stream().mapToInt(Integer::intValue).sum();
    }

    /** Rule 3: the largest differing BSN moved from S1 wins, so compare descending lists, larger first. */
    private static int largestMovedFromS1First(ResidentExchange a, ResidentExchange b) {
        var left = a.movedFromS1.reversed();
        var right = b.movedFromS1.reversed();
        for (var index = 0; index < left.size(); index++) {
            if (!left.get(index).equals(right.get(index))) {
                return Integer.compare(right.get(index), left.get(index));
            }
        }
        return 0;
    }

    /** Rule 4: the smallest differing BSN moved from S2 wins. */
    private static int smallestMovedFromS2First(ResidentExchange a, ResidentExchange b) {
        for (var index = 0; index < a.movedFromS2.size(); index++) {
            if (!a.movedFromS2.get(index).equals(b.movedFromS2.get(index))) {
                return Integer.compare(a.movedFromS2.get(index), b.movedFromS2.get(index));
            }
        }
        return 0;
    }

    private static List<Integer> bsns(int first, int last) {
        return Stream.iterate(first, bsn -> bsn <= last, bsn -> bsn + 1).toList();
    }

    @Override
    public String toString() {
        return movedFromS1 + "<->" + movedFromS2;
    }
}
