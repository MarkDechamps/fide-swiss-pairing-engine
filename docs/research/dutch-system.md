# Dutch System (C.04.3) and its reference programs

Research ticket: *Dutch System and its reference programs* (`.wayfinder/fide-swiss-engine/02-dutch-system.md`).
Researched 2026-09-23. Primary sources only. Tags in square brackets point to the source list at the end. Anything marked **AMBIGUITY** or **FLAG** needs a decision or a follow-up.

## TL;DR

- The Dutch System in force is **C.04.3 "effective from 1 February 2026"**. The FIDE Council approved it on 28/10/2025 [H-C0403]. The old text (Baku 2016, with Goynuk 2017 additions) applied until 31 January 2026 [H-C0403-old].
- The 2026 edition renumbers the criteria to **[C1]–[C21]** and reorganises the text into Articles 0–5. There are two behavioural changes. (1) The completion criterion [C4] now applies to every bracket, which removes the PPB/CLB/collapsed-scoregroup route and the PSD concept. (2) There is a new PAB criterion [C5] (lowest score) plus [C9] (fewest unplayed games). The float definitions and the MDP-selection rule have also changed [H-C0403], [ANN p.5–6].
- FIDE endorses **tournament programs**, not pairing engines. In the endorsed-programs table, **JaVaFo** is the Dutch engine for Vega, SwissMaster, Swiss-Manager, UTU Swiss, ChessManager and STOP. **bbpPairings** is the engine for SwissSys. Swiss-Chess uses an internal engine [FEP]. Chess Online (COPP) was endorsed in 2024 and uses JaVaFo [TEC-END], [COPP].
- **JaVaFo** (Roberto Ricca) is at **2.2 Build 3222**, dated 2017-10-29 / first published 2018-09-15. It implements the **2017 rules**: its own generated TRF says `092 Individual Swiss Dutch 2017 rules`, and its code package is `javafo/pairings2017`. It is closed source and obfuscated, "free of charge" with an attribution request, and has no redistribution licence [JVF], [JVF-AUM], (verified locally).
- **bbpPairings** (Jeremy Bierema) is **v6.0.0 (2026-02-01)**. It implements the "2025 rules for the Dutch system (the effective date for the rules was delayed to 2026)" and uses TRF-2026 I/O. The source is **Apache-2.0**. It always pairs with a **weighted maximum matching** in which each criterion is encoded as a bit field of the edge weight [BBP-README], [BBP-SRC].
- Local experiment: JaVaFo 2.2 and bbpPairings v5.0.1 (2017 rules) agree on **461/461** rounds. bbpPairings v6.0.0 disagrees with both on **90/461** rounds of JaVaFo-generated tournaments and **9/200** rounds of its own generated tournaments. **As of 2026-09 only bbpPairings v6 is a usable oracle for the rules in force.** JaVaFo 2.2 can only serve as a legacy-rules oracle.

---

## 1. Edition in force and history

| Edition | Status | Source |
|---|---|---|
| C.04.3 "effective from 1 February 2026" | "Approved by the Council on 28/10/2025, Applied from 1st February, 2026" | [H-C0403] |
| C.04.3 "effective till 31 January 2026" | "Version approved at the 87th FIDE Congress in Baku 2016. Terms and Definitions and Pairing Guidelines For Programmers added at the 88th FIDE Congress in Goynuk 2017" | [H-C0403-old] |
| Earlier | 2013 rules (Krakow/Istanbul/Tallinn 2011–2013), Abu Dhabi 2015 amendments, Baku 2016 full rewording | [JVF] history section; [MTD p.5] |

C.04.1 (Basic rules) and C.04.2 (General handling rules) were replaced at the same time, with the same approval date and effective date [H-C0401], [H-C0402]. C.04.3 depends on them for C1/C2 (C.04.1 Art. 2, 4), the PAB (C.04.1 Art. 3), the colour limits (C.04.1 Art. 6, 7), TPN (C.04.2 Art. 2) and colour history with unplayed rounds (C.04.2 Art. 3.4).

**AMBIGUITY (dating).** The approval date is given three ways. The Handbook says Council approval on 28/10/2025 [H-C0403]. bbpPairings calls them "the 2025 rules … effective date delayed to 2026" [BBP-README]. *Mastering the Dutch 2026* says "In year 2024, the Rules were modified again" [MTD p.5]. For naming, use "2026 edition (effective 2026-02-01)", which matches the Handbook title.

**FLAG (missing T&D).** Article 0 of the 2026 text defers "Terms and Definitions" to `https://tec.fide.com/2025-fide-dutch-terms-and-definitions`. That URL returns **HTTP 404**, with or without the trailing slash, and so does the TEC "Glossary2025.pdf" link. The old "Pairing Guidelines for Programmers" were hosted on spp.fide.com, which showed "Briefly unavailable for scheduled maintenance" on 2026-09-23. Neither document could be read.

### 1.1 What changed (2016/17 → 2026)

These are a diff of [H-C0403-old] against [H-C0403], with the TEC commentary noted where it applies.

