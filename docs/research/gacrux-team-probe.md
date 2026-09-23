# Gacrux on Swiss Team (C.04.6) edge cases: probe results

Ticket: wayfinder "Probe Gacrux on Swiss Team edge cases" (AFK).
Probed 2026-09-23. Background and article numbering: [swiss-team-system.md](swiss-team-system.md) (ambiguities A1–A13, G1). TRF columns: [trf-format.md](trf-format.md).

Each case below is a small hand-crafted TRF-2026 team tournament built to isolate **one** ambiguity. For each case the doc gives: the input file, the exact command, the observed output, the reading Gacrux follows (with the source line that decides it), and a verdict on whether that reading is a bug or a legitimate reading of the article.

**Labels.** **Observed** means Gacrux was run and printed this. **Observed (counterfactual)** means Gacrux was run with exactly one decision point replaced in memory by the competing reading (`alt.py`). **Derived** means worked out by hand from the article and the case's history, not run.

## Verdicts

| # | Ambiguity | Gacrux reading (observed) | Competing reading gives | Verdict |
|---|---|---|---|---|
| 1 | Bracket seat order (3.6.1), G1 | seats by (score, TPN); pairs **1-4, 2-3** | TPN only: **1-3, 2-4** (counterfactual) | **Bug.** It contradicts 3.6.1 and Gacrux's own docstring. Report upstream |
| 2 | [C6] pass/fail vs graded (A6) | pass/fail at the parity minimum; all sets fail, so the first set wins: **1-2** | graded: **1-3**, after which the next bracket needs 2 upfloaters instead of 4 (counterfactual) | Legitimate but disputed reading. **Ask TEC** |
| 3 | "complies with" when [C7] > 0 (A7) | minimises [C7]: set {3,5} (C7 = 1) | zero-or-first: set {3,4} (C7 = 2) (counterfactual) | Legitimate, and the better-supported reading (art. 2.3 "comply as much as possible"). Ask TEC to confirm |
| 4a | forfeit counts as "played" for [C1]? (A3) | no: the double-forfeit pair 1-2 is **paired again** in round 2 | yes: 1-2 would be illegal (derived) | Legitimate; GHR 3.5 supports it |
| 4b | forfeit counts in 3.4.3 "most matches played"? (A9) | no: PAB to **team 2** (1 played) over team 4 (0 played, 1 forfeit loss) | yes: tie at 1, so 3.4.4 gives it to **team 4** (derived) | Legitimate; "played" and 1.6.1 "actually played" support it |
| 5 | Type B, last round, CD 0 with last two WW (A1) | **strong** Black: 2-1 (4.3.4) | no preference: **1-2** (4.3.2) (counterfactual) | Legitimate but the text literally matches both. **Ask TEC** |
| 6 | 4.3.6: compressed-from-end vs round-by-round (A12) | compressed: 4.3.6 finds no difference, so 4.3.8 gives **1-2** (team 1 White) | round-by-round: 4.3.6 decides at round 3, so **2-1** (derived) | Legitimate; GHR 3.4's own example supports it |
| 7 | floater under acceleration: pairing vs real score (A8) | pairing score of that round: 2 and 5 are not floaters, so **1-2** | real score: 2 and 5 are floaters, so [C7] picks **1-6** (derived) | Legitimate (the Dutch precedent) but not stated. **Ask TEC** |

Side findings (all observed): three robustness bugs in Gacrux's CLI and TRF reader, and one hidden precondition that matters for fixing G1. See [Side findings](#side-findings).

---

## Setup

