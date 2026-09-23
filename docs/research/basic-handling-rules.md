# Basic and General Handling Rules (FIDE C.04.1 / C.04.2)

Research note for the wayfinder ticket "Basic and General Handling Rules".
Sources fetched 2026-09-23. All article numbers refer to the edition in force from 1 February 2026 unless marked "old".

## Question

What do FIDE C.04.1 (Basic Rules for Swiss Systems) and C.04.2 (General Handling Rules for Swiss Tournaments) require of a pairing library? Covers initial order and TPNs, late entries, withdrawals, forfeits (and how they count for colour and float history), pairing-allocated and requested byes and their scoring, and anything else every system relies on. Which editions are in force as of 2026?

## Sources

Primary (FIDE Handbook):

| Short | Document | URL |
|---|---|---|
| **BR** | C.04.1 Basic Rules for Swiss Systems, approved by Council 28/10/2025, applied from 1 Feb 2026 | https://handbook.fide.com/chapter/C0401202507 |
| BR-old | C.04.1, effective till 31 Jan 2026 | https://handbook.fide.com/chapter/C0401Till2026 |
| **GHR** | C.04.2 General Handling Rules for Swiss Tournaments, approved by Council 28/10/2025, applied from 1 Feb 2026 | https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournaments202602 |
| GHR-old | C.04.2, effective till 31 Jan 2026 | https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournamentsTill2026 |
| Dutch | C.04.3 FIDE (Dutch) System, from 1 Feb 2026 | https://handbook.fide.com/chapter/C0403202602 |
| Dutch-old | C.04.3, till 31 Jan 2026 | https://handbook.fide.com/chapter/C0403Till2026 |
| Dubov | C.04.4.1, from 1 Feb 2026 | https://handbook.fide.com/chapter/C040401202602 |
| Burstein | C.04.4.2, from 1 Feb 2026 | https://handbook.fide.com/chapter/C040402202602 |
| Lim | C.04.4.3, from 1 Feb 2026 | https://handbook.fide.com/chapter/C040403202602 |
| DSS | C.04.5 Double-Swiss System, from 1 Feb 2026 | https://handbook.fide.com/chapter/DoubleSwissSystem202602 |
| TPS | C.04.6 Swiss Team Pairing System, from 1 Feb 2026 | https://handbook.fide.com/chapter/SwissTeamPairingSystem202602 |
| Accel | C.04.7 FIDE-approved Accelerated Systems, from 1 Feb 2026 | https://handbook.fide.com/chapter/C0407202602 |
| GRC | C.05 General Regulations for Competitions (PDF) | https://handbook.fide.com/files/handbook/Competition_Rules.pdf |
| C.07 | Play-Off and Tie-Break Regulations, from 1 Mar 2026 | https://handbook.fide.com/chapter/TieBreakRegulations032026 |

FIDE Technical Commission (TEC, which absorbed the former SPP Commission) documents, used only for interpretation and status:

| Short | Document | URL |
|---|---|---|
| ADutch | M. Held, *Annotated Pairing Rules for the FIDE (Dutch) System (2026 edition)*, rev. 2512151400 (annotates C.04.1 and C.04.2 in full) | http://tec.fide.com/wp-content/uploads/2026/08/AnnotatedDutch-V2026.pdf |
| MDutch | M. Held, *Mastering the Dutch* (2026 rules) | https://tec.fide.com/wp-content/uploads/2026/07/Mastering_the_Dutch_2026.pdf |
| TEC26 | TEC Commission Meeting and Term Report 2022-2026 (Samarkand, 20 Sep 2026) | http://tec.fide.com/wp-content/uploads/2026/09/TEC-2026-Congress-Meeting.pdf |
| TRF26 | TRF-2026 specification | http://tec.fide.com/wp-content/uploads/2025/04/TRF-2026.pdf |