1. **Structure and numbering.** The lettered sections A–E and the criteria C.1–C.19 become numbered Articles 0–5 and criteria **[C1]–[C21]**. The pairing criteria (Art. 2) now come *before* the bracket procedure (Art. 3).
2. **Completion is universal.** Old C.4 applied only in the Penultimate Pairing Bracket. It was supported by the "collapsed" scoregroup, the PPB and the CLB (old A.3, A.9, C.4, C.7). New **[C4]** says "A pairing complying with all the absolute criteria … shall always exist for all players not yet paired". It applies to every bracket, and the PPB/CLB route no longer exists [ANN p.5, p.21–22]. The TEC commentary says this "can modify the resulting pairings but does not overturn them" [ANN p.5].
3. **PSD removed.** Old A.8 PSD and C.6 "minimize the PSD" are replaced by **[C7]** "Minimise the scores (taken in descending order) of the downfloaters" [ANN p.23: "could renounce the old concept of PSD in favour of a simpler way to obtain the same results"].
4. **Next-bracket look-ahead broadened.** Old C.7 covered only the number of pairs and PSD in the next bracket, and did not apply in the PPB/CLB. New **[C8]** says "Choose the set of downfloaters so that in the following bracket every criterion from [C1] to [C7] … is complied with".
5. **New PAB criteria.** **[C5]** "Minimise the score of the assignee of the pairing-allocated-bye" sits between the completion criterion and the quality criteria. It is "a critical change, which can significantly modify the resulting pairing" [ANN p.5–6]. **[C9]** "Minimise the number of unplayed games of the assignee of the PAB" is limited by a note to "brackets that downfloat exactly one player, who will end up receiving the pairing-allocated bye".
6. **C2 widened.** The old wording was "already scored a (forfeit) win due to an opponent not appearing in time". The new wording is "already scored in one single round, without playing, as many points as rewarded for a win" (C.04.1 Art. 4). Per [ANN p.7] this now also covers the full-point bye, but not requested half-point byes.
7. **Float definitions changed.**
   - Old A.4.b: "A player who, for whatever reason, does not play in a round, also receives a downfloat."
   - New 1.4.3: a downfloat goes to a PAB recipient or to anyone "who, without playing in a round, scores more points than those rewarded for a loss". New 1.4.4: "No players other than those listed … can receive floats."
   - Consequence: a zero-point absence **no longer** creates a downfloat. A half-point bye does.
8. **Float criteria re-scoped.**
   - Old C.12–C.15 counted "players who receive the same downfloat/upfloat as …".
   - New [C14]/[C16] count **resident downfloaters** who received a downfloat 1 or 2 rounds before. New [C15]/[C17] count **MDP opponents** who received an upfloat 1 or 2 rounds before. [ANN p.30] says [C14]/[C16] deliberately do not protect MDPs.
   - [C18]–[C21] are now phrased as "score differences (taken in descending order) of MDPs / MDP opponents".
9. **MDP selection.** Old D.3 "MDP-exchanges" between S1 and Limbo, sorted by highest score then lexicographic BSN, are replaced by Art. 4.4. A set of pairable MDPs is "valid if it leaves a Limbo compliant with [C7]". Valid sets are "sorted according to their smallest differing BSN". S1 of a heterogeneous bracket is now "the first set of M1 pairable MDPs as defined by Article 4.4.2" (3.2.2).
10. **Definition of M1.** Old: "the **maximum** number of MDP(s) that can be paired". New: "the number of MDP(s) that are paired in the bracket" (3.1.3).
11. **Best-candidate rule** (3.8.1) now names [C5] as well as [C6]–[C21].
12. **Pairing numbers → TPN.** The C.04.2 Art. 2 ordering is now rating, then title, then alphabetical. The old "pairing numbers … subsequent modifications" wording is gone [H-C0402 2.2–2.3], [ANN p.6].
13. **Wording.** Gender-neutral wording, and colour rules E.1–E.5 renumbered as 5.2.1–5.2.5. The colour logic itself is unchanged.

---

## 2. The 2026 text, condensed (normative content)

All article numbers refer to [H-C0403] unless stated otherwise.

### 2.1 Definitions (Art. 1)

- **Order (1.2).** Players are ranked by score, then by TPN ascending.
- **Scoregroup (1.3.1).** All players with the same score.
- **Bracket (1.3.2).** The *resident* players of a non-empty scoregroup plus the players left unpaired by the previous bracket. A bracket is homogeneous if every player has the same score, otherwise heterogeneous (1.3.3).
- **Remainder (1.3.4).** A sub-bracket of the residents of a heterogeneous bracket, left after the MDP-Pairing.
- **Downfloater / MDP (1.4.1).** A downfloater is a player left unpaired and moved to the next bracket. In that next bracket they are called a moved-down player (MDP).
- **Floats (1.4.2–1.4.4).**
  - When players with different scores meet, the higher-ranked one gets a downfloat and the other an upfloat.
  - A downfloat also goes to a PAB recipient, and to anyone who scores more than a loss without playing.
  - Nobody else receives floats.
