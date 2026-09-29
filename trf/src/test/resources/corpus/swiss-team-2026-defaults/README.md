# Swiss Team 2026 defaults corpus

Tournaments paired by plain Gacrux @ 6419149 (MIT, v1.10.62), the reference app the engine's defaults follow (ADR 0009):

```sh
DEFAULTS=1 GACRUX=<TieBreakServer clone> python3 ../gacrux-team-drive.py <absolute out-dir> 30 3000
```

Unlike `../swiss-team-2026` (the literal readings, made with the `tpn-order` patch), nothing of Gacrux's is avoided: bracket
seats by score then TPN (KD-1), `162` states no `P` so the PAB wins every board (KD-2), and Baku is played with a
secondary score (KD-3). `SwissTeamDefaultsCorpusTest` re-pairs each round from the rounds before it under the default
Interpretations and requires the identical pairing.
