---
title: FIDE Swiss reference library
labels: [wayfinder:map]
status: open
---

## Destination

An implementation-ready spec for an open-source Java **reference library** implementing every FIDE Swiss pairing system (Dutch, Swiss Team 2026, Dubov, Burstein, Lim, Double-Swiss, Olympiad Pairing Rules, plus acceleration), the Basic and General Handling Rules (C.04.1/C.04.2) and tie-breaks (C.07). It is a library core plus a TRF CLI, endorsement-grade (pairings identical to the endorsed reference programs), and the spec includes its verification strategy. It must be ready to hand straight to a build effort.

## Notes

- Domain: FIDE Handbook C.04 (Swiss systems), C.04.A (endorsement), C.07 (tie-breaks). Source that started it: https://handbook.fide.com/chapter/SwissTeamPairingSystem202602
- Glossary: `/home/mark/swiss/CONTEXT.md`. Keep it current with the `domain-modeling` skill.
- Language: **Java**, as clean, simple and object-oriented as possible.
- **Readability over performance**: the code mirrors the handbook articles, and performance issues are fixed when they surface.
- TDD, clean architecture and clean code are paramount. Skills every session should consult: `tdd`, `clean-java`, `clean-code`, `codebase-design`, `domain-modeling`.
- Open source, for the author's own use and for distribution; the aim is to become *the* reference library.
- Delivery shape: library core + TRF CLI wrapper.
- **Everything possible, good defaults**: every FIDE-defined system and option is in scope so it *can* be used, but the library ships sensible defaults packaged as profiles (e.g. "a simple club Swiss tournament"). The common case must stay simple.
- **Functional README**: during development keep `README.md` clear and current. It states what the library does and exactly which FIDE specs and editions it implements, with their status. The spec must make this part of the build's definition of done.
- Research findings live on `research/<name>` branches as `docs/research/<name>.md`; each research ticket points at its branch.

## Decisions so far

<!-- one line per closed ticket -->

- [Basic and General Handling Rules](01-basic-handling-rules.md): 2026 editions of C.04.1/C.04.2 in force; points and PAB value are configuration; unplayed rounds leave the colour history; floats are defined per system; round-record model with per-system hooks.
- [Tie-break regulations](07-tie-breaks.md): C.07 2026 (from 1 Mar 2026), 26 tie-breaks plus modifiers in the Technical Commission's code syntax; unplayed rounds use adjusted and capped-dummy scores; no official 2026 test data or open-source reference.
- [FIDE endorsement process and tooling](05-fide-endorsement.md): C.04.A was replaced by C.02.03 §7 technical acceptance, for full programs only (the library gets in by being embedded); needs a checker CLI + TRF26 generator; tested on 50k tournaments; only Dutch has accepted programs, and Gacrux (MIT) is TEC's reference tooling.
- [Swiss Team System 2026](03-swiss-team-system.md): top-down scoregroups (bye first, choose upfloaters, then first-best pairing in identifier order), colour decided afterwards and never blocking, no endorsed program; Gacrux has the only C.04.6 module; several articles are ambiguous.
- [TRF file format](06-trf-format.md): TRF26 is current (TRF16/06 must still be read); it standardises most JaVaFo XX? lines; the de-facto CLI is JaVaFo's `-p`; JaVaFo 2.2 and bbpPairings read different dialects; no team reply format exists.
- [Dubov, Burstein, Lim, Double-Swiss and acceleration](04-other-swiss-systems.md): all reissued 2026-02-01; none endorsed, so they are verified by tracing rules to the text, the handbook's examples and witnesses (Vega, chesspairing); acceleration is a pluggable virtual-points layer; Lim riskiest, do it last.
- [Dutch System and its reference programs](02-dutch-system.md): C.04.3 2026 (C1–C21, completion check in every bracket, new bye criteria); bbpPairings v6 (Apache-2.0, matching-based) is the only 2026 oracle; JaVaFo 2.2 is 2017-only and closed; the two agree fully on 2017 rules.
- [Olympiad Pairing Rules scope](16-olympiad-rules-scope.md): in scope. The principle behind it is that everything FIDE defines is possible, with good defaults as profiles.

## Not yet specified

- Algorithm design for each remaining system (Swiss Team once its rulings are made, Dubov, Burstein, Lim, Double-Swiss), probably one prototype per system once the Dutch prototype sets the pattern.
- How acceleration plugs in (leaning: a per-round virtual-points layer; open points: Double-Swiss virtual-point value, floats under acceleration).
- Tie-break module design and how standings are modelled, including rulings on the 12 C.07 ambiguities (see Tie-break regulations) and how fixtures re-derived from the 2023 exercise set get validated (international arbiter review?).
- Public library API (how a client hands over tournament state and receives pairings/standings).
- TRF CLI surface (JaVaFo/bbp-compatible `-p`/`-check`/generator), our team-pairing reply format, and how to handle what TRF26 can't express (Lim `192` code, XXS→162 losses).
- Publishing: Maven Central coordinates, release/versioning.
- Acceptance route: how the library reaches FIDE recognition given only full programs are accepted (partner program, own thin program, or offering to TEC as the reference for the systems that have none).
- An eventual performance budget, once real workloads show problems.

## Out of scope

- Rating calculation, tournament persistence and UI, result entry, running rounds, team lineup / board-order validation, non-Swiss formats (round-robin, knockout). Ruled out while charting.
