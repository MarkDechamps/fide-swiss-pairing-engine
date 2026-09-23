---
title: Verification strategy
labels: [wayfinder:grilling]
status: closed
assignee: mark
blocked_by: [02-dutch-system, 05-fide-endorsement, 06-trf-format, 12-historic-rule-editions]
---

## Question

How do we prove the library is endorsement-grade: which oracles per system (endorsed programs, the FIDE checker, hand-worked handbook examples), how differential testing is driven (random tournament generation, corpus size), how that fits the TDD workflow, and what counts as done for each system?

## Context

From FIDE endorsement process and tooling: acceptance requires a `-check` CLI and a TRF26 random generator, tested on 50k tournaments in both directions. Only Dutch has accepted oracles (JaVaFo, bbpPairings); Gacrux (MIT) offers a checker, generator and 120k test tournaments. The non-Dutch systems have no oracle at all. From TRF file format: both JaVaFo 2.2 and bbpPairings v6.0.0 run locally via `input -p`, but they read different dialects (JaVaFo: TRF16 + XX?; bbp: TRF26 + XXR/XXC/XXA/XXP), so oracle runs need dialect-specific writers. From Dubov, Burstein, Lim, Double-Swiss and acceleration: no oracle exists for the non-Dutch systems; witnesses are Vega, bbpPairings (old Burstein) and the Go library gnutterts/chesspairing; Burstein's worked-example list is usable as a fixture. From Dutch System research: pinned bbpPairings v6 as the primary 2026 oracle (disagreements are items to investigate, since it's the only one); JaVaFo 2.2 as a legacy 2017 oracle, downloaded at test time only (its licence has no redistribution grant: may CI use it, and may we publish outputs derived from it?); always state the initial colour explicitly; watch bbp ignoring XXZ/XXS. From Probe Gacrux on Swiss Team edge cases: Gacrux is usable as a Swiss Team oracle only with its bracket-order bug patched (tpn-order) or with the affected brackets skipped. Its checker's detailed mode crashes on rounds with a bye, and files need an explicit 162 record.

## Resolution

Decided on 2026-09-23. The author asked to go with the recommended answers and to stop only on real blockers; there were none. This builds on Dutch System and its reference programs, FIDE endorsement process and tooling, TRF file format, Historic rule editions, Project foundation and Probe Gacrux on Swiss Team edge cases.

- **Three kinds of evidence**, named in the glossary:
  - An **Oracle** is an external program whose output must match ours exactly. Any difference gets looked into.
  - A **Witness** is an external program whose output we compare with ours but which never gates a build. Every difference is explained.
  - The **Handbook** itself: its articles and worked examples, traced by tests that cite them.
- **Evidence per system and edition:**

  | Scope | Oracle | Witness | Handbook fixtures |
  |---|---|---|---|
  | Dutch 2026 | bbpPairings v6.0.0 (pinned) | — | C.04.3 examples |
  | Dutch 2017 (pre-2026 package) | JaVaFo 2.2 **and** bbpPairings v5.0.1, which must agree with each other and with us | — | — |
  | Swiss Team 2026 | Gacrux @ 6419149 with the tpn-order patch, run with the settings that match Gacrux's readings (see Swiss Team interpretation rulings) | — | C.04.6 examples |
  | Dubov, Burstein, Lim, Double-Swiss, Olympiad, acceleration (C.04.7) | none | Vega, gnutterts/chesspairing, bbp's old Burstein, where they cover the edition | worked examples (for Burstein: its list minus the duplicate line) |
  | Tie-breaks C.07 2024-08 | Mario Held's TEC exercise set (fixtures) | Gacrux tie-break checker | C.07 examples |
  | Tie-breaks C.07 2026-03 | none (no official data) | Gacrux tie-break checker | the exercise set re-derived for the 2026 caps, marked as our derivation |

- **Our own invariant checker** runs against every system, including the ones with no oracle. It lives in the test code of `core`. On any generated tournament it asserts the edition's absolute criteria: no rematch, at most one PAB, the colour limits where they apply, and everyone either paired or given a bye. The generator is required to produce only valid input, so any violation fails the build.
- **Differential testing, in both directions** (the shape FIDE's technical acceptance uses):
  - *Their pairings, our checker:* our `-check` must accept every round of the tournaments the oracle generates, as well as Gacrux's 120k published corpus (Dutch and Team).
  - *Our pairings, their checker or re-pairing:* the oracle re-pairs each round of the tournaments our generator produces, and the results must be identical.
  - Generators: ours (the TRF26 generator FIDE requires) plus the oracle's own (`bbp -g`, Gacrux's generator), so neither side only ever sees its own style of input. Always state the initial colour explicitly, and don't use XXZ/XXS with bbp (it ignores them).
  - Everything is reproducible: oracle versions are pinned (for Gacrux, commit plus patch), seeds are logged, and CI saves each failing tournament as an artifact.
- **Corpus sizes and cadence:**
  - *PR gate* (Surefire, runs on every commit): unit tests, Handbook fixtures, the invariant checker on a few hundred seeded tournaments, and a committed **regression corpus** (TRF files plus the expected pairings from bbp/Gacrux, which are Apache-2.0/MIT and so safe to commit). It never runs an external program.
  - *Nightly* (Failsafe in `oracle-it`): 5,000 tournaments per oracle-backed scope, with seeds rotating each night; witness diff reports for the other systems; PIT.
  - *Release gate* for a system: the full **50,000 tournaments in both directions**. The target is zero unexplained discrepancies, which is stricter than FIDE's ≤10.
- **JaVaFo**: downloaded at runtime in the nightly job and used as a separate process. Its outputs are never committed or published; only pass/fail counts and our own minimised TRF inputs are kept. This follows ADR 0001 (Apache-2.0 licence).
- **Known Divergence register**: `docs/verification/known-divergences.md`. Each entry records the scope, a minimised TRF, the oracle's output and ours, the article, our reading, and a link to the ruling (for example Swiss Team interpretation rulings, Tie-break interpretation rulings, or an ADR). A difference is either fixed or registered, never ignored. The oracle-it runner reads the register and only fails on unregistered differences. Where the text allows both readings, the divergence is also exposed as a configurable reading (per Contact FIDE Technical Commission).
- **Fit with TDD**: the inner loop is red-green-refactor on rule objects, where each test cites its article (Project foundation). Handbook examples are acceptance tests. Oracle runs are the outer loop: they find cases but never drive the design directly. A discrepancy becomes a minimised TRF, then a failing fixture test in the regression corpus, then a fix or a register entry.
- **Definition of done per system and edition** (the README status must follow it):
  1. Every article of the system's text has at least one test that cites it. A traceability list per system goes under `docs/verification/`.
  2. Every Handbook worked example passes.
  3. The invariant checker is clean on the nightly corpus.
  4. With an oracle: the 50k release gate passes in both directions with zero unexplained discrepancies. Without an oracle: the witness diff has been reviewed, and every difference is fixed or registered with the article cited.
  5. The README shows one of three statuses: **Oracle-verified** (Dutch, Swiss Team), **Handbook-verified** (with witness review), or **Experimental** (in progress).
- No mutation-score threshold: PIT is reported, not gated (Project foundation).