- **PAB (1.5 → C.04.1 Art. 3).** No opponent, no colour, and a win's points unless the tournament rules say otherwise. The value must be the same for every PAB.
- **Colour difference (1.6).** Games with White minus games with Black.
- **Colour preference (1.7).**
  - **Absolute:** |CD| > 1, or the same colour in the two latest *played* rounds.
  - **Strong:** CD = ±1, preferring the colour that reduces |CD|.
  - **Mild:** CD = 0, preferring to alternate from the last game.
  - **None:** the player has not played a game yet.
  - [ANN p.18]: unplayed rounds are skipped per C.04.2 3.4 when working out the preference.
- **Topscorers (1.8).** Players with more than 50% of the maximum possible score, *when pairing the final round* only.
- **Round-pairing outlook (1.9).**
  - A pairing is complete if every player except at most one is paired and [C1]–[C3] hold. The one exception "downfloats from the last bracket and receives the PAB".
  - Pairing runs bracket by bracket from the top scoregroup down.
  - If no complete pairing exists, the Chief Arbiter decides (1.9.3).

### 2.2 Criteria (Art. 2)

| # | Kind | Text (abridged) |
|---|---|---|
| C1 | absolute | Two participants shall not meet more than once (C.04.1 Art. 2). |
| C2 | absolute | A participant who already had a PAB, or who scored a win's points in one round without playing, cannot get the PAB (C.04.1 Art. 4). |
| C3 | absolute | Non-topscorers with the same absolute colour preference shall not meet (C.04.1 Art. 6, 7). |
| C4 | completion | A pairing complying with C1–C3 shall always exist for all players not yet paired. |
| C5 | PAB | Minimise the score of the PAB assignee. |
| C6 | quality | Minimise the number of downfloaters (= maximise pairs). |
| C7 | quality | Minimise the scores of downfloaters, taken in descending order. |
| C8 | quality | Choose the downfloaters so that every criterion C1–C7 is complied with in the following bracket. |
| C9 | quality | Minimise the unplayed games of the PAB assignee. Applies only to brackets that downfloat exactly one player who ends up with the PAB. |
| C10 | quality | Minimise topscorers or topscorers' opponents who get CD > +2 or < −2. |
| C11 | quality | Minimise topscorers or topscorers' opponents who get the same colour three times in a row. |
| C12 | quality | Minimise players who do not get their colour preference. |
| C13 | quality | Minimise players who do not get their *strong* colour preference. |
| C14 | quality | Minimise resident downfloaters who had a downfloat in the previous round. |
| C15 | quality | Minimise MDP opponents who had an upfloat in the previous round. |
| C16 | quality | Minimise resident downfloaters who had a downfloat two rounds before. |
| C17 | quality | Minimise MDP opponents who had an upfloat two rounds before. |
| C18 | quality | Minimise the score differences (descending) of MDPs who had a downfloat in the previous round. |
| C19 | quality | Minimise the score differences (descending) of MDP opponents who had an upfloat in the previous round. |
| C20 | quality | As C18, for two rounds before. |
| C21 | quality | As C19, for two rounds before. |

Candidate comparison works as follows. For each criterion, in priority order, compare a "failure value", and the first difference decides. If every criterion ties, the candidate generated earlier wins [H-C0403 3.8.1], [ANN p.22–23, p.36].

### 2.3 Bracket procedure (Art. 3)

- **Parameters (3.1).**
  - **M0** is the number of MDPs coming in.
  - **MaxPairs** is the maximum number of pairs the bracket can produce under C6. It is usually ⌊N/2⌋, but at most the number of residents when M0 exceeds them.
  - **M1** is the number of MDPs that end up paired, with M1 ≤ MaxPairs.
  - [ANN p.31–33] says MaxPairs and M1 are constants of the bracket. They are not known in advance and have to be "divined", for example with the "Refloaters Score List".
- **Subgroups (3.2).**
  - In a homogeneous bracket, S1 holds the first MaxPairs players.
  - In a heterogeneous bracket, S1 holds "the first set of M1 pairable MDPs as defined by 4.4.2".
  - S2 holds the remaining residents.
  - MDPs in neither subgroup are in the **Limbo** and are bound to float again.
- **Candidate (3.3).**
  - Pair S1[i] with S2[i].
  - Homogeneous bracket: those pairs plus the unpaired players form the candidate.
  - Heterogeneous bracket: the pairs form the **MDP-Pairing**, and the leftover residents form the **remainder**, which is paired like a homogeneous bracket.
  - Limbo players are downfloaters.
- **Evaluation (3.4).** A candidate that satisfies C1–C5 and fulfils C6–C21 is **perfect** and is accepted immediately.
- **Alterations (3.5–3.7).**
  - Homogeneous bracket or remainder (3.6): try each transposition of S2 in turn. When they run out, apply the next resident exchange between the *original* S1 and S2, re-sort both subgroups, and start the transpositions again.
  - Heterogeneous bracket (3.7): first exhaust the remainder's transpositions and exchanges, working from its original S1R/S2R. Then take the next transposition of S2, which gives a new MDP-Pairing and a new remainder. When those run out, take the next valid MDP set for S1 (only possible if there is a Limbo) and restore S2.
- **No perfect candidate (3.8).** Take the best candidate by criteria priority. Ties go to the earliest in generation order.

### 2.4 Generation order (Art. 4)

