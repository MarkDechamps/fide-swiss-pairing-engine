---
title: Readable Double-Swiss pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee:
blocked_by: []
---

## Question

Can the Double-Swiss System (C.04.5, 2026) be written literally and readably, following the pattern of Readable Dutch pairing algorithm and Readable Swiss Team pairing algorithm: a literal generator in handbook order, per-article criteria written as digits of one matching (the shared `cheapestPerfect(teams, cost)` mechanism and `Failure` value, but a criterion interface of its own), exact reachability pruning, known optimal candidates? Prototype its bracket procedure with its lexicographic identifiers (3.5.4, 3.6.2), the PAB of 1.5, and the two-game match with per-game results. Check whether its identifier order is the same "first in identifier order" search as Swiss Team 3.6, so that the two could share the generator. Compare with chesspairing, the only Witness.

## Context

Graduated from the "Algorithm design for each remaining system" fog. Evidence: `docs/research/other-swiss-systems.md` §6 (a new system in 2026, no colour criterion in the pairing) and Implication 6 (sub-round games). Swiss Team's 3.6 is structurally close (Readable Swiss Team pairing algorithm, decision 3). The Double-Swiss virtual-point value under acceleration stays in the acceleration fog.

From Readable Dubov pairing algorithm: chesspairing's CLI (`pair`, legacy `-p`) drops the last recorded round of every TRF, because its reader sets `CurrentRound` to the rounds played. Its random generator (`generate`) therefore writes tournaments full of rematches, and the Witness must be run through a driver like `prototypes/dubov-algorithm/witness/main.go` (branch `prototype/dubov-algorithm`). It also renumbers TPNs by score every round, and it lets a forfeit win leave the PAB open ([C2] 2026), so expect differences in order and in byes. A Literal Enumerator (plain enumeration in the text's orders) checks the search where no Oracle exists.

From Readable Burstein pairing algorithm: Burstein's 4.3 order compares the pairings position by position (BSN #1's opponent, then BSN #2's), which adds up over pairs. It became one digit per BSN below the criteria, so the whole bracket is one matching and the order is never walked. Check whether the Double-Swiss identifier order (3.5.4, 3.6.2) adds up the same way. bbpPairings v6 `--dutch` pairs a realistic Dutch history quickly through a TRF16 file, if Double-Swiss wants one (see `prototypes/burstein-algorithm/`, class `ExternalPairer`, on branch `prototype/burstein-algorithm`).