- **Source.** https://github.com/OttoMilvang/TieBreakServer (the Gacrux engine; MIT, "Copyright (c) 2024 FIDE"). Cloned at commit `6419149ede24fa76639a956b1a52ccac0ded730d` (2026-09-19), version `1.10.62` (`gacrux/version.py`). The repository has no tagged release for the team module, so the default branch head is the primary source.
- **Build.** Python 3.14.4, a venv with `pip install -r requirements.txt` (networkx 3.7). Gacrux's own team tests pass: `pytest tests/test_pairing_fideteam.py`, 101 passed.
- **Inputs.** Everything is under [`gacrux-team-probe/`](gacrux-team-probe/):
  - `gen.py` writes the `case*.trf` files. Every team has 2 boards: team T has players 2T-1 (board 1) and 2T (board 2). Board colours are the default WBWB, and the team colour is the board-1 colour (1.6.1). Records used: 012, 062, 072, 082, 142, 152, 192, 162, 362, 001, 310, plus 250 and 330 where needed.
  - `probe.py` runs the same path as `pairingchecker.py -i FILE -c -p -n ROUND` (check mode plus pairing, which keeps the bracket structure). It prints per team: pairing score, matches played (`num`), CD, colour sequence, preference (`w2`/`b2` = simple or strong, `w1`/`b1` = mild), previous-round float and history. Per bracket it prints the seat order, upfloaters, quality vector, and each pair with the colour rule that decided it. It exists because the CLI's own detail report crashes on odd-team rounds (side finding S1).
  - `alt.py PATCH FILE ROUND` reruns `probe.py` with one Gacrux method replaced in memory. Gacrux is never modified on disk.
  - `*.out` holds the captured output of every command below.
- **Commands** (from `gacrux/gacrux/` of the clone):
  - plain pairing: `python pairingchecker.py -i <case>.trf -p -n <round> -dT`
  - detail: `GACRUX=<clone> python probe.py <case>.trf <round>`
  - counterfactual: `GACRUX=<clone> python alt.py <patch> <case>.trf <round>`

In all cases `W - B` means White team – Black team.

---

## Case 1: bracket seat order (G1)

**Article.** 3.6.1: "For each pair, the team with the smaller TPN is the top member of the pair; the team with the larger TPN is the bottom member of the pair." 3.6.2: "A pairing is identified by the TPNs of the top members of each pair sorted in ascending order, followed by the TPNs of the bottom member of the corresponding pair." 3.6.3: sort by lexicographic order.

**Input** `case1-bracket-order.trf` (8 teams, 5 rounds, Type A). Round 1: 1-5 1 wins, 2-6 2 wins, 3-7 draw, 4-8 4 wins. Pair round 2. The top scoregroup {1, 2, 4} (2 MP) is odd. [C5] and 3.5.4 take upfloater {3} (1 MP), whose TPN is **smaller** than resident 4's. Nobody has a colour preference (Type A, one match each), and nobody floated in round 1.

**Article pairing (derived).** The legal pairings of {1,2,3,4} are 1-2/3-4 (id `1 3 2 4`), 1-3/2-4 (id `1 2 3 4`) and 1-4/2-3 (id `1 2 4 3`). The first is `1 2 3 4`, so **1-3, 2-4**.

**Observed.**
```
$ python pairingchecker.py -i case1-bracket-order.trf -p -n 2 -dT
4
4 1
3 2
7 5
6 8
-- bracket scorelevel 3: bsn order [1, 2, 4, 3], upfloaters [3]
     4 -   1   (colour rule 4.3.8)
     3 -   2   (colour rule 4.3.8)
```
Gacrux pairs **1-4, 2-3**.

**Observed (counterfactual)** `alt.py tpn-order`: seat numbers by TPN only, gives `3 - 1`, `4 - 2`, i.e. **1-3, 2-4**, the article's pairing. The rest of the round is unchanged. (The `bsn order` line in that output still shows `sort_nodes`' order, because the patch changes only the seats used in `update_bracket`.)

