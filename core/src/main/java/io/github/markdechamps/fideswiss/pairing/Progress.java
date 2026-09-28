package io.github.markdechamps.fideswiss.pairing;

/** Participants paired or given the PAB in a final step, out of the participants to be paired this round. */
public record Progress(int settled, int toPair) {}