- **BSN (4.1).** Players in the bracket, or in the remainder, get consecutive in-bracket numbers 1, 2, 3, … in Art. 1.2 order. [ANN p.41]: the remainder gets *fresh* BSNs.
- **Transpositions (4.2).**
  - Permutations of S2 (residents only), sorted lexicographically on their first N1 BSNs, where N1 = |S1|.
  - Worked examples in the text: an 11-player homogeneous bracket has 720 transpositions. A heterogeneous one with 2 MDPs has 72.
- **Resident exchanges (4.3).** Swap equal-size sets between the original S1 and S2, ordered by:
  1. fewest BSNs exchanged;
  2. smallest (ΣS2→S1 − ΣS1→S2);
  3. largest differing BSN moved from S1 to S2;
  4. smallest differing BSN moved from S2 to S1.
- **Useful exchanges (non-normative).** [ANN p.38] adds two shortcuts. Exchanging more than ⌊|S2|/2⌋ players is useless. Any pair in which the S1 player has a higher BSN than the S2 player was already evaluated earlier.
- **MDP sets (4.4).** A set is valid if the Limbo it leaves complies with C7. [ANN p.44] adds that it must also satisfy C4. Valid sets are sorted by smallest differing BSN, for example {1,3} < {1,4} < {3,4} [ANN p.44–45].
- **Next element (4.5).** Each application of 4.2–4.4 takes the next element in its order.

**Methods are free.** "The pairing can be made even with completely different methods, provided it yields identical results to those given by the algorithm described here" [ANN p.5]. Also: "both the arbiter and the programmer enjoy complete freedom in choosing their preferred method … (look-ahead, backtracking, weighted matching or other)" [ANN p.20]. [ANN p.40] describes an equivalent "Sieve pairing": enumerate every legal pairing, filter criterion by criterion, and break remaining ties by generation order.

### 2.5 Colour allocation (Art. 5)

- **Initial colour (5.1).** Drawn by lot before round 1.
- **Per pair, in priority order (5.2):**
  1. Grant both preferences.
  2. Grant the stronger preference. If both are absolute (topscorers only), grant the wider colour difference.
  3. Alternate relative to the most recent round in which one player had White and the other Black, applying C.04.2 3.4, which pushes unplayed rounds to the front of the history.
  4. Grant the higher-ranked player's preference.
  5. If the higher-ranked player has an odd TPN, give them the initial colour; otherwise the opposite colour.
- [ANN p.46] adds two points. For 5.2.4, the higher-ranked player gets *their* preferred colour, not necessarily White. Colour histories are compared per C.04.2 3.4: `uuWB` is equivalent to both `BWWB` and `WBWB`, but those two are not equivalent to each other.

### 2.6 Ambiguities and gaps in the text

1. **C8 "complied with".** C6 and C7 are minimisation criteria, so "complied with" can only mean "optimised" in the next bracket. [ANN p.25–26] reads it as: first maximise pairs in the next bracket, then minimise its downfloater scores, looking one bracket ahead only. bbpPairings encodes exactly that as "Maximize the number of pairs in the next bracket" followed by "Maximize the scores paired in the next bracket" [BBP-SRC dutch.cpp].
2. **C9 scope.** The note ("apply to brackets that downfloat exactly one player, who will end up receiving the PAB") is deliberately partial. [ANN p.26] says a general application "is impractical". The exact operational boundary is **not** defined normatively. bbpPairings needed several commits to settle it (2025-05 to 2025-07: "Work on C9", "Fix minimization of bye assignee unplayed games in lower brackets") [BBP-GIT].
3. **Score difference of a downfloater (C18/C20).** This is undefined in the text. [ANN p.31]: "Although the score difference of a downfloater in not explicitly defined by the rules, we should assume that it is greater than any score difference of a resident player." That is commentary, not rule.
4. **MaxPairs / M1 are not constructive.** The rules define them as outcomes, and the commentary says to guess them and iterate [ANN p.31–33]. An implementation has to derive them, for example with a maximum-cardinality matching.
5. **Candidate order in heterogeneous brackets.** [ANN p.33] says "each candidate must be regarded as a whole unit". Generation order is the *nested* order of 3.7: MDP set, then S2 transposition, then remainder exchange, then remainder transposition.
6. **Dangling reference.** [ANN p.40] cites "Article 4.3.3" for the claim that any transposition beats any exchange. The Handbook text has no 4.3.3; the order follows from 3.6.1 anyway.
7. **"Topscorers' opponents" in C10/C11.** [ANN p.27] states that a non-topscorer paired with a topscorer can lose their absolute preference, which the literal C3 allows only for non-topscorer pairs.
8. **Unreadable T&D document.** See FLAG in §1.

---

## 3. Programs and endorsement

### 3.1 What FIDE endorses

- C.04.2 1.4 says pairings must be reproducible by "different tournament handler programs approved by FIDE". A system is "deprecated, unless a tournament handler program approved by FIDE is available for them, provided with a free pairing-checker" [H-C0402].
- The endorsement procedure (C.04 Appendix A, as quoted in [COPP]) endorses a *program* "for the specific pairing systems". It requires a FIDE mode, TRF import/export, a public free pairings checker (FPC) and a public random tournament generator (RTG).
- **FIDE Endorsed Programs table** [FEP] (handbook file `C04Annex3_FEP22.pdf`):

