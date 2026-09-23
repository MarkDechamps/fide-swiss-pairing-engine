---
title: Tie-break interpretation rulings
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

For each of the 12 C.07 ambiguities listed in Tie-break regulations (for example whether acceleration virtual points count, see ambiguity 12), which reading does the library adopt, or is it configurable? Each ruling must stand on the text alone, because no TEC clarification is sought (see Contact FIDE Technical Commission).

## Context

Graduated from the tie-break fog. The module design is settled in Shared domain model across all systems (tie-break objects, modifiers as decorators, `UnplayedRoundPolicy`). How fixtures re-derived from the 2023 exercise set get validated belongs to Verification strategy.

## Resolution

Decided on 2026-09-23. The author handed the ticket over to the recommended answers ("continue with recommended and only stop on real blockers"). Evidence: `docs/research/tie-breaks.md` (ambiguities 1–12, art. 16) and a read of Gacrux's tie-break calculator (`gacrux/tiebreak.py` and `rating.py` at commit 6419149; Gacrux is the Witness for C.07 per Verification strategy).

**Policy.** This follows the policy of Swiss Team interpretation rulings (ADR 0003). A reading is **fixed** when the text, read with the rest of C.07, supports only one. It becomes an **Interpretation** only where the text literally allows both and the values change. None of the twelve qualified: each has one reading that stands on the text, and Gacrux agrees with it on every point except one bug (DE, below). The only switches are the ones C.07 itself gives the organiser: the 16.6 overrides, the art. 10 unrated-player rules, the 6.1.1 forfeit inclusion (`/P`) and the TPN/RTNG order. They are **configuration**, not Interpretations. Each ruling below becomes a hand-built JUnit fixture, and it is cross-checked against Gacrux where Gacrux implements the tie-break.

