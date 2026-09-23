---
title: Swiss Team System 2026
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

What exactly does C.04.6 Swiss Team Pairing System (effective 1 Feb 2026) specify: definitions, absolute/completion/quality criteria, the pairing procedure, colour allocation, byes, primary/secondary score options, what it inherits 'mutatis mutandis' from other systems, and how it differs from the previous team system? Does any implementation (endorsed or not) exist, and what TRF data does it need?

## Context

Findings: branch `research/swiss-team-system`, file `docs/research/swiss-team-system.md`.

## Resolution

Resolved by research (branch `research/swiss-team-system`, commits 5069782 and a28bd9f, `docs/research/swiss-team-system.md`).

- Procedure: the PAB (pairing-allocated bye) is chosen first, then scoregroups are processed top-down. For each bracket: choose the upfloater set (C4 fewest, C5 highest scores, C3 the rest can still pair, then C6 and C7), then take the first pairing in identifier order that best satisfies C1, C8, C9, C10. Colour (board 1) is decided afterwards by a 9-step cascade and never blocks a pairing. There are no downfloaters.
- Options: colour preference Type A, Type B or none. Primary score is match points (default) or game points; the secondary score is used only to pick the "first-team" for colour. A bye gives draw-level MP/GP.
- Inherits Basic Rules except arts. 6–7 (no ±2 colour limit, no three-in-a-row), and General Handling Rules 1, 2.4, 2.5, 3 and 4. Initial order is up to each competition. Acceleration (C.04.7) applies, except when game points are the primary score.
- Differences from 2024: renumbered, C2 reworded, the floater criterion split into C7 and C10, a new colour rule 4.3.9. The edition must be inferred from the tournament date.
- No endorsed program exists. **Gacrux** (MIT, July 2026) has a C.04.6 module: it is the only candidate oracle and it documents its readings of the ambiguous articles. It hasn't been run yet.
- TRF team records needed: 310 (013 during the transition), 001, 300, 352, 362, 162, 320, 330, 240, 142, 152, 250, 260, 299, 192. Team colour is derived from board 1.
- Open questions: C6 as pass/fail vs graded (the biggest risk to identical output); the meaning of "complies with" for minimise criteria; whether forfeits count as played; the Type B last-round case; how the colour-history comparison works; floaters under acceleration; a possible Gacrux bracket-order bug. These led to the tickets "Probe Gacrux on Swiss Team edge cases" and "Swiss Team interpretation rulings".
