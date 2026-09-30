---
title: Library API fit for the arbiter workflow
labels: [wayfinder:research]
status: closed
assignee: claude
blocked_by: [01]
---

## Question

Does the library's public API cover everything the arbiter workflow needs (result corrections, manual pairing changes, withdrawals and late entries, byes on request, standings explanations, progress and cancellation, TRF round trips)? Read the library's public API and docs/manual.md against the workflow ticket; list each gap as a proposed library change.

## Resolution

Read the exported API against the workflow of ticket 01. Pairing, `check`, the three GHR 4.3 corrections, withdrawal, requested byes, late entries, standings with explanations, progress and cancel are covered. The gaps are about rebuilding, guarding and adjusting, not pairing or scoring: (1) replaying stored facts needs a chronological order the library only reveals by exception (withdraw, requestBye and enterLate refuse recorded rounds; a rating correction differs before and after round 4), so propose a `TournamentChange` log with `apply`/`replay` and removal of a withdrawal or requested bye; (2) a manual `ProposedPairing` cannot be edited (swap opponents, colours, bye) or turned into a `Round` without the app knowing the `byes` map convention and GHR 3.6 board order; (3) `check` runs a full pairing, has no progress or cancel, and returns `differences` as strings; (4) no prefix snapshot (`asBefore`) to warn about later rounds after a correction; (5) no guarded `withNumberOfRounds`, `withTieBreaks` or `withSettings`, and no freeze of system, edition, scoring and acceleration; (6) tie-break validity against scoring and edition is found only when standings run, and there is no catalogue for a picker; (7) TRF carries only name, rating and title and a minimal header; (8) no result notation for keyboard entry; (9) TRF export uses registration order but the reader ranks as listed, and a late entry is not recognised on import. No mutability leaks (all returned collections are immutable copies), but raw `List`/`Map` returns invite first-class collections. All changes are additive, hence minor under ADR 0008.

Details, file:line references and signature sketches: `docs/research/library-api-fit.md` on branch `research/library-api-fit`.
