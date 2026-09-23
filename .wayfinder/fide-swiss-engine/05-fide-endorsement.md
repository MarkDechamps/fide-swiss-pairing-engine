---
title: FIDE endorsement process and tooling
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

How does FIDE endorse pairing software (C.04.A and related Systems of Pairings and Programs Commission documents)? What is tested per system, what tooling does FIDE use (free pairing checker, random tournament generator, test corpora), what must the submitted program's interface look like, and which systems currently have endorsed programs?

## Context

Findings: branch `research/fide-endorsement`, file `docs/research/fide-endorsement.md`.

## Resolution

Resolved by research (branch `research/fide-endorsement`, commit 672b488, `docs/research/fide-endorsement.md`).

- C.04.A is gone. The rules are now in Handbook C.02.03 §7 "Tournament Handler Programs" (from 1 Mar 2026), run by FIDE's Technical Commission (TEC); operational detail is in the TEC Manual v1.24.
- Process: vendor registration → self-assessment → technical acceptance (≥3 TEC testers + FIDE Council) → commercial "FIDE Endorsed". Fees are about USD 1,200 (+300 preliminary), not refundable, with a 1-year bar after a rejection.
- Testing covers pairings and tie-breaks. Required: a free checker CLI (`prog -check file.trf`: re-pairs every round, checks standings) and a free random tournament generator writing TRF26. The test is 50,000 generated tournaments with ≤10 discrepancies (the old rule said 5,000; possibly a typo), run in both directions. A system's first acceptance goes to a 4-person subcommittee with 9 months to report.
- Only **Dutch** has accepted programs: 11, of which 9 use JaVaFo, SwissSys uses bbpPairings, and Swiss-Chess has its own engine. Dubov, Burstein, Lim, Swiss Team and Double-Swiss have none and are formally "deprecated" under C.04.2 art. 1.4.
- Tooling: TEC's reference tooling is **Gacrux** (Otto Milvang, MIT licence): pairing checker, tie-break checker, generator, and 120,000 published test tournaments. It is "not FIDE approved".
- Only full tournament programs can be accepted, not a bare library. The library gets recognised by being embedded (as JaVaFo/bbpPairings are). Target: a JaVaFo/bbpPairings-compatible checker + generator CLI covering tie-breaks as well. For the non-Dutch systems the library could be the *first* reference.
- New acceptances are paused until the new Commission publishes the final checklist.
- Open questions: 50k vs 5k, whether a Gacrux comparison becomes mandatory, how systems with no reference get bootstrapped, JaVaFo's 2026-rules status, whether a standalone engine can be registered, the missing Lim code in TRF26.
