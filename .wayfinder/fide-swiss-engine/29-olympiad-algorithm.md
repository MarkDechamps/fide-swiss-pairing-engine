---
title: Readable Olympiad pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: markdechamps
blocked_by: [28-lim-algorithm]
---

## Question

Can the Olympiad Pairing Rules (D.02, 2022) be written literally and readably on the parts built for Swiss Team (team history, match points, the 1 MP + 2 GP bye) and Lim (the median-group order of 6.4: top down, then bottom up, the median group last)? Prototype it and record every reading.

## Context

In scope per Olympiad Pairing Rules scope; the `olympiad()` profile is fixed in Tournament profiles. Evidence: `docs/research/other-swiss-systems.md` §9 (a Lim-like median-group order, its own ranking by the average of the top four ratings). No Oracle, and no Witness is known yet.

From Readable Double-Swiss pairing algorithm: the Swiss Team parts are now one Top-Scoregroup Procedure shared with Double-Swiss (3.4 PAB, 3.5 upfloater set, 3.6 Pairing Identifier), with per-system criteria lists, float-lapse rounds, PAB value and colours. Check which of it D.02 can reuse, and whether its median-group order fits that procedure or needs its own.

From Readable Lim pairing algorithm: Lim is a procedure, not an optimisation. A matching only answers reachability there ("can the rest still pair", "how many pairings can this scoregroup keep"). The parts D.02 6.4 may reuse:
- the median-group order (`LimSystem.higherScores`/`lowerScores`, the median last and paired downward, always present even without residents);
- the `Direction` mirror (every upward rule is the downward rule on the reversed pairing numbers);
- the 2.6 cracking of the last pairing made on a side.

See `prototypes/lim-algorithm/` on branch `prototype/lim-algorithm`. The chesspairing Witness driver there shows how to run its pairers on our state.

## Resolution

Prototyped on 2026-09-25 on branch `prototype/olympiad-algorithm` (folder `prototypes/olympiad-algorithm/`; the README maps each class to its article). The author told this session to take the recommended options and stop only at real blockers. There were none, so the decisions below are the recommended ones, for the author to overturn.

**Answer: yes, readably, as its own procedure, built from Lim's parts plus one round-wide question.** D.02 has no quality criteria, no upfloater-set optimisation and no Pairing Identifier, so the Top-Scoregroup Procedure of Swiss Team and Double-Swiss does not fit it. It is Lim-shaped: groups from the top down to the Median Group, from the bottom up to it, and the Median Group last; the mirror between the two sides; and 9.3's order, which is Lim's generalised Article 4 word for word. It differs from Lim where the text differs. The median is found by position, not by score. There is no cracking. Floaters choose their opponent by rank, not by colour. And 8.4 ("another floater will be chosen") needs a look-ahead over the whole round. One heaviest perfect matching answers that question exactly (**Kept Pairings**), so the procedure never has to choose a floater again. The text needs thirteen readings, all fixed and none switchable. With them the procedure is complete: it never blocks a round that has a legal pairing.

- **Played events** (random Olympiads of 4-board teams: 3.1 ranking from five ratings each, board results from ratings, 1 in 100 matches lost by default, absences, withdrawals, 1 in 40 teams entering in round 2 or 3). Every round below is legal: no rematch, no ineligible bye, and 7.3 is broken only in a group where 7.4 disregarded it.
  - 3,000 events of 20–60 teams, 11 rounds: **33,000 rounds**, none blocked. 7.4 applied in 2,944 of them.
  - 200 events of 150–200 teams, 11 rounds (Olympiad size): **2,200 rounds**, none blocked. 7.4 applied in 106.
  - Small fields run out of legal pairings: 337 of 2,000 events of 8–22 teams stopped at a round where **no legal pairing exists at all**. **None** stopped at a round that had one.
