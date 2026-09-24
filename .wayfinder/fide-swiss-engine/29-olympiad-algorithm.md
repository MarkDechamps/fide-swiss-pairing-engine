---
title: Readable Olympiad pairing algorithm
labels: [wayfinder:prototype]
status: open
assignee:
blocked_by: [28-lim-algorithm]
---

## Question

Can the Olympiad Pairing Rules (D.02, 2022) be written literally and readably on the parts built for Swiss Team (team history, match points, the 1 MP + 2 GP bye) and Lim (the median-group order of 6.4: top down, then bottom up, the median group last)? Prototype it and record every reading.

## Context

In scope per Olympiad Pairing Rules scope; the `olympiad()` profile is fixed in Tournament profiles. Evidence: `docs/research/other-swiss-systems.md` §9 (a Lim-like median-group order, its own ranking by the average of the top four ratings). No Oracle, and no Witness is known yet.

From Readable Double-Swiss pairing algorithm: the Swiss Team parts are now one Top-Scoregroup Procedure shared with Double-Swiss (3.4 PAB, 3.5 upfloater set, 3.6 Pairing Identifier), with per-system criteria lists, float-lapse rounds, PAB value and colours. Check which of it D.02 can reuse, and whether its median-group order fits that procedure or needs its own.

From Readable Lim pairing algorithm: Lim is a procedure, not an optimisation. A matching only answers reachability there ("can the rest still pair", "how many pairings can this scoregroup keep"). The parts D.02 6.4 may reuse:
- the median-group order (`LimSystem.higherScores`/`lowerScores`, the median last and paired downward, always present even without residents);
- the `Direction` mirror (every upward rule is the downward rule on the reversed pairing numbers);
- the 2.6 cracking of the last pairing made on a side.

See `prototypes/lim-algorithm/` on branch `prototype/lim-algorithm`. The chesspairing Witness driver there shows how to run its pairers on our state.
