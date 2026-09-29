# Swiss Team 2026 regression corpus

Tournaments paired by the Oracle for the Swiss Team System 2026: Gacrux @ 6419149 (MIT, v1.10.62) with the
`tpn-order` patch (bracket seats by TPN only, C.04.6 3.6.1; Known Divergence KD-1):

```sh
GACRUX=<TieBreakServer clone> python3 ../gacrux-team-drive.py <out-dir> 40 2000
```

`../gacrux-team-drive.py` plays 6–24 teams of 2–6 boards over 4–11 rounds: every colour preference type, match
or game points primary with or without a secondary score, Baku acceleration as explicit `250` records, whole
matches and single boards forfeited, and the PAB. Every round in each file is Gacrux's pairing, so
`SwissTeamRegressionCorpusTest` re-pairs each round from the rounds before it and requires the identical
pairing. Two settings keep Gacrux's own departures out (see `docs/verification/known-divergences.md`): `162`
states `P 0.5` (KD-2), and Baku comes only without a secondary score (KD-3).
