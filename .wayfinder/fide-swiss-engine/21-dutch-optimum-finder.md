---
title: Exact optimum finder for Dutch brackets
labels: [wayfinder:prototype]
status: closed
assignee: mark
blocked_by: [11-dutch-algorithm]
---

## Question

How does the optimum finder compute, for one Dutch bracket, the exact best criteria vector ([C5]–[C21]) that any candidate can reach, and how does the literal candidate search use that vector to prune so that it reaches the first optimal candidate in bounded time? Prototype it on the existing literal prototype and show that it removes every candidate-cap hit in the 30–62-player corpora while staying identical to bbpPairings v6.

## Context

From Readable Dutch pairing algorithm: the literal procedure is decided as the code that picks the pairing, and the matching side only supplies the target. The prototype (branch `prototype/dutch-algorithm`) matched bbp v6 on every round it finished; all 16 misses were 2,000,000-candidate cap hits in brackets of 12–18 players. The two cost drivers are (1) not recognising the optimum when a criterion can't reach zero, and (2) an optimum sitting deep in the sequence (for example behind a resident exchange). Open points:

- Computing the target: weighted matching with the criteria encoded lexicographically (bbp's edge-weight layout in `dutch.cpp` is the reference, Apache-2.0), or a criterion-by-criterion sieve ([ANN p.40]). The target must include the MDP-Pairing/remainder structure of heterogeneous brackets and [C8]'s look-ahead.
- Pruning the literal search: which partial candidates (an S1 prefix of a transposition, an exchange, an MDP set) can be proved unable to reach the target, and how, without changing which candidate is found first.
- Whether the matching code lives in `core` (zero dependencies, so written ourselves) and how it stays readable, given the map's readability-over-performance rule.
- A performance budget for a round (the 50k-tournament release gate from Verification strategy sets the scale).

## Resolution

Prototyped on 2026-09-23 on branch `prototype/dutch-optimum-finder` (folder `prototypes/dutch-algorithm/`, on top of the literal prototype; the README maps each class). The author told this session to go ahead on the recommended options and stop only at real blockers, so the decisions below are the recommended ones, marked as such, for the author to overturn.

**Answer: one round-wide weighted matching gives the exact target, and the same matching, run on a branch, prunes the literal search exactly.** The literal candidate sequence still picks the pairing. There is no candidate cap and no hand-written bound.

- **The target.** A bracket is seen against the whole rest of the round: its players, every player below it, and a PAB seat when the count is odd. A perfect matching of all of them is a candidate for the bracket (its in-bracket pairs, and as downfloaters the bracket players matched outside) that keeps [C4] with [C5] folded in. Every criterion except [C9] adds up over the edges: [C6]/[C7]/[C14]/[C16] per downfloater, [C8] per player the following bracket leaves unpaired (a count, then their scores), and the rest per pair. [C18]/[C20] get a per-downfloater part for Limbo MDPs. Each edge's failures are written as one lexicographic number: a count is one digit, and "scores taken in descending order" are one digit per distinct score, highest first. That is exact because, whenever such a list is compared, every higher criterion is equal, which fixes its length. So the cheapest perfect matching *is* the best candidate. [C5] is the same for every candidate. [C9] (a single downfloater bound to get the PAB) is settled by trying each single downfloater, and only when the cheapest candidate fails [C9]. Each criterion declares how it adds up (`CandidateCriterion.Scope`: count, scores, following bracket, PAB score, single downfloater), so the finder stays edition-neutral. MaxPairs and M1 (3.1) are read off the optimum. The same matching replaces the exponential `MatchingOracle` search, the enumeration of downfloater sets and the [C8] look-ahead enumeration.
- **The pruning.** `canReach(branch)` asks the same matching restricted to a partial transposition (S1 players still to place against S2 players still available), an exchange, or a partial MDP-Pairing plus remainder, with the Limbo bound to float. Because it is exact, the search skips every subtree that cannot reach the target and never skips one that can. The first candidate it reaches is therefore the first perfect one (3.4) and the earliest best one (3.8). `Transpositions` only gained a branch test, and the generation order is untouched. A candidate known to reach the target (a *known optimal candidate*) proves every branch it lies in reachable, so most confirming checks cost nothing.
- **Results** (bbp v6.0.0 corpora, forfeits, PABs, half-point byes and withdrawals included). Pairs are identical to bbp in **every one of 5,364 rounds**:
  - 10–24 players 420/420; 30–40 360/360; 55–62 224/224.
  - Stress (12–62 players, up to 13 rounds, heavy forfeits and HPBs) 1377/1380.
  - Long (14–40 players over ~¾·n rounds, [C1]-saturated) 933/933.
  - Random mix (10–63 players, 5–15 rounds) 1934/1939.
  - Opens (90–222 players) 108/108.
  - The 8 misses are all **round-1 colours only**, in tournaments where someone was absent in round 1. bbp v6 takes 5.2.5's parity from an *effective* pairing number that counts only players already taken into account (`tournament.cpp`, `rankIndex = effectivePairingNumber++`). That is a TPN question, not an optimum-finder one: see the new ticket *Pairing numbers of participants not yet paired*.
  - For comparison, the old capped prototype on the same corpora: 30–40 players 348/360 with 12 cap hits and a 37 s round; 55–62 players 42 cap hits before it was stopped.
  - The blossom port matches exhaustive search on 20,000 random graphs (`MatchingSelfCheck`).
- **Speed** (one core, Java 25). Up to 63 players, the slowest round is 0.25 s and the mean about 30 ms. For 100/150/200-player 9-round opens, a tournament takes 2.8/9.4/22 s and the slowest round 8.9 s (222 players). Cost grows about with n³, and the time goes into the blossom's `BigInteger` arithmetic and weight building.

**Decisions (recommended options, taken under the author's go-ahead):**

1. **Target computation: bbp-style lexicographic weights on one round-wide matching, not a criterion-by-criterion sieve.** A sieve would need one matching per criterion and still needs weights for the score lists. The round-wide form also makes [C4] and [C8] exact without enumerating downfloater sets.
2. **Pruning: exact reachability per branch, plus known optimal candidates.** No per-criterion bounds and no cap. If a bracket's search ever ends without reaching the target, that is a bug, reported loudly ("target unreachable"), never a silent fallback.
3. **The matching code lives in `core`, written by us (zero dependencies), in an internal non-exported package**, shared by every system that needs it. It is a mechanism behind two narrow calls (`cheapest(layout)`, `canPairAll(group)`), tested against brute force. Rule code never sees it, and readability rules apply to the rule side. **Licence:** van Rantwijk's `mwmatching.py` carries *no* licence statement, so it must not be ported. Port from NetworkX's `max_weight_matching` (BSD-3, credited in NOTICE) or write from Galil (1986).
4. **A fifth documented reading, [C18]/[C20]:** a Limbo MDP's score difference is greater than any resident's ([ANN p.31]) *and* grows with its own score (bbp weighs repeated downfloaters by score group). The prototype uses 1000 + score. It was found by the long corpus (t534 round 20) and fixed there with no other change.
5. **Performance budget.** One round with ≤ 64 participants: ≤ 1 s (measured 0.25 s). One round with ≤ 250: ≤ 10 s (measured 8.9 s). Release gate: 50k tournaments (bbp's ranges, 15–215 players, 5–15 rounds) within one night on the CI runner, in parallel. At today's cost that is about 120 CPU-hours, so the gate will need the first lever. The levers, in order, all confined to the matching mechanism: fixed-width multi-word weights instead of `BigInteger`; per-digit bases sized to the bracket; reusing the previous duals between sibling branches (warm start). Readability over performance still holds for the rule code.
6. **Known risk, not observed:** `ResidentExchange.inOrder` sorts all exchanges of one size at once. No bracket needed more than a few exchanges, but the build should generate them lazily in 4.3.2 order.

**Pattern for the other systems** (updates Readable Dutch pairing algorithm): the literal generator, per-article `Failure`s that each declare how they add up, one round-wide matching for target and reachability, and known optimal candidates. Whether `CandidateCriterion`/`Failure`/`Scope` are shared across systems is still settled with the second system.
