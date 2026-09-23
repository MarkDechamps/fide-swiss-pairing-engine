---
title: Readable Lim pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee:
blocked_by: [25-dubov-algorithm, 26-burstein-algorithm, 27-double-swiss-algorithm]
---

## Question

Can the Lim System (C.04.4.3, 2026) be written literally and readably? Its procedure is a step-by-step exchange with no global criteria order, so decide first whether the pattern (a target from one matching, exact reachability) applies at all, or whether Lim is written as its own procedural object. Prototype the median-first group order, the up- and downfloaters, the exchange tables of 4.2–4.3 beyond six players, colour limits as compatibility (relaxed in the last round) and the "Maxi-tournament" rating rule, and record each ambiguity as a fixed reading or an Interpretation.

## Context

Graduated from the "Algorithm design for each remaining system" fog; last on purpose ("Lim riskiest, do it last", Dubov, Burstein, Lim, Double-Swiss and acceleration). Evidence: `docs/research/other-swiss-systems.md` §5 (numerous ambiguities; no `192` code in TRF26), Witnesses Vega and chesspairing (Maxi as a boolean).

From Readable Dubov pairing algorithm: chesspairing's CLI (`pair`, legacy `-p`) drops the last recorded round of every TRF, because its reader sets `CurrentRound` to the rounds played. Its random generator (`generate`) therefore writes tournaments full of rematches, and the Witness must be run through a driver like `prototypes/dubov-algorithm/witness/main.go` (branch `prototype/dubov-algorithm`). It also renumbers TPNs by score every round, and it lets a forfeit win leave the PAB open ([C2] 2026), so expect differences in order and in byes. A Literal Enumerator (plain enumeration in the text's orders) checks the search where no Oracle exists.
