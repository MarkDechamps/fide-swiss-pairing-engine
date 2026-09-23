---
title: Verification strategy
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: [02-dutch-system, 05-fide-endorsement, 06-trf-format]
---

## Question

How do we prove the library is endorsement-grade: which oracles per system (endorsed programs, the FIDE checker, hand-worked handbook examples), how differential testing is driven (random tournament generation, corpus size), how that fits the TDD workflow, and what counts as done for each system?

## Context

From FIDE endorsement process and tooling: acceptance requires a `-check` CLI and a TRF26 random generator, tested on 50k tournaments in both directions. Only Dutch has accepted oracles (JaVaFo, bbpPairings); Gacrux (MIT) offers a checker, generator and 120k test tournaments. The non-Dutch systems have no oracle at all.
