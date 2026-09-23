---
title: Readable Dutch pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee:
blocked_by: [02-dutch-system, 09-shared-domain-model]
---

## Question

Can the Dutch System be modelled literally and readably (brackets, transpositions, exchanges, criteria evaluated in handbook order) while still matching the reference programs? Prototype the shape of that model on the handbook's worked examples to react to.

## Context

From Dutch System research: bbpPairings reproduces the Handbook's order of trying candidates via weighted matching plus fixing decisions one at a time; the research suggests implementing the literal procedure with pruning, keeping matching as an optional cross-check.

- From Historic rule editions: the prototype must support Dutch 2026 and Dutch 2017 (with pre-2026 Basic Rules) as two compositions of per-article rule objects over one shared bracket procedure.
