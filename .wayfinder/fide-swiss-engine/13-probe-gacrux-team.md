---
title: Probe Gacrux on Swiss Team edge cases
labels: [wayfinder:task]
status: closed
assignee: mark
blocked_by: []
---

## Question

How does Gacrux (TEC's MIT-licensed reference tooling, C.04.6 module since July 2026) actually behave on the ambiguous Swiss Team articles? Install and run it (AFK) on hand-crafted TRF26 team tournaments that isolate each ambiguity: bracket ordering (score, TPN) vs art. 3.6.1, C6 pass/fail vs graded, "complies with" when C7/C10 can't reach zero, forfeits counted as played, the Type B last round, colour-history comparison (4.3.6), floaters under acceleration. Record observed outputs and whether each looks like a bug or a legitimate reading.

## Context

From Swiss Team System 2026 (`research/swiss-team-system`, open questions 1–7).

Findings: branch `research/gacrux-team-probe`, file `docs/research/gacrux-team-probe.md`.

## Resolution

Done (AFK). Branch `research/gacrux-team-probe`, commit d67305a, `docs/research/gacrux-team-probe.md`; the case TRFs, their outputs and the probe scripts are in `docs/research/gacrux-team-probe/`. Gacrux is TieBreakServer @ 6419149 (v1.10.62) on Python 3.14 + networkx 3.7; its own 101 team tests pass. Four alternative readings were observed by patching Gacrux in memory, not worked out by hand.

| # | Ambiguity | Gacrux reading | Verdict |
|---|---|---|---|
| 1 | Bracket order (3.6.1) | (score, TPN) | **Bug**: contradicts 3.6.1 and Gacrux's own docstring |
| 2 | C6 | pass/fail | defensible but disputed; the graded reading changes pairings. Ask TEC |
| 3 | "complies with" (C7) | lowest attainable value | defensible, the better reading |
| 4a | forfeit counts for C1 | no | defensible (GHR 3.5) |
| 4b | forfeit counts as "matches played" for the bye | no | defensible |
| 5 | Type B, last round, CD 0 after two same colours | strong preference | text allows both. Ask TEC |
| 6 | 4.3.6 colour history | played-only, from the end | defensible (GHR 3.4 example) |
| 7 | floaters under acceleration | pairing score | text is silent. Ask TEC |

- Also found: `pairingchecker -c -p` crashes on rounds with a bye (503), and a file with no drawn game crashes (510). The fix for bug 1 belongs in `update_bracket`, not `sort_nodes`. Reproducers are in the folder.
- Consequence: Gacrux is **not an unconditional oracle**. Brackets where an upfloater has a smaller TPN than a resident must be skipped, or Gacrux must be run with the tpn-order patch.
- For TEC: C6, Type B last round, floaters under acceleration, forfeits for C1/"played" (incl. partly played matches), "complies" = best attainable, from-the-end colour comparison. For Otto Milvang (Gacrux author): bug 1 and the two crashes.
