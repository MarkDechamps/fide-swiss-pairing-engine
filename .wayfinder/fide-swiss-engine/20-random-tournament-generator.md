---
title: Random tournament generator
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: [10-verification-strategy]
---

## Question

What does our random tournament generator produce, and how is it configured? It feeds the invariant checker, the regression corpus and the 50k release gate, and FIDE's technical acceptance requires it as a free TRF26 generator. Open points: the distributions of results, draws, forfeits, requested byes, withdrawals and late entries; rating spread; field size and number of rounds; which systems and editions it covers (individual, team, Double-Swiss matches); seeding and determinism; staying compatible with the inputs `bbp -g` and Gacrux's generator produce; and its CLI flags.

## Context

From Verification strategy: the generator must only produce valid input, so every checker violation is a library bug. The tournaments have to be reproducible from a logged seed, and bbp's and Gacrux's generators are run alongside ours so that neither side only sees its own style of input.
