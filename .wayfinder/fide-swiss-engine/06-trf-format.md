---
title: TRF file format
labels: [wayfinder:research]
status: closed
assignee: mark
blocked_by: []
---

## Question

What is the current FIDE Tournament Report File format (TRF16 and any newer revision, e.g. a 2025/2026 update)? Which records/fields exist for players, results, byes, forfeits, teams, acceleration and pairing-system configuration, and what non-standard extensions do the endorsed pairing programs use (e.g. XXR, XXC, XXS lines)?

## Context

Findings: branch `research/trf-format`, file `docs/research/trf-format.md`.

## Resolution

Resolved by research (branch `research/trf-format`, commit 69f9d00, `docs/research/trf-format.md`). JaVaFo 2.2 and bbpPairings v6.0.0 were actually run on sample files.

- Current format: **TRF26** (Annexure A of C.02.03, approved 12/05/2025, applies from 01/09/2025). FIDE programs must still *read* TRF16 and TRF06. Follow the Handbook copies of the spec and its code tables (tie-break codes, tournament-type codes).
- Record `001` and header records `012`–`132` are unchanged. New records: engine setup (`142` rounds, `152` initial colour, `162` scoring, `192` system), `250` acceleration, `260` forbidden pairings, `240` byes, team records (`310` replaces `013`, plus `320`/`330`/`300`/`299`/`352`/`362`/`801`/`802`), tie-breaks `202`/`212`, time control `222`.
- TRF26 standardises most JaVaFo `XX?` lines (142≈XXR, 152≈XXC, 162≈XXS but less expressive, 250≈XXA, 260≈XXP, 240≈XXZ).
- There's no FIDE-standard request/reply convention. The de-facto one is JaVaFo's: `input -p [out]` → the number of pairs, then `white black` lines, with `id 0` for the bye. bbpPairings copies it and adds exit codes 0–5.
- The engines read different dialects. JaVaFo 2.2 reads TRF16 + `XX?` only (it ignores 142/240 and needs XXR). bbpPairings reads TRF26 + `XXR/XXC/XXA/XXP` + its own `BB?` lines, and rejects team files.
- Implications: read every dialect into one model and keep unknown lines; write TRF26 by default with an option to also emit JaVaFo lines (for oracle runs); copy the JaVaFo/bbp CLI conventions; use exact decimal scores; define our own team-pairing output format.
- Open questions: no `192` code for Lim or for the 2026 Dubov/Burstein; the `250` example looks wrong for match-point teams; checker/generator CLI syntax isn't specified; encoding/line endings; no team reply format; what to do when XXS→162 can't be converted; whether JaVaFo will be updated for the 2026 rules (if not, it only checks 2017-rule pairings).