| Program | Dutch engine | Endorsed |
|---|---|---|
| Vega (Forlano) | JaVaFo | Goynuk, Oct 2017 |
| SwissSys (Suits) | **bbpPairings** | Minsk, Apr 2018 |
| SwissMaster (KNSB) | JaVaFo | Minsk, Apr 2018 |
| Swiss-Manager (Herzog) | JaVaFo | Minsk, Apr 2018 |
| Swiss-Chess (Weber) | Internal | Minsk, Apr 2018 |
| UTU Swiss, ChessManager, STOP | JaVaFo | Abu Dhabi, Feb 2020 |

- The TEC "FIDE Endorsed" page lists certificates for Vega 7.6.0 (2017), SwissSys 9.6, Swiss Manager 13, Swiss Master 5.7, Swiss-Chess 9.05 (2017–2018) and **Chess Online 7.7 (2024-04-13)** [TEC-END]. The COPP report says COPP "uses JaVaFo as its pairing engine". It adds that this "is not a 100% guarantee … JaVaFo has some very minor weaknesses in normal pairings when compared to" bbpPairings [COPP].
- **FLAG.** Every listed endorsement predates the 2026 rules. I found no TEC statement (as of 2026-09-23) on re-endorsement under C.04.3-2026, or on which engines are currently endorsed for it. The endorsement ticket (`05-fide-endorsement`) should confirm this.

### 3.2 JaVaFo

- **Author and ownership.** "authored and intellectually owned by Roberto Ricca", International Arbiter. The undated page calls him "current Secretary of the FIDE Commission \"Systems of Pairings and Programs\"" [JVF]; that commission's functions have since passed to the TEC [COPP]. Ricca is also the technical reviewer of the TEC's 2026 Dutch companion documents [MTD p.5].
- **Version.**
  - Current: "2.2 (made available for the first time on September 15th, 2018 – although programs relying on JaVaFo have started using it since November 2017)" [JVF].
  - Verified locally: `https://www.rrweb.org/javafo/current/javafo.jar` (sha256 `6d1eef8f…b247c`) prints `JaVaFo (rrweb.org/javafo) - Rel. 2.2 (Build 3222)`, and its inner `main.jar` is dated 2017-10-29.
  - No 2.3 or 3.x path exists: `/javafo/{2.3,2.4,3.0,3.1,2026}/javafo.jar` all return 404.
- **Rules implemented: 2017 edition.**
  - JaVaFo's RTG writes `092 Individual  Swiss Dutch 2017 rules` into its files.
  - The engine class is `javafo.pairings2017.JaVaFo` (verified locally).
  - The AUM check-list legend cites the old article letters (A.6.a etc.) [JVF-AUM].
  - **JaVaFo does not implement the rules in force.**
- **Licence.**
  - "JaVaFo is free of charge. Just mention rrweb.org/javafo when using it in a commercial product and drop a note to the author" [JVF].
  - No source code, and the bytecode is obfuscated (package names like `B/A/A/C`). No redistribution grant is stated.
  - The jar bundles One-JAR, which has its own licence (`doc/one-jar-license.txt`).
- **Invocation** [JVF-AUM]. The AUM uses `javafo` as shorthand for `java -ea -jar javafo.jar`. Requires Java 7 or later; runs on Temurin 17.
  ```
  javafo [-r]
  javafo [-r] input-file -c [round-number]                     # free pairings checker (FPC)
  javafo [-r] input-file [-b] -p [output-file] [-l [check-list-file]]
  javafo [-r] [model-file] -g [-b] -o trf-file                 # random tournament generator (RTG)
  javafo [-r] -g config-file|seed [-b] -o trf-file
  ```
  - `-b` applies the Baku acceleration (old C.04.5.1).
  - Output: the first line is the number of pairs P, then one "white black" pair per line using TRF pairing ids. The bye appears as `id 0`.
  - Java API: `String JaVaFoApi.exec(int op, Object... params)`, with ops 1000 pairing, 1001 Baku, 1100/1110/1111 check-lists, 1200 check tournament, 1210 check one round, 1300/1301 RTG. The AUM calls it "experimental".
- **TRF(x) conventions** (extensions to TRF16, UTF-8) [JVF-AUM]:
  - The partial TRF up to the last played round is the input. Points (cols 81–84) must be correct.
  - `XXR n` gives the total number of rounds. It is required, because the engine must know whether this is the last round (topscorers).
  - `XXC [rank] [white1|black1]` is configuration:
    - `rank`: pair by position in the file rather than by pairing id.
    - `white1`/`black1`: the initial colour. Without it, JaVaFo picks a *hash-of-TRF* pseudo-random colour.
  - Absence in the round being paired: `XXZ ids…`, or put `0000 - Z` (or `H`/`F`, with Points updated) in the round column.
  - `XXS CODE=VALUE …` sets the scoring system (WW, BW, WD, BD, WL, BL, ZPB, HPB, FPB, PAB, FW, FL, W, D). With XXS present, Points are strictly checked.
  - `XXA id pp.p …` gives accelerated (fictitious) points per round; the full history is needed for float history.
  - `XXP ids…` lists mutually forbidden pairs.
  - The checker output lists "Checker pairings" next to "Tournament pairings" for each discrepant round.

