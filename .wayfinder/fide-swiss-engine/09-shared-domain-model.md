---
title: Shared domain model across all systems
labels: [wayfinder:grilling]
status: open
assignee: mark
blocked_by: [01-basic-handling-rules, 02-dutch-system, 03-swiss-team-system, 04-other-swiss-systems, 06-trf-format, 07-tie-breaks, 12-historic-rule-editions, 16-olympiad-rules-scope]
---

## Question

What is the shared domain model all pairing systems and tie-breaks build on (tournament state, participant as player vs team, round, game vs match, result, colour history, float history, bye, score and its variants, pairing number), and where do the systems genuinely diverge so that a clean OO model keeps them separate?

## Context

- From Historic rule editions: model the Swiss Rules Edition and Tie-break Edition as first-class settings, and build each edition by composing per-article rule objects (no inheritance between editions).
