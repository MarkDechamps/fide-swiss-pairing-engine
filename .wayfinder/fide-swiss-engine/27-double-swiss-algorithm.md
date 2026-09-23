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