### 3.3 bbpPairings

- **Author and repository.** Jeremy Bierema, "Bierema Boyz Programming", https://github.com/BieremaBoyzProgramming/bbpPairings. The code is C++; the Makefile builds with MinGW and GCC/clang.
- **Version.**
  - Release **v6.0.0**, published 2026-02-01, with notes "Switch to 2025 Dutch rules (effective date in 2026)" and "Add initial support for TRF-2026".
  - `master` (2026-07-30) has unreleased TRF-2026 fixes: lines `162` and `192`, from GitHub user `zyzniewski`, presumably ChessManager's author Tomasz Żyźniewski [FEP]; this is inferred, not verified [BBP-GIT].
  - Earlier releases: v5.0.1 (2023-02-26) implements "the 2017 rules for the Dutch system" (from its README; verified locally).
- **Scope.** Dutch (2026) plus "a flawed implementation of a previous version of the Burstein system", which "has not been endorsed by FIDE" [BBP-README]. It is an engine only, not a tournament manager.
- **Licence.**
  - "The source code of BBP Pairings is released under the Apache License, Version 2.0". Official builds may append further terms to LICENSE.txt [BBP-LIC].
  - The v6.0.0 x86_64-linux build appends nothing (verified).
  - GitHub's licence detector reports `NOASSERTION` because of the custom LICENSE.txt wrapper; Apache-2.0.txt is included.
- **Binaries.** Linux i386/x86_64 tarballs and Windows i686/x86_64 zips on GitHub Releases. `bbpPairings-v6.0.0-x86_64-pc-linux.tar.gz` runs as-is (`-r` → `v6.0.0 (Built Feb 1 2026 17:54:55)`).
- **Invocation** [BBP-README]:
  ```
  bbpPairings.exe [-r]
  bbpPairings.exe [-r] (--burstein | --dutch) input-file -c [-l [check-list-file]]
  bbpPairings.exe [-r] (--burstein | --dutch) input-file -p [output-file] [-l [check-list-file]]
  bbpPairings.exe [-r] (--burstein | --dutch) (model-file -g | -g [config-file]) -o trf_file [-s random_seed] [-l [check-list-file]]
  ```
  - Exit codes: 0 ok, 1 no valid pairing, 2 unexpected error, 3 invalid request or file, 4 size limit, 5 file access.
  - The output format is the same as JaVaFo's.
  - It has no JaVaFo `-w`/`-q` options, because "BBP Pairings always performs pairings using a weighted matching algorithm".
  - It does not choose a random initial colour. If none is given, it *infers* one from the round-1 colours of the highest-ranked player.
- **File conventions** [BBP-README], [BBP-SRC trf.cpp]:
  - Primary format is **TRF-2026**. Recognised lines: `001`, `142` (rounds), `152 W|B` (initial colour), `162` (point system), `192 FIDE_DUTCH[_2025|_2026][_BAKU]` (system), `240` (byes/absences), `250` (accelerations), `260` (forbidden pairs). `013`/`310` (teams) and `299` are rejected.
  - Legacy **TRF(bx)** input: `XXR`, `XXC rank|white1|black1`, `XXA`, `XXP`, plus bbp's own `BBW/BBD/BBL/BBZ/BBF/BBU` point values.
  - bbp checks that every score is consistent with the results and refuses to proceed if not.
  - Output uses TRF-2026 codes, with **CR-only** line terminators (`'\r'` in the writer).
  - **FLAG (verified locally).** Unknown lines are silently ignored, including JaVaFo's `XXZ` and `XXS`. A JaVaFo-style file that marks an absence with `XXZ 4` makes bbp pair player 4; the `0000 - Z` round-column form works in both engines. A shared oracle corpus must mark absences in the round column (or with TRF-2026 `240`) and use `BB*`/`162` for point systems, not `XXS`.
- **Algorithm** [BBP-README], [BBP-SRC dutch.cpp]:
  - The core is the simpler of the two weighted-matching algorithms in Galil, Micali & Gabow (1986), "An O(EV log V) Algorithm for Finding a Maximal Weighted Matching in General Graphs". Claimed complexity for one Dutch round: O(n³·s²·log n), where s is the number of occupied scoregroups.
  - For each bracket, it builds a multi-precision edge weight whose bit fields, from high to low, encode:
    - compatibility (C1/C3; weight 0 means forbidden);
    - "completion requirement and bye eligibility" (C4, C2, C5);
    - pairs in the current bracket (C6);
    - scores paired (C7);
    - pairs and scores in the next bracket (C8);
    - the bye assignee's unplayed games (C9);
    - colour bits (C10–C13);
    - repeated floats from 1 and 2 rounds back (C14–C17);
    - score-weighted repeated floats (C18–C21);
    - low-order room for the ordering requirements.
  - It then emulates the Art. 3–4 *generation order*. It fixes decisions greedily and re-solves the matching after each one. Step by step, it chooses which MDPs are paired, then the MDPs' opponents, then the exchanges ("Minimize the number of exchanges", "Minimize the difference of the exchanged BSNs", then which lower S1 / higher S2 players to exchange), and finally the S2 partner of each S1 player with priority to higher players.
  - It is **not** a literal enumeration of transpositions and exchanges. The rules allow this ([ANN p.5, p.20]), but correctness depends on the weight encoding reproducing the lexicographic criteria and the earliest-generated tie-break.
