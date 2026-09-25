package io.github.markdechamps.fideswiss.dutch;

/** A pair with its colours allocated (Article 5), and the rule that decided them. */
record AllocatedPair(Player white, Player black, String article) {}
