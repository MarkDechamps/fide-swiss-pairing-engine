# Recorded rounds are facts; rule checking is a separate query

`Tournament.withRound` accepts any structurally valid round (known participants, the next round number, everyone accounted for exactly once) and does not reject rule violations such as a rematch or a second PAB. GHR 4.4 lets an arbiter change a published pairing, and GHR 4.3 corrections apply to the recorded history, so the snapshot must hold what was actually played. Legality is reported by `tournament.check(ProposedPairing)`, which returns the violations (each citing its article) and whether the proposal equals the system's own pairing. That one query serves the TRF `-check`, the invariant checker and manual pairings.

## Considered Options

- Validate every rule in `withRound`: this makes a legally changed pairing impossible to record, and history imported from other programs would fail on their bugs.
- Validation with an override flag: this puts two modes on one operation and still gives no report for the checker.

## Consequences

The library pairs on whatever history it is given. A client that wants to refuse illegal rounds calls `check` before `withRound`.