- **Tests shipped.** Only four regression fixtures: `dutch_2025_C5`, `dutch_2025_C9`, `issue_7`, `issue_15` (input TRF plus expected pairing output) [BBP-SRC test/tests]. There is no public conformance suite.

### 3.4 Other Dutch engines

- The only other engine in [FEP] is Swiss-Chess's "Internal" one. It is commercial, with no public checker I could confirm (not researched further).
- Vega uses JaVaFo for Dutch, with an internal engine for Dubov only [FEP].
- I found no other open-source engine with FIDE standing in primary FIDE sources.

---

## 4. Using them as test oracles (local experiment)

Setup: JaVaFo 2.2 b3222, bbpPairings v5.0.1 (2017 rules) and bbpPairings v6.0.0 (2026 rules), on Linux x86_64 with Temurin 17. Each generator produced tournaments from fixed seeds, and each engine then ran in checker mode (`-c`), which re-pairs every round from the actual history.

| Corpus | Rounds | JaVaFo 2.2 | bbp v5.0.1 | bbp v6.0.0 |
|---|---|---|---|---|
| JaVaFo RTG, seeds 1001–1040 (`javafo -g <seed> -o …`) | 461 | (generator) | **0** discrepant rounds | **90** discrepant rounds (36/40 tournaments) |
| bbp v6 RTG, seeds 2001–2040 (`--dutch -g -o … -s <seed>`) | 200 | **9** discrepant | **9** discrepant | 0 (self) |

Notes:
- JaVaFo 2.2 and bbp v5.0.1 agreed on every round in both corpora, and they flagged the same 9 rounds in the bbp v6 corpus. Two independent implementations of the 2017 rules therefore agree fully, which supports using them as a cross-check pair for the old edition. JaVaFo's own page reports "thousands of positive tests … against bbpPairings" [JVF].
- The 2026 rules change the result in roughly 5–20% of rounds, depending on how often the generator produces byes and forfeits. JaVaFo's RTG uses forfeit, ZPB and HPB rates, which exercise the changed PAB and float rules. Example: in bbp's own `dutch_2025_C5` fixture, JaVaFo gives the PAB to a 1-point player, while bbp v6 gives it to the 0-point player that [C5] requires.
- To feed a bbp-generated file to JaVaFo, I had to convert CR line endings to LF, drop the `092` line and add `XXR`.
- Reproduce: `bbpPairings.exe --dutch -g -o t.trf -s <seed>` then `bbpPairings.exe --dutch t.trf -c`; and `java -ea -jar javafo.jar -g <seed> -o t.trf` then `java -ea -jar javafo.jar t.trf -c`.

**Oracle suitability**

| | bbpPairings v6 | JaVaFo 2.2 |
|---|---|---|
| Rules in force (2026) | yes | **no** (2017) |
| Download | GitHub Releases, stable URLs, binaries for Linux and Windows | `rrweb.org/javafo/current/javafo.jar` (plain curl gets HTTP 999; a browser User-Agent works) |
| Licence for CI use | Apache-2.0: can be fetched, cached or even vendored with the licence and notice | "free of charge" plus an attribution request. No redistribution grant, so **download at test time; do not vendor** |
| Checker / RTG | `-c`, `-g`, seeded (`-s`) | `-c`, `-g`, seeded |
| Independence | a single implementation of 2026, with no second 2026 engine to cross-check | n/a for 2026 |

---

## Implications for the library

1. **Target C.04.3-2026 (effective 2026-02-01) as the default edition.** Consider a separate, explicitly named legacy edition (2017) only if the verification strategy needs it. That strategy should weigh that JaVaFo and bbp v5 give an **agreeing two-engine oracle** for 2017, which 2026 does not have.
2. **Primary oracle: bbpPairings v6.x** in checker mode over RTG corpora and over our own TRF fixtures. Pin exact versions (v6.0.0 or a later tag) and record checksums. Treat each disagreement as a triage item, *not* as proof that we are wrong: bbp is a single implementation of new, still-settling criteria (C9 took several commits).
3. **JaVaFo as a legacy oracle only.** Fetch it at test time, never commit the jar, add a note crediting rrweb.org/javafo, and keep it out of anything we distribute. Its API is closed and obfuscated, so we cannot learn from its internals.
4. **Readability-first implementation.** Implement Art. 3–4 *literally*: BSNs, S1/S2/Limbo, transpositions, resident exchanges, MDP-set order, and a lexicographic failure-value vector for C5–C21 with earliest-generated tie-break. The Handbook describes this reference process, and it is what arbiters explain (C.04.1 Art. 9). Brute-force enumeration blows up on large brackets (an 11-player bracket already has 720 transpositions), so plan for **pruning**:
   - the [ANN p.38] exchange bounds;
   - early rejection of MDP-Pairings that already break C1/C3;
   - a separate legality oracle (maximum-cardinality matching) for C4 completion tests, and to derive MaxPairs and M1.
   Keep bbp's weighted-matching approach as an optional fast path, or as a differential cross-check, but not as the specification.
