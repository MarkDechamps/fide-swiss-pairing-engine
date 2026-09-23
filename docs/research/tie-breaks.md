# Research: FIDE tie-break regulations (C.07)

Status: resolved (research ticket "Tie-break regulations"). Researched 2026-09-23.

## Question

What does the current FIDE tie-break regulation (Handbook C.07) specify: which edition is in force
as of 2026-09, the full list of tie-breaks with exact definitions (acronyms, modifiers such as Cut-1
and Median) for individual and team events, how unplayed games, byes, forfeits and withdrawals are
treated, and are there reference implementations or official worked examples usable as test data?

## Sources

Primary (FIDE Handbook, handbook.fide.com):

- **[C07-2026]** C.07 "Play-Off and Tie-Break Regulations (effective from 1 March 2026)",
  <https://handbook.fide.com/chapter/TieBreakRegulations032026>. Approved by FIDE Council 02/02/2026,
  applied from 1 March 2026. **This is the edition in force as of 2026-09**: it is the newest of the
  five C.07 editions in the Handbook index, and its predecessor is labelled "effective from 1 August
  2024 till 28 February 2026".
- [C07-2024-08] C.07, 1 Aug 2024 to 28 Feb 2026, <https://handbook.fide.com/chapter/TieBreakRegulations082024>
- [C07-2024-04] C.07, 1 Apr 2024 to 31 Jul 2024 (the text approved 01/08/2023: applied from
  1 Sep 2023 for EVE/GSC events and from 1 Apr 2024 for all rated events),
  <https://handbook.fide.com/chapter/TieBreakRegulations042024>
- [C07-pre2023] C.07 in force until 31 Aug 2023, <https://handbook.fide.com/chapter/TieBreakRegulationsPre2023>
- [B02] FIDE Rating Regulations effective from 1 March 2024, art. 8.1 conversion tables,
  <https://handbook.fide.com/chapter/B022024>
- [C0401] C.04.1 Basic rules for Swiss Systems (from 1 Feb 2026), <https://handbook.fide.com/chapter/C0401202507>
- [C0405] C.04.5 Double-Swiss Pairing System, <https://handbook.fide.com/chapter/DoubleSwissSystem202602>
- [C0406] C.04.6 Swiss Team Pairing System (from 1 Feb 2026), <https://handbook.fide.com/chapter/SwissTeamPairingSystem202602>

First-party FIDE but **not** in the Handbook (FIDE Technical Commission, tec.fide.com). Used only
for the questions on test data and software, and labelled as TEC wherever cited:

- [TEC-MTB] "Mandatory Tie-Breaks" (TRF26 attachment),
  <https://tec.fide.com/wp-content/uploads/2025/04/MandatoryTieBreaks-TRF26.pdf>
- [TEC-TRF26] "Format of Tournament Report File, Version 2026",
  <https://tec.fide.com/wp-content/uploads/2025/04/TRF-2026.pdf> (page <https://tec.fide.com/trf-2026/>)
