package io.github.markdechamps.fideswiss.olympiad;

/** A match with board 1's colours given, and the article of Article 7 that gave them. */
record Game(Team white, Team black, String article) {

    Team higherRanked() {
        return white.isRankedAbove(black) ? white : black;
    }

    Team lowerRanked() {
        return higherRanked() == white ? black : white;
    }
}
