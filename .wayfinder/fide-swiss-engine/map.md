---
title: FIDE Swiss reference library
labels: [wayfinder:map]
status: open
---

## Destination

An implementation-ready spec for an open-source Java **reference library** implementing every FIDE Swiss pairing system (Dutch, Swiss Team 2026, Dubov, Burstein, Lim, Double-Swiss, plus acceleration), the Basic and General Handling Rules (C.04.1/C.04.2) and tie-breaks (C.07). It is a library core plus a TRF CLI, endorsement-grade (pairings identical to the endorsed reference programs), and the spec includes its verification strategy. It must be ready to hand straight to a build effort.

## Notes

- Domain: FIDE Handbook C.04 (Swiss systems), C.04.A (endorsement), C.07 (tie-breaks). Source that started it: https://handbook.fide.com/chapter/SwissTeamPairingSystem202602
- Glossary: `/home/mark/swiss/CONTEXT.md`. Keep it current with the `domain-modeling` skill.
- Language: **Java**, as clean, simple and object-oriented as possible.
- **Readability over performance**: the code mirrors the handbook articles, and performance issues are fixed when they surface.
- TDD, clean architecture and clean code are paramount. Skills every session should consult: `tdd`, `clean-java`, `clean-code`, `codebase-design`, `domain-modeling`.
- Open source, for the author's own use and for distribution; the aim is to become *the* reference library.
- Delivery shape: library core + TRF CLI wrapper.
- Research findings live on `research/<name>` branches as `docs/research/<name>.md`; each research ticket points at its branch.

## Decisions so far

<!-- one line per closed ticket -->

- [Basic and General Handling Rules](01-basic-handling-rules.md): 2026 editions of C.04.1/C.04.2 in force; points and PAB value are configuration; unplayed rounds leave the colour history; floats are defined per system; round-record model with per-system hooks.

## Not yet specified

- Algorithm design for each remaining system (Swiss Team, Dubov, Burstein, Lim, Double-Swiss), probably one prototype per system once the Dutch prototype sets the pattern.
- How acceleration methods plug into the pairing systems.
- Tie-break module design and how standings are modelled.
- Public library API (how a client hands over tournament state and receives pairings/standings).
- TRF CLI surface, and TRF extensions for systems/data the TRF16 format can't express.
- Publishing: Maven Central coordinates, release/versioning, and the FIDE endorsement submission itself.
- An eventual performance budget, once real workloads show problems.

## Out of scope

- Rating calculation, tournament persistence and UI, result entry, running rounds, team lineup / board-order validation, non-Swiss formats (round-robin, knockout). Ruled out while charting.
