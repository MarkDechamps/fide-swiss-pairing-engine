package io.github.markdechamps.fideswiss.olympiad;

/** Two teams paired against each other, colours not yet given. */
record Pair(Team first, Team second) {

    Team higherRanked() {
        return first.isRankedAbove(second) ? first : second;
    }

    Team lowerRanked() {
        return higherRanked() == first ? second : first;
    }
}
