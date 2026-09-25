package io.github.markdechamps.fideswiss.dutch;

/** The candidate a bracket chose, and how it fails the criteria (only the failing ones). */
record BracketOutcome(Candidate candidate, String failedCriteria) {}
