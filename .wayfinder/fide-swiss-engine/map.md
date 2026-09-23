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
- **Domain-driven design** with a **clean public API**: the model speaks the Handbook's language (see the glossary), and clients see a small, intention-revealing surface.
- Object-oriented, **composition over inheritance**. When in doubt about a design choice, ask the author rather than guessing.
- Java style: `Optional` instead of `null`; `var` for locals; extracted methods with intention-revealing names instead of comments; domain value objects instead of primitives.
- **Readability over performance**: the code mirrors the handbook articles, and performance issues are fixed when they surface.
- TDD, clean architecture and clean code are paramount. Skills every session should consult: `tdd`, `clean-java`, `clean-code`, `codebase-design`, `domain-modeling`.
- Open source, for the author's own use and for distribution; the aim is to become *the* reference library.
- Delivery shape: library core + TRF CLI wrapper.
- **Everything possible, good defaults**: every FIDE-defined system and option is in scope so it *can* be used, but the library ships sensible defaults packaged as profiles (e.g. "a simple club Swiss tournament"). The common case must stay simple.
- **Functional README**: during development keep `README.md` clear and current. It states what the library does and exactly which FIDE specs and editions it implements, with their status. The spec must make this part of the build's definition of done.
- Research findings live in `docs/research/<name>.md` on `main`. New research is done on a `research/<name>` branch and merged into `main` when its ticket is resolved.
- The build scaffold (poms, wrapper, CI, README) exists per Project foundation; the functional README is kept current as systems land.

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
- [Probe Gacrux on Swiss Team edge cases](13-probe-gacrux-team.md): Gacrux has a real bracket-order bug and two crashes; 6 of its 7 readings are defensible, 3 need a TEC ruling (C6, Type B last round, floaters under acceleration); it can serve as an oracle only with the bug worked around.
- [Project foundation](08-project-foundation.md): Java 25 and Maven with a wrapper, Apache-2.0, `io.github.markdechamps:fide-swiss-pairing-engine-*`; modules core (zero dependencies) / trf / cli / oracle-it, each with a module-info; JUnit 6 + AssertJ without mocks; Spotless, PIT nightly; GitHub Actions; this repo goes public with the map and research.
- [Contact FIDE Technical Commission](15-contact-fide-tec.md): deferred; no contact with TEC or the Gacrux/JaVaFo authors until the author asks. Open questions are settled by our own documented readings, configurable where the text allows both.
- [Historic rule editions](12-historic-rule-editions.md): editions are first-class. The Swiss Rules Edition (2026 or pre-2026, one C.04 package) and the Tie-break Edition (2026-03 or 2024-08) are separate explicit settings; 2026 everywhere plus Dutch 2017 and C.07 2024-08 as verification anchors; each edition is a composition of per-article rule objects.
- [Shared domain model across all systems](09-shared-domain-model.md): immutable `Tournament` snapshot (settings hold the system and editions) with `pairNextRound()` and `standings()`; one `Participant` with a stable id apart from its per-round Pairing Number; round-centric facts, derived shared history, per-system float rules; distinct `Score` and `PairingScore`; tie-breaks as composable objects with an unplayed-round policy.
- [Verification strategy](10-verification-strategy.md): Oracles (bbp v6 for Dutch 2026; JaVaFo 2.2 + bbp v5 for Dutch 2017; patched Gacrux for Swiss Team) must match exactly; Witnesses for the other systems are reviewed but never gate; our own invariant checker runs on everything; testing runs in both directions (PR gate: committed corpus, nightly: 5k, release: 50k) with zero unexplained differences, justified ones kept in a Known Divergence register; a per-system definition of done drives the README status.
- [Readable Dutch pairing algorithm](11-dutch-algorithm.md): yes; the literal Art. 3–4 procedure (lazy candidate stream, per-article criteria vector, a matching oracle only for legality) matched bbp v6 on every round it finished; decided: literal search + an exact optimum target, four documented readings (4.4.1, C5, C8, C9), Dutch 2017 as its own procedure object over shared parts.
- [Swiss Team interpretation rulings](14-swiss-team-rulings.md): Gacrux's bracket-seat order is a bug (the library follows 3.6.1, a Known Divergence); nine other points get a fixed reading and odd-board PAB game points become configuration; three become switchable Interpretations ([C6] look-ahead, Type B last round, floaters under acceleration) that default to Gacrux's reading so the Oracle verifies the default (ADR 0003).

## Not yet specified

- Algorithm design for each remaining system (Dubov, Burstein, Lim, Double-Swiss), one prototype per system following the Dutch pattern (literal generator, criteria vector, matching oracle, optimum finder). Whether criteria get a shared abstraction is settled with the second one.
- Acceleration details: the shape is settled (a virtual-points layer feeding `PairingScore`); still open is the Double-Swiss virtual-point value (for Swiss Team, floats under acceleration are the `FloatScore` Interpretation; whether the other systems share it is open).
- TRF CLI surface (JaVaFo/bbp-compatible `-p`/`-check`; the generator's own design is the Random tournament generator ticket), our team-pairing reply format, and how to handle what TRF26 can't express (Lim `192` code, XXS→162 losses).
- Publishing: release process, versioning and signing for Maven Central (coordinates fixed in Project foundation).
- Acceptance route: how the library reaches FIDE recognition given only full programs are accepted (partner program, own thin program, or offering to TEC as the reference for the systems that have none)). On hold: no contact with FIDE until the author asks.
- An eventual performance budget, once real workloads show problems.

## Out of scope

- Rating calculation, tournament persistence and UI, result entry, running rounds, team lineup / board-order validation, non-Swiss formats (round-robin, knockout). Ruled out while charting.
