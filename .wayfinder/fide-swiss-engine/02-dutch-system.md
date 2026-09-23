---
title: Dutch System and its reference programs
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

What does the current C.04.3 Dutch System say (edition in force, recent changes, full criteria list C1–C21, bracket/transposition/exchange procedure, colour rules)? Which programs are FIDE-endorsed for it (JaVaFo, bbpPairings, others), under what licences, how are they invoked (CLI, TRF conventions), and how do they implement the rules (e.g. weighted matching vs literal search)?

## Context

Findings: branch `research/dutch-system`, file `docs/research/dutch-system.md`.

## Resolution

Resolved by research (branch `research/dutch-system`, commit d1facec, `docs/research/dutch-system.md`).

- In force: C.04.3 effective 1 Feb 2026 (approved 28/10/2025), criteria C1–C21. The changes that alter pairings: the completion check C4 now applies in every bracket (the old collapsed-last-bracket / PSD mechanism is gone); new bye criteria (lowest possible score C5, fewest unplayed games C9); revised float definitions and rules for pairing moved-down players. The full criteria, procedure and colour rules are in the note with article numbers.
- FIDE endorses programs, not engines. JaVaFo powers 7 programs, bbpPairings powers SwissSys, and Swiss-Chess has its own engine. Every endorsement predates the 2026 rules.
- **JaVaFo** 2.2 (2017) implements the 2017 rules. It is closed, obfuscated and free of charge, with no redistribution grant. Its CLI, the XX? lines and its Java API are documented in the note.
- **bbpPairings** v6.0.0 (2026-02-01) implements the 2026 rules and TRF-2026, under Apache-2.0. It works by weighted maximum matching (criteria as bit bands in the edge weight), fixing decisions one at a time to reproduce the Handbook's order of trying candidates.
- Oracle experiment (fixed seeds): JaVaFo 2.2 and bbp v5.0.1 agree on 461/461 rounds under 2017 rules. bbp v6 differs from them on 90/461 rounds (JaVaFo-generated) and 9/200 rounds (bbp-generated). So 2017 has two agreeing oracles; 2026 has one.
- Traps: bbp silently ignores XXZ/XXS (an absent player gets paired); bbp writes CR-only line endings; the initial colour must always be stated explicitly.
- Implications: target 2026 with a pinned bbp v6 as the primary oracle, treating disagreements as items to investigate rather than proof we're wrong. JaVaFo is a legacy 2017 oracle only, downloaded at test time and never committed. Implement the Handbook's procedure literally, with pruning; matching is an optional cross-check.
- Open questions: JaVaFo for 2026; which endorsements carry over; the 2026 Terms and Definitions page returns 404 (scope of C9, a downfloater's score difference in C18/C20); a TEC test corpus; JaVaFo's licence for CI use and publishing its outputs; a 2017-rules mode.
