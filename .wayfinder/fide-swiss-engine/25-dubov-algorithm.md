---
title: Readable Dubov pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee:
blocked_by: []
---

## Question

Can the Dubov System (C.04.4.1, 2026) be written literally and readably, following the pattern of Readable Dutch pairing algorithm and Readable Swiss Team pairing algorithm: a literal generator in handbook order, per-article criteria written as digits of one matching (the shared `cheapestPerfect(teams, cost)` mechanism and `Failure` value, but a criterion interface of its own), exact reachability pruning, known optimal candidates? Prototype the bye, the colour split into G1/G2, the G2 order and permutations (ARO, rating), the upfloater sets and the criteria. Show that every criterion either adds up over pairs or is settled with its own matching, and compare the result with the Witnesses (Vega, `gnutterts/chesspairing`) and the worked examples of the text.

## Context

Graduated from the "Algorithm design for each remaining system" fog once Readable Swiss Team pairing algorithm settled the shared parts. Evidence: `docs/research/other-swiss-systems.md` §3 (2026 edits: a wider C2, a reworded upfloater-set order) and Implications 1 and 4 (Dubov's G2 permutation is factorial when enumerated, so the reachability pruning is what keeps it bounded). There is no Oracle, so Verification strategy's per-system definition of done applies: trace every rule to its article, take the text's examples as fixtures, review Witness differences.