**Deciding source.**
- `gacrux/pairingfideteam.py:289-290`: `sort_nodes` returns `sorted(nodes, key=lambda node: (-node["scorelevel"], node[self.rank]))`.
- `gacrux/crosstablefideteam.py:248`: `update_bracket` takes seat numbers from that order (`self.bsn = bsn = {node["cid"]: i + 1 ...}`). The weights at lines 259-266 then make "smaller bsn = top member".
- The docstring at `crosstablefideteam.py:234-235` says "bsn is the position of a team in the bracket, the teams taken in TPN order", so the code contradicts its own stated intent.
- Gacrux's test `test_art_3_6_2_top_and_bottom_members` only covers round 1, where there are no upfloaters. That is why the tests don't catch this.

**Verdict: bug.** When an upfloater's TPN is smaller than a resident's, Gacrux makes the resident the top member, and 3.6.1 does not allow that. It can happen whenever TPN order and score order disagree, which is common from round 2 on. The fix belongs in `update_bracket` (seat by TPN), not in `sort_nodes`: see S3.

---

## Case 2: [C6], pass/fail vs graded (A6)

**Article.** 2.3.3 [C6]: "Unless all the teams in the following scoregroup became or are upfloaters (thus this scoregroup is now empty), choose the set of upfloaters so that criteria [C1], [C3] and [C4] [...] are complied with in the bracket where this (not empty) scoregroup is paired."

**Input** `case2-c6-graded.trf` (10 teams, 5 rounds, Type A). Pair round 4. Round 4 is one of the last two rounds, so [C7]/[C10] are off and only [C6] separates the candidate sets.
- Team 1 has 6 MP and is alone in the top scoregroup.
- The following scoregroup F = {2, 3, 4, 5, 6} has 3 MP. Teams 3, 4, 5 and 6 have **all met each other** (all draws). Team 2 has met only 8, 10 and 9.
- The lower teams are 7 (0 MP) and 8, 9, 10 (1 MP). Absences were used to keep their scores low.

Every candidate {x} with x in F is legal and passes [C3]. What is left of F has 4 teams, so its parity minimum is 0 upfloaters:
- x = 2 leaves {3,4,5,6}, which have all met. That bracket needs **4** upfloaters.
- x = 3, 4, 5 or 6 leaves team 2 plus three teams that have all met each other. That bracket needs **2** upfloaters.

No set reaches the parity minimum.

**Observed.**
```
$ python pairingchecker.py -i case2-c6-graded.trf -p -n 4 -dT   ->  2-1, 9-3, 10-4, 5-8, 6-7
-- bracket scorelevel 4: bsn order [1, 2], upfloaters [2]
   quality {... 'QC6': 1, ...}
-- bracket scorelevel 3: bsn order [3, 4, 5, 6, 8, 9, 10, 7], upfloaters [8, 9, 10, 7]
```
Gacrux takes {2}. [C6] fails (`QC6: 1`), and the next bracket pulls up 4 teams.

**Observed (counterfactual)** `alt.py c6-graded`: the key becomes (upfloaters the following scoregroup needs, C7, index). It gives `chosen key = (2, 0, 1)`, i.e. set {3}. The pairing is 3-1, 6-2, 9-4, 5-8, 10-7, and the next bracket pulls up only 2 teams.

**Deciding source.**
- `gacrux/pairingfideteam.py:427-443` `check_c6`: F's remainder must be pairable with `len(following) % 2` upfloaters, a Boolean.
- `pairingfideteam.py:353`: `key = (0 if c6 else 1, c7, index)`. When every set fails, the lexicographic index decides.
- The docstring (`:420-424`) argues that any other reading "would leave [C6] with no content at all".

**Verdict: a legitimate reading, but disputed.** "Complied with [C4] in the bracket" can mean "reaches its minimum possible" (Gacrux) or "as few as possible" (graded). The 2024 text was graded: "maximise the number of remaining teams that can be legally paired in the following scoregroup" [T24 3.3.3]. Gacrux's argument that the graded reading adds nothing is **refuted by this case**: the graded reading changes the pairing when no set reaches the parity minimum. **Needs a TEC ruling.**

---

## Case 3: "complies with [C7]" when [C7] cannot reach zero (A7)

