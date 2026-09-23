---
title: Readable Burstein pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee:
blocked_by: []
---

## Question

Can the Burstein System (C.04.4.2, 2026) be written literally and readably, following the pattern of Readable Dutch pairing algorithm and Readable Swiss Team pairing algorithm: a literal generator in handbook order, per-article criteria written as digits of one matching (the shared `cheapestPerfect(teams, cost)` mechanism and `Failure` value, but a criterion interface of its own), exact reachability pruning, known optimal candidates? Prototype the seeding rounds, the index order (Buchholz and Sonneborn-Berger with Burstein's own unplayed-round rules), the "fold" order of candidates, and the rewritten 2026 floater criteria C6/C7. Compare with the text's worked ordering (4.3, minus its duplicate line) and the Witnesses (Vega, chesspairing; bbp's old Burstein only where the 2026 edits do not reach).

## Context

Graduated from the "Algorithm design for each remaining system" fog. Evidence: `docs/research/other-swiss-systems.md` §4 (Burstein changed substantively in 2026) and Implication 6 (its Buchholz/SB is reusable from the C.07 module, but not identical to it). Open point: whether the index is part of the Participant history (Shared domain model) or computed by the system.

From Readable Dubov pairing algorithm: chesspairing's CLI (`pair`, legacy `-p`) drops the last recorded round of every TRF, because its reader sets `CurrentRound` to the rounds played. Its random generator (`generate`) therefore writes tournaments full of rematches, and the Witness must be run through a driver like `prototypes/dubov-algorithm/witness/main.go` (branch `prototype/dubov-algorithm`). It also renumbers TPNs by score every round, and it lets a forfeit win leave the PAB open ([C2] 2026), so expect differences in order and in byes. A Literal Enumerator (plain enumeration in the text's orders) checks the search where no Oracle exists.
