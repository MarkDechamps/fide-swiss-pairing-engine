# Known Divergence register

Every difference between an Oracle and the library is either fixed or registered here (Verification strategy).
Each entry gives the scope, a minimised input, both outputs, the article, our reading and the ruling behind it.

## KD-1 Swiss Team: Gacrux seats bracket members by score, then TPN

- **Scope:** Swiss Team 2026 (C.04.6 3.6.1), Gacrux @ 6419149 unpatched.
- **Input:** `trf/src/test/resources/swiss-team/probe/case1-bracket-order.trf`, round 2.
- **Gacrux:** `4-1, 3-2, 7-5, 6-8`. **Library:** `4-2, 3-1, 7-5, 6-8`.
- **Article:** 3.6.1, "the team with the smaller TPN is the top member of the pair".
- **Reading:** the top member is the smaller TPN whatever the score; Gacrux's `sort_nodes` orders by score first,
  against its own docstring. Ruling G1 of Swiss Team interpretation rulings calls it a Gacrux bug.
- **Handling:** the Oracle always runs with the `tpn-order` patch (`update_bracket` over TPN-sorted nodes), in
  `trf/src/test/resources/corpus/gacrux-team-drive.py`. Unpatched, 7.0% of Gacrux's corpus rounds differ.

## KD-2 Swiss Team: Gacrux's PAB is a win per board without a 162 `P`

- **Scope:** Swiss Team 2026 (C.04.6 1.4), game points primary, a TRF whose `162` has no `P` value.
- **Input:** any `FIDE_TEAM_*_GP*` file with a PAB and `162  W 1.0    D 0.5    L 0.0`.
- **Gacrux:** the PAB team scores a win on every board (4 GP over four boards). **Library:** a drawn match
  (2 GP), as 1.4 says: "as many match points and game points as are rewarded for a draw".
- **Reading:** 1.4 fixes the default; `162`'s `P` or a `320` record overrides it, and the library reads both.
- **Handling:** the corpus states `P 0.5`.

## KD-3 Swiss Team: Gacrux accelerates the secondary score by match points

- **Scope:** Swiss Team 2026 with acceleration (C.04.7) and a secondary score for colours (4.2.2).
- **Input:** a `FIDE_TEAM_TYPEB_MP_GP_BAKU` file with `250  2.0  0.0   1   2    1    4`: two teams of the
  Accelerated Group and outside it on equal Pairing Scores, both preferring White.
- **Gacrux:** adds the `250` match points to the game points it compares in 4.2.2
  (`tiebreak.get_accelerated` reads `matchPoints` for every score but individual points). **Library:** the
  secondary score is the real game points; Virtual Points only enter the Pairing Score (C.04.7 1.5, Acceleration
  readings decision 1: the text names the secondary score, so it is not the Pairing Score).
- **Handling:** the corpus accelerates only codes without a secondary score (`FIDE_TEAM_TYPEA_MP_BAKU`,
  `FIDE_TEAM_TYPEB_MP_BAKU`).

## KD-4 Dutch 2017: JaVaFo 2.2 pairs differently when half-point byes are in play

- **Scope:** Dutch System, pre-2026 Swiss Rules Edition (C.04.1 2017), JaVaFo 2.2 (Build 3222) against bbpPairings v5.0.1 and the library.
- **Input:** `docs/verification/inputs/kd-4-javafo-half-point-byes.trf` (our own generated input, minimised by size only), round 3, with
  half-point byes (`H`) in the history and in the round to pair.
- **JaVaFo:** `16-8, 17-6, 18-10, 5-15, 7-14, 9-2`. **bbpPairings v5.0.1 and library:** `5-8, 6-2, 7-10, 9-16, 17-14, 18-15`.
- **Reading (analysed; the cause is narrowed, not proven):** JaVaFo is closed source, so this is from black-box experiments on the input (16 variants, each with
  the points column recomputed so that bbpPairings accepts it). Round 3 has 16 present players, the 1.0 bracket has 10 members (8 residents and the
  moved-down 2 and 5, who have met) and can be paired completely. bbpPairings and the library do so and float one player, 7, to the 0.0 bracket. JaVaFo pairs
  only four boards in it and floats two players, 14 and 18, so it does not maximise the pairs of the bracket (C.04.3 2017, completion criteria; C.04.1
  1.4.3 for who counts as a floater). The divergence needs half-point byes (`H`) in the history **and** requested ones in the round to pair: with every `H`
  changed to `Z` (zero-point) JaVaFo agrees in all 16 variants, and with a single `H` changed it still differs. Which combination of `H` and `Z` differs is not
  monotonic (`4:H 13:H,H 15:Z` differs; `4:Z 13:H,H 15:Z` agrees), so it is not an effect of the score of any one player. Best hypothesis: JaVaFo counts a
  requested half-point bye of the round to pair together with earlier ones (for the downfloat of C.04.1 1.4.3, "without playing in a round, scores more
  points than those rewarded for a loss") and lets that outweigh completion in the bracket. Our reading of 1.4.3 (an `H` gives a downfloat in its own
  round only, as bbpPairings v5.0.1 also reads it) stands; nothing in the text supports leaving a paired bracket incomplete. The engine is not changed.
- **Handling:** the register companion (`oracle-it/src/main/resources/known-divergences.tsv`) recognises a JaVaFo difference whose input has an `H`
  bye, so the JaVaFo gate plays requested byes like the bbpPairings gates and reports these rounds apart ("registered") instead of failing on them; a
  difference in an input without an `H` bye still fails. 26 of 320 rounds in 40 tournaments (seed 20260929) are registered. Open item of the
  Verification strategy: ask the JaVaFo maintainer with this input, or drop JaVaFo as a 2017 Oracle.
