---
title: Readable Double-Swiss pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

Can the Double-Swiss System (C.04.5, 2026) be written literally and readably, following the pattern of Readable Dutch pairing algorithm and Readable Swiss Team pairing algorithm: a literal generator in handbook order, per-article criteria written as digits of one matching (the shared `cheapestPerfect(teams, cost)` mechanism and `Failure` value, but a criterion interface of its own), exact reachability pruning, known optimal candidates? Prototype its bracket procedure with its lexicographic identifiers (3.5.4, 3.6.2), the PAB of 1.5, and the two-game match with per-game results. Check whether its identifier order is the same "first in identifier order" search as Swiss Team 3.6, so that the two could share the generator. Compare with chesspairing, the only Witness.

## Context

Graduated from the "Algorithm design for each remaining system" fog. Evidence: `docs/research/other-swiss-systems.md` §6 (a new system in 2026, no colour criterion in the pairing) and Implication 6 (sub-round games). Swiss Team's 3.6 is structurally close (Readable Swiss Team pairing algorithm, decision 3). The Double-Swiss virtual-point value under acceleration stays in the acceleration fog.

From Readable Dubov pairing algorithm: chesspairing's CLI (`pair`, legacy `-p`) drops the last recorded round of every TRF, because its reader sets `CurrentRound` to the rounds played. Its random generator (`generate`) therefore writes tournaments full of rematches, and the Witness must be run through a driver like `prototypes/dubov-algorithm/witness/main.go` (branch `prototype/dubov-algorithm`). It also renumbers TPNs by score every round, and it lets a forfeit win leave the PAB open ([C2] 2026), so expect differences in order and in byes. A Literal Enumerator (plain enumeration in the text's orders) checks the search where no Oracle exists.

From Readable Burstein pairing algorithm: Burstein's 4.3 order compares the pairings position by position (BSN #1's opponent, then BSN #2's), which adds up over pairs. It became one digit per BSN below the criteria, so the whole bracket is one matching and the order is never walked. Check whether the Double-Swiss identifier order (3.5.4, 3.6.2) adds up the same way. bbpPairings v6 `--dutch` pairs a realistic Dutch history quickly through a TRF16 file, if Double-Swiss wants one (see `prototypes/burstein-algorithm/`, class `ExternalPairer`, on branch `prototype/burstein-algorithm`).

## Resolution

Prototyped on 2026-09-24 on branch `prototype/double-swiss-algorithm` (folder `prototypes/double-swiss-algorithm/`; the README maps each class to its article). The author told this session to take the recommended options and stop only at real blockers. There were none, so the decisions below are the recommended ones, for the author to overturn.

**Answer: yes, and it is the Swiss Team procedure.** C.04.5's 3.3–3.6 (PAB first, the top-scoregroup plus the first set of upfloaters in 3.5.4 order, the first Pairing Identifier) are word for word C.04.6's. The quality criteria are a subset: Double-Swiss [C4]–[C7] are Swiss Team's [C4]–[C7], and Double-Swiss [C8] is Swiss Team's [C10]. The differences: Swiss Team's colour criteria [C8]/[C9] do not exist here, the float criteria lapse only in the last round (not the last two), Article 4 is shorter and counts Whites (not the colour difference), and the PAB is worth a game won plus a game drawn. Written literally, the procedure is readable and exact. No Oracle exists, so exactness was checked against a plain enumeration of the text.

- **Against plain enumeration.** `LiteralDoubleSwiss` lists every set of potential upfloaters and every pairing of every bracket, sorts them exactly as 3.5.3/3.5.4 and 3.6.2/3.6.3 say, and assesses each by exhaustion (legality and [C3] by recursion over subsets, [C6] by trying every pairing of the rest), with no matching. The search gives identical pairs, colours and PAB in **every round tried**, in both search modes and under both [C6] readings:
  - played events (6–16 players, 11 rounds planned, two-game matches with per-game results, ½-0 and 0-0 games, single-game and whole-match forfeits, half- and zero-point byes, withdrawals): **55,957 rounds** (25,787 read off one matching, 30,170 by the walk);
  - synthetic rounds with random histories (scores with many ties, opponents met, [C2] facts, previous-round floats, colours, last rounds): **130,000 rounds** (70,000 read off one matching, 60,000 by the walk; 30,000 of them under the graded [C6]), where both agree whenever no legal round-pairing exists (3.3.3).
