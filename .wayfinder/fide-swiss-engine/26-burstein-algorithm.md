---
title: Readable Burstein pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

Can the Burstein System (C.04.4.2, 2026) be written literally and readably, following the pattern of Readable Dutch pairing algorithm and Readable Swiss Team pairing algorithm: a literal generator in handbook order, per-article criteria written as digits of one matching (the shared `cheapestPerfect(teams, cost)` mechanism and `Failure` value, but a criterion interface of its own), exact reachability pruning, known optimal candidates? Prototype the seeding rounds, the index order (Buchholz and Sonneborn-Berger with Burstein's own unplayed-round rules), the "fold" order of candidates, and the rewritten 2026 floater criteria C6/C7. Compare with the text's worked ordering (4.3, minus its duplicate line) and the Witnesses (Vega, chesspairing; bbp's old Burstein only where the 2026 edits do not reach).

## Context

Graduated from the "Algorithm design for each remaining system" fog. Evidence: `docs/research/other-swiss-systems.md` §4 (Burstein changed substantively in 2026) and Implication 6 (its Buchholz/SB is reusable from the C.07 module, but not identical to it). Open point: whether the index is part of the Participant history (Shared domain model) or computed by the system.

From Readable Dubov pairing algorithm: chesspairing's CLI (`pair`, legacy `-p`) drops the last recorded round of every TRF, because its reader sets `CurrentRound` to the rounds played. Its random generator (`generate`) therefore writes tournaments full of rematches, and the Witness must be run through a driver like `prototypes/dubov-algorithm/witness/main.go` (branch `prototype/dubov-algorithm`). It also renumbers TPNs by score every round, and it lets a forfeit win leave the PAB open ([C2] 2026), so expect differences in order and in byes. A Literal Enumerator (plain enumeration in the text's orders) checks the search where no Oracle exists.

## Resolution

Prototyped on 2026-09-23 on branch `prototype/burstein-algorithm` (folder `prototypes/burstein-algorithm/`; the README maps each class to its article). The author asked this session to take the recommended options and stop only at real blockers. There were none, so the decisions below are the recommended ones, for the author to overturn.

**Answer: yes, and more simply than the earlier systems.** C.04.4.2 written literally (1.6 seeding rounds, 1.7–1.8 Index and ranking, 3.1 PAB, 3.2 brackets top-down, Article 4 order, 5.2 as five rules chained in priority order) is readable and exact. Every criterion adds up over the pairs, and so does the 4.3 order itself. So one matching per bracket gives the chosen pairing directly: the criteria as its most significant digits, 4.3 below them. No Oracle exists, so exactness was checked against a plain enumeration of the text.

- **Against plain enumeration.** `LiteralBurstein` enumerates every pairing of every bracket, with every number of virtual players, in 4.3 order. It assesses each by exhaustion ([C4] and [C7] by trying every pairing of the players below), with no matching. The search gives identical pairs, colours and PAB in **every one of 57,519 rounds**, in both search modes (one matching, and a walk of 4.3 option by option). That is 11,594 rounds of played events (6–16 players, seeding rounds paired by bbpPairings v6 `--dutch`, forfeits, absences, withdrawals) and 45,925 synthetic rounds with random histories (scores, colours, opponents met, [C2] facts, Index values with many ties). Where no legal round-pairing exists (1.9.3), both agree that none does.
- **Every criterion matters.** Dropping one digit breaks rounds against the enumeration. On played rounds: [C6] 2,478 of 11,594, [C7] 541, [C8] 3,073. On synthetic rounds: [C6] 3,368 of 45,925, [C7] 987, [C8] 12,243.
- **The text's examples** are fixtures, and all pass. The 4.3 note (six players, two floaters) lists 46 lines. Removing its one duplicated line, "1-3, 2-0, 4-5, 6-0", leaves 45 distinct pairings, exactly the generated order. The other fixtures are 1.6.2's seeding count, the 1.7.2 unplayed-round rules on a hand-built event, the 1.8 note (scores unused) and 3.1.5.
- **Speed** (one core, Java 25). Slowest round: 94 ms at 64 players, 214 ms at 128, **0.84 s at 250**. The walk of 4.3 option by option took 120 ms at 64 and 3.3 s at 250.
- **Witnesses** (300 played events, 10–60 players, 7–11 rounds, 1,542 post-seeding rounds). Neither Witness is **ever better** on any criterion. Criteria are judged the same way for both sides, and they do not depend on the Index.
  - **chesspairing** (`gnutterts/chesspairing` ba9d4f6, Burstein pairer through `witness/main.go`):
    - 78 rounds identical.
    - 5 have the same pairs with other colours, decided at 5.2.5. chesspairing takes the higher-ranked player by score and TPN, not by the 1.8 Index.
    - 21 have another PAB: 16 at 3.1.5 (lowest ranking by TPN, not Index) and 5 at 3.1.4.
    - 790 differ between pairings of equal quality, and in each the prototype's comes first in 4.3 order. chesspairing's Burstein is its Dutch global matching (S1/S2 order) over players renumbered by score, then Buchholz and SB. It has no fold order and no virtual players. Its Index has no self-games and no zero-point-bye exception, and it counts every bye as 1.0.
    - 132 are better in the prototype at the first criterion that differs: [C5] 95, [C6] 14, [C7] 5, [C8] 18.
    - 516 break [C2], because a forfeit win does not block its PAB (the 2026 C2, as in Dubov).
  - **bbpPairings v6 `--burstein`**, "a flawed implementation of a previous version", with its default acceleration switched off by one `XXA` line:
    - 137 rounds identical.
    - 13 have other colours at 5.2.5.
    - 96 have another PAB (3.1.4: 59, 3.1.5: 37).
    - 1,013 differ between pairings of equal quality, and the prototype's comes first in 4.3 order in every one.
    - 280 are better in the prototype: [C5] 187, [C6] 90, [C7] 1, [C8] 2. The old C6/C7 maximised incoming floaters paired rather than minimising outgoing floater scores.
    - 3 are refused as unpairable although a legal pairing exists. This was not investigated, since it is the old edition.
    - bbp's old Index compares players from different scoregroups by score × Buchholz. The 2026 text dropped that, together with the rule that every unplayed game counts as a draw.
  - Nothing here is a Known Divergence, because neither Witness claims 2026 conformance and they never gate.

**Decisions (recommended options, taken under the author's go-ahead):**

1. **A bracket is one matching: criteria, then the 4.3 order.** The matching covers the bracket and every lower player.
   - A pair inside the bracket costs its [C8].
   - A bracket player paired below is an outgoing floater: it costs one [C5] and one [C6] at its score, the highest score weighing most. That is "minimise the scores taken in descending order", given that [C5] fixes how many floaters there are.
   - [C7] is the following bracket's [C5] and [C6]. The following bracket is the outgoing floaters plus the next scoregroup. Each of its players paired below the next scoregroup costs one [C7]-[C5] and one [C7]-[C6] at its score. So a floater that floats through two brackets is counted in both.
   - Two players who are both below the bracket cost nothing, because [C4] only needs them to exist.
   - Below those digits sits 4.3: one digit per BSN, BSN #1 most significant. A pair (i, j) writes n+1−j into position i and n+1−i into position j, and a float (virtual BSN 0) writes n+1. "A pairing precedes another if BSN #1's opponent has a larger BSN, then BSN #2's…" is exactly the smaller sum.
   - The cheapest perfect matching is therefore the first pairing in 4.3 order that best satisfies [C1]–[C8], and 3.2.1's "maximum number of pairs" and 4.2's number of virtual players are read off it.
   - The build writes `precedence` as its own method mirroring the 4.3 sentence. The literal walk is not kept in `main`: it matched in every round but costs about four times as much and breaks the budget at 250 players. The Literal Enumerator keeps the literal order checkable (decision 5).
2. **Seeding rounds are Dutch rounds.**
   - The Burstein system object delegates rounds 1 to min(⌊R/2⌋, 4) whole to the Dutch procedure object of the same Swiss Rules Edition (Dutch 2026 for Burstein 2026, per Historic rule editions), PAB and colours included.
   - R is the planned number of rounds from the settings (`XXR`).
   - After the seeding rounds, Burstein reads only the shared history. Dutch float history plays no part.
   - The research question of a tournament straddling 2026-02-01 does not arise, because the edition is a setting.
3. **The Index is computed, not stored.** This settles the ticket's open point. As Shared domain model already decided, the Index is `Buchholz` then `SonnebornBerger` composed with a Burstein `UnplayedRoundPolicy`, computed per round from the shared history.
   - The prototype shows the policy needs exactly two hooks:
     - *a round the player did not play* counts as a game against themselves, for the points registered and at the player's own current score;
     - *an opponent's score as seen by the player* adds a draw for each zero-point bye in a run that reaches the last round played.
   - The Index reads standings points only (1.7.2 excludes virtual points). It is a ranking, not one of the Standings' tie-breaks, so no modifiers apply.
4. **Fixed readings (none needs an Interpretation):**
   - [C7] means the following bracket at its best on [C5], then [C6], over its pairings that keep [C4]. It looks one bracket ahead only, because [C7] is not among the "[C1] to [C6]" it names.
   - [C8] counts pairs in which both players have a preference for the same colour. A player with no preference (1.5.4) never counts, and strength does not matter ([C3] already forbids the absolute clash).
   - 1.7.2 "does not play in a round" covers forfeits (won or lost), the PAB, and half- and zero-point byes. A forfeit opponent is not an opponent met (Basic and General Handling Rules).
   - 1.7.2's exception covers unpaired rounds scored zero (TRF `Z`, and rounds after a Withdrawal), not forfeit losses. It covers every bye of a run that reaches the last round played, and it applies only to the player's score as their over-the-board opponents see it, never to the player's own Index. Once the player plays again, the run no longer reaches the last round, and those byes count as zero again.
   - 1.8 ranks the whole bracket, incoming floaters included, by Index alone. Scores are unused, so the same order serves 3.1.5, 5.2.1 and 5.2.5.
   - Broken cross-references: 1.7's "2.1.5" means 3.1.5, 1.9.1's "Article 3.1" means 2.1, and 1.9.2's "Article 2.1" means 3.1.
   - 3.1.4 counts games played over the board, as in Dubov.
   - 5.2.1 applies only when both players have yet to play. When only one has played, 5.2.2 grants that player's preference (1.5.4, "the preference of their opponents is granted"). 5.2.4 drops unplayed rounds (GHR 3.4).
   - The 2026 text defines no default acceleration. bbp's default comes from the old text.
5. **A Literal Enumerator for Burstein.** `FoldOrder` (4.3 by enumeration) and `LiteralBurstein` go into test scope, following Readable Dubov pairing algorithm decision 5. They run as a property test on small fields (≤ 16 players) against the matching, and the 4.3 note is a fixture on `FoldOrder`.
6. **Shared mechanism.** Nothing new is needed beyond `cheapestPerfect(players, cost)`, the lexicographic weights and the `Failure` value. The one addition is placing one weight vector above another (the criteria above an order's digits), which belongs in `LexicographicWeights`. Burstein needs no K-of-pool set search. Its criterion interface is its own, as for Swiss Team and Dubov.
7. **Performance budget:** one Burstein round with ≤ 250 players in ≤ 1 s (measured 0.84 s). The margin is thin: the order digits make weights of about n·log₂(n) bits. The fixed-width weights that Exact optimum finder for Dutch brackets already requires before the 50k gate apply here too.
8. **Notes for open tickets.**
   - Readable Double-Swiss pairing algorithm: check whether its identifier order (3.5.4, 3.6.2) also adds up per position. If it does, it too is digits below the criteria, with no walk.
   - The acceleration fog: Burstein's brackets, [C6] and 3.1.3 read the Pairing Score, while its Index uses standings points.
