---
title: Readable Dubov pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

Can the Dubov System (C.04.4.1, 2026) be written literally and readably, following the pattern of Readable Dutch pairing algorithm and Readable Swiss Team pairing algorithm: a literal generator in handbook order, per-article criteria written as digits of one matching (the shared `cheapestPerfect(teams, cost)` mechanism and `Failure` value, but a criterion interface of its own), exact reachability pruning, known optimal candidates? Prototype the bye, the colour split into G1/G2, the G2 order and permutations (ARO, rating), the upfloater sets and the criteria. Show that every criterion either adds up over pairs or is settled with its own matching, and compare the result with the Witnesses (Vega, `gnutterts/chesspairing`) and the worked examples of the text.

## Context

Graduated from the "Algorithm design for each remaining system" fog once Readable Swiss Team pairing algorithm settled the shared parts. Evidence: `docs/research/other-swiss-systems.md` §3 (2026 edits: a wider C2, a reworded upfloater-set order) and Implications 1 and 4 (Dubov's G2 permutation is factorial when enumerated, so the reachability pruning is what keeps it bounded). There is no Oracle, so Verification strategy's per-system definition of done applies: trace every rule to its article, take the text's examples as fixtures, review Witness differences.

## Resolution

Prototyped on 2026-09-23 on branch `prototype/dubov-algorithm` (folder `prototypes/dubov-algorithm/`; the README maps each class to its article). The author asked this session to take the recommended options and stop only at real blockers. There were none, so the decisions below are the recommended ones, for the author to overturn.

**Answer: yes.** C.04.4.1 written literally (3.1 PAB, 3.2.2 upfloater sets in 4.2 order, the two 3.2.4 shifts in 4.3 order, T2 in 4.4 order, 5.2 as five rules chained in priority order) is readable and exact. Every criterion either adds up over pairs or follows from one that does. No Oracle exists, so exactness was checked against a plain enumeration of the text.

- **Against plain enumeration.** `LiteralDubov` enumerates every set of upfloaters, every set of shifters and every transposition in the text's order, and assesses each by exhaustion, with no matching. The search gives identical pairs, colours and PAB in **every one of 67,385 rounds**: 9,481 rounds of played events (6–16 players, forfeits, absences) and 57,904 synthetic rounds with random histories (scores, colours, opponents met, upfloat counts up to 9, [C2] and [C10] facts, last rounds). Where no legal round-pairing exists (1.9.3), both agree that none does.
- **Every criterion matters.** Dropping one digit from the search breaks rounds against the enumeration: [C6] 1,915 of 7,357 played rounds, [C7] 1,358, [C10] 243, and [C9] 295 of 19,321 synthetic rounds. [C8] only matters in constrained cases (see reading 3), so a hand-made fixture covers it.
- **The text's examples** are fixtures, and all pass: the 4.3.3 middle-out numbering (A–G → D, C, E, B, F, A, G), the 4.4.2 transposition order, the 4.1.3 set order, MaxT (1.8.2) and ARO rounding (1.7.1).
- **Speed** (one core, Java 25). Slowest round: 37 ms at ≤ 60 players, 74 ms at 64, 150 ms at 128, **0.40 s at 250**.
- **Witness: chesspairing** (`gnutterts/chesspairing` ba9d4f6, Dubov pairer through `witness/main.go`): 2,348 rounds (300 played events, 10–60 players, 5–11 rounds). The Witness is **never better** on any criterion.
  - 238 rounds identical.
  - 110 have the same pairs with other colours, all decided by 5.2.1: chesspairing alternates round-1 colours by board number, not by the TPN's parity.
  - 867 differ between pairings of equal quality: 415 took other upfloaters, 452 paired the same bracket otherwise. Causes in its code: it renumbers TPNs by score every round, shifts the last players of the sorted list instead of numbering from the middle, has no first-instance shift, caps G2 at 120 transpositions, uses an unrounded ARO, and counts the rating of an absent opponent as 0.
  - 891 are better in the prototype at the first criterion that differs: [C5] 53, [C6] 629, [C7] 174, [C8] 1, [C10] 34. chesspairing has no upfloater selection: it floats unpaired players *down* into the next bracket, as Dutch does, and it gives players who have not played no colour preference (1.6.4 gives them a mild preference for Black).
  - 229 rounds leave players unpaired, or pair a player twice or with themselves; 7 fail; 6 break [C2], because a forfeit win does not block its PAB (the 2026 C2).
  - Vega was not run: its site was down, and it is a GUI program with no scriptable Dubov engine.
  - Nothing here is a Known Divergence, because the Witness does not claim 2026 conformance and never gates.

**Decisions (recommended options, taken under the author's go-ahead):**

1. **Upfloater selection (3.2.1–3.2.2) is one round-wide matching with every criterion as a digit.** The matching covers the remaining residents plus every lower player. A resident paired with a lower player costs [C5], [C6] at the upfloater's score (the lowest score weighs most, which is "maximise the scores taken in ascending order"), [C7], and that upfloater's [C8], [C9] and [C10]. Two residents cost only [C7]. Two lower players cost nothing, because [C4] only needs them to exist. So 3.2.1's minimum and 4.2.3's "number of upfloaters and their scores in valid sets" are the target's profile. The sets are generated in 4.2 order, and exact reachability pruning makes the first set reached the answer. Every legal round-pairing splits top-down into Dubov brackets, so the search reports 1.9.3 exactly when no legal round-pairing exists at all.
2. **[C7] needs no search of its own, and 3.2.4 is the same matching.** A pairing that 3.2.6 can produce always pairs G1 against G2. So a pair of two players from the same initial subgroup needs exactly one of them shifted, and the fewest such pairs is the best [C7]. The pairs that "must unavoidably be paired together" are the fewest same-subgroup pairs inside the smaller subgroup. The equalising shift follows from them: (|larger| − |smaller|)/2 + k players. Each shift is a set generated in 4.3 order, pruned by a matching in which a same-subgroup pair holds exactly one shifted player and a pair across the subgroups holds none. With those shifts, *every* legal transposition has the fewest same-colour pairs. So 3.2.6's "first legal" never loses [C7], and the bracket's [C7] always equals the upfloater stage's target (checked in every round). The research question, whether [C7]–[C10] are judged on the whole bracket pairing, is answered: yes, and that pairing is the bracket's best one.
3. **Fixed readings (none needs an Interpretation):**
   - "Upfloated" (1.8, [C8]–[C10]) means paired with an opponent who had a higher score when the round was paired, whether or not the game was then played. A PAB or other bye is not an upfloat. This is the same as "was an upfloater of a bracket", because at the minimum [C5] two upfloaters never meet each other. Under acceleration, the score is the Pairing Score (see the acceleration fog).
   - [C9] adds up the upfloats of the maximum upfloaters in the set; reading it as a count would only repeat [C8]. Under this reading [C8] decides only when the constraints forbid the obvious swap, for example {a player upfloated 9 times, a non-maximum player} against {two players upfloated 3 times each}. That case is a fixture.
   - 4.4.1's "Article 2.2.4" means 3.2.4, a typo.
   - The shifter pools: the first shift takes from the whole smaller subgroup. The equalising shift takes only from the larger subgroup's own players, since shifting a player back cannot keep the fewest same-subgroup pairs. In a bracket where nobody has played, the TPN halves get the 4.3.2 order of the colour their subgroup stands for. Every ARO is 0 there, so both orders come down to TPN.
   - ARO is an integer, rounded half up (1.7.1), and S1 (3.2.5) and 4.3.2 use the rounded value.
   - 3.1.4 "played the highest number of games" counts games played over the board. 5.2.4 compares the colour sequences of games played, from the latest back, with unplayed rounds dropped, as Basic and General Handling Rules decided.
   - Rnds in MaxT (1.8.2) is the planned number of rounds from the settings. TRF carries it as `XXR`. chesspairing uses the rounds played so far instead.
   - 1.2.2 (recalculate the TPNs when a rating changes before round 4) is a Chief Arbiter correction under GHR 4.3 that the library receives as input, not a step of the pairing.
4. **Shared mechanism.** Besides `cheapestPerfect(players, cost)`, the lexicographic weights and the `Failure` value, one more piece is pure mechanism: choosing a set of K from a pool in 4.1.3 order ("smallest differing sequence number") with a reachability test on each branch. Dubov's upfloater stage and both shifts use it, and so does Swiss Team 3.5.4. The prototype's code for it is a copy of Swiss Team's. It moves into `core`'s internal matching package. The sequence order (a comparator, or 4.3.3's middle-out numbering) and the reachability test stay with each system. Dubov's criterion interface is its own, as with Swiss Team.
5. **A Literal Enumerator is part of verifying every system without an Oracle.** `LiteralDubov` found no difference, but it is what makes "exact" checkable when no endorsed program exists. The build keeps one per such system in test scope: plain enumeration in the text's orders, run as a property test on small fields (≤ 16 players) against the search. It sits with the handbook fixtures and the Witness review in Verification strategy's per-system definition of done.
6. **Performance budget:** one Dubov round with ≤ 250 players in ≤ 1 s (measured 0.40 s).
7. **Notes for open tickets.** chesspairing is also the Witness for Burstein, Double-Swiss and Lim. Its CLI drops the last recorded round of every TRF, because `ToTournamentState` sets `CurrentRound` to the rounds played. Its random generator therefore writes tournaments full of rematches. It also renumbers TPNs by score every round. The driver `prototypes/dubov-algorithm/witness/main.go` works around the first fault. These notes are added to those three tickets.