- **Every criterion matters.** Dropping one digit from the search breaks rounds against the enumeration. On played rounds (about 8,560 each): [C5] 3,136, [C6] 88, [C7] 876, [C8] 207, the 3.5.4 order 116, the 3.6.3 order 2,520. On 5,000 synthetic rounds: [C5] 1,451, [C6] 134, [C7] 317, [C8] 131, 3.5.4 280, 3.6.3 1,693.
- **The text's examples** are fixtures, and both pass. 3.6.2: "11-24 16-6 10-9 8-4" has identifier 4 6 9 11 8 16 10 24. 3.5.4: the nine sets {2,6,1} < … < {6,8,5} come in the text's order, and on a field built to need exactly that profile (three residents who have all met, and a 0-point player who has met 1, 3 and 5, which rules out {2,6,8}) 3.5.5 takes {2,6,1}. There are also fixtures for 3.4, 1.4 and each rule of 4.3.
- **Both orders add up over the pairs.** Among sets that tie on [C4] and [C5] (so they take the same number of players from each score), 3.5.4's order is one membership bit per potential upfloater in 3.5.3 order. The first set is the larger binary number, so a player left out costs its bit. 3.6.3's order is one bit per bottom member (the top members compare first), then the bottom members in the order of their top members, one digit each. Gacrux's C.04.6 module already writes 3.6 exactly this way (`update_bracket`). Fixtures check both encodings against the literal orders (every pairing of 8 and of 10 players; sets of 300 random fields). So "the cheapest perfect matching is the chosen one", as in Burstein. But:
- **Speed** (one core, Java 25, slowest round of played events). The literal walk, with a known optimal candidate: **97 ms at 64 players, 193 ms at 128, 0.59 s at 250**. The read-off of one matching with the order digits: 149 ms, 502 ms and **4.3 s**. The order digits make weights of n + n·log₂(n) bits (about 2,250 bits for a 250-player bracket), and the BigInteger blossom pays for each one. The walk's matchings carry only the criteria.
- **The other [C6] reading** changes 15 of 110,000 synthetic rounds and none of 55,957 played rounds. Why so few: [C4] and [C5] fix how many upfloaters come from the following scoregroup, so the parity of what it will need is the same for every set still in play. Pass/fail and graded therefore agree whenever any set passes, and differ only when every set fails.
- **Witness** (chesspairing `gnutterts/chesspairing` ba9d4f6, its Double-Swiss steps run through `witness/main.go` over our state, since its own state has one game per round). 300 played events, 10–60 players, 7–11 rounds, 2,670 rounds. chesspairing is **never better** on any criterion and never first in either order.
  - 6 identical, and 3 with the same pairs but other colours: its Article 4 is not the text's (a three-in-a-row rule, board alternation in round 1, rank before alternation).
  - 26 have another PAB: it ignores 3.4.1 and 3.4.3.
  - 796 differ between pairings of equal quality, and the prototype's comes first in every one (203 at 3.5.4, 593 at 3.6.3). It takes the lowest-ranked player of each odd group upward one at a time, and its bracket search pairs the first player with its lowest partner, not in identifier order.
  - 393 are better in the prototype: [C5] 350, [C7] 31, [C8] 12. Its "C8" is a colour check, which the text does not have.
  - 1,446 leave players unpaired: it has no [C3] and no look-ahead across brackets, so a bracket it cannot complete is paired partially.
  - It also renumbers TPNs by score every round and scores 1-½-0 per round. Nothing here is a Known Divergence, because Witnesses never gate.