- **Against plain enumeration.** Every question the procedure asks of a matching (the bye, 9.3's "subgroup unsolvable", and every Kept Pairings step) was answered a second time by trying every pairing (`LiteralEnumeration`). The results were identical in all **17,394 rounds** (2,000 events of 8–22 teams, 9 rounds).
- **The text's examples are fixtures, and all 16 pass.** They cover the 9.3 table (all fifteen rows in order; its row 4 prints `3 x 5` for `3 x 6`), the 6.4 example with 88 teams, and one fixture each for 8.2.1, 8.2.2, 8.2.3, 8.2.4 reaching the median, 8.3.1, 9.4, 7.2, 7.5.1, 7.6 (both sentences), 4.2, 7.4 and 11.1.
- **6.2 ("as small as possible") holds in practice.** The procedure's sum of matchpoint differences was compared with the smallest any legal pairing of the same teams has. It was larger in 846 of 33,000 rounds of small fields and in **1 of 2,200** Olympiad-sized rounds (by 2 matchpoints). The text's procedure, not the global minimum, is what 6.2 is read through.
- **Every rule matters.** Dropping one rule changes the pairs in this many rounds (of 5,500 played rounds with 20–60 teams / of 550 with 150–200):
  - the floater's opponent of 8.2.1/8.3.1 (lowest or highest ranked): 4,736 / 482;
  - the 7.3 limits: 2,123 / 418, and 462 / 5 more with other colours only;
  - Kept Pairings for the floater's opponent (take the first opponent even if the group loses a pairing): 768 / 25, and 91 / 0 more rounds fail;
  - 8.2.3/8.3.3's preference for a floater with an opponent in the adjacent group: 48 / 1.
  - **The look-ahead itself** (Kept Pairings asked of the group alone): the pairs are never different, but 96 / 2 rounds fail. So the look-ahead is 8.4 made exact, not a new rule.
- **Speed** (one core, Java 25, slowest round): 176 ms at 58 teams, **528 ms at 169 teams** (round 1, one group of everyone).
- **No Oracle, no Witness.** The only open-source "Olympiad" pairer found, `shitijseth/olympiad_tracker`, runs C.04.6, not D.02. Swiss-Manager pairs Olympiads but is closed. So D.02 is verified like Lim: text-traced, fixtures, the Literal Enumerator and the invariant checker.

**Decisions (recommended options, taken under the author's go-ahead):**

1. **The Olympiad is its own procedure object.** The build has `OlympiadSystem` (Article 4 and 6.4), `OlympiadRanking` (3.1, 3.2), `GroupPairing` (7.4, Article 8, 9.4, 9.5), `KeptPairings` (the round-wide question), `Scrutiny` with `ExchangeOrder` (9.1–9.3), `ColourAllocation` (Article 7) and `PublicationOrder` (11.1).
   - From Lim it reuses the side order (`higherScores`/`lowerScores` given the median value), `Direction` and `ExchangeOrder` as they are, and the rule "a group keeps as many pairings as it can, and exactly the rest float". Lim's median definition, Cracking, Floater Types and colour-driven choices do not apply.
   - From Swiss Team it reuses the team history (opponents met, matchpoints) and the configurable bye value. Not the Top-Scoregroup Procedure, not the criteria, not the colour rules.
   - Mechanism: the `core` cardinality matching (the bye, 9.3), and the `core` weighted matching for Kept Pairings (weight 1 for a pairing inside the group, 0 for any other, perfect matchings only). A known solution is reused while it still fits, as in the other systems.
2. **Kept Pairings answers 9.5, 9.4, 8.2.2/8.3.2 and 8.4 as one question.** Among the ways to pair every team not yet paired, take those that keep the most pairings inside the group being paired. Each step of the group's pairing (a floater's opponent, each floater chosen) must still reach that number. A group floats exactly the teams it cannot keep, and no choice ever makes the rest of the round unpairable, so 8.4 never has to undo one.
3. **Fixed readings:**
   - **Ranking (3.1, 3.2).** 3.1 is computed from the ratings of each team's players (a TRF26 `310` team record with the `001` ratings of its players carries them). The comparison is by the exact average of the four highest ratings, then the fifth. An unrated or missing player counts as 0. 3.1.3 is the team name. For later rounds only matchpoints and the initial pairing number count; game points never do.
   - **Numbering.** The initial pairing number is the library's Pairing Number (ADR 0006) with 3.1 as the order, so a Late Entry takes its place in the 3.1 order.
   - **Bye (Article 4).** The lowest ranked eligible team whose bye leaves the others pairable, as in Swiss Team and Double-Swiss 3.4.1. 4.2.2 means a whole match won because the opposing team was not there, not a forfeit on some boards. 4.2.3 means a Late Entry. The bye is worth 1 matchpoint and 2 game points (4.3), whatever the number of boards.
   - **Met (6.1).** Only a played match counts. A match won or lost by default leaves the two free to meet, as an unplayed match leaves no colour (7.7).
   - **Median Group (6.4).** Among the teams to be paired after the bye, in the 3.2 ranking, the group of the team in position n/2 + 1 (the lower of the two middle teams). It always has residents and is paired downward (9.2).
   - **No floater passes the Median Group.** Floaters move towards it and stop in it. 8.2.4/8.3.4's "next group" beyond it is reached through it: when one side cannot reach the median, Kept Pairings makes the other side float a team down to meet it there. The 8.2.4 fixture shows it.
   - **The floater's opponent (8.2.1, 8.3.1).** Incoming floaters are paired first, as they arrive. In the Median Group downfloaters come first, highest ranked first, then upfloaters, lowest ranked first. A downfloater takes the highest ranked team of the group, an upfloater the lowest ranked, residents before other floaters (6.2). "Which it has not already played" is read as *available*: compatible, and the group still reaches its Kept Pairings. A floater with no available opponent moves on (8.2.4, 8.3.4).
   - **The floaters of a group (8.2.2, 8.3.2, 8.2.3, 8.3.3, 9.4).** Each is the first in order whose move still reaches Kept Pairings: above the median the lowest ranked first, below it the highest ranked first. A team that has not played every team of the adjacent group is preferred (8.2.3, 8.3.3).
   - **9.3.** "Against N − 1, N − 2 and so on" means N, N − 1, … 2, as its six-team example shows. "The subgroup is unsolvable" means the rest of the group cannot all be paired. Below the median the same order runs on the mirrored ranking (9.2).
   - **7.3 and 7.4.** The limits are counted on played matches, as 7.7 does for 7.5/7.6, and they are part of compatibility. 7.4 is generalised: the limits are disregarded for a group when, with them, it would keep fewer pairings (float more teams), not only when it would otherwise need no floater at all. The same applies to the Median Group when it cannot be completed within them.
   - **Colours of board 1 (7.5–7.7).** For the pair's higher ranked team, in order:
     1. a colour that breaks 7.3 for either team is not given, when the other colour is possible;
     2. equalisation (7.5.1): with different colour differences, the team with the larger one gets black. We read 7.6's "white the same number of times" as the same colour difference, because unplayed matches make the counts differ;
     3. alternation (7.5.2, 7.6): at the latest round in which both played with different colours, each gets the colour the other had then;
     4. if they never had different colours, the higher ranked team alternates from its last colour, or the lower ranked one if only it has colours;
     5. with no colours at all, 7.2: the higher ranked team gets the colour drawn by lot when its pairing number is odd, the other colour when it is even. In round 1 this is 7.2 as written.
   - **Publication order (11.1).** The `RoundPairing` of an Olympiad round (and so the Pairing Reply) is in the order of 11.1, each key highest first, with the higher ranked team's pairing number last. 11.2 and 11.3 are the arbiter's changes and stay outside the library.
   - **Unfinished games (5.1, 5.2).** No new concept. The caller records an unfinished game as a draw and later enters the real result as a Correction. Recorded rounds are facts (ADR 0004), so the published pairing stands, as 5.2 requires.
   - **Articles 2, 10, 11.2, 11.3** are organiser and arbiter acts (host "B"/"C" teams, who is paired in round 1, dropping a team short of players). They reach the library as Late Entry, Withdrawal or absence. Lineups stay out of scope.
4. **A Literal Enumerator** (`LiteralEnumeration`, which answers both the cardinality and the Kept Pairings questions by trying every pairing) goes into test scope, following Readable Dubov pairing algorithm decision 5. It runs as a property test on fields of ≤ 22 teams and must agree with the matchings everywhere. The text's examples above are fixtures.
5. **Configuration and surface.** D.02 has a single edition (2022, effective 2022-01-01). The system carries it, so no edition setting is added, and the README states it. The `olympiad()` profile sets the bye value (1 MP, 2 GP). There are no Interpretations and no new flags. The provisional `192` code stays `FIDE_OLYMPIAD` (TRF CLI surface).
6. **Performance budget:** one Olympiad round with ≤ 200 teams in ≤ 1 s (528 ms measured).
7. **Nothing new graduates.** D.02 has no acceleration, so the acceleration fog is unchanged.
