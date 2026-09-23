---
title: Historic rule editions
labels: [wayfinder:grilling]
status: closed
assignee: mark
blocked_by: []
---

## Question

Does the library support only the 2026 editions of C.04 (in force since 1 Feb 2026), or also earlier editions (e.g. Dutch 2017), so that historic tournament files can be replayed and checked? The editions differ in PAB eligibility, Dutch float rules for unplayed rounds and colour exceptions, and the endorsed reference programs may still implement the older ones, which matters for differential testing. The same question applies to tie-breaks: C.07 2024 vs 2026 (dummy-opponent caps), and the pre-2023 virtual-opponent rules. If older editions are supported, is an edition a first-class concept in the model?

## Context

Surfaced by the research on Basic and General Handling Rules (`research/basic-handling-rules`, open question 5). Also raised by Tie-break regulations (open question 6). TRF file format found that JaVaFo 2.2 (the engine behind 9 of the 11 accepted Dutch programs) reads only TRF16 and may still implement the 2017 Dutch rules, which bears directly on whether older editions are needed for differential testing. Dutch System research confirmed that JaVaFo 2.2 implements the 2017 rules, and that JaVaFo and bbpPairings v5 agree on 461/461 rounds under 2017 rules, whereas 2026 has only one oracle (bbp v6). A 2017 mode would give a doubly-confirmed conformance baseline.

## Resolution

Grilled with the author on 2026-09-23.

- **Editions are first-class in the model.** Two independent settings, both chosen explicitly and defaulting to the latest:
  - **Swiss Rules Edition** (`2026`, `pre-2026`): the C.04 package moves as one, so it selects C.04.1, C.04.2 and the pairing system's text together. Dutch 2017 can't be combined with 2026 Basic Rules. A system that has no text in the chosen edition (for example Swiss Team in pre-2026) is rejected when the tournament is configured.
  - **Tie-break Edition** (`2026-03`, `2024-08`).
- **Implemented editions**: 2026 for every system and C.07 2026-03, plus two historic ones that are verification anchors:
  - **Dutch 2017 with pre-2026 C.04.1/C.04.2**: JaVaFo 2.2 and bbpPairings v5 agree on it (461/461), so it checks the shared Dutch machinery against two oracles; 2026 has only bbp v6.
  - **C.07 2024-08**: makes the TEC exercise set (Mario Held, 2023/2024) usable as fixtures without re-deriving values.
  - Every other historic edition (pre-2026 Dubov/Burstein/Lim, C.07 2023 and pre-2023, older Dutch) only on real demand. "Everything FIDE defines" covers the rules in force, not superseded text.
- **No selection by date.** The library never infers the edition from the tournament date. FIDE gives no transitional rule for an event that straddles a switch, so the arbiter declares the editions. Profiles fix them. The output metadata reports both (GHR 1.3).
- **Design steer** for Shared domain model across all systems and Readable Dutch pairing algorithm: composition over inheritance. Each article that differs between editions is a small rule object behind a narrow interface (for example PAB eligibility, float rule, colour-exception scope) that cites its article and is tested on its own. An edition is a composition of those rule objects, assembled in one factory per edition, so the differences can be read line by line. There is no inheritance chain between editions. Template method is acceptable only locally, inside one system's fixed procedure.