**Decisions (recommended options, taken under the author's go-ahead):**

1. **One shared Top-Scoregroup Procedure for Swiss Team and Double-Swiss.** 3.4 (PAB assignment), 3.5 (upfloater set) and 3.6 (Pairing Identifier) become one procedure object used by both systems. Each system supplies:
   - its set criteria ([C4]–[C7], identical in both),
   - its pair criteria (Swiss Team [C8], [C9], [C10]; Double-Swiss [C8]),
   - the rounds where float criteria lapse (last two; last),
   - its PAB value, its colour allocation and its `Interpretation`s.
   This answers the ticket's question: the identifier order is the same search, so the two systems share the generator. It refines Readable Swiss Team pairing algorithm decision 4, where only the matching mechanism was shared. The per-system criterion lists stay separate objects in each system's package; the procedure holds no system-specific rule.
2. **The search is the literal walk, with a known optimal candidate.** As Readable Swiss Team pairing algorithm decision 3 has it: sets in 3.5.4 order, then identifiers in 3.6.3 order, each option tested by one criteria-only matching against the exact target.
   - New: an option that the last reachable matching already takes needs no matching, the Dutch known-optimal-candidate rule (Exact optimum finder for Dutch brackets). That cut the 250-player round from 1.06 s to 0.59 s and applies to Swiss Team as well.
   - The read-off of one matching with the order digits is exact too, but 7× slower at 250 players. It is not kept in `main`. The equivalence of the digits and the orders is recorded here as a fact (Gacrux's Oracle uses it), and the fixtures above go into test scope.
3. **Fixed readings (identical text keeps Swiss Team's reading):**
   - [C6] is Swiss Team's Interpretation `UpfloaterLookAhead`, with the same default, pass/fail. The Swiss Team default is the Oracle's reading, and one procedure object gives identical text one default. "Following scoregroup" is as corrected in Readable Swiss Team pairing algorithm: the scoregroup right below the bracket's score in the round's standings, the PAB player included. If all its players are already paired, [C6] holds.
   - Quality criteria that say "complies with" mean the best value attainable, in priority order, then the first in the text's order (Swiss Team A7).
   - [C8] "upfloaters' opponents" is the higher-scored player of a pair whose scores differ, as in Gacrux's [C10]. So two upfloaters of different scores count their higher one, and two of the same score count nothing. *Note for the Swiss Team build:* the Swiss Team prototype counted only resident–upfloater pairs. The build follows Gacrux (the Oracle); the prototype's corpus never had the difference.
   - A floater of the previous round (1.5, for [C7] and [C8]) played a match, not forfeited and not a bye, against an opponent whose score before that round differed. This is Gacrux's `FLTFT` for teams. Without acceleration the score is the Pairing Score; under acceleration it follows the acceleration fog.
   - [C1]: a forfeited match (a player forfeited both games) is not a meeting (Preface). [C2] is the text's own list: a PAB, a match won by forfeit (the opponent forfeited both games), or a full-point bye.
   - 3.1.2's "[C.3]" is [C3], and 3.4.1's "a legal pairing for all players" includes [C3].
   - 3.4.3 counts matches not forfeited, byes excluded (Swiss Team A9).
   - 1.6: a player had a colour in a match if at least one game was actually played and they were scheduled White or Black in game 1. 4.3.1's "yet to play a match" means neither player has had a colour. 4.3.2 counts matches with White. 4.3.3 compares played-only histories aligned at their ends (GHR 3.4, Swiss Team A12).
   - The Preface's odd case, each player forfeiting one game, is not a forfeit match: it counts as played and as a meeting, but gives no colour (1.6).
   - The PAB value is configuration, per Basic and General Handling Rules. The default is a game won plus a game drawn (1.5 points).
4. **A Literal Enumerator for the procedure.** `LiteralDoubleSwiss` goes into test scope, following Readable Dubov pairing algorithm decision 5. It runs as a property test on small fields (≤ 16 players) for both systems, with each system's criteria. The 3.5.4 and 3.6.2 examples are fixtures on the shared procedure.
5. **Performance budget:** one Double-Swiss round with ≤ 250 players in ≤ 1 s (measured 0.59 s). The time goes into 250-player round-1 brackets; round 9 of a 250-player event takes about 0.26 s.
6. **Graduated.** TRF26 has one result character per round and cannot carry a two-game match, and nothing on the map decides how the CLI reads and writes one: new ticket *Double-Swiss matches in TRF*. The domain model (`MatchOutcome`) already holds per-game results. The random tournament generator's per-game result model (Random tournament generator) matched what this prototype played.
