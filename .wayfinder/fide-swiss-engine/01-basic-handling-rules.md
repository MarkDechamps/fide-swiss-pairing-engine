---
title: Basic and General Handling Rules
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

What do the current FIDE C.04.1 (Basic Rules for Swiss Systems) and C.04.2 (General Handling Rules) require of a pairing library? Covers initial ordering and pairing numbers, late entries, withdrawals, forfeits and how they count for colour and float history, pairing-allocated and requested byes and their scoring, and anything else every system relies on. Which editions are in force as of 2026?

## Context

Findings: branch `research/basic-handling-rules`, file `docs/research/basic-handling-rules.md`.

## Resolution

Resolved by research (branch `research/basic-handling-rules`, commit 45412f7, `docs/research/basic-handling-rules.md`).

- In force: C.04.1 and C.04.2 in the 2026 editions (approved 28/10/2025, applied from 1 Feb 2026). The whole C.04 family switched on that date, and nothing is pending on the Sep 2026 FIDE Technical Commission agenda.
- The library must take win/draw/loss points and the pairing-allocated bye (PAB) value as configuration; the PAB value is the same for every PAB in an event.
- Bye eligibility: a PAB is blocked by an earlier PAB or by any unplayed round worth a win (forfeit win, full-point bye). A half-point bye does not block it.
- Pairing numbers: initial order is strength, then title, then name. Corrections can renumber until round 4 is paired. Late entries score 0 for missed rounds by default and get a number when they arrive.
- Withdrawn players are not paired. Unplayed rounds are dropped from the colour history, and a forfeit does not count as having met.
- Floats are defined per system, not in C.04.1/C.04.2. Under Dutch 2026, a PAB or any unplayed round worth more than a loss counts as a downfloat.
- Model shape: a round record per participant (unplayed-round kinds taken from TRF-2026 / C.07 art. 16); derived views (colour history, opponents met, PAB eligibility); per-system hooks for floats, colour preference, colour exceptions and PAB selection. Everything deterministic.
- Open questions: 11 ambiguities flagged in the note (Dubov/Burstein bye wording, late-entry renumbering after round 4, "no valid game" definition, and others) feed the domain-model and per-system tickets. Support for pre-2026 editions became the ticket "Historic rule editions".
