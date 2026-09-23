---
title: Dubov, Burstein, Lim, Double-Swiss and acceleration
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

What are the current FIDE texts for the Dubov, Burstein and Lim systems, the Double-Swiss system, and the accelerated-pairing methods (e.g. Baku acceleration)? For each: edition in force, how its procedure differs from Dutch, which programs are endorsed or implement it, and the input data it needs beyond Dutch.

## Context

Findings: branch `research/other-swiss-systems`, file `docs/research/other-swiss-systems.md`.

## Resolution

Resolved by research (branch `research/other-swiss-systems`, `docs/research/other-swiss-systems.md`).

- In force, all reissued 2026-02-01: Dubov C.04.4.1, Burstein C.04.4.2, Lim C.04.4.3, Double-Swiss C.04.5 (new), accelerated systems C.04.7 (Baku).
- None has an endorsed program; the only Dubov endorsement ever recorded (Vega, 2006) has been dropped. Witnesses (not oracles): Vega (Dubov/Burstein/Lim/Baku, edition unclear); bbpPairings' old, self-described "flawed" Burstein; the Go library `gnutterts/chesspairing` (claims all six systems, but its Baku is out of date), the closest prior art. Gacrux covers only Dutch, Team and round-robin.
- 2026 changes: Dubov and Lim mostly editorial (Dubov widened bye ineligibility); Burstein rewrote its floater criteria C6/C7; Baku now uses "points for a win" and covers team match points.
- Differences from Dutch: Dubov (players with a low average opponent rating get high-rated opponents, upfloaters only, bye first); Burstein (Dutch seeding rounds, then an index built from Buchholz and Sonneborn-Berger, ignores floats); Lim (step-by-step exchanges, median scoregroup paired last); Double-Swiss (two-game matches, no colour criterion in pairing).
- Extra input per system is listed in the note: ratings, upfloat history, full cross-table, per-game results within a round, virtual-point history.
- TRF26 gaps: no Lim code in the tournament-type table, and it can't express a two-game Double-Swiss match.
- Implications: identical-output conformance is only possible for Dutch. The others are verified by tracing rules to handbook articles, the texts' worked examples (Burstein's list minus its duplicate line), and comparison with witnesses. Acceleration fits as a pluggable per-round virtual-points layer. Lim is riskiest, so do it last.
- Open questions: TEC test suites for these systems (→ Contact FIDE Technical Commission), which editions Vega/bbp follow, the undefined "Maxi-tournament" in Lim, the Double-Swiss + Baku virtual-point value and TRF encoding, floats under acceleration, the wrong `250` example, and the Olympiad Pairing Rules (D.02) (→ Olympiad Pairing Rules scope).
