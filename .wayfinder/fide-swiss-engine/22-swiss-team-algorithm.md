---
title: Readable Swiss Team pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: markdechamps
blocked_by: [14-swiss-team-rulings]
---

## Question

Can the C.04.6 (2026) procedure be written literally and readably, following the Dutch pattern (a literal generator in handbook order, a criteria vector of per-article failures, a matching oracle for legality, an optimum finder for the target), and still match patched Gacrux (`tpn-order`) exactly with default Interpretations? Prototype the top-down scoregroup procedure: bye first (3.4), upfloater sets (3.5, [C3]–[C7]), first-best bracket pairing in identifier order (3.6, [C8]–[C10]), then colour allocation (4.3). Settle whether `CandidateCriterion`/`Failure` become shared with Dutch.

## Context

From Swiss Team interpretation rulings (fixed readings and three Interpretations: `UpfloaterLookAhead`, `LastRoundZeroCdTypeB`, `FloatScore`) and Readable Dutch pairing algorithm (the pattern). Probe cases in `docs/research/gacrux-team-probe/` are the first fixtures. From Exact optimum finder for Dutch brackets: the Dutch target and pruning come from one round-wide weighted matching, with each criterion declaring how it adds up (`CandidateCriterion.Scope`) and known optimal candidates to skip confirming checks (branch `prototype/dutch-optimum-finder`). Check whether Swiss Team's first-best-in-identifier-order (3.6) fits the same reachability test. Open points: the combinatorial cost of set enumeration (A11) and whether the graded [C6] needs its own look-ahead matching.

## Resolution

Prototyped on 2026-09-23 on branch `prototype/swiss-team-algorithm` (folder `prototypes/swiss-team-algorithm/`; the README maps each class to its article). The author told this session to go ahead on the recommended options and stop only at real blockers, so the decisions below are the recommended ones, for the author to overturn.

**Answer: yes.** C.04.6 written literally (3.4 bye, 3.5 upfloater sets in 3.5.3/3.5.4 order, 3.6 pairings in identifier order, 4.3 as nine rules chained in priority order) makes exactly the pairings of patched Gacrux (`tpn-order`) with the default Interpretations, in every round tried.

- **Set-up.** `gacrux/export.py` runs patched Gacrux on each round and dumps the state the pairing starts from (score, matches played, CD, colour sequence, previous-round float, [C1]/[C2] facts, TPN) with Gacrux's pairing. The Java prototype pairs the round from that state. So this verifies the *procedure* (3.3–4.3); deriving the history from TRF is Shared domain model's and TRF's job, verified when the build feeds the same Oracle end to end. `gacrux/play.py` plays random events of any size with patched Gacrux as the engine.
- **Results.** Pairs, colours and PAB identical to patched Gacrux in **every one of 6,094 rounds**:
  - Gacrux's corpus, all 700 valid team fixtures: 4,204/4,204 rounds (4–19 teams; Type A/B/none, MP or GP primary, Baku acceleration, forfeits, PABs, record 260 prohibitions, 299 assignments). 9,632 brackets had upfloaters, 1,151 more than one.
  - The eight probe cases (`docs/research/gacrux-team-probe/`): 8/8, so they are the first JUnit fixtures.
  - Played events of 16, 21, 30, 41, 60, 61, 81, 101 and 191 teams (4 boards, 7–13 rounds): 1,882/1,882.
  - Our colour preferences equal Gacrux's for every team in every round.
  - Every criterion matters: switching any one off breaks between 1 and 407 corpus rounds ([C7] 317, [C8] 260, [C10] 102, [C6] 81, 4.3.6 407 colours).
- **G1, sized.** Against *unpatched* Gacrux, 293 of 4,204 corpus rounds (7.0%) differ, all from the bracket-seat bug. The Known Divergence is common, and the Oracle must always run with `tpn-order`.
- **Speed** (one core, Java 25). Slowest round: 45 ms at ≤ 19 teams, 0.29 s at 101 teams, 0.89 s at 191 teams (Gacrux: about 1.2 s a round on the same field).