- [TEC-EX] IA Mario Held, "Exercises in Tie-Breaking", rev. 2403220900, written for **C.07-2023**,
  <https://tec.fide.com/wp-content/uploads/2024/04/C.07-2023-Tiebreak-exercises-V01-1.pdf>
  (post: <https://tec.fide.com/2024/03/18/tie-break-exercise/>)
- [TEC-END] FIDE-endorsed programs, <https://tec.fide.com/endorsement/>
- [TEC-SAMPLE] TRF25 sample file "Grandmommy's Cup",
  <https://tec.fide.com/wp-content/uploads/2025/01/GrandMommysCup03_trf.txt>

All article numbers below refer to [C07-2026] unless another source is named.

---

## 1. Edition and structure

- In force: C.07 approved 02/02/2026 and applied from 1 March 2026 [C07-2026 header]. It applies to
  all FIDE-rated competitions (art. 1).
- Articles: 2 ranking of tied participants; 3 play-offs; 4 general tie-break rules; 5 list;
  6 to 10 individual tie-breaks; 11 to 13 team tie-breaks; 14 modifiers; 15 and 16 unplayed rounds.
- **Default when regulations are silent**: use off-the-board tie-breaks (2.2.2), with the list
  completed by the Chief Arbiter (4.1.1) (art. 2.1). This default was added in [C07-2024-08].
- **The tie-break list is an ordered list** chosen by the Chief Organiser from art. 5 or
  "self-defined in the specific regulations of the tournament" (4.1). The Chief Arbiter completes it
  if needed and publishes it before the start (4.1.1).
- **Application (4.2, reworded in 2026)**: rank by the first tie-break, then move to the next
  tie-break "for each subgroup of participants still tied". When the list is exhausted, use drawing
  of lots unless the rules say ties are not broken. (2024-08 said "whenever a persisting tie cannot
  be broken".)
- **Tie-break types (4.3)**:
  - A: based on a subset of games among the tied participants; may appear more than once in the list.
  - B: participant's own results or data.
  - C: opponents' final results, so computable only at the end of the tournament.
  - D: opponents' prior known data (ratings, results of previous rounds), computable once pairings
    are published.
  - Combinations of these also exist (e.g. "BC", "DB").
- **Repeated opponents (4.4)**: every game or match against the same opponent is a separate
  encounter, so that opponent's data counts once per encounter in sums and averages. The exception
  is DE (6.1.2).

## 2. Full list (art. 5 table)

The ● marks tie-breaks flagged in the art. 5 "Cut-1" column.

| Name | Acronym | Type | Art. | Cut-1 |
|---|---|---|---|---|
| Average of Opponents' Buchholz | AOB | CC | 8.2 | |
| Average Perfect [Tournament] Performance of Opponents | APPO | DC | 10.5 | |
| Average [Tournament] Performance Rating of Opponents | APRO | DC | 10.4 | |
| Average Rating of Opponents | ARO | D | 10.1 | ● |
| Buchholz | BH | C | 8.1 | ● |
| Direct Encounter | DE | A | 6 | |
| Fore Buchholz | FB | D | 8.3 | ● |
| Rounds one Elected to Play in | REP | B | 7.6 | |
| Koya System for Round Robin | KS | BC | 9.2 | |
| Number of Games Played with Black | BPG | B | 7.3 | |
| Number of Games Won | WON | B | 7.2 | |
| Number of Games Won with Black | BWG | B | 7.4 | |
| Number of Wins | WIN | B | 7.1 | |
| Perfect Tournament Performance | PTP | DB | 10.3 | |
| Rating | RTNG | B | 10.6 | |
| Sonneborn-Berger | SB | BC | 9.1 | ● |
| Standard Points | STD | B | 7.7 | |
| (Sum of) Progressive Scores | PS | B | 7.5 | ● |
| Tournament Pairing Number | TPN | B | 7.8 | |
| *Team knock-out:* Board Count | BC | B | 12.1 | |
| *Team knock-out:* Bottom Board Elimination | BBE | B | 12.3 | |
| *Team knock-out:* Top Board Results | TBR | B | 12.2 | |
| *Team:* Extended Sonneborn-Berger | ESB (EMMSB/EMGSB/EGMSB/EGGSB) | BC | 13.2 | ● |
| *Team:* Extended Direct Encounter | EDE | A | 13.3 | |
| *Team:* Match Points or Game Points | MPvGP | B | 13.1 | |
| *Team:* Scores and Schedule Strength Combination | SSSC | BC/BD | 13.4 | |

Changes since earlier editions:

- **New in 2026**: STD (7.7), TPN (7.8) and RTNG (10.6).
- **Renamed in 2024-08**: "Games one Elected to Play (GE)" became "Rounds one Elected to Play (REP)"
  ([C07-2024-04] vs [C07-2024-08], 7.6).

## 3. Definitions: individual tie-breaks

### Direct Encounter, DE (art. 6, type A)

- **6.1** Sum each tied participant's scores from games among the tied participants to produce
  separate standings. Two caveats:
  - **6.1.1** Forfeit wins and losses not covered by 15.2 are excluded, unless the tournament
    regulations say otherwise. If included, they count as played games.
  - **6.1.2** If two participants met more than once, the addend is the *average* score of those
    games. This is an explicit exception to 4.4.
- **6.2** If all tied participants have met each other, the separate standings rank them. Any
  sub-ties are resolved by reapplying art. 6 until nothing more resolves.
- **6.3** (Swiss): if not all mutual games were played, but one participant is alone at the top of
  the separate standings *whatever the outcome of the missing games*, that participant ranks first.
  The same then applies to second place, and so on. Art. 6 is then reapplied to the remaining
  participants.

### Type B: own record (art. 7)

- **WIN (7.1)**: number of rounds in which the participant obtained, "with or without playing", the
  points awarded for a win. This includes forfeit wins, the PAB when it is worth a win, and
  full-point byes.
- **WON (7.2)**: games won over the board.
- **BPG (7.3)**: games played over the board with Black.
- **BWG (7.4)**: games won over the board with Black.
- **PS (7.5)**: sum of the participant's cumulative score after each round.
- **REP (7.6)**: number of rounds minus the number of half-point byes, zero-point byes and forfeit
  losses.
- **STD (7.7)**:
  - 1 for each round in which the participant scored more than the scheduled opponent, or obtained
    without playing more than a draw's points;
  - plus ½ for each round in which the participant scored the same as the scheduled opponent, or
    obtained without playing exactly a draw's points.
  - Effect: it normalises non-standard scoring systems (e.g. 3/1/0) back to wins and draws.
- **TPN (7.8)**: sort by final tournament pairing number, ascending. Descending is allowed as an
  alternative.

### Buchholz family (art. 8)

Art. 8 "must not be used in round-robins" (new note in 2026).

- **BH (8.1)**: sum of the scores of each of the participant's opponents.
- **AOB (8.2)**: average of the Buchholz (or Fore Buchholz, added in 2026) of the opponents *played
  over the board*.
- **FB (8.3)**: Buchholz computed "as if all paired games for the final round had ended in draws".
- Unplayed rounds: see art. 16 (section 6 below).

### Sonneborn-Berger and Koya (art. 9)

- **SB (9.1)**: for each round, the opponent's final score multiplied by the points scored against
  that opponent. Unplayed rounds follow art. 16.
- **KS (9.2)**: points achieved against all participants who scored at least 50% of the maximum
  possible tournament score. The limit can be changed with modifier 14.5.

### Rating-based tie-breaks (art. 10, "for Individuals")

- **Unrated players**: drop these tie-breaks from the list when unrated players are present, unless
  the tournament regulations, or the Chief Arbiter before the start, publish rules for handling them
  (art. 10 preamble).
- **Multiple ratings** (new in 2026): these tie-breaks are not recommended when a player can have
  more than one rating during the event. If they are used anyway, the *first* rating is used unless
  the regulations say otherwise.
- **ARO (10.1)**: average rating of the opponents played over the board, rounded to the nearest
  whole number, with 0.5 rounded up.
- **TPR (10.2)**: ARO + RD, where RD is the [B02] 8.1.1 p→dp conversion of the fractional score. The
  fractional score is OTB points divided by the number of OTB games. [B02] 8.1 gives p = 0 or 1.0 a
  notional dp of 800.
- **PTP (10.3)**:
  - The lowest whole-number rating R for which Σ PD(R − R_opp) ≥ the tournament score.
  - PD comes from the [B02] 8.1.2 D→PD table, and the full scale is used ("no ±400 cut").
  - For a zero score, PTP = lowest-rated opponent − 800.
- **APRO (10.4)**: average of the OTB opponents' TPRs, rounded with 0.5 up.
- **APPO (10.5)**: average of the OTB opponents' PTPs, rounded with 0.5 up.
- **RTNG (10.6)**: rating from highest to lowest. Reverse order is allowed.

## 4. Definitions: team tie-breaks

### Scores (11.1)

Each match produces two scores:

- **Match Points (MP)**: points for a team win, draw or loss.
- **Game Points (GP)**: the sum of the individual points scored by the team's players.

### Knock-out tie-breaks (art. 12)

These apply when teams are equal on both MP and GP, but may also be used in team competitions.

- **Common rules** (art. 12 preamble):
  - individual forfeits count as standard wins or losses;
  - a PAB counts, per board, as the game points of a standard win.
- **BC (12.1)**:
  - Σ board number × GP scored on that board, over all matches and regardless of who played the board.
  - *Lower is better.*
  - Only usable when all tied teams have the same GP.
- **TBR (12.2)**: GP on board 1. If that does not decide, reapply on the next board down, and so on.
- **BBE (12.3)**: GP on all boards except the bottom board. If that does not decide, exclude the next
  board up, and so on.

### Team competition tie-breaks (art. 13)

- **Reuse of individual tie-breaks** (art. 13 preamble): the tie-breaks of Arts. 6 to 10 "or some
  variation of them" may be applied to teams. The reference score is MP or GP, and defaults to the
  primary score when not stated.
- **MPvGP (13.1)**: MP in competitions decided by GP, or GP in competitions decided by MP.
- **ESB (13.2)**: Σ over opponents of (opponent's final MP or GP) × (MP or GP scored against that
  opponent). The four variants:

  | Variant | Opponent's total | × Score against them |
  |---|---|---|
  | EMMSB (13.2.1) | MP | MP |
  | EMGSB (13.2.2) | MP | GP |
  | EGMSB (13.2.3) | GP | MP |
  | EGGSB (13.2.4) | GP | GP |

  Unplayed rounds follow art. 16.
- **EDE (13.3)**:
  - Apply DE on the primary score. If that breaks no ties, apply it on the secondary score (13.3.1).
  - If exactly two teams are still tied on both MP and GP, the competition rules must say whether
    art. 12 applies, and in what order (13.3.2).
  - The "four reasonable possibilities", named in 2026:

    | Name | Sequence |
    |---|---|
    | EDEBT | EDE + BC + TBR |
    | EDEBB | EDE + BC + BBE |
    | EDET | EDE + TBR |
    | EDEB | EDE + BBE |

  - Restart from 13.3.1 whenever a new tied subset forms (13.3.3).
- **SSSC (13.4)**: secondary score + Schedule Strength.
  - Schedule Strength = BH on the primary score (FB if the value must be known before play),
    divided by a normalising factor.
  - Normalising factor = (highest achievable primary score in the tournament) ÷ (highest secondary
    score achievable in one match), rounded toward zero, unless the competition rules set another
    value.

## 5. Modifiers (art. 14)

Modifiers apply to "each tie-break based on a sum of values" (14 preamble).

- **Cut-1 (14.1)**: drop the least significant value.
  - Examples (14.1.1):
    - BH-C1 drops the opponent's lowest score.
    - ARO-C1 drops the lowest rating.
    - PS-C1 drops the score after round 1.
    - SB-C1 drops the product for the lowest-scoring opponent; if several opponents share that
      score, drop the lowest of their products.
  - ESB Cut-1 (14.1.2): drop the product for the opponent with the lowest MP (EMMSB, EMGSB) or
    lowest GP (EGMSB, EGGSB); on ties, drop the lowest product.
  - The SB and ESB tie rules changed in 2024-08. Earlier they read "the one with which the worst
    result was achieved" [C07-2024-04].
- **Cut-2 (14.2)**: drop the two least significant values, e.g. BH-C2.
- **Median-1 (14.3)**: drop the least and the most significant values, "in that order", e.g. BH-M1.
- **Median-2 (14.4)**: drop the two least and the two most significant values, e.g. BH-M2.
- **Limit (14.5)**: move the Koya 50% threshold up or down in half-point steps.
- **All modifiers are subject to art. 16** (14, last line).

**Acronym format.** The Handbook uses a hyphenated form ("BH-C1", "ARO-C1"). The TEC TRF format
[TEC-MTB] uses a slash form instead, e.g. `BH/C1`, `BH:MP/M1/P`, `KS/L+1`, `SSSC/K4`:

- `:MP` / `:GP` selects the team score.
- `/Cn` is Cut-n; `/Mn` is Median-n.
- `/L±n` is the Koya limit in half points.
- `/Kx` is the SSSC normalising factor.
- `/P` treats forfeits as played against the scheduled opponent.
- `/F` uses Fore Buchholz.
- Codes are case-insensitive.
- Self-defined tie-breaks are written `OTHER_...`.

## 6. Unplayed rounds (arts. 15 and 16). No virtual opponent any more.

- **Definition (15.1)**: an unplayed round is any round in which a participant (paired or not) did
  not play a game (individual event) or a match (team event).
- **Round robin and other pre-determined pairings (15.2, extended in 2026)**:
  - Forfeits are the only possible unplayed rounds, and they are treated as regular games or matches.
  - **Exception 1**: *all* forfeits stay unplayed for the rating-based tie-breaks (art. 10).
  - **Exception 2**: forfeit *losses* stay unplayed for Type B tie-breaks (art. 7).
- **Swiss (15.3)**: art. 16 applies.
- **Scope of art. 16 (preamble)**: art. 16 covers BH, SB (9.1 and 13.2), FB, and the Cut/Median
  modifiers 14.1 to 14.4.

### Terms (16.1)

- **Requested bye**: a half-point or zero-point bye. *Every round after a withdrawal is a zero-point
  bye* (16.1.1).
- **VUR** (voluntary unplayed round): a requested bye or a forfeit loss (16.1.2).

### Categories (16.2)

| Category | What it covers |
|---|---|
| .1 | Pairing-allocated byes or full-point byes |
| .2 | Forfeit wins |
| .3 | Requested byes followed by at least one non-VUR round |
| .4 | Forfeit losses |
| .5 | Requested byes followed only by VURs, or in the last round (this covers withdrawals) |

### Score adjustments

- **Participant's score as seen by opponents (16.3)**, used only for the opponents' tie-breaks:
  - Categories .1 to .4 count at the points actually awarded (16.3.1).
  - Category .5 counts as draws (16.3.2).
  - So a withdrawn player's remaining rounds each give their opponents ½ instead of 0.
- **Participant's own tie-break (16.4)**: each own unplayed round counts as a game against a
  **dummy**, with the result that matches the points awarded. The dummy's score is the
  participant's own final score, **capped (new in 2026)** at:
  - **16.4.1**, forfeits (categories .2 and .4): the scheduled opponent's *adjusted* score (16.3);
  - **16.4.2**, all other unplayed rounds (categories .1, .3 and .5): draw points × number of rounds.
  - Teams: "points" means MP and GP.

### Cut-1 exception (16.5)

- When a Cut or Median modifier cuts the least significant value of a participant with VURs, cut the
  lowest VUR contribution instead, "as long as such contribution is not lower than the least
  significant value".
  - **BH**: cut the lowest VUR contribution.
  - **SB**: compare the lowest VUR contribution with the least significant value and cut the higher
    of the two.
- This repeats for each further cut (16.5.2).

### Overrides (16.6)

Competition rules may set alternatives to 16.3, 16.4 or 16.5 in advance.

### The old "virtual opponent" is abolished

- [C07-pre2023] 15.2 used a virtual opponent. Its score was the player's score before the round,
  plus the complement of the forfeit result, plus ½ for every later round:
  `Svon = SPR + (1 − SfPR) + 0.5·(n − R)`.
  Unplayed games of opponents were counted as draws (15.3).
- The 2023 rewrite replaced it with the dummy (16.4). The TEC exercise foreword states "the
  abolition of the virtual opponent" [TEC-EX]. The TEC exercise explicitly warns: "dummy opponent
  (not to be confused with the virtual opponent!)".

### Related points from other chapters

- **Individual Swiss PAB**: worth a win's points by default; at most once per participant
  ([C0401] art. 3 to 4).
- **Team Swiss PAB**: worth a *draw's* MP and GP by default ([C0406] 1.4).
- **Double-Swiss**: a match is forfeited only if a player forfeits both games. A single forfeited
  game inside a match counts as played for tie-breaks and standings ([C0405] art. 0,
  Preface).

## 7. Reference implementations and worked examples

- **The 2026 Handbook text contains no worked examples.** None of the three editions since 2023
  contains examples or attachments. Only [C07-pre2023] has examples, for the obsolete
  virtual-opponent formula.
- **TEC exercises [TEC-EX]**: 70 pages, first-party FIDE (TEC), written against **C.07-2023**, with
  full crosstables and step-by-step solutions:
  - a 16-player, 5-round Swiss that includes HPBs, a ZPB, forfeits, a withdrawal and PABs;
  - a 6-player round robin with a forfeit;
  - a 14-team, 4-board Swiss.
  - Covered: BH, BH-C1, AOB, FB, SB (Swiss and RR), KS, ARO, TPR, APRO, PTP, DE (Swiss and RR),
    WIN/WON, BPG/BWG, GE (now REP), PS, MPvGP, team BH, ESB (including the Olympiad tie-break),
    EDE, BC, TBR, BBE and SSSC.
  - **Not directly reusable as expected values under 2026.** The 16.4.1 and 16.4.2 dummy caps
    change results. Verified on the exercise's Swiss crosstable:
    - **Ex. 4, David #4** (3.5 pts, HPB in round 2, category .3):
      - 2023 rules: dummy 3.5, so BH = 15.0.
      - 2026 rules: dummy capped at 0.5 × 5 = 2.5, so **BH = 14.0**.
    - **Ex. 3, Maria #11** (2.5 pts, forfeit win in round 4 vs Jessica, whose adjusted score is 1.5):
      - 2023 rules: dummy 2.5, so BH = 13.5.
      - 2026 rules: dummy capped at 1.5, so **BH = 12.5**.
      - Consequence: #8 (13.5) now ranks ahead of #11, which the 2023 solution left tied.
    - **Other cases**: Franck #6 (3.0 pts, PAB) would likewise get dummy 2.5 instead of 3.0 in his
      own BH and SB. Nick #12 and Paul #14 are unaffected, because their own scores are at or below
      both caps.
  - Also affected: exercises relying on the pre-2024-08 SB-C1 and ESB-C1 tie rule (section 5), and
    on the old 13.3.1 EDE wording ("if all the teams are still tied").
- **Mandatory tie-break list [TEC-MTB]**:
  - It lists every code an endorsed program must implement: every art. 5 tie-break plus the
    EDEBT/EDEBB/EDET/EDEB combinations.
  - It gives an exhaustive list of the code strings, e.g. `BH:GP/M2/P`, `SSSC/F/P/Kx`, where "x"
    means "any reasonable value must be implemented".
  - **It omits the tie-breaks added in 2026** (STD, TPN, RTNG). It predates the 2026 edition
    (uploaded 2025/04).
- **TRF-2026 [TEC-TRF26]**:
  - Record **202** lists the tie-breaks for players or teams tied on points; record **212** lists
    the full standings order, starting with `PTS`. Both are comma-separated [TEC-MTB] codes.
  - Record **162** sets the scoring point system, including the PAB value (`P`).
  - Record **362** sets the team MP scoring.
  - Records **240/320/330/299** cover byes, team PAB, team forfeits and abnormal point assignments.
  - Result codes in round fields include `+ - = 1 0 W D L H F U Z`.
  - **Status**: the header still reads "Approved by ??? TRF26 / ??? - ??/??/????". Treat its
    approval as unconfirmed.
- **Sample data [TEC-SAMPLE]**:
  - A 50-team, 14-round TRF25 file with `202 EDET/P,EMGSB/C1/P,BH:MP/C1/P,MPvGP` and final ranks.
  - It gives the final order only, with no per-participant tie-break values. It is usable as a
    ranking regression test only once its conformance to the current C.07 is established.
- **Endorsed programs [TEC-END]**: Vega 7.6.0, SwissSys 9.6, Swiss Manager 13, Swiss Master 5.7,
  Swiss-Chess/WinSwiss 9.05 and Chess Online 7.7.
  - These are closed-source and endorsed in 2017 to 2024, i.e. before the 2026 C.07.
  - They are practical cross-check oracles, not normative references.
  - **No open-source reference implementation is published by FIDE.**

## Ambiguities in the primary text

1. **"14.1.1.d"** (16.5.1). The HTML renders 14.1.1's examples as unlettered bullets. "d" is
   presumably the fourth bullet (SB-C1). Confirm against the official PDF or print version.
2. **Scope of art. 16 vs art. 14.** Art. 14 says "all modifiers are subject to Unplayed Rounds
   Management", but the art. 16 preamble lists only BH, SB and FB and their Cut/Median variants.
   The following have no stated unplayed-round treatment:
   - **PS-C1 / PS-C2**: cut round-1 cumulative score even when round 1 was a VUR?
   - **ARO-C1 / ARO-C2**: ARO counts only OTB opponents anyway.
   - **KS in Swiss**: KS is defined "for Round Robin" and has no stated Swiss treatment.
3. **[TEC-MTB] `/P` cross-reference.** It says `/P` means forfeits are "considered as played games
   against the scheduled opponent (see Article C.07.16.5)". In the 2026 text 16.5 is the Cut-1
   exception. The only relevant hooks are 6.1.1 (for DE) and 16.6 (general override). The exact
   semantics of `/P` for BH, SB, ESB and SSSC therefore have to be inferred:
   - Presumably the forfeit counts as a game against the real scheduled opponent, using that
     opponent's (adjusted?) score.
   - Presumably it does not use the dummy.
4. **FB (8.3)**: "as if all *paired games* for the final round had ended in draws". The text does
   not say what happens to:
   - final-round byes and PABs (presumably their awarded points);
   - final-round forfeits already known when pairings are made.
5. **TPR fractional score.** [B02] 8.1.1 tabulates p at two decimals. The rounding of, e.g., 2/3
   before lookup is not stated. It is also unstated whether TPR uses the *rounded* ARO (10.1 defines
   ARO as rounded, and 10.2 says "adding to ARO").
6. **PTP for a 100% score.** Only the zero-score case is special-cased. With the [B02] 8.1.2 table
   (PD = 1.00 only for D > 735), a perfect score gives max(opponent) + 736. This is implied, not
   stated.
7. **PAB game points for knock-out tie-breaks.** Art. 12 treats a team PAB as a standard win on each
   board, while [C0406] 1.4 awards a draw's MP and GP by default. For BC, TBR and BBE the
   board-by-board values therefore differ from the GP actually credited. This is intentional per the
   text, but easy to get wrong.
8. **Rating tie-breaks for teams.** Art. 13 allows "Articles 6-10" for teams, but art. 10 was
   retitled "for Individuals" in 2026. It is unclear whether team ARO or TPR (e.g. average rating
   of opposing boards) is still intended.
9. **"Number of rounds" in 16.4.2.** Presumably this means the scheduled rounds of the tournament.
   For a tie-break computed mid-tournament (Type D, FB), the text does not say whether it means
   rounds played so far.
10. **DE 6.3** requires reasoning over "whatever the outcome of the missing games". It gives no
    procedure: it is unclear whether the averaging in 6.1.2 applies to hypothetical games, and how
    forfeits excluded under 6.1.1 count as "missing".
11. **REP (7.6)** subtracts "half-point-byes, zero-point-byes or forfeit losses". It does not say
    whether rounds after a withdrawal count. They should, since they are zero-point byes per 16.1.1,
    but that definition sits in art. 16, which is scoped to BH and SB.
12. **Accelerated pairings.** Fictitious points (C.04.7, TRF record 250) are not mentioned in C.07.
    Presumably they are excluded from every score used in tie-breaks, but the text does not say so.

## Implications for the library

- **Target C.07 effective 2026-03-01** and model editions explicitly. Every rule change since 2023
  alters numbers:
  - 2024-08: SB-C1 and ESB-C1 tie rules, the VUR definition, EDE 13.3.1;
  - 2026: 16.4 dummy caps, 15.2 RR exceptions, STD/TPN/RTNG, AOB over FB.
  - A `TieBreakRules` edition parameter (at least `2024-08` and `2026-03`) makes the TEC exercises
    usable as fixtures for the 2023/2024 behaviour.
- **Unplayed rounds belong in the domain model.** The result model must distinguish:

  | Result kind | Category (16.2) |
  |---|---|
  | OTB win / draw / loss | not unplayed |
  | forfeit win | .2 |
  | forfeit loss | .4 |
  | PAB | .1 |
  | full-point bye | .1 |
  | half-point bye | .3 or .5 |
  | zero-point bye | .3 or .5 |
  | withdrawal (becomes zero-point byes) | .5 |
  | double forfeit | not named in C.07 |
  | "less than one move" (TRF W/D/L) | not named in C.07 |

  Categories .3 and .5 depend on what follows, so classification is a whole-tournament computation.
  Keep three separate derived views per participant:
  - raw score;
  - adjusted score for opponents (16.3);
  - dummy value for own rounds (16.4, capped).
- **Scoring must be configurable** for the win, draw and PAB values and for team MP and GP, because
  STD, WIN, the 16.4.2 caps and the SSSC divisor all depend on the point system (TRF 162 and 362).
- **Tie-break engine**:
  - An ordered list of descriptors, applied per still-tied subgroup (4.2).
  - Type A tie-breaks (DE, EDE) are recomputed on each subgroup; EDE restarts per 13.3.3.
  - Finish with lots or leave ties shared, as configured.
  - Use the [TEC-MTB] descriptor grammar (`NAME[:MP|:GP][/Cn|/Mn|/L±n|/Kx][/P][/F]`) as the public
    parse/format syntax, so it interoperates with TRF records 202 and 212. Add the 2026 acronyms
    STD, TPN and RTNG, plus a reverse-order option for TPN and RTNG.
  - Provide an extension point for `OTHER_*` self-defined tie-breaks (4.1).
- **Modifiers** are generic over "sum of contributions" tie-breaks. Each contribution needs these
  flags:
  - whether it comes from a VUR (for 16.5);
  - the opponent's score, since SB-C1 selects by opponent score and then by product.
- **Rating tie-breaks** need:
  - the [B02] 8.1.1 and 8.1.2 tables embedded verbatim, with PTP using the full scale;
  - the "first rating" rule;
  - a policy hook for unrated players (the default is to drop the tie-break);
  - explicit rounding: half-up for ARO, APRO and APPO.
- **Team tie-breaks** need board-level results, including board order and forfeits per board, for
  BC, TBR and BBE, and the PAB-as-win-per-board rule in art. 12.
- **Round robin vs Swiss** is a tournament-level property:
  - it switches 15.2 against art. 16;
  - it forbids the Buchholz family in round robins (art. 8 note);
  - it scopes KS.
- **Test data**:
  - Transcribe the TEC exercise crosstables into TRF fixtures, with expected values tagged by
    edition, and re-derive the 2026 values. Two corrected values are already verified above.
  - Hand-build small fixtures for every ambiguity above, and for STD/TPN/RTNG, which have no
    official examples.
  - Cross-check against an endorsed program (e.g. Swiss Manager) where one is available.

## Open questions

1. What exactly does the `/P` option mean for BH, SB, ESB and SSSC under the 2026 text? Its
   [TEC-MTB] cross-reference (16.5) is stale. Ask TEC, or look for a newer Mandatory Tie-Breaks
   document that also adds STD, TPN and RTNG.
2. Has TRF-2026 actually been approved, and is there a newer Mandatory Tie-Breaks list aligned with
   C.07-2026? Monitor <https://tec.fide.com/trf-2026/>.
3. Is an updated TEC exercise set for C.07-2026 planned? If not, the library's own re-derived
   fixtures become the de-facto reference and should be reviewed by an IA.
4. How should the ambiguous cases above be resolved: PS-C1 with a round-1 VUR, FB with final-round
   byes, TPR rounding, PTP at 100%, team rating tie-breaks, DE 6.3 procedure, and accelerated
   fictitious points? Decide on a default, make it configurable where 16.6 allows, and record it in
   an ADR.
5. Does "14.1.1.d" in 16.5.1 refer to the SB-C1 bullet? Check the official PDF or print version of
   C.07-2026, if one exists.
6. Should the library also support the pre-2023 virtual-opponent rules, for recomputing historical
   events? This is out of scope unless requested.