**Article.** 3.5.5: "Choose the first set that, together with the top-scoregroup, produces a legal pairing that also complies with criteria [C6] and [C7]". 2.3: "comply as much as possible with the following criteria, given in descending priority". 2.3.4 [C7]: "With the exception of the last two rounds, minimise the number of upfloaters that were floaters in the previous round".

**Input** `case3-c7-nonzero.trf` (10 teams, 6 rounds, Type A). Pair round 3.
- T = {1, 2} (3 MP). They drew each other in round 2, so they need 2 upfloaters.
- F = {3, 4, 5} (2 MP). Teams 3 and 4 floated in round 2: each went from 1 to 2 MP by drawing a 0-MP team. Team 5 did not float (it beat a 0-MP team when both had 0).
- Every set of two passes [C6] and [C3]. Their [C7] values: {3,4} = 2, {3,5} = 1, {4,5} = 1. Zero cannot be reached.

**Observed.**
```
$ python pairingchecker.py -i case3-c7-nonzero.trf -p -n 3 -dT   ->  5-1, 2-3, 4-8, 6-9, 7-10
-- bracket scorelevel 4: bsn order [1, 2, 3, 5], upfloaters [3, 5]   quality {... 'QC7': 1 ...}
```
Gacrux takes {3,5}, the minimum [C7] = 1, and not the first set {3,4}.

**Observed (counterfactual)** `alt.py c7-zero`: [C7] is only "0, or not complied". It gives {3,4} (`QC7: 2`) and the pairing 4-1, 2-3, 8-5, 6-9, 7-10.

**Deciding source.**
- `gacrux/pairingfideteam.py:353-357`: key `(c6fail, c7, index)`, minimised; the loop stops early only at `(0, 0)`.
- **Inferred (not run):** the bracket stage uses the same minimising reading. `crosstablefideteam.py:251-266` weights C8 > C9 > C10 > identifier in one min-weight matching, so a non-zero [C10] is minimised too. It is not "first pairing with zero, else fall back".

**Verdict: legitimate, and the better-supported reading.** "Comply as much as possible [...] in descending priority" makes minimisation the natural reading of "complies". The "first set with zero, else the first set" reading only differs when zero is impossible. Low risk, but it's worth one line of confirmation from TEC, together with A6.

---

## Case 4: forfeits and "played" (A3, A9)

### 4a: [C1]

**Article.** C1 = Basic Rules art. 2: "Two participants shall not play against each other more than once." GHR 3.5: "Two paired participants, who did not play their game or match, may be paired together in a future round."

**Input** `case4a-forfeit-c1.trf` (6 teams). Round 1: 1-2 **double forfeit** (every board `-`/`-`, 0 MP each), 3-4 draw, 5-6 draw. Pair round 2.

**Observed.** `pairingchecker.py -p -n 2` gives `5 3`, `4 6`, **`1 2`**. The probe shows teams 1 and 2 with `num 0`, empty colour sequence and history `-`, and 1-2 decided by rule 4.3.1 (neither team has played a match).

**Derived alternative.** If a forfeit used up the pairing, 1-2 would be illegal. [C3] would then fail for the empty set in the top bracket, and 1 and 2 would have to be pulled into the 1-MP bracket.

**Deciding source.**
- `gacrux/tiebreak.py:690-693`: a match enters the `num` list (the opponents met) only `if comp["played"]`.
- `gacrux/crosstable.py:162`: C1 counts `bhasmet` from that list.
- `gacrux/games2matches.py:469`: a team match is "played" if **any** board was played (`played = played or cgame["played"]`). **Inferred:** a *partially* forfeited match (at least one board played) therefore does use up C1, and gives a colour.

### 4b: art. 3.4.3

**Article.** 3.4: "The pairing-allocated-bye is assigned to the team that: 3.4.1 leaves a legal pairing for all teams 3.4.2 has the lowest score 3.4.3 has played the highest number of matches 3.4.4 has the largest TPN". 1.6.1: a colour only "if the match was actually played".

