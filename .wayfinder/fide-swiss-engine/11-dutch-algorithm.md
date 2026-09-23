---
title: Readable Dutch pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: mark
blocked_by: [02-dutch-system, 09-shared-domain-model]
---

## Question

Can the Dutch System be modelled literally and readably (brackets, transpositions, exchanges, criteria evaluated in handbook order) while still matching the reference programs? Prototype the shape of that model on the handbook's worked examples to react to.

## Context

From Dutch System research: bbpPairings reproduces the Handbook's order of trying candidates via weighted matching plus fixing decisions one at a time; the research suggests implementing the literal procedure with pruning, keeping matching as an optional cross-check.

- From Historic rule editions: the prototype must support Dutch 2026 and Dutch 2017 (with pre-2026 Basic Rules) as two compositions of per-article rule objects over one shared bracket procedure.

## Resolution

Prototyped on 2026-09-23 and reviewed with the author. Prototype: branch `prototype/dutch-algorithm`, folder `prototypes/dutch-algorithm/` (README maps each Handbook article to its class; harnesses `HandbookExamples`, `CompareWithBbp`, `ShowRound`).

**Answer: yes.** C.04.3 (2026) Articles 3–4 can be modelled literally and readably, and the result is identical to bbpPairings v6 wherever the literal search runs to the end.

- **Shape** (about 2,000 lines of throwaway Java): `Bracket` (MDPs + residents, BSN = position), `Transpositions` (4.2, lazily generated in lexicographic order, illegal pairs pruned as whole subtrees), `ResidentExchange.PRIORITY` (the four 4.3.2 rules as one comparator), `MdpSets` (4.4), `Candidate`, and `Dutch2026Criteria` (one method per criterion from [C5] to [C21], in priority order, each returning a `Failure`). The candidate sequence of 3.6/3.7 is one lazy `Stream<Candidate>`. The search accepts the first perfect candidate (3.4), otherwise the strictly best one, so ties go to the earliest (3.8). `ColourAllocation` chains the five 5.2 rules with `Optional.or`. An edition is `DutchEdition` = float rule + criteria list.
- **A matching oracle is needed, but never chooses.** `MatchingOracle` answers only "can these players all still be paired under [C1]–[C3]?". It backs [C4], makes MaxPairs and M1 (3.1) constructive instead of guessed, and computes the perfect bound: the best vector any candidate with a given set of downfloaters could reach.
- **Results** (bbp v6.0.0 generated corpora, forfeits and PABs included):
  - 10–24 players: 500/500 rounds identical; worst round 0.3 s.
  - 30–40 players: 133/135 identical.
  - 55–62 players: 41/55 identical.
  - **Every** difference in all three corpora (2 + 14) is a round in which the literal search hit its 2,000,000-candidate cap. There is no rule difference at all.
  - All the Article 4 worked examples hold. One of them is a slip in the text: the 4.2.2 note lists `6-7-8-10-11` third, but strict lexicographic order (and the stated count of 720) puts `6-7-8-10-9` third. The same slip is in the pre-2026 text.
- **Where the cost is.** It is not in finding legal candidates. It is in recognising the optimum:
  1. When a criterion cannot reach zero (for example, 8 of 14 players prefer White), a zero bound makes nothing "perfect", and the whole sequence is enumerated. A counting bound for [C12]/[C13] (`ColourBounds`) fixed the small corpora.
  2. Even with the right target, the first optimal candidate can sit hundreds of thousands of candidates deep, when it needs a resident exchange (302,525 in one 13-player bracket).

**Decisions (author):**

1. **Algorithm: literal procedure + exact target.** The library keeps the literal candidate sequence as the code that decides the pairing. A matching-based **optimum finder** computes the best reachable criteria vector for the bracket exactly, so "perfect" is always recognised. The search prunes every branch that can no longer reach that vector. Hand-written per-criterion bounds and a candidate cap are prototype stopgaps, not part of the design: a cap that silently returns a non-optimal pairing is not acceptable. The design is the new ticket *Exact optimum finder for Dutch brackets*.
2. **Four documented readings of the 2026 text** (each cited in code and README, and verified against bbp v6):
   - **4.4.1** "a Limbo compliant with [C7]" means the lowest downfloater scores *actually achievable*, not the lowest in the list. An MDP who can meet no resident stays in the Limbo whatever its score.
   - **[C5]** outranks every quality criterion, so the lowest PAB score the round can reach is fixed before the first bracket (as bbp's `byeAssigneeScore`). Every bracket must keep it reachable, even at the cost of more downfloaters. It folds into the completion check.
   - **[C9]** ("brackets that downfloat exactly one player, who will end up receiving the PAB") applies when a candidate downfloats exactly one player and no player below could take the PAB instead. It is not limited to the last bracket.
   - **[C8]** means the most pairs in the following bracket first, then the lowest scores of its downfloaters, looking one bracket ahead ([ANN p.25–26], bbp).
   - Supporting points that also held: a forfeit is not a meeting for [C1] (GHR 3.5); colour preferences use played games only (GHR 3.4); in a heterogeneous bracket, MDPs meet residents only, never each other (3.3); the score difference of a Limbo MDP counts as larger than any resident's ([ANN p.31]).
3. **Dutch 2017 is a separate bracket-procedure object.** 2017 is not just a different criteria list: C.4 applies only in the penultimate bracket, with collapsed scoregroups (PPB/CLB), and PSD replaces [C7]. Transpositions, exchanges, MDP sets, colour allocation and the criteria vector are shared. Each edition composes its own procedure object in its edition factory. This refines Historic rule editions ("one shared bracket procedure" becomes "shared generation-order parts, one procedure per edition"). There is no inheritance chain.

**Pattern for the other systems:** a literal candidate generator in handbook order, a criteria vector of per-article `Failure`s compared in priority order, a matching oracle for legality/completion, and an optimum finder for the target. Whether `CandidateCriterion`/`Failure` become shared across systems is still open until a second system is prototyped (as Shared domain model left it).
