---
title: Exact optimum finder for Dutch brackets
labels: [wayfinder:prototype]
status: open
assignee: mark
blocked_by: [11-dutch-algorithm]
---

## Question

How does the optimum finder compute, for one Dutch bracket, the exact best criteria vector ([C5]–[C21]) that any candidate can reach, and how does the literal candidate search use that vector to prune so that it reaches the first optimal candidate in bounded time? Prototype it on the existing literal prototype and show that it removes every candidate-cap hit in the 30–62-player corpora while staying identical to bbpPairings v6.

## Context

From Readable Dutch pairing algorithm: the literal procedure is decided as the code that picks the pairing, and the matching side only supplies the target. The prototype (branch `prototype/dutch-algorithm`) matched bbp v6 on every round it finished; all 16 misses were 2,000,000-candidate cap hits in brackets of 12–18 players. The two cost drivers are (1) not recognising the optimum when a criterion can't reach zero, and (2) an optimum sitting deep in the sequence (for example behind a resident exchange). Open points:

- Computing the target: weighted matching with the criteria encoded lexicographically (bbp's edge-weight layout in `dutch.cpp` is the reference, Apache-2.0), or a criterion-by-criterion sieve ([ANN p.40]). The target must include the MDP-Pairing/remainder structure of heterogeneous brackets and [C8]'s look-ahead.
- Pruning the literal search: which partial candidates (an S1 prefix of a transposition, an exchange, an MDP set) can be proved unable to reach the target, and how, without changing which candidate is found first.
- Whether the matching code lives in `core` (zero dependencies, so written ourselves) and how it stays readable, given the map's readability-over-performance rule.
- A performance budget for a round (the 50k-tournament release gate from Verification strategy sets the scale).