**Input** `case4b-forfeit-pab.trf` (5 teams). Round 1: 1 beats 2 over the board, 3 beats 4 **by forfeit** (player results `+`/`-`, plus record `330 +-   1   3   4`), and 5 gets the PAB. Pair round 2. The lowest-score teams are 2 and 4 (0 MP each). Team 3 (forfeit win) and team 5 (PAB) are barred by [C2] anyway.

**Observed.** The pairing is `3 1`, `5 4`, **`2 0`**: the PAB goes to **team 2**. The probe shows `num` = 1 for team 2 and 0 for team 4.

**Derived alternative.** If the forfeit counted as a match, 2 and 4 would tie at 1 match, and 3.4.4 (largest TPN) would give the PAB to **team 4**.

**Deciding source.** `gacrux/pairingfideteam.py:197`: `-cmp[edge["cb"]]["num"].get("val", 0)`. `num["val"]` counts only `played` matches with a real opponent (`tiebreak.py:692-693`).

**Verdict (4a and 4b): legitimate readings.** GHR 3.5 (inherited through the Preface) explicitly lets unplayed pairs meet again. "Has played" and 1.6.1's "actually played" point the same way for 3.4.3. The one soft spot is the partial-forfeit case, where one board is played and the rest forfeited. Gacrux treats that match as played. This is reasonable, but C.04.6 doesn't say so. Include it in the TEC question.

---

## Case 5: Type B, last round, CD 0 with the last two White (A1)

**Article.** 1.7.2: "A team has a strong (Type B) colour preference for Black if its CD is more than +1, or, being its CD 0 or +1, the team had White in the last two played matches. [...] A team has a mild (Type B) colour preference for Black if its CD is +1, or, if it is zero and it is not the last round, the team had White in the last played match. A team has no (Type B) colour preference when it has yet to play a match, or when its CD is zero when pairing for the last round."

**Input** `case5-typeb-lastround.trf` (6 teams, 5 rounds, `192 FIDE_TEAM_TYPEB_MP_GP`). Rounds 1-4 are four 1-factors of K6, so round 5 (the last) is forced to 1-2, 3-6, 4-5.
- Team 1: B B W W, so CD 0 and the last two are White.
- Team 2: double forfeit in round 1, then W B W, so CD +1 and a mild Black preference under any reading.

**Observed.** The pairing is `3 6`, **`2 1`**, `4 5`. Team 1 is `b2` (strong Black), team 2 is `b1`. Pair 2-1 is decided by **4.3.4** ("if only one team has a strong colour preference, grant it"), and the bracket reports `QC8: 1`.

