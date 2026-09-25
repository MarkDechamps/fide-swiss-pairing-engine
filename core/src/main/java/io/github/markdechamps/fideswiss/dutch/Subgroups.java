package io.github.markdechamps.fideswiss.dutch;

import java.util.List;

/** S1 and S2 of a homogeneous bracket or remainder, each in BSN order (3.2). */
record Subgroups(List<Player> s1, List<Player> s2) {}
