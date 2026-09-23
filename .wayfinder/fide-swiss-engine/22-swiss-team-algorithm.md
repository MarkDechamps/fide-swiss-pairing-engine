---
title: Readable Swiss Team pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee: markdechamps
blocked_by: [14-swiss-team-rulings]
---

## Question

Can the C.04.6 (2026) procedure be written literally and readably, following the Dutch pattern (a literal generator in handbook order, a criteria vector of per-article failures, a matching oracle for legality, an optimum finder for the target), and still match patched Gacrux (`tpn-order`) exactly with default Interpretations? Prototype the top-down scoregroup procedure: bye first (3.4), upfloater sets (3.5, [C3]–[C7]), first-best bracket pairing in identifier order (3.6, [C8]–[C10]), then colour allocation (4.3). Settle whether `CandidateCriterion`/`Failure` become shared with Dutch.

## Context

From Swiss Team interpretation rulings (fixed readings and three Interpretations: `UpfloaterLookAhead`, `LastRoundZeroCdTypeB`, `FloatScore`) and Readable Dutch pairing algorithm (the pattern). Probe cases in `docs/research/gacrux-team-probe/` are the first fixtures. From Exact optimum finder for Dutch brackets: the Dutch target and pruning come from one round-wide weighted matching, with each criterion declaring how it adds up (`CandidateCriterion.Scope`) and known optimal candidates to skip confirming checks (branch `prototype/dutch-optimum-finder`). Check whether Swiss Team's first-best-in-identifier-order (3.6) fits the same reachability test. Open points: the combinatorial cost of set enumeration (A11) and whether the graded [C6] needs its own look-ahead matching.