ADutch and MDutch are commentary by an individual author, published on the TEC "Official documents" page (https://tec.fide.com/official-documents/). They are not Handbook text; where they go beyond the rules this note says so.

## 1. Editions in force (as of 2026-09-23)

- **C.04.1 and C.04.2: the 2026 editions**, "Approved by the Council on 28/10/2025, Applied from 1st February, 2026" (BR header; GHR header). The Handbook index lists the previous editions as "effective till 31 January 2026".
- The whole C.04 family (Dutch, Dubov, Burstein, Lim, DSS, TPS, Accel) switched to new editions on the same date, 1 Feb 2026 (each chapter header). Lim's page still carries its historic 1987-1999 approval note but is listed as "effective from 1 February 2026".
- TEC26 (p. 14) records "Swiss rules in force 1 Feb 2026" and "C.04 amendments 2024 and 2025: Dutch clarified, Burstein corrected, bye and colour rules (FMB CM3-2025/17)". The Samarkand 2026 TEC agenda (TEC26 p. 2) contains no C.04 item; the only regulation amendments on it concern C.07. **So nothing suggests a C.04.1 / C.04.2 change is pending for the rest of 2026**, but Congress decisions are ratified by the Council afterwards and should be re-checked.
- A library that replays historic tournaments (paired before 1 Feb 2026) needs the old editions too. Differences that change pairings are listed in section 10.

## 2. C.04.1 Basic Rules: what every system inherits

These apply "for each Swiss system unless explicitly stated otherwise" (BR preamble).

| BR art. | Rule | Library consequence |
|---|---|---|
| 1 | The number of rounds is declared beforehand. | `totalRounds` is a required tournament parameter. It is needed for "last round" exceptions and for Dutch topscorers (Dutch 1.8). ADutch (p. 7) notes it may be forced to change by circumstances. |
| 2 | Two participants shall not play each other more than once. | The absolute no-rematch criterion (Dutch/Dubov/Burstein/DSS/TPS [C1]). ADutch p. 7: "the only principle ... that a FIDE-approved system cannot dispense with". Interacts with GHR 3.5 (unplayed games do not count). |
| 3 | If the number of participants *to be paired* is odd, one is not paired and receives a **pairing-allocated bye (PAB)**: no opponent, no colour, and as many points as a win "unless the rules of the tournament state otherwise". **The PAB value shall be the same for all PABs.** | PAB value is a configurable tournament constant (default = win points), fixed for the whole event (ADutch p. 7: the rules "cannot change it during the tournament"). DSS 1.4 default: points for one game won plus one drawn. TPS 1.4 default: match and game points of a **draw**. |
| 4 | A participant who already received a PAB, **or has already scored in one single round, without playing, as many points as rewarded for a win**, shall not receive the PAB. | PAB eligibility is a function of the unplayed-round history, not just "had a PAB". Blocks forfeit wins and full-point byes; per ADutch p. 7 it does **not** block players who had half-point byes, even several. |
| 5 | In general, participants are paired to others with the same score. | Score-group principle; ranks above colour rules (ADutch p. 8). |
| 6 | Colour difference (Black rounds minus White rounds) must stay within [-2, +2]. "Each pairing system may have exceptions." | Exceptions are now system-defined, in any round (old text: only "in the last round"). Dutch uses it in the last round for topscorers; TPS never applies arts. 6-7 (TPS intro); Dubov allows none (ADutch p. 8). |
| 7 | Nobody gets the same colour three times in a row. Same exception clause. | As above. |
| 8 | In general, give the colour played fewer times; if balanced, alternate from the last colour played. | Basis of every system's colour preference. |
| 9 | Pairing rules must be transparent enough for the pairing official to explain. | Non-functional: favours explainable, traceable output (for example, a pairing trace). |

**Scoring system.** BR 3 and 4 are written in terms of "points rewarded for a win", and Dutch 1.4.3 in terms of "points rewarded for a loss". So the point values for win, draw and loss must be parameters, not hard-coded 1 / 0.5 / 0.

## 3. Initial order and TPNs (GHR art. 2)

- **2.1 Strength.** Each participant is given a strength measure, usually a rating. If one rating list covers all players, that list should be used. The Chief Arbiter estimates unreliable or unknown ratings. ADutch p. 10 adds that the rating in force on the day the tournament starts is used, and later rating-list updates do not change the initial order.
- **2.2 Ranking keys, in order:** (2.2.1) strength; (2.2.2) FIDE title in the order GM, IM, WGM, FM, WIM, CM, WFM, WCM, none, "for individual tournaments"; (2.2.3) alphabetical, "unless it has been previously stated that this criterion has been replaced by another one". ADutch p. 11 suggests a random order for unrated players as an acceptable declared replacement.
- **2.3 TPN.** Ranking position = Tournament Pairing Number (#1 is highest). If the ranking data were wrong, it "can be adjusted at any time" and TPNs "may be reassigned". "**No modification of a TPN for this reason is allowed after the fourth round has been paired.**" Rating and title corrections still apply after that (for rating purposes) without renumbering (ADutch p. 11).
  - Dubov 1.2.2 makes renumbering **mandatory** ("must recalculate") whenever a rating is introduced or modified before the pairing of round 4.
  - TPS 1.1 overrides GHR 2.1-2.3: the competition rules (or the Chief Arbiter) define team TPNs, which are then fixed except for late entries (TPS 1.1.2-1.1.3 and its introduction).
- **Why TPN changes matter to the engine.** All current systems use TPN parity for the initial-colour rule (Dutch 5.2.5; Dubov 5.2.1; Burstein 5.2.1; DSS 4.3.1; TPS 4.3.1), and TPN as the final tie-break for ranking and ordering.

## 4. Late entries (GHR 2.4, 2.5; Accel 1.3)

- **2.4** A Late Entry is "a participant who is only taken into account for the pairing of rounds after the first". Late entries receive **no points for unplayed rounds** (unless the tournament rules say otherwise). They get "an appropriate TPN" and are "paired only when they actually arrive".
- **2.5** TPNs given at the start are therefore provisional. Definitive TPNs are fixed only when the List of Participants closes, "and corrections made accordingly in the results charts".
- ADutch (pp. 11, 13) reads the round-4 freeze in 2.3 as applying only to *corrections* ("for this reason"). A late entry "whatever the entry round" gets its correct place in the list, and TPNs are reassigned. This shifts the TPN parity of everyone below them (ADutch's example: a late #31 flips the round-1 colour logic for #33, #35, ...).
- Accel 1.3 places late entries into accelerated groups "according to Article 2 of the GHR". The TPN of the last group-A participant may then differ from the Accel 1.2 formula.
- GHR 4.4.4 allows a *published* pairing to be changed to admit a late entry if the changes are minimal and agreed by all involved.
- Tournament rules may admit late entries with points for missed rounds, for example a "Half-Point Bye" (ADutch p. 11).

## 5. Withdrawals, absences, requested byes

- **GHR 3.2** Participants who withdraw are no longer paired.
- **GHR 3.3** Participants "known in advance not to play in a particular round are not paired in that round and score zero (unless the rules of the tournament say otherwise)". This is the legal basis of requested byes: a zero-point bye by default, or a half-point bye if the tournament rules allow it (ADutch p. 12).
- **GRC 6.7(4)**: in L2/L3 tournaments, the rules may allow **one** half-point bye per player, with notice and arbiter agreement. The per-player limit is an event rule, so the library should not enforce it.
- **Absent without notice.** The old GHR D.2 ("considered as withdrawn unless the absence is explained ... before the next pairing is published") was **dropped from the 2026 GHR**. The same rule remains in **GRC 6.5**, with "The rules of the competition may specify otherwise". This is a tournament-management decision the library should take as input (withdrawn from round N), not infer.
- C.07 16.1 gives a vocabulary the library can reuse: *requested bye* means a half-point or zero-point bye; "any round after a participant withdraws is a zero-point-bye". *Voluntary unplayed round* means a requested bye or a forfeit loss. Unplayed-round categories (C.07 16.2): PAB/full-point bye, forfeit win, requested bye, forfeit loss.
- TRF26 (result codes, around line 80 and the bye section) encodes the same set: `+`/`-` forfeit win/loss, `H` half-point bye, `F` full-point bye, `U` PAB ("player unpaired by the system"), `Z` zero-point bye. A domain model with these unplayed-round kinds maps one-to-one onto the TRF26 exchange format.

## 6. Forfeits and unplayed games: colour history

- **GHR 3.4** "Only played games or matches count in situations where the colour sequence is meaningful." Unplayed rounds (`u`: "no valid game or match") are removed from the history and treated as if they were at the start: BWBuW is read as uBWBW, and BWWuBuW as uuBWWBW.
  - Result: colour difference, "same colour twice in a row", and "most recent round with different colours" (Dutch 5.2.3 and equivalents) all run on the **compressed sequence of played games**. Every 2026 system points back to this explicitly ("Note: Always consider Article 3.4 of the GHR": Dutch 5.2.3, Dubov 5.x, Burstein 5.x, DSS 4.x, TPS 4.x).
  - A forfeited game has no colour: "the colour initially allocated for a game that was forfeited is irrelevant and shall be ignored" (MDutch p. 17 fn. 26). The PAB has "no colour" (BR 3).
  - ADutch p. 12: when comparing two histories, WuuB, WuBu, WBuu, uWuB and uWBu are all equivalent to uuWB.
- **GHR 3.5** Two paired participants "who did not play their game or match" may be paired again later. A forfeit therefore does **not** count as a meeting for BR 2 / [C1] (MDutch p. 12 fn. 18).
- **DSS special case:** a match counts as forfeited only if at least one player forfeits *both* games. A single forfeited game inside a match counts as played for tie-breaks and standings (DSS introduction).
- Players with no played game have: no colour preference in Dutch (1.7.4) and Burstein (1.5.4); a **mild preference for Black** in Dubov (1.6.4). This is a per-system hook.

## 7. Forfeits, byes and floats

C.04.1 and C.04.2 say **nothing** about floats. Floats are defined per system. For the Dutch system the 2026 edition changed the rule in a way that changes pairings:

- **Dutch 1.4.3 (2026):** a downfloat is given to any player who receives a PAB, "or who, without playing in a round, scores more points than those rewarded for a loss". **1.4.4:** "No players other than those listed ... can receive floats."
  - So: PAB gives a downfloat (whatever the PAB's value, even 0). Forfeit win gives a downfloat. Full-point bye gives a downfloat. **Half-point bye gives a downfloat** (MDutch p. 14: "Scoring more than zero, the player will also get a 'downfloat'"). Forfeit loss and zero-point bye give **no float**.
- **Dutch-old A.4.b:** "A player who, for whatever reason, does not play in a round, also receives a downfloat." So under the old rules a forfeit loss or zero-point bye *did* give a downfloat.
- Float history is indexed by **tournament round**, not by played-game sequence. ADutch p. 18 contrasts this with colours: "for floats, ... we look only at the last two rounds of the tournament schedule". The GHR 3.4 compression must **not** be applied to float history.
- Other systems handle floats differently (Dubov: global float limits; TPS: limits on repeated floats in either direction; ADutch p. 17). Each system must define its own float-recording rule over the same round-record model.

## 8. PAB assignment across systems

The eligibility rule is BR 4 (cited as [C2] in Dutch 2.1.2, Dubov 2.1.2, Burstein 2.1.2). How the PAB receiver is chosen is per system:

| System | Who gets the PAB | Eligibility wording |
|---|---|---|
| Dutch | The player left over after pairing the last bracket (1.9.1); [C5] minimise the receiver's score; [C9] minimise the receiver's unplayed games | [C2] = BR 4 verbatim |
| Dubov | Chosen **before** pairing (1.9.2, 3.1), ..., 3.1.5 largest TPN | 3.1.1: "neither received a PAB, nor scored a (forfeit) win" |
| Burstein | Chosen before pairing (1.9.2 refers to "Article 2.1", the section itself is 3.1) | 3.1.1: same as Dubov |
| Lim | "lowest rank in the lowest scoregroup" (1.1); 7.1 "lowest rated player in the Pairing List" | refers to BR 3 |
| DSS | Chosen first (3.4), ..., 3.4.4 largest TPN | [C2]: PAB, or match won by forfeit, "or been given a FIDE-deprecated full-point bye" |
| TPS | Chosen first (3.4), ..., 3.4.4 largest TPN | [C2]: same as DSS, for teams |

## 9. Other GHR rules a library touches

- **GHR 1.4** FIDE systems must give **identical pairings** across arbiters and FIDE-approved programs. A system is "deprecated" unless an approved program *and* a free pairing checker exist for it. So a reference library must be fully deterministic, including tie-breaks down to TPN.
- **GHR 1.1-1.3** Only published FIDE systems and accelerations (C.04.7), or QC-authorised ones. The Chief Arbiter must declare the system and acceleration used, so the library should report which system and edition produced a pairing.
- **GHR 3.1** Adjourned games or matches count as **draws for pairing purposes only**. The library needs a "pending/adjourned" result type that is scored as a draw when pairing.
- **GHR 3.6** Recommended board order: (1) higher score of the pair's higher-ranked participant; (2) higher sum of both scores; (3) smaller TPN of the higher-ranked participant. This is now "recommended" (old D.9 was mandatory), and arbiters may fix boards (ADutch p. 12). With acceleration, the pairing score including virtual points is used for board order (Accel 1.5; ADutch p. 12).
- **GHR 4.3** Corrections to results, colours or ratings reported within the deadline apply to the next pairing. If reported after the next pairing but before that round ends, they affect the *following* pairing. If reported later, they apply to rating only. So the pairing input must be the **recorded** history as of pairing time, and the library must never re-pair past rounds.
- **GHR 4.4** Published pairings change only for: an accidental rematch; the competition rules allowing it; two participants close in the standings, both without an opponent, agreeing to play; a late entry (minimal changes, agreed); unforeseeable top-board events in the final round. (Old D.10: only for a rematch.) This is arbiter workflow. The library only has to accept manual pairings (and flag rule violations in them).
- **GHR 4.1** Participants who have not confirmed their presence before the drawing of lots are excluded unless the Chief Arbiter decides otherwise. This is an input concern.
- **TPS introduction:** BR and GHR arts. 1, 2.4, 2.5, 3 and 4 apply *mutatis mutandis* to teams. BR 6 and 7 never apply. GHR 2.1-2.3 are replaced by competition rules.

## 10. Changes 2026 vs pre-2026 that affect pairings

| Topic | Old | 2026 |
|---|---|---|
| PAB bar (BR 4 / old d) | PAB or "(forfeit) win due to an opponent not appearing in time" | PAB or any round scored at win value without playing (adds full-point byes) |
| PAB value | configurable | configurable **and identical for all PABs** (BR 3) |
| Colour exceptions (BR 6-7 / old f-g) | "in the last round" only | any round, as each system defines |
| Dutch floats | any unplayed round gives a downfloat | only PAB, or unplayed with points > loss (Dutch 1.4.3-1.4.4) |
| Absent without notice | treated as withdrawn (GHR-old D.2) | removed from GHR; still GRC 6.5 |
| Board sorting | mandatory (D.9) | recommended (3.6) |
| Changing published pairings | only for a rematch (D.10) | five cases (4.4) |
| Terms | players / pairing numbers / "=" for unplayed | participants / TPN / "u" |

## Ambiguities flagged

1. **BR 4 vs Dubov/Burstein 3.1.1.** BR 4 bars anyone who "scored in one single round, without playing, as many points as rewarded for a win". Dubov 3.1.1 and Burstein 3.1.1 say only "nor scored a (forfeit) win", while citing [C2] = BR 4. Does a full-point bye bar the PAB in Dubov/Burstein? The absolute criterion [C2] says yes; the 3.1.1 wording does not mention it. Recommendation: apply BR 4, the absolute criterion.
2. **"Scored ... as many points as rewarded for a win" with non-standard values.** If a tournament gives a late entry, say, 1 point for a missed round, or a PAB worth less than a win, BR 4 applies literally by points. Is a late-entry award "scored ... without playing"? The text says yes; ADutch does not discuss it.
3. **"FIDE-deprecated full-point bye"** (DSS [C2], TPS [C2]). No article in the C.04 family or GRC that we read formally deprecates full-point byes, yet ADutch p. 7 calls them a "last option" for the arbiter. The library still has to model them.
4. **TPN freeze.** GHR 2.3 says "after the fourth round has been paired"; Dubov 1.2.2 says "before the pairing of the fourth round". Both read as "renumbering allowed up to and including the round-4 pairing", but the boundary (a correction made at round-4 pairing time) is not stated explicitly. It is also unclear whether a *late entry* after round 4 renumbers everyone. GHR 2.3's "for this reason" and ADutch pp. 11 and 13 say yes. That keeps changing TPN parity late in the event, which could conflict with checker reproducibility.
5. **"Appropriate TPN" for late entries** (GHR 2.4) is not defined. ADutch reads it as the correct place in the ranking with everyone renumbered. The provisional/definitive split (GHR 2.5) also leaves open which numbering pairing-checkers should use when replaying rounds.
6. **"No valid game" (GHR 3.4)** is not defined. Clear cases: forfeits, byes, not paired. Unclear: games annulled by the arbiter, games started but voided, double forfeits. C.07 15.1 ("did not play a game") is the nearest definition.
7. **One-sided forfeit and GHR 3.5.** "Two paired participants, who did not play their game" clearly covers double non-appearance. MDutch fn. 18 extends it to any forfeit, so a rematch is allowed even though one player showed up. This is consistent with Dutch 1.4.3, but it is commentary, not rule text.
8. **Dutch floats for a PAB worth 0.** Dutch 1.4.3 gives the PAB receiver a downfloat unconditionally. A zero-point bye requested by the player gives none. Two identical score outcomes get different float treatment. Intended, but worth a test case.
9. **Float round indexing.** That floats use raw tournament rounds (not compressed played games) is stated only in ADutch p. 18, not in Dutch 1.4 or [C14]-[C21] ("previous round", "two rounds before"). This reading matters for players returning from byes.
10. **Team strength / title key.** GHR 2.2.2 (titles) applies "for individual tournaments" only. TPS delegates team ordering to competition rules, so no default exists for teams.
11. **Adjourned = draw** (GHR 3.1) for pairing, but the rules do not say which colour or float such a game produces. Colours are known (the game was played). Floats follow from the provisional scores, which are then recalculated once the real result arrives.

## Implications for the library

- **Tournament configuration:** total rounds; point values for win, draw and loss (individual) or match and game points (team); a single PAB value (per-system default: win for Dutch, Dubov, Burstein and Lim; win+draw for DSS; draw for TPS); the replacement for the alphabetical tie-break; acceleration method; rule edition (2026 default, pre-2026 optional for replay).
- **Round record per participant per round**, as a closed set of variants: played (opponent, colour, result); forfeit win/loss (opponent, no colour); PAB; full-point bye; half-point bye; zero-point bye (requested, or implied by withdrawal); not yet entered (late entry); adjourned (draw for pairing). This mirrors C.07 16 and TRF26 codes.
- **Derived views, computed centrally and shared by all systems:**
  - played-colour sequence using GHR 3.4 compression (colour difference, last-two-same, last differing colour);
  - opponents met, excluding unplayed games (BR 2 + GHR 3.5);
  - PAB eligibility per BR 4 (PAB received, or any single unplayed round with points >= win);
  - count of unplayed games (Dutch [C9]);
  - pairing score (standings points plus acceleration virtual points).
- **Per-system hooks rather than shared code:** float recording (Dutch 1.4.3; others differ), colour preference for players with no games (none in Dutch/Burstein, Black in Dubov), whether BR 6-7 exceptions apply (Dutch last round only; TPS never enforces them; Dubov none), PAB selection strategy (post-pairing in Dutch, pre-pairing in Dubov/Burstein/DSS/TPS, lowest-ranked in Lim).
- **Participant set for a round** = registered minus withdrawn minus requested-bye minus not-yet-arrived late entries. The PAB is triggered only if this set is odd (BR 3 "to be paired").
- **TPN management** as a separate, explicit operation: initial ranking by strength, then title, then name or a declared key. Renumbering is allowed for corrections until round 4 is paired and for late entries at any time. Emit the TPN map used for each round so checkers can reproduce it.
- **Determinism** (GHR 1.4): every ordering must end in a TPN tie-break. No randomness except the declared initial-colour lot and any declared random replacement for alphabetical order, both supplied as inputs.
- **Board ordering** per GHR 3.6 as the default, overridable.
- **Output metadata:** system and edition, acceleration, and a pairing trace (BR 9, GHR 1.3).

## Open questions

1. Does a full-point bye bar the PAB in Dubov and Burstein (BR 4 vs their 3.1.1 wording)? Raise with TEC. Until then, follow BR 4.
2. Is a late entry after round 4 supposed to renumber everyone (ADutch says yes), and which TPN map should a checker use to replay earlier rounds (provisional or definitive, GHR 2.5)?
3. Where, if anywhere, is the "FIDE-deprecated full-point bye" deprecation stated normatively?
4. Exact meaning of "no valid game" in GHR 3.4 for annulled or voided games. Is C.07 15.1 the intended definition?
5. Should the library support pre-2026 editions (different PAB bar, different Dutch float rule, last-round-only colour exceptions) for replaying historic TRFs, or only the 2026 rules?
6. How do Dubov, Burstein, DSS and TPS record floats for unplayed rounds? This needs the per-system research tickets; C.04.1/C.04.2 are silent.
7. Watch for Council ratification after the Samarkand 2026 Congress. No C.04 item was on the TEC agenda, but Council may still amend the Handbook.
