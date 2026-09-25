---
title: Acceleration across the systems
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

With the shape settled (a virtual-points layer feeding `PairingScore`, C.04.7 Baku as its one method), what does each system read from the Pairing Score and what from the real score? Open points: the Double-Swiss virtual-point value (a game win, 1, or a match win, 2); whether Lim's Median Scoregroup (half of what the rounds played can give) moves with the virtual points; and whether Dutch, Lim, Olympiad and Double-Swiss share Swiss Team's `FloatScore` reading, Dubov's (upfloats by Pairing Score) or Burstein's (brackets by Pairing Score, Index by standings points). Decide one reading per system, fixed or an Interpretation, and what `250` and `_BAKU` imply for each.

## Context

Graduated from the "Acceleration details" fog when the frontier emptied (Double-Swiss matches in TRF). Evidence: `docs/research/other-swiss-systems.md` §7 and open questions 4–5, Dubov, Burstein, Lim, Double-Swiss and acceleration, Swiss Team interpretation rulings (floaters under acceleration), the per-system algorithm tickets (Dubov, Burstein, Lim, Double-Swiss, Olympiad), `docs/research/trf-format.md` §5 (`250`), Double-Swiss matches in TRF (`250` ranges cover whole matches).

## Resolution

Decided on 2026-09-25. Floats, the Double-Swiss value, the Lim median and board order were grilled one by one; after that the author asked to take the recommended answers and to stop only on real blockers. There were none, so decisions 5 to 10 are the recommended ones, for the author to overturn.

**New evidence (bbp v6 and v5, run this session on hand-made TRF files, 12 players, `XXA` +1 for players 1–6).** bbp judges floats on the Pairing Score of the round in which the game was paired (`getFloat` reads `scoreWithAcceleration(roundsBack)` in the v5 source; v6 behaves the same). It sorts the **boards by real score** (`sortResults` uses `scoreWithoutAcceleration`): both versions print the Accelerated-Group losers' pair `6 5` (Pairing Scores 1 and 1) below the drawn Group-B pairs (Pairing Score ½). C.04.7 2026 1.5 says the Pairing Score sorts the boards. Oracle comparisons use `samePairingAs`, which ignores board order, so this never gates.

1. **One rule for every system: the Pairing Score is the "score" wherever the text says score**, unless the text names another. That covers scoregroups and brackets, the PAB, float history and board order. Standings and every tie-break read the real Score only. Virtual Points apply to the round being paired and never accumulate.
2. **Floats.** Fixed reading, *Pairing Score of the round in which the game was paired*, for Dutch (both editions; the Oracles agree), Dubov (upfloats) and Lim (3.10). Swiss Team keeps its `FloatScore` Interpretation (Swiss Team interpretation rulings, A8). **Double-Swiss reuses that same Interpretation and default**, because it shares the Top-Scoregroup Procedure, as it did for [C6]. Burstein has no float history.
3. **Double-Swiss value: 2, then 1** (a match won 2–0, then half), fixed. Virtual Points equal what one round can give, as team Baku gives 2 MP per round; with 1 the Accelerated Group would sit only half a round ahead. Premise 1.1 holds per game, so Baku is allowed. A `250` for `FIDE_DOUBLESWISS_BAKU` carries 2.0 and 1.0 in the game-points column over match ranges (Double-Swiss matches in TRF). chesspairing's hard-coded 1/½ is a Witness difference, never a gate.
4. **Lim median.** The Median Scoregroup keeps its value (rounds played × points for a win ÷ 2); participants are placed in scoregroups by Pairing Score. In round 1 of 9 the Accelerated Group sits above a median of 0 and pairs inside itself, and Group B forms the median. Virtual Points only move participants between groups, as in every other system. Fixed; `FIDE_LIM_BAKU` stays the provisional code.
5. **Board order (GHR 3.6) is per edition**, for every system: Swiss Rules 2026 sorts by Pairing Score (C.04.7 1.5), pre-2026 by real Score (the old text is silent; bbp and JaVaFo practice). bbp v6 orders by real score; that is not a Known Divergence, because order is never compared.
6. **Olympiad rejects acceleration.** D.02 is not a C.04 system and has none, so any `Acceleration` other than none with Olympiad fails `Tournament.of` with `InvalidSettingsException`. There is no `FIDE_OLYMPIAD_BAKU`.
7. **The other systems, fixed:**
   - *Dutch* (2026 and 2017): Pairing Score everywhere, topscorers included (they only arise in the last round, where no Virtual Points apply).
   - *Dubov*: brackets, upfloats and the maximum upfloaters by Pairing Score; ARO reads ratings and is unaffected.
   - *Burstein*: brackets, [C6] and 3.1.3 by Pairing Score; the Index (Buchholz, Sonneborn-Berger) by real Score, as 1.7.2 says. Its seeding rounds go to the Dutch object of the same edition, on Pairing Score.
   - *Swiss Team*: Virtual Points are match points (1.4.4). A game-point-primary team tournament with Baku fails `Tournament.of`, as Gacrux enforces.
8. **The `Acceleration` setting has three shapes:** `none`, `baku()` (computed from the settings), and `explicit(VirtualPoints)` (per participant, per round, from `250` or `XXA`). Explicit points are allowed with every C.04 system, because C.04.2 1.1 also admits methods the Qualification Commission authorises. Baku computes the Accelerated Group from the round-1 list (2·⌈N/4⌉); a Late Entry inserted above the Last Accelerated Participant joins it (1.3), which can make the group odd. Unit fixtures: the 9-round example (+1 ×3, +½ ×2), the 11-round MP team example (+2 MP ×3, +1 MP ×3), N = 161 gives 82, and a late entry above and below the boundary.
9. **Pre-2026 edition.** The pre-2026 Baku takes the literal 1 and ½ and requires 1/½/0 scoring; any other scoring with pre-2026 Baku fails `Tournament.of`. The schedule is otherwise the same (research §7). JaVaFo's `-b` is an even older variant and is read as `baku()` of the chosen edition.
10. **TRF.** `250` (and `XXA`) become `explicit(...)` and override a `_BAKU` code, as TRF26 says. `_BAKU` without them becomes `baku()`. Both columns of `250` are read literally. TRF26's own team example puts the virtual points in the game-points column: under a match-point-primary system, a `250` with game points only is accepted with a warning that the scoregroups do not move. The writer puts team virtual points in the match-points column. The random generator's 20% Baku applies to every system except Olympiad.
11. **Verification.** Dutch under acceleration is gated by bbp v6 (2026) and bbp v5/JaVaFo (2017), which take explicit `XXA`, so the Oracle checks how the systems read the Pairing Score, and the unit fixtures check how Baku computes it. Every other system follows its existing Witness or Literal Enumerator, now also with Baku on.

**Glossary.** Pairing Score now says where it applies; new terms Virtual Points and Accelerated Group. No ADR: each reading is a fixed, cheap-to-change reading on the text.

**README.** The C.04.7 row now says Baku for every C.04 system (not Olympiad) or explicit virtual points, in the 2026 and pre-2026 editions.

**New ticket.** Explaining standings, pairings and progress, raised by the author during this grilling (why one participant ranks above another on equal points, guidance on which rules decided a pairing, a progress indicator while pairing).