5. **Model the domain with the 2026 vocabulary.** Terms: TPN, resident, MDP, Limbo, remainder, S1R/S2R, PAB-candidate scoregroup, topscorer, and float kinds per 1.4.2–1.4.4. Floats are computed from the round-by-round tournament schedule; colour preferences come from *played* games only (C.04.2 3.4).
6. **Handle the I/O traps:**
   - always emit an explicit initial colour (`152` / `XXC white1|black1`), because JaVaFo's default is hash-random and bbp infers one;
   - always emit the number of rounds (`142` / `XXR`);
   - mark absences in round columns or with `240`, never `XXZ`, which bbp ignores;
   - make points consistent;
   - accept CR, LF and CRLF line endings.
   The TRF ticket (`06-trf-format`) should settle the canonical dialect.
7. **Build our own conformance fixtures.** The TEC worked examples ([MTD] nine-round tournament; [ANN] examples for C7, C13, C18–C21, MDP sets and exchange order) and bbp's four fixtures are the only public 2026 expected outputs found.

## Open questions

1. **Is there, or will there be, a 2026-rules JaVaFo?** None is published at rrweb.org as of 2026-09-23, although its author reviewed the 2026 TEC documents. Ask the TEC/Ricca directly.
2. **Which programs and engines are FIDE-endorsed for C.04.3-2026?** All listed endorsements are 2017–2024 and predate the new rules. Does the endorsement lapse or carry over? This is for the endorsement ticket.
3. **Where is the normative "Terms and Definitions (2025)"?** The Handbook links to a URL that returns 404. Does it contain anything normative beyond Art. 1, such as a definition of a downfloater's "score difference" (C18/C20) or the exact scope of C9?
4. **Exact operational scope of C9** (which brackets, and what happens when several players downfloat towards the PAB). It is under-specified in the Handbook; commentary only [ANN p.26].
5. **Is bbp v6 fully conformant?** There is no independent 2026 engine and no official 2026 test suite. Does the TEC hold an endorsement test corpus (FPC/RTG-based, VCL) we could obtain?
6. **Licence permission for JaVaFo in open-source CI.** Is downloading it at test time within "free of charge" use, and may we publish JaVaFo-derived expected outputs as fixtures? We should ask the author.
7. **Should the library also ship a 2017-edition mode** (for re-checking pre-2026 tournaments), or 2026 only?

---

## Sources

- **[H-C0403]** FIDE Handbook C.04.3 FIDE (Dutch) System (effective from 1 February 2026). https://handbook.fide.com/chapter/C0403202602
- **[H-C0403-old]** FIDE Handbook C.04.3 (effective till 31 January 2026). https://handbook.fide.com/chapter/C0403Till2026
- **[H-C0401]** FIDE Handbook C.04.1 Basic rules for Swiss Systems (effective from 1 February 2026). https://handbook.fide.com/chapter/C0401202507
- **[H-C0402]** FIDE Handbook C.04.2 General handling rules for Swiss Tournaments (effective from 1 February 2026). https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournaments202602
- **[ANN]** M. Held, *Annotated Pairing Rules for the FIDE (Dutch) System (2026 edition)*, Rev. 2512151400, listed under TEC "Official Documents". http://tec.fide.com/wp-content/uploads/2026/08/AnnotatedDutch-V2026.pdf (TEC commentary, not normative)
- **[MTD]** M. Held, *Mastering the Dutch* (C.04.3 version 2026), Ver. 2606191500, TEC Official Documents. https://tec.fide.com/wp-content/uploads/2026/07/Mastering_the_Dutch_2026.pdf
- **[TEC-END]** TEC "FIDE Endorsed" page. https://tec.fide.com/endorsement/
- **[FEP]** "FIDE Endorsed Programs" (C.04 Annex 3). https://handbook.fide.com/files/handbook/C04Annex3_FEP22.pdf
- **[COPP]** TEC endorsement report for Chess Online Pairing Program 7.7. http://tec.fide.com/wp-content/uploads/2024/05/COPP-Report.pdf
- **[JVF]** JaVaFo home page. https://www.rrweb.org/javafo/JaVaFo.htm
- **[JVF-AUM]** JaVaFo Advanced User Manual (2.2). https://www.rrweb.org/javafo/aum/JaVaFo2_AUM.htm; jar: https://www.rrweb.org/javafo/current/javafo.jar
- **[BBP-README]** bbpPairings README (master and v6.0.0). https://github.com/BieremaBoyzProgramming/bbpPairings/blob/master/README.txt
- **[BBP-LIC]** bbpPairings LICENSE.txt / Apache-2.0.txt. https://github.com/BieremaBoyzProgramming/bbpPairings
- **[BBP-SRC]** bbpPairings source at commit `8f9e3c5` (2026-07-30): `src/swisssystems/dutch.cpp`, `src/fileformats/trf.cpp`, `test/tests/`.
- **[BBP-GIT]** bbpPairings releases and commit history. https://github.com/BieremaBoyzProgramming/bbpPairings/releases
