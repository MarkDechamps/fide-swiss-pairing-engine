# Swiss Team System (C.04.6, in force 1 Feb 2026): research notes

Ticket: wayfinder `03-swiss-team-system` ("Swiss Team System 2026").
Researched 2026-09-23. Everything below comes from primary sources: the FIDE Handbook, FIDE Technical Commission (TEC) documents, and the FIDE-owned Gacrux source code. Quotes are verbatim. Claims carry a source key in square brackets, and the keys are defined in [Sources](#sources).

> **Read this first.** The 2026 Swiss Team System is short: about 40 numbered articles [T26]. It is also *procedural*. It does not build a general weighted matching over all criteria. It works greedily from the top scoregroup down, in two stages:
> (1) choose the **set of upfloaters** for the current top scoregroup (C4, C5, then C6, C7);
> (2) pick the **lexicographically first pairing** of that bracket that best meets C1, C8, C9 and C10.
> Colours are allocated **after** the whole round has been paired, by a 9-step cascade. No colour rule is ever absolute.

---

## 1. Scope and what is inherited ("mutatis mutandis")

Preface, verbatim [T26 §0]:

> "The Swiss Pairing System Rules specified in the Basic Rules for Swiss Systems and in the Articles 1, 2.4, 2.5, 3 and 4 of the General Handling Rules for Swiss Tournaments are for individuals, but can also be applied mutatis mutandis to teams, with one significant exception: the Articles 6 and 7 of the Basic Rules for Swiss Systems never apply."

> "Articles 2.1-2.3 of the General Handling Rules for Swiss Tournaments, dealing with the initial order of the teams, have been deliberately omitted [...] it is preferable to leave any details out of the general rules and let the initial order of teams be determined by the rules of each specific competition."

> "In any case, the colour will never be a factor so decisive as to prevent two teams from playing against each other. Therefore, there are no absolute colour preferences outlined in these regulations."

### 1.1 Basic Rules C.04.1 (2026) as they apply to teams [BR26]

| BR art. | Content (paraphrased unless quoted) | Status for teams |
|---|---|---|
| 1 | Number of rounds declared beforehand | Applies |
| 2 | "Two participants shall not play against each other more than once." | Applies; this is [C1] (T26 2.1.1) |
| 3 | PAB: "no opponent, no colour and as many points as are rewarded for a win, unless the rules of the tournament state otherwise. This number of points shall be the same for all pairing-allocated byes." | **Overridden** on the points: a team PAB scores a *draw* (T26 1.4, see §2.4). The "same for all PABs" clause is repeated in T26 1.4 |
| 4 | No PAB for anyone who "has already received a pairing allocated bye, or has already scored in one single round, without playing, as many points as rewarded for a win" | **Restated** for teams as [C2] (T26 2.1.2), in different words (see §3.1) |
| 5 | "In general, participants are paired to others with the same score." | Applies (as a principle) |
| 6 | Colour difference must stay within ±2 | **Never applies** (T26 §0) |
| 7 | Never the same colour three times in a row | **Never applies** (T26 §0) |
| 8 | General equalise-then-alternate colour principle | Applies in spirit; the precise rule is T26 art. 4 |
| 9 | Rules transparent enough to explain | Applies |

### 1.2 General Handling Rules C.04.2 (2026) as they apply to teams [GHR26]

- **Art. 1 (pairing systems)** applies. Relevant here: GHR 1.4 says pairings must be reproducible ("different tournament handler programs approved by FIDE, must be able to arrive at identical pairings"). It also says: "the use of such systems is deprecated, unless a tournament handler program approved by FIDE is available for them, provided with a free pairing-checker able to verify tournaments run with that system." See §7: no THP is yet approved for this system.
- **Art. 2.1–2.3 (initial order and TPN assignment)** do **not** apply. T26 1.1.2 replaces them.
- **Art. 2.4–2.5 (late entries, provisional TPNs)** apply. This is new in 2026 (see §6).
- **Art. 3 (pairing, colour and publishing rules)** applies. For teams this means:
  - 3.1: "adjourned matches (in a team competition) are considered draws for pairing purposes only."
  - 3.2: teams that withdraw are no longer paired.
  - 3.3: teams known to be absent in a round are not paired and score zero (unless the rules say otherwise).
  - 3.4: "Only played games or matches count in situations where the colour sequence is meaningful." Unplayed rounds are squeezed out of the history, and the played colours are right-aligned. Example from the text: "BWBuW ... will be treated as if their colour history was uBWBW".
  - 3.5: "Two paired participants, who did not play their game or match, may be paired together in a future round."
  - 3.6: recommended sort order of published pairs: (1) highest score of the higher-ranked participant, (2) highest sum of both scores, (3) smallest TPN of the higher-ranked participant. C.04.6 itself does not specify board order.
- **Art. 4 (competition rules)** applies: result corrections, and when published pairings may be changed.

### 1.3 Accelerated systems C.04.7 (2026) [ACC26]
C.04.7 is not in the preface's list, but it states that "Unless explicitly specified otherwise, each described acceleration method is applicable to any Swiss Pairing System". Its Baku worked example is a team event: "In an 11-round team competition with matchpoints [*] as the primary score (2 MP for win, 1 MP for draw), the teams in GA are assigned two virtual matchpoints in the first three rounds and one virtual matchpoint in the next three rounds." The footnote reads: "[*] Note that if gamepoints were the primary score, the Baku Acceleration could not be used" (C.04.7 1.4.4). The pairing score is standings points plus virtual points (C.04.7 1.5).

### 1.4 Not related: Olympiad Pairing Rules
The Olympiad has its own Swiss rules in D.02.02 [OLY]. They differ from C.04.6: board-1 colour limits of ±2 and no three in a row (7.3), a median-group pairing order (6.4), and PAB = 1 MP + 2 GP (4.3). They are **not** C.04.6 and are out of scope for this system.

---

## 2. Definitions (T26 art. 1)

### 2.1 TPN (1.1)
- 1.1.1: "Each team must have a different TPN, from 1 to the TPN corresponding to the number of teams."
- 1.1.2: "The rules of the team competition shall describe how to assign a TPN to each team. Otherwise, it is a decision of the Chief Arbiter." Note: "This provision overrides Articles 2.1 to 2.3 of the General Handling Rules".
- 1.1.3: "Once defined, the TPN should not be modified (except as stated in Articles 2.4 and 2.5 of the General Handling Rules [...]), unless the Chief Arbiter decides otherwise."

The library therefore **accepts TPNs as input**. It must not derive them from ratings.

### 2.2 Score (1.2)
- 1.2.1: "The rules of the competition shall state which, between "match points" and "game points", is called "primary score" (or, more simply, "score"), and whether the other ("secondary score") is used for colour allocation (see Article 4.2.2)."
- 1.2.2: "The default is to use "match points" as the (primary) score and "game points" for colour allocation."
- The system uses the secondary score **only** in 4.2.2, to choose the first-team. It never uses it to build brackets.
- The point values (match points per team win/draw/loss, game points per board result) are **not** set in C.04.6. They come from the competition rules. In TRF they are records 362 and 162 (§8); the TRF default is TW=2, TD=1, TL=0 [TRF26].

### 2.3 Scoregroups and brackets (1.3)
- 1.3.1: "A scoregroup is composed of all the teams with the same score."
- 1.3.2: "A (pairing) bracket is an even numbered group of teams all to be paired. It is composed of teams coming from the same scoregroup (called resident teams) and (possibly) of teams coming from lower scoregroups (called upfloaters)."
- The system has **no downfloaters**. Every resident of the top scoregroup is paired inside its own bracket. When the bracket cannot be paired, more upfloaters are pulled up; no resident is pushed down. (This follows from 3.3.2 and 3.5. Gacrux's module docstring states the same reading [GAC pairingfideteam.py].)

### 2.4 Pairing-allocated bye (1.4), verbatim
> "Should the number of teams to be paired be odd, one team is not paired. This team receives a pairing-allocated-bye: no opponent, no colour, and as many match points and game points as are rewarded for a draw, unless the regulations of the team competition state otherwise. These numbers of points shall be the same for all pairing-allocated byes (See Article 3 of the Basic Rules for Swiss Systems)."

- "Game points rewarded for a draw" means the game points of a drawn *match*, for example 2 GP in a 4-board event. The TRF-2026 PAB example gives "01.0 MP, 02.0 GP" [TRF26, record 320]. ⚠ The text does not define it further. With an odd number of boards a drawn match is impossible without drawn games (e.g. 2.5–2.5 needs a half point). The value should be a configuration input.

### 2.5 Floater (1.5)
> "A floater is a team that plays against an opponent with a different score."
- "Score" here is the primary score (1.2.1: "primary score (or, more simply, "score")").
- A PAB is not "playing against an opponent", so a PAB team is not a floater. Gacrux reads it this way [GAC crosstablefideteam.py `floatrule` docstring].
- Floater status is only checked for the **previous round** (C7, C10). The Dutch system looks two rounds back.

### 2.6 Colour and colour difference (1.6), verbatim
- 1.6.1: "A team is said to have (had) a colour (White or Black) in a match if the match was actually played and the player on the first board was scheduled to play with that colour."
- 1.6.2: "The colour difference of a team is the number of matches where the team had White minus the number of matches where the team had Black."
- Forfeited matches and byes give **no colour**. GHR 3.4 squeezes them out of the colour sequence.

### 2.7 Colour preferences (1.7), verbatim
"Type A colour preferences are used unless the rules of the team competition specify that either Type B colour preferences shall be used, or colour preferences are not to be used at all."

**Type A (1.7.1):**
> "A team has a simple (Type A) colour preference for White if its CD is less than -1, or, being its CD 0 or -1, the team had Black in the last two played matches.
> A team has a simple (Type A) colour preference for Black if its CD is more than +1, or, being its CD 0 or +1, the team had White in the last two played matches.
> In all other situations, the team has no (Type A) colour preference."

**Type B (1.7.2):**
> "A team has a strong (Type B) colour preference for White if its CD is less than -1, or, being its CD 0 or -1, the team had Black in the last two played matches.
> A team has a strong (Type B) colour preference for Black if its CD is more than +1, or, being its CD 0 or +1, the team had White in the last two played matches.
> A team has a mild (Type B) colour preference for White if its CD is -1, or, if it is zero and it is not the last round, the team had Black in the last played match.
> A team has a mild (Type B) colour preference for Black if its CD is +1, or, if it is zero and it is not the last round, the team had White in the last played match.
> A team has no (Type B) colour preference when it has yet to play a match, or when its CD is zero when pairing for the last round."

Decision table (CD = colour difference; "last2" = colours of the last two *played* matches):

| Condition | Type A | Type B |
|---|---|---|
| CD ≤ −2 | W (simple) | W strong |
| CD ∈ {0, −1} and last2 = BB | W (simple) | W strong |
| CD ≥ +2 | B (simple) | B strong |
| CD ∈ {0, +1} and last2 = WW | B (simple) | B strong |
| CD = −1 (otherwise) | none | W mild |
| CD = +1 (otherwise) | none | B mild |
| CD = 0, last = B, not last round | none | W mild |
| CD = 0, last = W, not last round | none | B mild |
| no played match yet | none | none |
| CD = 0, last round (not caught by the strong rows) | none | none (see A1) |

Unlike C.04.3, a team with CD = +1 whose last two matches were BB has **no** Type A preference, and only a *mild Black* Type B preference. The last two BB do not produce a White preference because CD = +1 is outside {0, −1}. Gacrux's test `test_art_1_7_colour_difference_plus_one_after_two_blacks` makes the same point [GAC].

> ⚠ **Ambiguity A1 (Type B, last round, CD 0 with last two the same colour).** The strong clause ("being its CD 0 [...] the team had White in the last two played matches") and the no-preference clause ("when its CD is zero when pairing for the last round") both match this case. Gacrux checks strong first, so a strong preference results [GAC crosstablefideteam.py `color_preference`]. Most likely the no-preference sentence only closes the mild clauses, which carry their own "not the last round" condition. Log this as an interpretation.

> ⚠ **Ambiguity A2 (overlapping strong and mild clauses).** CD = −1 with last two BB matches both "strong White" and "mild White (CD is −1)". The most specific clause (strong) must win. Gacrux does this.

> ⚠ **"Last round."** The number of rounds must be known. It is Basic Rule 1, TRF record 142. It is used in Type B mild preferences and in the exemptions for C7 and C10.

---

## 3. Pairing criteria (T26 art. 2): full list

### 3.1 Absolute criteria (2.1): "No pairing shall violate the following absolute criteria:"
- **[C1]** 2.1.1: "See the Basic Rules for Swiss, Article 2 (Two participants shall not play against each other more than once)."
- **[C2]** 2.1.2: "A team that has already received a pairing-allocated bye or won a match by forfeit (or been given a FIDE-deprecated full-point bye) shall not receive the pairing-allocated bye."

> ⚠ **A3: does a forfeited match count as "played" for [C1]?** GHR 3.5 allows two participants "who did not play their game or match" to be paired again. For teams this means a forfeit (one team absent) or a double forfeit does not use up the pairing. The library should count only matches actually played. This matches T26 1.6.1's "actually played" and the usual Dutch practice. The text never states it for teams in so many words. Needs confirmation.

> ⚠ **A4: [C2] and Basic Rule 4.** BR 4 (2026) bars anyone who "scored in one single round, without playing, as many points as rewarded for a win". T26 [C2] lists instead: a PAB, a match won by forfeit, and an FPB. The two differ in edge cases. Example: a 299 "abnormal assignment" that gives win-level points without playing (TRF26 record 299). The team text is the specific rule, so follow it. Gacrux bars exactly {PAB, forfeit win, FPB}, and a half-point bye does not bar a PAB [GAC crosstablefideteam.py `NOPAB`; test `test_art_2_1_2_c2_a_half_point_bye_does_not_bar_the_bye`].

### 3.2 Completion criterion (2.2)
- **[C3]** 2.2.1: "A pairing complying with all the absolute criteria (see Article 2.1) shall always exist for all teams not yet paired."
- 3.1.2: "During the pairing, the completion criterion [C.3] (see Article 2.2) is also to be complied with." (sic, "[C.3]")
- In practice: after the PAB is assigned and after each bracket is fixed, the teams still unpaired must admit a perfect matching under C1 (and C2, which only concerns the PAB). This is a plain perfect-matching feasibility test. Any perfect matching of the remaining teams can be reached by the greedy process, because a bracket may take upfloaters from any lower scoregroup. So the test is both necessary and sufficient. Gacrux gives the same argument in its `can_be_paired` docstring [GAC].

### 3.3 Quality criteria (2.3): "In order to best pair all teams of the top-scoregroup (see Article 3.2), comply as much as possible with the following criteria, given in descending priority:"
- **[C4]** 2.3.1: "Minimise the number of upfloaters."
- **[C5]** 2.3.2: "Minimise the score differences (taken in descending order) in the pairs involving upfloaters, i.e. maximise the scores (taken in ascending order) of the upfloaters."
- **[C6]** 2.3.3: "Unless all the teams in the following scoregroup became or are upfloaters (thus this scoregroup is now empty), choose the set of upfloaters so that criteria [C1], [C3] and [C4] (see Articles 2.1.1, 2.2.1 and 2.3.1) are complied with in the bracket where this (not empty) scoregroup is paired."
  Note: "Only the mentioned scoregroup is involved, even though some of the upfloaters come from lower scoregroups."
- **[C7]** 2.3.4: "With the exception of the last two rounds, minimise the number of upfloaters that were floaters in the previous round (see Article 1.5)."
- **[C8]** 2.3.5: "Minimise the number of teams whose colour preference, if any, is not fulfilled."
- **[C9]** 2.3.6: "(Type B only) Minimise the number of teams whose strong colour preference, if any, is not fulfilled."
- **[C10]** 2.3.7: "With the exception of the last two rounds, minimise the number of upfloaters' opponents that were floaters in the previous round (see Article 1.5)."

Where each criterion is evaluated:

| Criterion | Stage | Depends on |
|---|---|---|
| C4, C5 | upfloater-set selection (3.5.2), "by construction" | which teams are in the set |
| C6, C7 | upfloater-set selection (3.5.5) | which teams are in the set |
| C1, C3 | both stages (legality) | the set (C3); the pairs (C1) |
| C8, C9, C10 | bracket pairing (3.6.4) | the pairs inside the bracket |

Because every team of the bracket is paired inside it, C3 and C6 depend **only on the set**, never on how the bracket is paired internally. This separation is clean and the library's structure should follow it.

**C8/C9 per pair.** Art. 4 allocates colours after pairing, and art. 4 grants *every* preference in a pair unless both teams want the same colour (4.3.2, 4.3.3). So:
- C8 = the number of pairs whose two teams prefer the same colour. Exactly one team of each such pair is unfulfilled.
- C9 (Type B) = the number of pairs in which both teams have a *strong* preference for the same colour. When one is strong and the other mild, 4.3.4 grants the strong one, so no strong preference is left unfulfilled.

Gacrux computes C8 and C9 this way, per edge [GAC crosstablefideteam.py `update_edge`]. ⚠ This is a derived reading. It holds because the colour cascade is deterministic and grants preferences first. It should be recorded as a design note.

> ⚠ **A5 ([C5] wording: "pairs involving upfloaters").** Upfloaters may be paired with each other inside the bracket, e.g. one resident and three upfloaters. "Score differences in the pairs involving upfloaters" would then include pairs between two upfloaters. The "i.e." clause settles it: compare the **multiset of upfloater scores**, sorted ascending, and maximise it lexicographically. The 2024 wording ("maximise the lowest score among the upfloaters (and then the second lowest, and so on)") confirms this reading [T24 3.3.2].

> ⚠ **A6 ([C6] is the most ambiguous article).** What does "[C4] complied with in the bracket where this scoregroup is paired" mean?
> (a) **Parity-minimum reading (Gacrux):** what is left of the following scoregroup can be paired, with C1 and C3 respected, using the fewest upfloaters its parity allows: 0 if even, 1 if odd. Binary pass/fail [GAC `check_c6`]. Gacrux argues that any other reading would leave C6 with no content.
> (b) **Comparative reading:** among the candidate sets, prefer the one for which the next bracket needs the fewest upfloaters. This is graded, not binary.
> The two readings give different pairings when no candidate set lets the next scoregroup reach its parity minimum. The 2024 text was graded differently again: "maximise the number of remaining teams that can be legally paired in the following scoregroup" [T24 3.3.3]. "Following scoregroup" is also not defined when the next score level holds no teams from the start (for example, after the PAB removal). Needs an authoritative ruling (TEC/SPP).

> ⚠ **A7 ("complies with" applied to minimisation criteria).** 3.5.5 says "choose the first set that [...] produces a legal pairing that also complies with criteria [C6] and [C7]". 3.6.4 says "choose the first pairing that also complies with criteria [C1], [C8], [C9] and [C10]". C7–C10 are *minimise* criteria, so "complies" must mean "attains the best value available", with the criteria taken in descending priority, and ties broken by the lexicographic order. That is Gacrux's reading: for sets the key is (C6 fails?, C7 count, lexicographic index); for pairings the weight order is C8 > C9 > C10 > identifier [GAC `select_upfloaters`, `update_bracket`]. An alternative is to take the first set or pairing that reaches zero, and fall back otherwise. It gives the same result whenever zero is reachable.

> ⚠ **A8 (what "score" means for C7/C10 floaters under acceleration).** C.04.7 1.5 builds scoregroups on "pairing score" (real plus virtual points). Is a "floater in the previous round" judged by the pairing score of that round or by the real score? The text does not say. The Dutch-system precedent is to use the pairing score of that round.

---

## 4. Pairing procedure (T26 art. 3)

### 4.1 Legal pairing, top scoregroup, outline (3.1–3.3)
- 3.1.1: "A pairing is legal when the absolute criteria [C1] and [C2] (see Article 2.1) are complied with." 3.1.2: during the pairing [C3] must also hold.
- 3.2: "During the pairing, it [the Top-Scoregroup] is the group of one or more teams that have the highest score among the teams that are yet to be paired."
- 3.3.1: "The pairing of a round (called round-pairing) is complete if all the teams (except at most one, which receives the pairing-allocated bye) have been paired and the absolute criteria [C1] and [C2] (see Article 2.1) have been complied with."
- 3.3.2, verbatim steps:
  > "The first step in the pairing process is the assignment of the pairing-allocated-bye (if needed) by applying Article 3.4.
  > Then, the top-scoregroup is combined, when needed, with a set of upfloaters (selected according to Article 3.5), to form a bracket that is paired according to Article 3.6.
  > The previous step-2 is then repeated until the round-pairing is complete.
  > Colours are then assigned according to Article 4."
- 3.3.3: "If it is impossible to complete a round-pairing, the Chief Arbiter shall decide what to do." → the library reports "no legal pairing". It does not invent a fallback.

### 4.2 PAB assignment (3.4), done FIRST
"The pairing-allocated-bye is assigned to the team that:
3.4.1 leaves a legal pairing for all teams
3.4.2 has the lowest score
3.4.3 has played the highest number of matches
3.4.4 has the largest TPN"

Algorithm: the candidates are the teams allowed a PAB under [C2]. Sort them by (score ascending, matches played descending, TPN descending). Pick the first candidate whose removal leaves a perfect matching of the other teams (C1).

> ⚠ **A9 ("has played the highest number of matches").** Do forfeited matches count? Given 1.6.1 ("actually played") and GHR 3.5, probably not. Gacrux counts matches played against opponents [GAC `find_pab` uses `num`]. Confirm.

The PAB is fixed before any bracket. This differs from C.04.3 (2026), where the PAB goes to the player "who downfloats from the last bracket" (C.04.3 1.9.1), inside the bracket optimisation ([C5] PAB criterion) [https://handbook.fide.com/chapter/C0403202602]. C.04.3 [C2] also simply refers to Basic Rules art. 4, while C.04.6 [C2] gives its own list (see A4).

### 4.3 Selection of upfloaters for the top scoregroup (3.5), verbatim
> "3.5.1 All teams with a lower score than the resident teams of the top-scoregroup are potential upfloaters.
> 3.5.2 Consider all sets of potential upfloaters that comply with [C4] and [C5] (see Articles 2.3.1 and 2.3.2).
> Note: This somehow determines the number of upfloaters in the set and their scores.
> 3.5.3 In each set, the potential upfloaters, identified by their TPN, are first sorted by descending score and then, when scores are equal, by ascending TPN.
> 3.5.4 These sets are then sorted among themselves by the lexicographic order of their TPNs.
> Example: Let's assume that 2,6,8 have 3 points, and 1,3,5 have 2.5 points. [C4] determines that a set of three upfloaters is needed, and [C5] determines that two upfloaters must have 3 points and the other 2.5 points. The possible set of upfloaters are: {2,6,1} < {2,6,3} < {2,6,5} < {2,8,1} < {2,8,3} < {2,8,5} < {6,8,1} < {6,8,3} < {6,8,5}, already sorted in the proper order.
> 3.5.5 Choose the first set that, together with the top-scoregroup, produces a legal pairing that also complies with criteria [C6] and [C7] (see Articles 2.3.3 and 2.3.4) - besides [C4] and [C5] (see Articles 2.3.1 and 2.3.2), which it complies with by construction."

An implementable reading (Gacrux's):
1. For k = (residents mod 2), then k+2, k+4, … (the bracket size must be even):
2. For each score profile of k upfloaters, best C5 first (the ascending-sorted score tuple, maximised lexicographically):
3. Enumerate every set with that profile, in 3.5.3/3.5.4 order. Keep a set only if (a) the bracket has a perfect matching under C1 and (b) the teams left outside the bracket have one too (C3).
4. Among the kept sets, choose by (C6 satisfied first, then minimum C7, then earliest in the order). If no set is kept, move to the next profile; if no profile works, move to the next k.

Reading "C4/C5 by construction" as "C4/C5 among the sets that can be legal" is required. Otherwise 3.5.2 could fix a (count, profile) for which no legal pairing exists. C3 is a completion (absolute-level) criterion and outranks the quality criteria C4 and C5.

> ⚠ **A10 (the example has a gap).** The example gives the number and scores of upfloaters as if C4/C5 fixed them in isolation. It does not show a fallback to a larger k or a worse profile. The fallback is implied only by the priority of [C3].

> ⚠ **A11 (combinatorial cost).** Enumerating sets is exponential in the worst case, e.g. a large lower scoregroup with many teams in one profile level. This is acceptable under "readability over performance", but the Java design needs a guard, such as pruning by C1 feasibility.

### 4.4 Pairing of a bracket (3.6), verbatim
> "3.6.1 A pairing is a sequence of pairs that includes all teams in the bracket. For each pair, the team with the smaller TPN is the top member of the pair; the team with the larger TPN is the bottom member of the pair.
> 3.6.2 A pairing is identified by the TPNs of the top members of each pair sorted in ascending order, followed by the TPNs of the bottom member of the corresponding pair.
> Example: If 11-24 16-6 10-9 8-4 is a pairing, its identifier is 4 6 9 11 8 16 10 24.
> 3.6.3 Pairings are sorted by the lexicographic order of their identifiers.
> 3.6.4 Choose the first pairing that also complies with criteria [C1], [C8], [C9] and [C10] (see Articles 2.1.1 and 2.3.5 to 2.3.7 - besides the other criteria, which it complies with by construction)."

Notes for the design:
- The identifier has length 2·(number of pairs). Its first half is the *set* of top members in ascending order. Comparing two identifiers therefore compares the top-member sets first: A < B exactly when the smallest team in the symmetric difference is a top member in A. The bottom halves are compared only when the top sets are equal.
- Consequence: with no constraints, the first pairing is always "1st half vs 2nd half in order" (T1–T(n+1), T2–T(n+2), …, by TPN rank within the bracket). This resembles the Dutch S1/S2 split with no transpositions heuristic. **Ranking is by TPN only**, not by score. An upfloater with a smaller TPN than a resident becomes the *top* member (3.6.1).
- Brute-force enumeration in identifier order, filtered by C1 and optimised for C8 > C9 > C10, is the most readable implementation. Gacrux instead encodes the order as the weights of a minimum-weight perfect matching [GAC `update_bracket`].

> ⚠ **Observed Gacrux divergence to verify (G1).** Gacrux gives bracket-seat numbers from `sort_nodes`, which sorts by (score descending, TPN), not by TPN alone. Its docstring says "the teams taken in TPN order" [GAC pairingfideteam.py `sort_nodes`, crosstablefideteam.py `update_bracket`]. When an upfloater's TPN is smaller than a resident's, Gacrux therefore treats the resident as the top member. 3.6.1 says the smaller TPN is the top member. This can change the chosen pairing. A cross-check test must cover this case before Gacrux is used as the oracle.

---

## 5. Colour allocation (T26 art. 4), verbatim
> "4.1 The initial-colour is the colour determined by drawing of lots before the pairing of the first round.
> 4.2 The first-team is the team (first that applies):
> 4.2.1 with the higher primary score; or
> 4.2.2 with the higher secondary score (unless the rules of the competition state not to use it); or
> 4.2.3 with the smaller TPN.
> 4.3 For each pair apply (with descending priority):
> 4.3.1 When both teams have yet to play a match, if the first-team has an odd TPN, give it the initial-colour; otherwise, give it the opposite colour.
> 4.3.2 If only one team has a colour preference, grant it.
> 4.3.3 If the two teams have opposite colour preferences, grant them.
> 4.3.4 (Type B only) If only one team has a strong colour preference, grant it.
> 4.3.5 Give White to the team with the lower colour difference.
> Note: -2 is lower than -1; +1 is lower than +2.
> 4.3.6 Alternate the colours to the most recent time in which one team had White and the other Black.
> Note: Always consider Article 3.4 of the General Handling Rules for Swiss Tournaments.
> 4.3.7 Grant the colour preference of the first-team.
> 4.3.8 Alternate the colour of the first-team from its last played round.
> 4.3.9 Alternate the colour of the other team from its last played round."

Notes:
- "Colour" always means the colour of **board 1** (1.6.1). The board-by-board pattern (e.g. WBWB) is set by the competition rules. In TRF it is record 352. Board colours are outside the pairing system.
- The cascade always ends with a decision. 4.3.9 covers the case where the first-team has never played but the other team has. In 2024 that case was not covered.
- 4.3.1 applies whenever **neither** team has played a match, not only in round 1. Late entries and teams whose matches were all forfeited also qualify. It depends on the first-team's TPN parity and the drawn initial-colour. The initial-colour is TRF record 152, which is needed only if it differs from what the highest-ranked participant received [TRF26].
- 4.3.6 compares the two played-colour histories **right-aligned** after unplayed rounds are removed (GHR 3.4). Gacrux compares the squeezed sequences from the end [GAC `color_allocation`]. ⚠ **A12:** The alternative is to compare round by round (same round number). It differs when the two teams have different gaps. The note "Always consider Article 3.4 of the GHR" favours the right-aligned, squeezed comparison.
- When "colour preferences are not to be used at all" (1.7), no team has a preference. The cascade then reduces to 4.3.1, 4.3.5, 4.3.6, 4.3.8 and 4.3.9, and C8/C9 are always 0.
- The PAB has no colour (1.4).

---

## 6. Differences from the previous edition (C.04.6 effective 1 Aug 2024 – 31 Jan 2026)

The Handbook lists only two editions of C.04.6: the 2024 one (approved by the Council 27/07/2024, applied from 01/08/2024 [T24]) and the 2026 one (approved 28/10/2025, applied from 1 Feb 2026 [T26]). No earlier team system is listed under C.04.6. The TEC's 2024 TRF post says "the FIDE Handbook now contains a team pairing algorithm for which endorsement can be sought" [TEC-TRF26-page].

| Topic | 2024 [T24] | 2026 [T26] |
|---|---|---|
| Article layout | 1 definitions (incl. 1.8 top-scoregroup, 1.9 outline); **2 pairing rules; 3 criteria**; 4 colours | 1 definitions; **2 criteria; 3 pairing procedure** (top-scoregroup, outline, PAB, upfloaters, bracket); 4 colours |
| Inherited GHR articles | "Articles 1, 3 and 4"; all of GHR art. 2 omitted | "Articles 1, **2.4, 2.5**, 3 and 4"; only GHR 2.1–2.3 omitted (late entries now inherited) |
| Who decides TPN / impossible rounds | "the arbiter" | "the Chief Arbiter" |
| Secondary score | "whether the other [...] is used, and if so, for what"; default GP "only for colour allocation" | "whether the other [...] is used for colour allocation"; the only use is colour allocation |
| PAB | no "same for all PABs" clause | adds "These numbers of points shall be the same for all pairing-allocated byes" |
| Floater definition | "plays against an opponent with a different **primary** score" | "plays against an opponent with a different score" (same meaning, since score = primary score) |
| [C1] | "Two teams shall not play against each other more than once." | refers to Basic Rules art. 2 |
| [C2] | no PAB after a PAB or after having "scored in one single round, without playing, the same score rewarded for a win" | no PAB after a PAB, **a forfeit win, or an FPB** (list stated explicitly) |
| [C3] | "Choose the set of upfloaters (which may be empty) so that all the remaining teams outside the top-scoregroup allow the completion of the round-pairing." | "A pairing complying with all the absolute criteria [...] shall always exist for all teams not yet paired." |
| [C5] | "maximise the lowest score among the upfloaters (and then the second lowest, and so on)" | "Minimise the score differences (taken in descending order) in the pairs involving upfloaters, i.e. maximise the scores (taken in ascending order)" (same intent) |
| [C6] | "maximise the number of remaining teams that can be legally paired in the following scoregroup" | new wording: following scoregroup must satisfy [C1], [C3], [C4] in its bracket, unless it has been emptied (see A6) |
| Floater criteria | **one**: [C9] "With the exception of the last two rounds, minimise the number of teams that float in consecutive rounds." (bracket level) | **two**: **[C7]** upfloaters that floated in the previous round (set-selection level) and **[C10]** upfloaters' opponents that floated in the previous round (bracket level) |
| Colour criteria numbers | [C7] preferences, [C8] strong (Type B) | [C8] preferences, [C9] strong (Type B) |
| Upfloater choice (3.5.5 / 2.2.5) | first legal set complying with [C6] | first legal set complying with **[C6] and [C7]** |
| Bracket choice (3.6.4 / 2.3.4) | [C1], [C7], [C8], [C9] | [C1], [C8], [C9], [C10] |
| PAB tie-breaks | "highest number of **games**"; "highest TPN" | "highest number of **matches**"; "largest TPN" |
| 4.3.1 | "give them the initial-colour"; note referring to GHR Late Entries for TPNs | "give it the initial-colour"; note dropped |
| 4.3.6 note | "Article 4.5 of the GHR" | "Article 3.4 of the GHR" (renumbering) |
| 4.3.8 | "from the last played round" | "from **its** last played round" |
| 4.3.9 | (absent) | **new**: "Alternate the colour of the other team from its last played round." |

The colour-preference definitions (1.7) are word-for-word identical in both editions. So are the brackets, the upfloater sort (3.5.3/3.5.4) and the bracket identifier (3.6.1–3.6.3), apart from small editorial changes.

Implication: under 2024 and 2026 the same tournament can pair differently, through C2, C6, C7/C10 and 4.3.9. The TRF 192 codes do **not** carry the edition (e.g. `FIDE_TEAM_TYPEA_MP_GP`) [TTC192], so the edition must be chosen from the tournament date. By contrast, 192 does distinguish `FIDE_DUTCH_2017` from `FIDE_DUTCH_2025`.

---

## 7. Implementations (endorsed or not)

- **No FIDE-endorsed or TAPC-accepted program for the Swiss Team System was found.** The current public register, C.02.04 (in force 1 March 2026), lists 11 Tournament Handler Programs: Vega, SwissSys, SwissMaster, Swiss-Manager, Swiss-Chess, UTU Swiss, ChessManager, STOP, TournamentService, Tornelo and Chess Online. Every one of them is listed with Swiss Pairing System "Dutch". The endorsements show an expiry of "2026/02/01" [C0204]. The older SPP list, FEP22, is also Dutch-only (plus a Vega/Dubov entry) [FEP22].
- In 2024 the TEC announced that under the new "FIDE Approval" process, "FIDE approval can also be requested for THPs running team tournaments and using the FIDE defined Team Pairing System ("TPS") as their pairing system" [TRF25-draft-post].
- By GHR 1.4 (2026), a system without an approved THP and a free checker is formally "deprecated" [GHR26]. For now this appears to cover the Team System. ⚠ The rule is odd, but it is what the text says.
- **Gacrux (FIDE open-source reference engine), non-endorsed:** https://github.com/OttoMilvang/TieBreakServer, MIT licence, "Copyright (c) 2024 FIDE" [GAC LICENSE]. The TEC 2026 Congress report calls it an "open-source reference platform by Otto Milvang: pairing checker, tie-break calculator and tournament generator, tested on 120,000+ generated tournaments" and says: "Gacrux is an engine, not a Tournament Handler Program, and is not itself "FIDE approved"" [TEC26 pp. 14, 40].
  - The changelog entry "2026-07-21 1.10.59" includes: "Contribution from Colin Scheriff, - Added the FIDE Swiss Team Pairing System (C.04.6), including type A and type B colour preferences, automatic TRF selection, and team-corpus coverage." [GAC changelog.txt]. The files are `gacrux/pairingfideteam.py` (623 lines) and `gacrux/crosstablefideteam.py` (286 lines), headed "FIDE C.04.6 [...] approved by the Council on 28/10/2025". The tests are in `tests/test_pairing_fideteam.py` (about 40 article-named tests), plus a corpus of team TRF records. Checked at commit 6419149 (2026-09-19); version 1.10.62.
  - CLI: `pairingchecker.py -p` pairs and `-c` checks, with `-m fideteam` / `fideteam-typeb`. "FIDE team Swiss tournaments are selected automatically from TRF record 192." [GAC README].
  - Its reading of the ambiguous points is documented inline: A1, A6(a), A7, A9, A12. The possible divergence G1 is described in §4.4.
  - It is Python and uses networkx min-weight matching. It is the only third-party oracle available for cross-checking. It is itself new: the team module was added two months before this research.
- The team module was not run for this research. Commercial THPs (Swiss-Manager, Vega and others) run team Swiss events, but I found no primary source saying they implement *C.04.6 (2026)*, so I make no claim about them.

---

## 8. TRF data needed (TRF-2026) [TRF26]

TRF-2026 ("TRF26") is the TEC format that extends TRF16 for team pairing, tie-breaks and in-tournament data exchange (ITDX) [TEC-TRF26-page]. The TEC report says it was "approved by FMB May 2025" [TEC26 p. 13]. ⚠ The PDF itself still reads "Updated on TEC Website TRF26 Online - ??/??/2025, Approved by ??? " [TRF26 p. 1]. Treat field positions as provisional and check them against the TEC sample file [TRFSAMPLE].

What the Team System needs, with the source of each item:

| Need (T26 article) | TRF record(s) |
|---|---|
| System, colour-preference type, primary/secondary score, acceleration (1.2, 1.7) | **192** Encoded Type of Tournament, e.g. `FIDE_TEAM_TYPEA_MP_GP`, `FIDE_TEAM_TYPEB_MP`, `FIDE_TEAM_GP_MP` (no preferences), `FIDE_TEAM_*_BAKU`; `FIDE_TEAM` defaults to `FIDE_TEAM_TYPEA_MP_GP` [TTC192]. The BAKU variants exist only with MP as primary |
| Teams, TPN (1.1), board order, final MP/GP | **310** (new; TPN pos 5-7, name, nickname, strength factor, MP, GP, rank, roster of 001 start ranks). **013** is the legacy team record ("to be phased out"); both are required during the transition [TRF25-draft-post] |
| Per-board results → match and game points, played or unplayed | **001** player records (opponent, colour, result per round; `+`/`-` forfeits, `U` PAB, `Z` rest, `H`/`F` byes) |
| Board-1 colour → team colour (1.6.1) | 001 of the board-1 player. The board order is the 310 default unless a **300** "Out-Of-(default)Order" record lists the actual board line-up (0000 = empty board). **352** gives the board colour pattern (e.g. `WBWB`) |
| Match-point scale | **362** (TW/TD/TL; default 2/1/0) |
| Game-point scale | **162** (W/D/L/A/P/X; defaults 1/0.5/0/0/=W/=D) |
| PAB points and PAB history (1.4, C2) | **320**: "PAB Match Points", "PAB Game Points", then the PAB team per round (example `320 01.0 02.0 000 000 050 ...`) |
| Forfeited matches (C2, colour, C1) | **330**: `+-` / `-+` / `--`, round, White team, Black team |
| FPB/HPB/ZPB (C2 FPB, absence) | **240** (F/H/Z, round, team list); "Mandatory" for teams for F/H |
| Future absences (GHR 3.3), ITDX | 240 for rounds not yet paired |
| Number of rounds ("last round", "last two rounds") | **142** (mandatory for ITDX) |
| Initial colour (4.1) | **152** (W/B) |
| Acceleration (C.04.7) | **250** (fictitious MP and/or GP, round range, TPN range) |
| Prohibited pairings (competition rules) | **260** |
| Abnormal points (penalties, non-standard forfeit scoring) | **299** |
| Informative per-round team view | **801** (variable-width, with board results and RIDs), **802** (fixed-width: opponent or bye acronym, colour, GP, forfeit flag). Optional; they "duplicate some information that already exists" |
| Team count | **082** |

The sample file is a 50-team, 14-round "Grandmommy's Cup" TRF25 sample. It uses `192 FIDE_TEAM_BAKU`, `352 WBWB`, `362 TW 2 TD 1 TL 0` and has 310/013/240/250/260/300/320/330/801/802 lines [TRFSAMPLE]. It is a good parsing fixture. The TEC notes that it "was prepared by hand, so it is very likely that there are errors" (said of the 2024 draft sample) [TRF25-draft-post].

> ⚠ **A13 (deriving the team colour).** No mandatory record states "team X had White in round r" directly. The value is derived: board-1 player (from 310 plus 300) → that player's 001 colour, for played matches only. The informative 801/802 records carry the team colour directly. The library should derive it and treat 801/802 as a cross-check only.

---

## Implications for the library

1. **Its own module, not a Dutch variant.** The criteria, bracket model (no downfloaters), PAB timing (first), floater horizon (one round) and colour rules (no absolutes, post-hoc cascade) all differ from C.04.3. Reuse is limited to the Basic/GHR layer: C1 history, the GHR 3.4 colour-history squeeze, late entries, withdrawals and absences, and the perfect-matching feasibility check.
2. **Structure that mirrors the articles:** `ByeAssigner` (3.4) → loop { `UpfloaterSelector` (3.5: C4 → C5 → C3 filter → C6 → C7 → lexicographic) → `BracketPairer` (3.6: identifier-ordered enumeration, C1 filter, C8 > C9 > C10) } → `ColourAllocator` (4.3.1 … 4.3.9, each rule a named step). Every decision is traceable to an article number, as Gacrux does with `colorrule`.
3. **Colour preferences as a strategy:** `TypeA`, `TypeB` and `None`. Each carries `strength` (simple/strong/mild) because C9 and 4.3.4 need it. They are computed from a squeezed, board-1-based colour history.
4. **Configuration input** (maps from TRF 192/362/162/320/152/142/250): primary score (MP/GP), whether the secondary score is used, preference type, PAB MP/GP, initial colour, number of rounds, acceleration. Reject Baku with GP as primary (C.04.7 1.4.4).
5. **Edition pinning:** support at least `C0406_2026`. `C0406_2024` is an option for replaying old events. TRF 192 does not encode the edition, so choose it from the tournament dates.
6. **The one algorithmic primitive needed:** "does this set of teams admit a perfect matching under C1?" A readable Edmonds blossom, or a JGraphT dependency, covers C3, 3.4.1 and 3.5.5 legality.
7. **Enumeration is fine for readability** (3.5.3/3.5.4 set order; 3.6.2 identifier order). Add pruning, since worst-case cost is exponential (A11).
8. **Verification:** Gacrux is the only oracle and it is not endorsed. Build article-named unit tests from the Handbook examples (3.5.4, 3.6.2). Cross-check against Gacrux on the TEC sample and on generated tournaments. Log every disagreement against A1–A13 and G1 rather than silently following Gacrux.
9. **An interpretation register** (ADR-style) for A1–A13. Endorsement-grade conformance is impossible to claim until TEC rules on at least A6 and A7.

## Open questions

1. **[C6] meaning (A6):** does "comply with [C4] in the bracket where this scoregroup is paired" mean the parity minimum (binary, as in Gacrux) or the fewest upfloaters over the candidate sets (graded)? What happens when the "following scoregroup" was empty from the start? → ask TEC/SPP (secretary.tec@fide.com is the contact listed [FIDE news 2026-03-24]).
2. **"Complies with" for minimisation criteria (A7):** is the lexicographic "first" taken before or after minimising C7/C10 counts that cannot reach zero?
3. **Forfeits:** do they count as "played" for [C1] (A3) and for "matches played" in 3.4.3 (A9)?
4. **Type B last round, CD = 0, last two the same colour (A1):** strong preference or none?
5. **4.3.6 alignment (A12):** right-aligned squeezed histories, or same round number?
6. **Floater status under acceleration (A8):** pairing score or real score?
7. **Gacrux G1:** does Gacrux's (score, TPN) bracket ordering deviate from 3.6.1's pure-TPN top/bottom rule? Verify with a targeted test, and report upstream if confirmed.
8. **TRF26 status:** is the published TRF-2026.pdf final (FMB-approved May 2025 per TEC26) even though it still reads "??/??/2025"? Is a Team System edition code planned for 192?
9. **Endorsement path:** C.04.A has been merged into C.02.03 (TEC26 p. 13). What does the Verification Checklist require for a *pairing engine* (not a THP) for team events? Is a library eligible at all, or only through a THP? (Overlaps with ticket 05-fide-endorsement.)
10. **PAB game points with an odd number of boards** (§2.4): what does "game points rewarded for a draw" mean there? Probably a config value; confirm.

---

## Sources

- **[T26]** FIDE Handbook C.04.6 Swiss Team Pairing System (effective from 1 February 2026), approved by the Council 28/10/2025. https://handbook.fide.com/chapter/SwissTeamPairingSystem202602 (fetched 2026-09-23)
- **[T24]** FIDE Handbook C.04.6 Swiss Team Pairing System (effective till 31 January 2026), approved 27/07/2024, applied from 01/08/2024. https://handbook.fide.com/chapter/SwissTeamPairingSystem082024
- **[BR26]** C.04.1 Basic Rules for Swiss Systems (effective from 1 February 2026). https://handbook.fide.com/chapter/C0401202507 ; previous edition https://handbook.fide.com/chapter/C0401Till2026
- **[GHR26]** C.04.2 General Handling Rules for Swiss Tournaments (effective from 1 February 2026). https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournaments202602 ; previous edition https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournamentsTill2026
- **[ACC26]** C.04.7 FIDE-approved Accelerated Systems (effective from 1 February 2026). https://handbook.fide.com/chapter/C0407202602
- **[OLY]** D.02.02 Olympiad Pairing Rules (effective from 1 January 2022). https://handbook.fide.com/chapter/OlympiadPairingRules2022
- **[C0204]** C.02.04 FIDE Certified and Endorsed Equipment (effective from 1 March 2026), Tournament Handler Programs table. https://handbook.fide.com/chapter/FIDECertifiedAndEndorsedEquipment032026
- **[FEP22]** FIDE Endorsed Programs list (SPP). https://handbook.fide.com/files/handbook/C04Annex3_FEP22.pdf ; TEC endorsement page https://tec.fide.com/endorsement/
- **[TRF26]** TRF-2026 specification. http://tec.fide.com/wp-content/uploads/2025/04/TRF-2026.pdf (linked from **[TEC-TRF26-page]** https://tec.fide.com/trf-2026/)
- **[TTC192]** Tournament Type Code Table (192), TRF26. http://tec.fide.com/wp-content/uploads/2025/04/TournamentTypeCodeTable192-TRF26.pdf
- **[TRF25-draft-post]** TEC, "Draft TRF 2025 (extensions for team pairing and tie-breaks)", 2024-09-04. https://tec.fide.com/2024/09/04/draft-trf-2025-extensions-for-team-pairing-and-tie-breaks/ ; follow-up "TRF25 Final Draft", 2025-01-09. https://tec.fide.com/2025/01/09/trf25-final-draft/
- **[TRFSAMPLE]** TEC TRF25 sample (50-team "Grandmommy's Cup"). http://tec.fide.com/wp-content/uploads/2025/01/GrandMommysCup03_trf.txt
- **[TEC26]** FIDE Technical Commission, 2026 FIDE Congress meeting report (Samarkand). http://tec.fide.com/wp-content/uploads/2026/09/TEC-2026-Congress-Meeting.pdf (pp. 13, 14, 34, 40, 59)
- **[GAC]** Gacrux / TieBreakServer, https://github.com/OttoMilvang/TieBreakServer, commit 6419149ede24fa76639a956b1a52ccac0ded730d (2026-09-19): `gacrux/pairingfideteam.py`, `gacrux/crosstablefideteam.py`, `tests/test_pairing_fideteam.py`, `README.md`, `changelog.txt`, `LICENSE`.
- **[FIDE news 2026-03-24]** "FIDE reminds organizers and arbiters of updated Swiss Rules effective February 1, 2026". https://www.fide.com/fide-reminds-organizers-and-arbiters-of-updated-swiss-rules-effective-from-february-1-2026/
