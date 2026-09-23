---
title: Swiss Team interpretation rulings
labels: [wayfinder:grilling]
status: closed
assignee: mark
blocked_by: [13-probe-gacrux-team]
---

## Question

For each ambiguous C.04.6 article, which reading does the library adopt: follow Gacrux, follow our own reading of the text, or make it configurable? Record each ruling (probably as ADRs). No TEC clarification is sought (see Contact FIDE Technical Commission), so each ruling must stand on the text alone.

## Context

From Swiss Team System 2026 (`research/swiss-team-system`). Evidence comes from Probe Gacrux on Swiss Team edge cases.

## Resolution

Decided (the author delegated these to the recommended readings, 2026-09-23). Evidence: `docs/research/gacrux-team-probe.md` (cases 1–7) and `docs/research/swiss-team-system.md` (A1–A13). Policy: ADR 0003.

**Policy.** A reading is **fixed** when the text, the GHR or the probe supports only one. It is an **Interpretation** (a named, switchable setting of the Swiss Team rule objects) only where the text literally allows both and the choice changes pairings. Every Interpretation defaults to Gacrux's reading, so the default is what the Oracle verifies. Non-default readings are cross-checked against Gacrux with the matching `alt.py` counterfactual patch. That patch is ours, so it counts as a Witness, not an Oracle. Each ruling cites its probe case as a JUnit fixture.

| Point | Ruling | Kind | vs Gacrux |
|---|---|---|---|
| G1 bracket seats (3.6.1) | Top member = smaller TPN, whatever the score; the pairing identifier follows | fixed | **Known Divergence (Gacrux bug)**; the Oracle runs with the `tpn-order` patch |
| A1 Type B, last round, CD 0 after two same colours (1.7.2) | Interpretation `LastRoundZeroCdTypeB`: **strong preference** (default; the no-preference sentence only closes the mild CD-0 clause) or no preference | Interpretation | default = Gacrux |
| A2 overlapping strong/mild clauses | The strong clause wins | fixed | same |
| A3 forfeit and [C1] | A forfeited or double-forfeited match is not a meeting (GHR 3.5). A match counts as **played** if at least one board was played over the board | fixed | same |
| A4 [C2] vs BR 4 | The team text's own list (PAB, forfeit win, FPB) is the specific rule | fixed | same |
| A5 [C5] "pairs involving upfloaters" | Compare the ascending multiset of upfloater scores lexicographically | fixed | same |
| A6 [C6] | Interpretation `UpfloaterLookAhead`: **parity minimum**, pass/fail (default) or **graded** (fewest upfloaters the following scoregroup needs, then [C7], then the first set). Either way, the "following scoregroup" is the next non-empty score level among the teams still to be paired | Interpretation | default = Gacrux; graded = `alt.py c6-graded` |
| A7 "complies with" minimisation criteria (3.5.5, 3.6.4) | Best value attainable, in priority order, then the first in lexicographic order | fixed | same |
| A8 floaters under acceleration (1.5, [C7], [C10]) | Interpretation `FloatScore`: **pairing score of that round** (default, the Dutch precedent) or real score | Interpretation | default = Gacrux |
| A9 3.4.3 "matches played" | Only played matches count (same "played" as A3) | fixed | same |
| A10 example gap (3.5.4) | Falls back to a larger upfloater count or a worse profile, following [C3]'s priority | fixed | — |
| A11 set-enumeration cost | Not a reading. Belongs to the algorithm design (optimum-finder pattern) | — | — |
| A12 4.3.6 history comparison | Right-aligned played-only histories (GHR 3.4 example) | fixed | same |
| A13 team colour | Derived from the board-1 player's colour in played matches; 801/802 are a cross-check only | fixed | — |
| PAB game points, odd boards (§2.4) | Configuration (Basic and General Handling Rules already made the PAB value configuration); the default is draw points × boards / 2 | config | — |

Consequences: the Swiss Team rule objects take three Interpretation settings, which Profiles may set. The functional README lists every fixed reading and Interpretation with its default. The three Interpretations stay open to a TEC ruling, if the author ever asks for one (Contact FIDE Technical Commission). New ticket: Readable Swiss Team pairing algorithm.