**Decisions (recommended options, taken under the author's go-ahead):**

1. **Upfloater selection (3.5) is one round-wide matching with every criterion as a digit.** The matching covers the residents plus every lower team. [C4] and [C5] (lowest score weighs most) and [C7] count per upfloater. [C6] counts one per edge between a team left in the following scoregroup and a team below it: that sum is the fewest upfloaters the group will need, which is what [C3]+[C4] "in the bracket where this scoregroup is paired" asks. [C3] holds because the matching is perfect. Under the default `PARITY_MINIMUM` reading, if even the optimum misses the parity minimum, every set fails [C6], and a second matching drops the [C6] digit. The literal set generator runs in 3.5.4 order with exact reachability pruning, so the first set reached is the answer. **A11 is closed:** no set enumeration. On an adversarial round (5 residents who have all met, a following group whose teams have all met), the first version, which assessed every legal set, took 4.5 s at 20 teams (15,504 sets) and grows as C(n, 5). The exact version needs 9 matchings (4 ms at 30 teams). The two agree on all 19,538 random rounds of `StressUpfloaters`, under both [C6] readings.
2. **The graded [C6] needs no look-ahead of its own.** It is the same digit, minimised instead of tested against parity. It reproduces `alt.py c6-graded` on probe case 2 (3-1, 6-2, 9-4, 5-8, 10-7), and it never differs from the default on the corpus.
3. **Bracket pairing (3.6) fits the same reachability test.** Pairings are generated in identifier order: each team, in TPN order, is tried as a top member before a bottom member, then each top member takes the smallest-TPN bottom member still possible. [C8], [C9] and [C10] are per-pair digits of one bracket matching, and every branch is checked against it. The first pairing reached is 3.6.4's pairing.
4. **Shared with Dutch: the mechanism, not the criterion interface.** `core`'s internal matching package offers `cheapestPerfect(teams, cost per pair)` plus the lexicographic weight encoding, and both systems use only that. The `Failure` value (a count, or scores in order) is shared too. `CandidateCriterion` and its `Scope` stay per system: Swiss Team has two stages with different assessed objects (a set of upfloaters with [C4]–[C7], a pairing with [C8]–[C10]), and each chooses the first in its own order. Dutch has one candidate vector over [C5]–[C21], and Dutch's scopes (following bracket, PAB score, single downfloater) have no team counterpart. The shared *pattern* (literal generator, per-article criteria as digits, one matching for target and reachability) is documented, not abstracted. This settles the map's "shared abstraction" question.
5. **Correction to Swiss Team interpretation rulings (A6).** The "following scoregroup" is the scoregroup right below the bracket's score in the round's standings, the PAB team included. When all its teams are already paired ("became or are upfloaters"), [C6] holds. The ruling's wording "next non-empty score level among the teams still to be paired" is not Gacrux's reading: it changes 6 of 4,204 corpus rounds. The text ("thus this scoregroup is now empty") supports Gacrux's. The ruling is corrected in its ticket.
6. **Performance budget:** one Swiss Team round with ≤ 200 teams in ≤ 1 s (measured 0.89 s at 191 teams). The time goes into the bracket matchings of big round-1 brackets. The levers, all in the mechanism: skip the matching for branches consistent with the target matching itself (a known optimal pairing, as in Dutch), and fixed-width weights instead of `BigInteger`.
7. **Two notes for open tickets.** Gacrux numbers only teams that are present or were paired before (`crosstable.py`: `if rfp or rip: tpn += 1`), the same effective numbering as bbp v6: evidence for *Pairing numbers of participants not yet paired*. C.04.6 says nothing on board order, and Gacrux orders matches by the pair's higher score, then the sum of scores, then the lower TPN, with the bye last: an input for *TRF CLI surface*. Both notes are added to those tickets.