| # | Point | Ruling | vs Gacrux |
|---|---|---|---|
| 1 | "14.1.1.d" in 16.5.1 | The fourth 14.1.1 example, SB-C1. The 16.5 exception: BH cuts the lowest VUR contribution; SB compares the lowest VUR contribution with the least significant value and cuts the higher one; ESB works like SB. It repeats for every further low cut (16.5.2). The high cut of a Median has no VUR exception, since 16.5 speaks only of the least significant value | same |
| 2 | Art. 16 scope | Art. 16 (adjusted score, dummy, 16.5) applies to exactly what its preamble names: BH, FB, SB, ESB and their Cut/Median modifiers, plus AOB and SSSC because they are built on BH/FB. The 14 line "all modifiers are subject to art. 16" means "when the underlying tie-break is". So PS-C1 drops the round-1 cumulative score even when round 1 was a VUR; ARO-C1 (and TPR/APRO) drops the lowest over-the-board opponent rating; KS in Swiss uses raw final scores and counts every paired round, forfeits at their awarded points | same |
| 3 | `/P` | The forfeit is a game against the scheduled opponent. That opponent's 16.3 adjusted score is used (16.3 is what an opponent's tie-breaks see), there is no dummy, and the round is no longer a VUR, so 16.5 skips it. The SB/ESB factor is the points awarded. For DE, `/P` is the 6.1.1 inclusion: the forfeit counts at its awarded points. Rating tie-breaks ignore `/P` (15.2 exception 1). In a round robin `/P` is implied (15.2) | same |
| 4 | FB final round | Every final-round pairing with an opponent counts as a draw, including a forfeit known at pairing time (it is a paired game). A final-round PAB, full-point bye or requested bye keeps its awarded points, except that a category .5 bye already counts as a draw for opponents (16.3.2). FB can be computed once the final round is paired | same |
| 5 | TPR rounding | p = over-the-board points ÷ over-the-board games, scored 1/½/0 whatever the Scoring Scheme (B.02 tables assume that scale). It is rounded half-up to two decimals before the 8.1.1 lookup, with p = 0 and p = 1 giving ∓800. The dp is added to the rounded ARO (10.1 defines ARO as rounded, and 10.2 says "adding to ARO"), so TPR is an integer | same |
| 6 | PTP at 100% | Use the full 8.1.2 scale ("no ±400 cut", PD = 1.00 only above 735), so 100% gives the highest opponent rating + 736. Only the zero score is special-cased (lowest − 800). Same 1/½/0 scale as TPR | same |
| 7 | Team PAB in BC/TBR/BBE | As art. 12 says: a PAB counts as a standard win on every board, even though the GP credited in the Score is the system's PAB value (C.04.6 1.4). A forfeited match counts as standard wins or losses on every board | same |
| 8 | Team rating tie-breaks | Not offered for teams. Art. 10 is "for Individuals" in 2026, and C.07 defines no team rating. A Tie-break List with ARO/TPR/PTP/APRO/APPO/RTNG in a team competition fails in `Tournament.of`. A team event that wants one declares it as a self-defined `OTHER_*` tie-break (4.1). Tie-break Edition 2024-08 follows the same rule, for one consistent behaviour | Gacrux reads a team rating field that TRF never fills, so in practice it gives no value either |
| 9 | "Number of rounds" in 16.4.2 | The rounds recorded in the snapshot the Standings are computed on. For final standings this is the scheduled number of rounds. Mid-tournament standings treat the snapshot as the tournament so far; otherwise the cap could never bind before the last round | same |
| 10 | DE 6.3 | A participant is ranked first when its lowest possible DE score is higher than every other tied participant's highest possible score. Every missing mutual encounter, including a forfeit excluded under 6.1.1, may take any result, and repeated encounters are averaged per 6.1.2, hypothetical ones included. Repeat for the next place. At the first place that cannot be decided, art. 6 is reapplied to the participants still tied, as a new subgroup (4.2), until nothing more resolves | **Known Divergence (Gacrux bug)**: Gacrux resets its encounter counts on a repeat meeting (`tiebreak.py:841,843`), so averages of three or more meetings, and repeat pairings in Swiss, come out wrong |
| 11 | REP after withdrawal | 16.1.1 defines a C.07 term ("requested bye"), not a BH-only rule, so every round after a withdrawal is a zero-point bye and is subtracted. A double forfeit is a forfeit loss for both sides and is subtracted too. "Number of rounds" follows #9 | same |
| 12 | Acceleration | Virtual points never enter a tie-break. The model already enforces this: tie-breaks see `Score`, never `PairingScore` (Shared domain model across all systems) | same |

**Two points C.07 never names** (the result table in the research):
- **Double forfeit**: a forfeit loss (category .4, a VUR) for both participants. Its 16.4.1 dummy is capped at the scheduled opponent's adjusted score, which is 0 points gained in that round.
- **"Less than one move"** (TRF `W`/`D`/`L`): a game played over the board for every tie-break, rating ones included. It is paired, has a result, and is not a forfeit, so it is not an unplayed round under 15.1. Gacrux agrees.

**Configuration** (C.07 gives these to the organiser, and they are set in `TournamentSettings` or a Profile):
- The **16.6 overrides**: a custom `UnplayedRoundPolicy` replacing 16.3, 16.4 or 16.5. The default is the one the Tie-break Edition defines.
- **Unrated players** (art. 10 preamble). The default drops rating tie-breaks from the list when any participant is unrated. The alternative is a declared rating for unrated players.
- **Forfeits in DE** (6.1.1) via `/P`, and the **TPN/RTNG reverse order**.

**Consequences.**
- C.07 has no Interpretations, so the Tie-break Edition alone fixes every value.
- Every ruling here is listed in the functional README next to the C.07 entries, each with its article.
- The DE averaging entry goes into the Known Divergence register when the Gacrux Witness runs are wired up.
- The glossary gained Unplayed Round, Voluntary Unplayed Round, Adjusted Score and Dummy Opponent.
- No new tickets. The build implements the tie-break module as designed in Shared domain model across all systems.
