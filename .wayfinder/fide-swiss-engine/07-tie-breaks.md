---
title: Tie-break regulations
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

What does the current FIDE tie-break regulation (C.07) specify: edition in force, the full list of tie-breaks and their exact definitions for individual and team events, how unplayed games/byes/forfeits are treated, and are there reference implementations or official worked examples usable as test data?

## Context

Findings: branch `research/tie-breaks`, file `docs/research/tie-breaks.md`.

## Resolution

Resolved by research (branch `research/tie-breaks`, commit d59dc9c, `docs/research/tie-breaks.md`).

- In force: C.07 Play-Off and Tie-Break Regulations, approved 2 Feb 2026, applied from 1 Mar 2026 (the previous edition was 1 Aug 2024 to 28 Feb 2026).
- The regulation lists 26 tie-breaks (art. 5), for individual and team events. New since 2024: STD, TPN, RTNG, REP (renamed from GE), and the EDE* combinations. The Buchholz family is banned in round robins.
- Modifiers: Cut-1/2, Median-1/2, Limit. Use the FIDE Technical Commission's code syntax (`BH/C1`, `BH:MP/M1/P`, ...) for tie-break lists.
- Unplayed rounds (arts. 15–16) fall into five Swiss categories. Opponents see an adjusted score, and a player's own unplayed rounds count against a dummy opponent whose score is capped in 2026. The "virtual opponent" was abolished in 2023.
- Test data: there are no official 2026 examples. Mario Held's 2023 exercise set on tec.fide.com can be re-derived for 2026 (the caps change some results). No open-source reference implementation exists; six closed-source programs are endorsed.
- Implications: rule edition as a parameter; three derived scores per participant (raw, adjusted, capped dummy); embed the FIDE rating conversion tables.
- Open questions: the meaning of `/P`, whether TRF-2026 is approved, 12 textual ambiguities needing ADRs (PS cut with a round-1 bye, FB with final-round byes, TPR rounding, ...), and whether re-derived fixtures need an international arbiter's review.
