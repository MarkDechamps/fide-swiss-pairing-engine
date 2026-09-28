package io.github.markdechamps.fideswiss.dutch;

/** Whether a bracket's candidates must keep the round completable, which decides how far its matching looks. */
enum CompletionScope {

    /**
     * 2026 [C4] in every bracket; 2017 C.4 in the Penultimate Pairing Bracket, and the last bracket (or the
     * Collapsed Last Bracket) that completes the round. The matching sees the bracket, everyone below it and a PAB
     * seat, and must be perfect.
     */
    WHOLE_ROUND,

    /**
     * 2017 brackets other than those: no completion requirement. The matching sees the bracket and the following
     * scoregroup's residents only (C.7 looks "just in the following bracket"), and anyone may stay unpaired there:
     * each player has a private sink, and the sinks not taken pair among themselves.
     */
    FOLLOWING_BRACKET_ONLY
}
