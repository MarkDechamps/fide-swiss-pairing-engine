---
title: TRF CLI surface
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: []
---

## Question

What exactly does the `cli` module accept and print? Covers the JaVaFo/bbpPairings-compatible commands (`-p` pair the next round, `-c`/`-check` check the last round, output file layout, exit codes), how the trace is shown, flags for system/profile/edition/Interpretations (precedence is settled in Tournament profiles), our reply format for team pairings (no standard exists), and how to handle what TRF26 cannot express (Lim has no `192` code; what `XXS` can say that `162` cannot).

## Context

Graduated from the "TRF CLI surface" fog once Public library API fixed the operations the CLI wraps: `pairNextRound()` → `RoundPairing` (with `PairingTrace`), `check(ProposedPairing)` → `PairingCheck`, and the error model. Evidence: `docs/research/trf-format.md` (TRF26, JaVaFo `-p`, dialect differences between JaVaFo 2.2 and bbp). The generator's own CLI is the Random tournament generator ticket.
