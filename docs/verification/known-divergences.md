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
- **Reading:** not analysed yet. JaVaFo is closed source; corpora with only forfeits, withdrawals and late entries alone all agree
  (320 of 320 rounds each), and only corpora with requested byes differ. The cause may be a TRF16 dialect detail of pending byes
  or a reading of how a bye counts in the history; bbpPairings v5.0.1, the other 2017 Oracle, agrees with the library on every such input.
- **Handling:** the JaVaFo gate of `oracle-it` plays no requested byes by default (`fideswiss.oracle.events`, see the manual, section 10),
  so the nightly run never hides a new difference behind this one. Open item of the Verification strategy: explain it or drop JaVaFo as a
  2017 Oracle.