**Observed (counterfactual)** `alt.py typeb-none` (CD 0 in the last round gives no preference): team 1 is `nc`, and pair **1-2** is decided by **4.3.2** (grant team 2's only preference). `QC8` becomes 0.

**Deciding source.** `gacrux/crosstablefideteam.py:110-113`: the strong clauses are tested first, with no last-round guard. The last-round guard is applied only to the CD-0 mild clause (`:122`). Gacrux's test `test_art_1_7_2_no_mild_preference_on_zero_in_the_last_round` checks only the mild case.

**Verdict: a legitimate reading, but the text is literally contradictory.** The "no preference when its CD is zero when pairing for the last round" sentence and the strong clause both match this team. Gacrux's reading is that the no-preference sentence only closes the mild CD-0 clause, which has its own "not the last round" condition. That is the more coherent reading: otherwise a CD-0 WW team would have a strong preference in round n-1 and none in round n. It still changes colours, and through C8/C9 it can change pairings. **Ask TEC.**

---

## Case 6: 4.3.6, compressed-from-the-end vs round-by-round (A12)

**Article.** 4.3.6: "Alternate the colours to the most recent time in which one team had White and the other Black. Note: Always consider Article 3.4 of the General Handling Rules for Swiss Tournaments." GHR 3.4: "Only played games or matches count in situations where the colour sequence is meaningful", with the example "BWBuW ... will be treated as if their colour history was uBWBW".

**Input** `case6-433-alignment.trf` (6 teams, 6 rounds, `192 FIDE_TEAM_TYPEA_MP`, so there is no secondary score). Pair round 5.
- Team 1: absent, B, W, B.
- Team 2: B, W, B, absent.
- Both are on 6 MP and are alone in the top scoregroup. Both have CD -1 and no Type A preference. Team 1 is the first-team (4.2: same MP, no secondary score, smaller TPN).

**Observed.** The pairing is **`1 2`**, `5 3`, `6 4`: team 1 White, rule **4.3.8**. Both compressed sequences are `bwb`, so 4.3.6 finds no position where they differ. 4.3.7 does not apply (no preference). 4.3.8 alternates the first-team from its last played round: team 1 had Black in round 4, so it gets White.

**Derived alternative.** Compare round by round: round 4 is skipped (team 2 didn't play), and in round 3 team 1 had W and team 2 had B. 4.3.6 then alternates, which gives **2-1** (team 1 Black).

**Deciding source.** `gacrux/pairingfideteam.py:601-605`: `for i in range(1, min(len(fsq), len(ssq)) + 1)` compares `fsq[-i]` with `ssq[-i]`. `csq` holds played matches only (`tiebreak.py:667-683`). Gacrux's test `test_art_4_3_6_alternate_from_the_most_recent_difference` has no gaps, so it cannot tell the two readings apart.

**Verdict: legitimate, and well supported.** The note sends the reader to GHR 3.4, whose example right-aligns the squeezed history (`uBWBW`). This is exactly what Gacrux does. The round-by-round reading gives "the most recent time" a calendar meaning that the note seems written to exclude. Worth one line in the TEC letter only because the effect is a real colour flip.

---

## Case 7: floater status under acceleration (A8)

**Article.** C.04.6 1.5: "A floater is a team that plays against an opponent with a different score." C.04.7 builds scoregroups on the pairing score, i.e. real points plus virtual points (C.04.7 1.5, paraphrased in [swiss-team-system.md §1.3](swiss-team-system.md)). Neither text says which score 1.5 means for a past round.

**Input** `case7-acc-floater.trf` (10 teams, 7 rounds, `192 FIDE_TEAM_TYPEA_MP_GP_BAKU`, `250  2.0  0.0   1   2    1    4`: TPN 1-4 get +2 virtual MP in rounds 1-2). Pair round 3; there are no virtual points in round 3.
- In round 2, team 2 (real 0, pairing score 2) beat team 5 (real 2, pairing score 2). They are equal on pairing score and different on real score.
- In round 3, team 1 (4 MP) is alone at the top. Its candidate upfloaters, in TPN order, are 2, 5, 6, 7, 8 and 10 (it has met 3 and 4).
- Team 6 played team 7 in round 2 with equal pairing and equal real score (1/1), so it is a non-floater under both readings.
- Every candidate passes [C6] and [C3].

**Observed.** The pairing is **`2 1`**, `3 7`, `5 8`, `10 6`, `9 4`. The probe shows float `-` for teams 2 and 5, i.e. not floaters, so [C7] = 0 for {2}, and the first set wins.

**Derived alternative.** On real scores, 2 and 5 floated in round 2, so [C7] = 1 for {2} and {5}. The first set with [C7] = 0 is {6}, which gives **1-6** (legal: 1 has met only 4 and 3).

**Deciding source.** `gacrux/tiebreak.py:1449-1451` (`compute_flt`, used through `floatrule() == "FLTFT"`, `crosstablefideteam.py:82-83`) compares `acc[rnd - 1]` of both teams. `acc[r]` is the points after round r plus the virtual points of round r+1 (`tiebreak.py:1415-1433`), i.e. the **pairing score with which that round was paired**.

**Verdict: legitimate** (and consistent with how Dutch practice treats acceleration). The text is silent, and the choice changes pairings. **Ask TEC.**

---

## Side findings

All observed on commit 6419149.

- **S1. The CLI detail report crashes on an odd-team round.** `pairingchecker.py -i <file> -c -p -n <r> -dT` fails with "Error 503 Error when writing file: -" whenever the round has a PAB. With `-v` the cause shows: `helpers.py:185 format_pair`, `KeyError: 0` (the bye "competitor" 0 is not in `pcomp`). Seen on the corpus fixture `team_0037`, round 7. Plain `-p` works. `probe.py` exists to work around this.
- **S2. No game-point scale without draws.** A team file with no drawn game (e.g. case 4b before `162` was added) fails with "Program error" 510: `tiebreak.py:548 compute_score`, `KeyError: 'D'`. An explicit `162  W 1.0    D 0.5    L 0.0` fixes it. The scale inference in `scoresystem.py` should fall back to the TRF defaults.
- **S3. Hidden precondition for fixing G1.** `pairing.py:299 get_edges` assumes its node list is sorted by descending scorelevel (`msl = nodes[-1]["scorelevel"] - 1`, then binary search). Changing `sort_nodes` to TPN-only order silently drops edges: bracket {1,2,3,4} in case 1 came back "unpairable", and the upfloater changed to 7. The G1 fix must therefore reseat inside `update_bracket` (`sorted(nodes, key=tpn)`), as `alt.py tpn-order` does.
- **S4. TRF note.** The `362` record must follow the column layout of trf-format.md §3.1: `362 TW 2.0   TD 1.0   TL 0.0`. The loose `362 TW 2 TD 1 TL 0` is rejected ("Error in trf-file").

## For FIDE TEC (secretary.tec@fide.com)

1. **[C6] (2.3.3):** is "[C4] complied with in the bracket where this scoregroup is paired" binary (reach the parity minimum) or graded (as few upfloaters as possible)? Include case 2 as the counter-example: it shows the graded reading changes the pairing when no set reaches the minimum.
2. **Type B, last round (1.7.2):** does "no preference when its CD is zero when pairing for the last round" also cancel the *strong* preference of a CD-0 team whose last two played matches had the same colour? Include case 5.
3. **Acceleration (C.04.7 with C.04.6 1.5, [C7], [C10]):** is a "floater in the previous round" judged on the pairing score of that round (real plus virtual) or on real points? Include case 7.
4. **Forfeits:** confirm that a forfeited or double-forfeited match does not count for [C1] (GHR 3.5) or for 3.4.3's "matches played". Also: does a match with at least one board played count as played?
5. **Minimisation (3.5.5, 3.6.4):** confirm that "complies with [C7]" / "[C8]-[C10]" means "the best value attainable", with the lexicographic order as the final tie-break. Low risk.
6. For information: 4.3.6 alignment. Gacrux right-aligns the squeezed histories, which is what the GHR 3.4 example implies. A one-line confirmation would close A12.

## For Gacrux upstream (Otto Milvang)

G1 (case 1, with the fix location from S3), S1 and S2. All three have small reproducers in this folder.

## Implications for the library

- **Do not use Gacrux as an unconditional oracle for C.04.6.** Any round where an upfloater's TPN is smaller than a resident's may differ because of G1. A cross-check harness must either skip those brackets or run Gacrux with the `tpn-order` patch.
- The interpretation register needs entries for A1, A3, A6, A7, A8, A9 and A12, each defaulting to Gacrux's reading except G1. Each entry should cite the case file here as its regression test. Where TEC has not ruled (A1, A6, A8), make the reading a named, switchable policy.
- The case files are small enough to become JUnit fixtures directly. The expected results are the "article" column of the verdict table (for G1) and the Gacrux column for the rest, until TEC rules.
