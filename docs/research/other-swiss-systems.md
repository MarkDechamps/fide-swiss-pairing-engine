# Other FIDE Swiss systems: Dubov, Burstein, Lim, Double-Swiss and acceleration

Research ticket: `.wayfinder/fide-swiss-engine/04-other-swiss-systems.md`.
Sources retrieved 2026-09-23. The handbook pages were fetched directly from handbook.fide.com. The SPP site (spp.fide.com) was down for maintenance that day, so its pages are cited from the Wayback Machine and marked **[archived]**.

## TL;DR

- All five texts were re-issued as part of the **2026 Swiss rules package** ("Approved by the Council on 28/10/2025, Applied from 1st February, 2026"). As of 2026-09 that edition is the one in force for Dubov (C.04.4.1), Burstein (C.04.4.2), Lim (C.04.4.3), Double-Swiss (C.04.5, a new system) and the accelerated systems (C.04.7). The pre-2026 texts are still online, marked "effective till 31 January 2026".
- **No program is endorsed for any of these systems.** The only public register (C.02.04, in force from 2026-03-01) lists 11 Tournament Handler Programs. Every one of them is registered for **Dutch only**, and every entry shows the expiry date 2026/02/01. TEC decided in September 2026 that new technical acceptances (TAPCs) will only start under the next Commission. (The old SPP page records a Dubov endorsement of Vega 4.2 from Torino 2006; that entry no longer appears in the register.)
- The only programs that implement these systems are non-endorsed: Vega (Dubov, Burstein, Lim, plus Baku), bbpPairings (an old Burstein, self-described as "flawed") and the Go library `gnutterts/chesspairing`, which claims all six systems. Gacrux, the TEC reference engine, implements only Dutch, the Swiss Team System and Berger.
- The 2026 edits to Dubov and Lim are mostly editorial, apart from Dubov's wider C2 and its reworded upfloater-set ordering (§3). **Burstein changed substantively**: its C6 and C7 were rewritten. The Baku rules were generalised so that they no longer assume 1/½/0 scoring.
- The handbook has **no Swiss system we have missed within C.04**, which contains C.04.1 to C.04.7. Outside C.04 there is the **Olympiad Pairing Rules** (a Lim-like team Swiss under D.02), and TRF26's ETT26 table names further formats, such as `CUSTOM_SWISS`, that are not defined anywhere.

## 1. Handbook inventory (C.04, as of 2026-09-23)

From the handbook table of contents (https://handbook.fide.com/):

| Chapter | Title | In force | URL |
|---|---|---|---|
| C.04.1 | Basic rules for Swiss Systems | from 2026-02-01 | https://handbook.fide.com/chapter/C0401202507 |
| C.04.2 | General handling rules for Swiss Tournaments | from 2026-02-01 | https://handbook.fide.com/chapter/GeneralHandlingRulesForSwissTournaments202602 |
| C.04.3 | FIDE (Dutch) System | from 2026-02-01 | https://handbook.fide.com/chapter/C0403202602 |
| C.04.4 | Other FIDE-approved Pairing Systems (umbrella) | **till 2026-01-31 only** | https://handbook.fide.com/chapter/OtherApprovedPairingSystemsTill2026 |
| C.04.4.1 | Dubov System | from 2026-02-01 | https://handbook.fide.com/chapter/C040401202602 |
| C.04.4.2 | Burstein System | from 2026-02-01 | https://handbook.fide.com/chapter/C040402202602 |
| C.04.4.3 | Lim System | from 2026-02-01 | https://handbook.fide.com/chapter/C040403202602 |
| C.04.5 | Double-Swiss Pairing System | from 2026-02-01 (no "till" edition exists) | https://handbook.fide.com/chapter/DoubleSwissSystem202602 |
| C.04.6 | Swiss Team Pairing System | from 2026-02-01 | https://handbook.fide.com/chapter/SwissTeamPairingSystem202602 |
| C.04.7 | FIDE-approved Accelerated Systems | from 2026-02-01 | https://handbook.fide.com/chapter/C0407202602 |

The superseded texts are C040401Till2026, C040402Till2026, C040403Till2026, C0407Till2026 and C0401Till2026, all under https://handbook.fide.com/chapter/.

Header of every 2026 text: "Approved by the Council on 28/10/2025 / Applied from 1st February, 2026". Lim also keeps its history line: "Approved by the General Assembly of 1987. Amended by the 1988, 1989, 1997, 1998 General Assemblies and 1999 Executive Board."

**Deprecation clause.** The old umbrella C.04.4 said: "Use of these systems is deprecated unless for a system there is a FIDE endorsed program … with a free pairing-checker … able to verify tournaments run with this system." In 2026 that rule moved into C.04.2 Art. 1.4 and now covers *every* FIDE Swiss system: "the use of such systems is deprecated, unless a tournament handler program approved by FIDE is available for them, provided with a free pairing-checker able to verify tournaments run with that system." C.04.2 Art. 1.1 to 1.3 require that acceleration methods be published in C.04.7 (or authorised by the QC), announced in advance, and declared when the tournament is reported.

**Endorsement home moved.** C.04.A (endorsement) is shown as "effective till 31 December 2024" and was merged into C.02.03 (§7, Tournament Handler Programs), with the public register in C.02.04. See https://handbook.fide.com/chapter/ChessEquipmentWithElectronicComponenets032026, https://handbook.fide.com/chapter/FIDECertifiedAndEndorsedEquipment032026 and the TEC 2026 Congress report (p. 10, "C.04.A software endorsement was merged into C.02.03"): http://tec.fide.com/wp-content/uploads/2026/09/TEC-2026-Congress-Meeting.pdf. Ticket 05 owns the detail; the point that matters here is C.02.03 §7.1.1.b: a THP must "Pair participants according to at least one of the FIDE-defined Swiss pairing systems **and support all FIDE-defined acceleration methods**". Per §7.1.2, "Compliance must be declared for specific pairing systems".

## 2. Programs: endorsed and implementing

### 2.1 Endorsed (official register)

C.02.04 "FIDE Endorsed Equipment", approved 2025-12-11, applied from 2026-03-01 (https://handbook.fide.com/chapter/FIDECertifiedAndEndorsedEquipment032026). The register lists these THPs: Vega 7.6.0, SwissSys 9.6, SwissMaster 5.7, Swiss-Manager 13, Swiss-Chess 9.05, UTU Swiss, ChessManager, STOP, TournamentService, Tornelo and Chess Online 7.7. **Every "Swiss Pairing System" cell reads "Dutch"**, and every expiry reads **2026/02/01**. Engines listed: JaVaFo, except bbpPairings (SwissSys) and an internal engine (Swiss-Chess).

- The older SPP page **[archived 2026-05-15]** (http://web.archive.org/web/20260515112844/http://spp.fide.com/endorsed-tournament-managers/) still shows a second entry for **Vega: "Pairing System Dubov … Internal Pairing Engine YES … Endorsement Congress Torino (2006) … Endorsed Version 4.2 … FPC availability NO"**. This is the only Dubov endorsement ever recorded, and it is absent from the 2026 register.
- The TEC 2026 report (pp. 31–35) says the current Commission will not issue new TAPCs: "new TAPC assessments will commence under the newly appointed Commission". Its pipeline shows one product in TAPC (Diclano), and UTU Swiss and STOP as "Not-Operational".
- Under C.02.03 §7.3.6–7.3.7, THPs whose TAPC expired at the start of the new cycle may keep being used, with their existing rules, during the Transition Period.
- **Consequence:** as of 2026-09 no program is endorsed or accepted for Dubov, Burstein, Lim, Double-Swiss, the Swiss Team System, or for the 2026 edition of any system. By the C.04.2 Art. 1.4 wording, all four non-Dutch individual systems are "deprecated".

### 2.2 Implementations (not endorsed)

| Program | Systems relevant here | Source |
|---|---|---|
| **Vega** (L. Forlano), manual v12, Jan 2026 | "FIDE Dubov, FIDE Burstein, FIDE Dutch 2017 (systems provided by JaVaFo engine …), FIDE Dutch 2025 (… Gracux [sic] engine …), FIDE Lim, Swiss USCF"; "Accelerated rounds according to FIDE Baku system". Its TRF interface accepts only `swiss_dutch_2017[_baku]`, `swiss_dutch_2025[_baku]` and round robin. The manual does not say which *edition* of Dubov, Burstein or Lim it implements. | https://www.vegachess.com/dwn/vega_en.pdf (pp. 4, 93) |
| **JaVaFo** (R. Ricca) | The public product is Dutch only ("a FIDE pairing engine for the Dutch Algorithm", v2.x aligned to the 2017 rules). The AUM documents fictitious points (`XXA`) and the `-b` Baku option, the latter "old FIDE Handbook, i.e. works only for tournaments longer than eight rounds". Vega's manual credits JaVaFo with its Dubov and Burstein, so there appears to be a non-public JaVaFo build. | [archived] https://www.rrweb.org/javafo/JaVaFo1.html; [archived 2026-08-20] http://web.archive.org/web/20260820060953/https://www.rrweb.org/javafo/aum/JaVaFo2_AUM.htm |
| **bbpPairings** (C++) | Dutch 2025/26 plus "a flawed implementation of a previous version of the Burstein system. The implementation of the Burstein system has not been endorsed by FIDE." For Burstein it applies a default acceleration taken from the old rules. CLI: `--burstein \| --dutch`. | https://github.com/BieremaBoyzProgramming/bbpPairings/blob/master/README.txt (commit 8f9e3c5, 2026-07-30) |
| **Gacrux** (O. Milvang, TEC reference, MIT) | It maps the ETT26 codes `FIDE_DUBOV`, `FIDE_BURSTEIN` and `FIDE_DOUBLESWISS` in `trf2json.py`, but only `dutch`, `fideteam` and `berger` engines exist (`pairingchecker.py`). Its manual v1.7 says "Only dutch is implemented" for the generator. TEC: "Gacrux is an engine, not a THP, and is not itself 'FIDE approved'". | https://github.com/OttoMilvang/TieBreakServer (commit 6419149, 2026-09-19); http://gacrux.no/spp/doc/GacruxSoftware.pdf; TEC 2026 report p. 40 |
| **Swiss-Manager** | Its changelog shows Baku support (adapted to changed rules 2023-04-22, "old accelerated system has been disabled"), the Gacrux engine enabled for individual (April 2026) and team (July 2026) Swiss, and "double/multi-round pairings for singles Swiss" (Aug 2026, which may be Double-Swiss; unconfirmed). It says nothing about Dubov, Burstein or Lim. | https://swiss-manager.at/downloadhist.aspx?lan=1 |
| **chesspairing** (Go, G. Nutterts, Apache-2.0) | Claims "all six FIDE-approved Swiss pairing systems": Dutch, Burstein, Dubov, Lim, Double-Swiss and Team, plus Baku (`swisslib/acceleration.go`). It is AI-assisted, has golden tests only against bbpPairings and JaVaFo (i.e. Dutch), and hard-codes Baku virtual points as 1.0/0.5 rather than "points for a win" and half of that. Lim's "Maxi" behaviour is a boolean option. This is the closest prior art to our library. | https://github.com/gnutterts/chesspairing (commit ba9d4f6, 2026-09-22) |

**Consequence:** there is no endorsed oracle for Dubov, Burstein, Lim or Double-Swiss. The TEC official documents page lists annotated texts and test material only for Dutch and TPS (https://tec.fide.com/official-documents/).

## 3. Dubov System (C.04.4.1, 2026)

Source: https://handbook.fide.com/chapter/C040401202602. The prior edition, "Approved by the 2018 General Assembly", is at C040401Till2026.

**Goal (Preface).** Players with more points should have a higher performance. The system equalises the **ARO** (average rating of opponents) within a scoregroup by pairing low-ARO players against high-rated opponents.

**Procedure:**
1. Definitions. Every player **must have a rating**; the CA assigns a provisional one if needed (1.1). TPNs are recalculated whenever a rating changes before round 4 is paired (1.2.2). Brackets contain residents plus **upfloaters only**; there are no downfloaters (1.3.2; the 2018 text said so explicitly). Colour preferences are absolute, strong or mild as in Dutch, **except that a player with no games has a mild preference for Black** (1.6.4). ARO is the mean rating of OTB opponents only, rounded half-up, and 0 before the first game (1.7). A **maximum upfloater** is a player upfloated MaxT = 2 + ⌊Rnds/5⌋ times (1.8).
2. Criteria. The absolute criteria are C1 (no rematch), C2 (no second PAB, per Basic Rule 4) and **C3 (no two players with the same absolute colour preference meet, with no topscorer exception)**. C4 is completion. Quality criteria: C5 minimise the number of upfloaters; C6 minimise score differences, i.e. maximise upfloater scores in ascending order; C7 minimise players not getting their colour preference; and C8–C10, which apply except in the last round: minimise upfloaters who are maximum upfloaters, the number of times a maximum upfloater is upfloated, and upfloaters who upfloated in the previous round (2.1–2.3).
3. PAB first (3.1). The PAB goes to the eligible player who allows completion, then with the lowest score, then with the most games played, then with the largest TPN.
4. Per scoregroup, in descending order (3.2):
   - Find the minimum number of upfloaters needed for a legal pairing of the residents. Choose the first upfloater set, in the order of 4.2 (descending score, then ascending TPN; sets ordered by smallest differing sequence number), that best satisfies C1–C10.
   - Split the bracket into **G1 = White seekers** (in the first round, or when nobody has played, the first half by TPN) and **G2 = the rest**. If players in the smaller subgroup must meet each other, shift them. Then equalise the sizes by shifting the "best" shifters (4.3). The shifter order starts from the *middle* of the list. White seekers are sorted by ARO ascending then TPN; Black seekers by TPN.
   - Sort G1 into **S1 by ascending ARO**, then TPN. Pair S1[i] with T2[i], where T2 is the **first legal permutation of G2** in lexicographic TPN order (4.4). All permutations are enumerated, not just a first block.
5. Colours (5.2). If neither player has played, the higher-ranked player gets the initial colour if their TPN is odd. Then: grant both preferences; grant the stronger preference; alternate from the most recent round in which the colours differed; grant the higher-ranked player's preference.

**Differences from Dutch (C.04.3 2026).** Dubov has upfloaters only, where Dutch has downfloaters, MDPs and a Limbo. Dubov assigns the PAB before pairing; Dutch settles it through C5/C9 in the last bracket. Dubov's pairing order depends on rating (via ARO), where Dutch's depends on score and TPN. Dubov splits the bracket by colour preference (G1/G2), not by rank (S1/S2). There are no exchanges, only G1/G2 shifts and a full permutation of G2. Float history is a count of *upfloats* (total, and in the previous round) rather than Dutch's down/up floats one and two rounds back. There are no topscorer rules. Non-players prefer Black rather than having no preference. The first-round colour rule comes first in Dubov and last in Dutch (Dutch 5.2.5).

**2026 vs 2018 changes.** C2 widened from "a (forfeit) win due to an opponent not appearing in time" to Basic Rule 4 ("scored in one single round, without playing, as many points as rewarded for a win"), which also covers full-point byes. The upfloater-set ordering was reworded: the 2018 text used "containers" of same-score sets sorted per C6 and then lexicographically; the 2026 text says "sorted according to the smallest differing sequence number" together with 4.2.3, "Compliance with [C5] and [C6] determines the number of upfloaters and their scores in valid sets". "Initial ranking list" became TPN. The rest is renumbering (A–E sections became Articles 1–5).

**Input data beyond Dutch:** a rating for every player, including provisional ratings; the ratings of each player's OTB opponents (for ARO); per-player upfloat counts (total, and whether the player upfloated last round); the total number of rounds (MaxT and the last-round exemptions).

**Ambiguities:**
- 4.4.1 cites "Article 2.2.4" for the G1/G2 manoeuvres. That must mean **3.2.4**; 2.2.4 does not exist.
- "Upfloated" is not defined in the 2026 text. Presumably it means paired against a higher-scored opponent. Does it include upfloats *by pairing score* under acceleration (§7)?
- The shifter order in 4.3 ranks by ARO. For players with no games, ARO is 0, so every non-player ties and falls back to TPN.
- 3.2.2 chooses the "first set … that … complies at best with all the pairing criteria". Whether C7 through C10 are evaluated on the *whole* bracket pairing (the pairing of 3.2.3–3.2.6) or only on the choice of upfloaters needs confirming against the note under 3.2.2. It says the ensuing bracket is compared, which implies a full trial pairing per set.

## 4. Burstein System (C.04.4.2, 2026)

Source: https://handbook.fide.com/chapter/C040402202602. Prior edition: C040402Till2026.

**Goal (Preface).** Players with the same score should have met equally strong opposition. Strength is measured by a tie-break **Index** computed from tournament data only, and players are paired high-Index against low-Index within a scoregroup.

**Procedure:**
1. **Seeding rounds** (1.6). The first min(⌊Rounds/2⌋, 4) rounds are paired **by the FIDE (Dutch) System**.
2. After seeding, players in a bracket are ranked (1.8) by the **Index**: Buchholz (sum of opponents' current scores), then Sonneborn-Berger, then TPN ascending. "Players' scores are not used in the pairing ranking order." Common rules for the Index (1.7.2): an unplayed round counts as a game against oneself with the result that gives the registered points. Exception: a run of consecutive zero-point byes up to the current round counts as draws "for the benefit of the player's actual over-the-board opponents". **Virtual (acceleration) points are excluded.**
3. Colour preferences are as in Dutch; players with no games have no preference (1.5.4). Brackets are residents plus *incoming floaters*, i.e. downfloaters as in Dutch (1.2.2).
4. Criteria. C1–C3 are absolute (C3 is the same-absolute-preference rule, with no topscorer exception) and C4 is completion. Quality criteria: **C5** maximise pairs; **C6** minimise the scores, in descending order, of outgoing floaters; **C7** choose outgoing floaters so that C1–C6 hold in the next bracket; **C8** minimise players not getting their colour preference (2.3). *There are no float-history criteria.*
5. PAB first (3.1). Order: eligible, allows completion, lowest score, most games played, **lowest Index ranking**.
6. For each bracket, compute the maximum number of pairs. Pad with virtual players of BSN 0 up to the number of outgoing floaters (4.2). Enumerate pairings in order: a pairing precedes another if BSN #1's opponent has a **larger** BSN, then compare BSN #2's opponent, and so on. BSN 0 sorts last. Choose the first pairing that best satisfies C1–C8 (3.2, 4.3). The result is a "fold" order (1 v n first) over *all* perfect matchings, not S1/S2 transpositions.
7. Colours are as in Dubov 5.2, with the higher-ranked player defined by the Index ranking.

**Differences from Dutch.** Dutch is used for the seeding rounds only. After that, the in-bracket order is Index-based (Buchholz, SB, TPN) and ignores score. There are only 8 criteria, with no float history and no topscorer rules. The PAB is assigned first. Candidate generation enumerates all matchings with virtual "float" partners instead of S1/S2 transpositions and exchanges. The colour rules start with the first-round rule.

**2026 vs pre-2026 changes (substantive).** The old C6 read "first maximize the number and then the scores of the incoming floaters that can be paired". The old C7 read "choose the outgoing floaters so that in the following bracket C.4, C.5 and C.6 are complied with in the best possible way". The 2026 C6 and C7 instead mirror Dutch C7/C8 (minimise outgoing floater scores; next-bracket compliance with C1–C6). C2 widened as for Dubov. TEC's 2026 report lists "Burstein corrected" among the C.04 amendments of 2024 and 2025 (p. 14). bbpPairings implements "a previous version", which is not this one.

**Input data beyond Dutch:** the full cross-table, including opponents' *current* scores and per-game points (for Buchholz and SB); unplayed-round types (zero-point bye versus other) and their sequence; virtual points kept *separate* from standings points; the total number of rounds (seeding count). A Dutch engine is also required for the seeding rounds.

**Ambiguities:**
- Cross-references are broken. 1.7 cites "Articles 2.1.5, 5.2.1, 5.2.5, 4.1"; 2.1.5 does not exist and 3.1.5 is meant. 1.9.1 cites "Article 3.1" for the absolute criteria (2.1 is meant) and 1.9.2 cites "Article 2.1" for the PAB (3.1 is meant).
- The 4.3 example list contains a duplicate entry, "1-3, 2-0, 4-5, 6-0", listed twice. I enumerated every 6-player, 2-pair candidate under the 4.3 rule. There are 45 distinct pairings: the first candidate plus 44 others. The handbook lists 45 lines after the first candidate, so the duplicate is simply a spurious extra line and the order is otherwise correct. This makes the example usable as a test fixture once the duplicate is removed.
- The seeding rounds use "the FIDE (Dutch) System", presumably C.04.3 2026. Which edition applies for a tournament that straddles 2026-02-01?
- The Index is for "players in a bracket". It is unclear whether incoming floaters are ranked by Index together with residents (it appears so, since score is explicitly unused).
- The unplayed-game rule differs from C.07 (2026), which caps dummy scores (TEC 2026 report p. 44). Burstein's own rule wins because it is self-contained, but the difference should be tested explicitly.

## 5. Lim System (C.04.4.3, 2026)

Source: https://handbook.fide.com/chapter/C040403202602. Prior: C040403Till2026. **The content is unchanged** apart from renumbering ("section 3" became "Article 3", and so on), the reference changing from "C.04.1.c" to Basic Rules Art. 3, and gender-neutral wording.

**Procedure** (procedural, not criteria-based):
1. PAB: the lowest-ranked player in the lowest scoregroup, subject to Basic Rules 3 and 4 (1.1). In round 1 it goes to the "lowest rated player" (7.1).
2. **Order of scoregroups** (2.2). Pair from the top down to just above the **median scoregroup** (score = half the rounds played), then from the bottom up, and the median group last, paired downward. When the median group is blocked, "crack" pairs from the lower or higher neighbour, depending on which side sent more floaters (2.6).
3. **Compatibility** (2.1): the players have not met, and the pairing would not give either player the same colour three times in a row or three more of one colour than the other.
4. Before pairing a group, transfer players out ("floaters") who have played everyone in the group, who cannot get a permissible colour, or to make the group even (2.3). Floater selection rules (3.x): prefer a player who equalises the numbers due White and Black; direction-dependent choice of the lowest- or highest-numbered player; four floater types (a–d) by disadvantage; avoid repeat floaters from the previous round (3.10); order in which incoming DF and UF are paired (3.6–3.7).
5. Tentative pairing is top half against bottom half, 1 v n/2+1 (2.4). Scrutiny and exchanges proceed in a fixed order (4.1–4.4, with worked six-player tables).
6. Colours (5): alternate, and equalise after even rounds. Hard limits: no three in a row and no ±3 (5.1). Ties are broken by colour history, then by rank: in the median group and above, the higher-ranked player gets the alternate colour; below the median, the lower-ranked player does (5.4).
7. **"Maxi-tournaments"**: exchanges and floater choices are restricted to players whose ratings are within 100 points (3.2.3, 3.8, 5.7).
8. Last round: Basic Rule 5 (same score) takes priority over colour rules, even allowing three in a row or ±3 (6).
9. Round 1: #1's colour is drawn by lot; the odd-numbered players in the top half get the same colour (7.2).

**Differences from Dutch.** Lim uses median-first bi-directional group ordering, with both up- and down-floaters. It is an exchange procedure with no global criteria ordering, so there is no "best candidate" evaluation. Colour limits are part of compatibility, so they are effectively absolute, and they are relaxed only in the last round. Rating matters directly in maxi-tournaments. There is no topscorer concept. The first-round rule is essentially the same as Dutch.

**Input data beyond Dutch:** ratings (for the 100-point maxi rule), a **maxi-tournament flag**, whether the player floated in the previous round, and the number of rounds played (to find the median).

**Ambiguities (numerous; Lim is the least algorithmic text):**
- **"Maxi-tournament" is never defined** in the 2026 or the older text. chesspairing makes it a user option.
- "Highest numbered player" means TPN #1, i.e. the *lowest* number (4.2: "Player #1, the highest numbered player"), which is confusing. 3.6.2 and 3.7.2 then say "not always the one with the highest pairing number", which mixes the two senses.
- The PAB goes to the "lowest rank" (1.1) but, in round 1, to the "lowest rated" player (7.1). These differ when TPN is not rating order (titles, alphabetical order, unrated players).
- The median scoregroup is defined as "score equal to half the number of rounds that have been played". With non-1/½/0 scoring or acceleration it may not exist or may shift. It may also be empty.
- The exchange tables (4.2–4.3) cover six players only. Generalising them to n players, and combining them with floater types a–d and the "cracking" of 2.6, needs interpretation. This makes endorsement-grade determinism doubtful without an oracle.
- **ETT26 has no `FIDE_LIM` code** (see §8), so a Lim tournament cannot be declared in TRF26 record 192 other than as `CUSTOM_SWISS`.

## 6. Double-Swiss System (C.04.5, new in 2026)

Source: https://handbook.fide.com/chapter/DoubleSwissSystem202602.

**Concept (Preface).** Every pairing is a **two-game match** with alternating colours, and points are scored per game. Match results can be 2-0, 1½-½, …, and also the odd ones (1½-0, ½-½, 0-0, …). A match is a forfeit only if a player forfeits *both* games, and only then may the pairing be repeated later. Otherwise a forfeited single game counts as played for tie-breaks and standings (though not for rating). Byes apply only to whole matches.

**Procedure:**
1. Order: score, then TPN (1.2). Brackets are residents plus **upfloaters** (1.3.2). A floater is any player paired against a different score, in either direction (1.5). A player "had" a colour if at least one game was played and they were scheduled White or Black in game 1 (1.6).
2. PAB: "as many points as if the player had played a match winning a game and drawing the other", i.e. 1.5 by default (1.4).
3. Absolute criteria: C1 (no rematch) and **C2 (no second PAB for a player who had a PAB, a forfeit match win, "or been given a FIDE-deprecated full-point bye")**. C3 is completion. **There is no colour criterion at all.** Quality criteria, applied when selecting upfloaters for the top scoregroup: C4 minimise the number of upfloaters; C5 maximise upfloater scores; C6 keep the next non-empty scoregroup's bracket legal (C1, C3) and C4-optimal; C7 and C8, except in the last round, minimise upfloaters who floated last round and upfloaters' opponents who floated last round (2.3).
4. Process (3.3): PAB first (3.4: legal remainder, lowest score, most matches played, largest TPN). Then repeatedly take the **top scoregroup** of the unpaired players, add upfloaters and pair it. Upfloater sets are those satisfying C4/C5, sorted internally by descending score then TPN, and ordered lexicographically by TPN; take the first set that is legal and satisfies C6/C7 (3.5). The bracket pairing is the lexicographically first **identifier** (top members' TPNs ascending, then the corresponding bottom members) satisfying C1 and C8 (3.6). This is a pure enumeration with no S1/S2 split.
5. Colours (4.3). If neither player has played a match, the HRP gets the initial colour if their TPN is odd. Then: **White to the player with fewer Whites**; alternate from the most recent round in which the colours differed; alternate the HRP's last colour; alternate the opponent's last colour. The White player plays game 1 as White and game 2 as Black (4.4).

**Differences from Dutch.** Each round is a two-game match. There are upfloaters only, and the top scoregroup is re-formed after each bracket. There is no colour-preference criterion in pairing. Enumeration is purely lexicographic on TPN. Float history looks back one round only and covers upfloaters and their opponents. The PAB is assigned first and is worth 1.5 by default. C2 differs in wording from Basic Rule 4.

**Input data beyond Dutch:** **per-game results within a round** (two games per match, including the odd ½-0 and 0-0 outcomes, per-game forfeits, and a flag for whether at least one game was played); the number of Whites counted per *match*; match-level forfeit status (both games forfeited) for C1's repeat exception; a "FIDE-deprecated full-point bye" marker; the PAB value.

**Ambiguities:**
- Numbering: 3.1.2 cites "[C.3]", and 3.6.4's parenthesis is unbalanced ("see Articles 2.1.1 and 2.3.5 - besides …").
- 3.1.1 defines legal as "[C1] and [C2]". In 3.4.1 the PAB assignee must leave "a legal pairing for all players", which presumably means C3 completion.
- C6 refers to "the following scoregroup". Its note clarifies "only the mentioned scoregroup is involved, even though some of the upfloaters come from lower scoregroups". It is still unclear what "following" means when the top scoregroup plus upfloaters empties more than one group.
- **Colour history is per match**, but a match whose first game was forfeited and whose second was played still "has" a colour (1.6). The GHR 3.4 "unplayed rounds" rule presumably applies only to fully unplayed matches.
- TRF26 has one result character per round (`1 = 0 + - W D L H F U Z`). It **cannot encode a 1½-½ match** or per-game forfeits (see §8). ETT26 still describes `FIDE_DOUBLESWISS` as "(possible evolution - a Double Swiss Pairing Algorithm will be defined)", which is stale.
- Baku with Double-Swiss (the `FIDE_DOUBLESWISS_BAKU` code exists): C.04.7 gives virtual points "equal to the number of points awarded for a win". Is that a game win (1) or a match win (2)? The text does not say.

## 7. Accelerated systems: Baku (C.04.7, 2026)

Source: https://handbook.fide.com/chapter/C0407202602. Prior: C0407Till2026, whose section was titled "C.04.7.1 Baku Acceleration". Before that it was C.04.5.1 (JaVaFo AUM; SPP archive URL `c-04-5-1-baku-acceleration`).

**Preface.** Acceleration modifies the first rounds by rearranging score brackets, e.g. with **virtual points** for higher-rated participants. "Unless explicitly specified otherwise, each described acceleration method is applicable to any Swiss Pairing System." In 2026 the preface explicitly covers team competitions ("games … or matches").

**Baku Acceleration Method:**
1. Premise (1.1). Applicable when "the points for a win equal the points for two draws and the points for a loss are zero". Pre-2026 required the standard 1/½/0 system.
2. Groups (1.2). Before round 1, split the sorted list into **GA** (the first half rounded up to an even number, i.e. 2·⌈N/4⌉; for N = 161, GA = 82) and GB (the rest).
3. Late entries (1.3). They are inserted per C.04.2 Art. 2. **"The last GA-participant shall be the same participant as in the previous round"**, so GA's size and TPN range may change and GA may become odd.
4. Virtual points (1.4). The accelerated rounds are the first ⌈R/2⌉. For the first ⌈accelerated/2⌉ of them, GA gets virtual points = **the points for a win**. For the remaining accelerated rounds that value is **halved**. GB never gets virtual points, and nobody gets them after the accelerated rounds. Example: 9 rounds gives 5 accelerated rounds, 3 at +1 and 2 at +½. Team example: 11 rounds with MP 2/1/0 gives rounds 1–3 at +2 MP and rounds 4–6 at +1 MP. "If gamepoints were the primary score, the Baku Acceleration could not be used."
5. Pairing score (1.5) = standings points + virtual points. It is used "to define the scoregroups and internally sort them", and also to sort the boards (C.04.2 Art. 3.6).

**Differences from pre-2026.** The premise was generalised from 1/½/0; the virtual points went from literally 1 and 0.5 to "points for a win", halved; team and matchpoint wording was added; and board sorting was added to 1.5. Swiss-Manager's changelog records that the rules had already changed in 2023 (the "old accelerated system has been disabled", 2023-04-22). JaVaFo's `-b` refers to an even older variant ("works only for tournaments longer than eight rounds").

**How systems consume it:**
- Dutch and Dubov use the pairing score wherever the text says "score". JaVaFo requires the full per-round history of fictitious points "because this record is used to determine the floaters history of each player".
- Burstein explicitly **excludes** virtual points from the Index (1.7.2).
- Lim's median group becomes ill-defined (§5). Double-Swiss has the win-value question (§6).
- The Swiss Team System (C.04.6) has no text of its own on acceleration. Gacrux enforces the "not with GP primary" rule (`pairingfideteam.py`, line 78).

**Input data beyond Dutch:** the total number of rounds; points for a win (or match points for a team win); GA membership, i.e. the identity of the last GA participant, which is stable across late entries; per-round virtual-point history (for float history and for checkers). TRF26 record **250** encodes fictitious MP and GP by round range and ID range; for ITDX, 250 records override any acceleration implied by the 192 code.

**Ambiguities:**
- The TRF26 record 250 example ("11 round Swiss team tournament based on matchpoints with 178 teams") puts the values in the **game-points** column (`250 00.0 02.0 001 003 0001 0090`). That contradicts C.04.7 1.4.4 (virtual *matchpoints*; GP-primary not allowed). The GA size (90 = 2·⌈178/4⌉) is consistent.
- It is not stated whether a player's floats (Dutch C14–C21, Dubov C8–C10, Double-Swiss C7–C8) are judged on pairing score or on real score. JaVaFo's practice implies pairing score.
- For "properly sorted" (1.2), is the list the TPN order at round 1?
- For a late entry inserted *above* the last GA participant: are they in GA? It appears so, since GA is defined as "up to the last GA participant".
- C.04.7 is "applicable to any Swiss Pairing System", and C.02.03 7.1.1.b requires THPs to support "all FIDE-defined acceleration methods". Yet ETT26 has no Lim code, so no `LIM_BAKU` either.

## 8. TRF26 / ETT26 coverage (input format check)

Sources: TRF26 (approved 2025-05-12, applied from 2025-09-01), https://handbook.fide.com/files/handbook/TRF26.pdf; ETT26, https://handbook.fide.com/files/handbook/ETT26.pdf.

- Record 192 codes (ETT26): `FIDE_DUTCH_2017`, `FIDE_DUTCH_2026`, `FIDE_DUTCH`, `FIDE_DUBOV`, `FIDE_BURSTEIN`, the `*_BAKU` variants of each, `CUSTOM_SWISS`, `FIDE_DOUBLESWISS`, `FIDE_DOUBLESWISS_BAKU`, `CUSTOM_DOUBLESWISS`, and `FIDE_TEAM_{TYPEA,TYPEB,}_{MP,GP}[_{GP,MP}][_BAKU]`. **No Lim code exists. Dubov and Burstein have no edition split**, unlike Dutch 2017/2026.
- Record 250 carries acceleration, record 260 prohibited pairings, and record 162 the scoring system. Record 152 carries the initial colour.
- Player rounds allow one opponent, one colour and one result character per round, so **Double-Swiss matches cannot be represented** as-is (see ticket 06).

## 9. Swiss systems we might have missed

- **Within C.04, none.** The table of contents is exhaustive: C.04.1–C.04.7, and C.04.A is retired. The Swiss Team System (C.04.6) is covered by ticket 03.
- **Olympiad Pairing Rules** (D.02, effective 2022-01-01; https://handbook.fide.com/chapter/OlympiadPairingRules2022). A team Swiss with its own ranking (average of the top four ratings), bye (1 MP / 2 GP) and a **Lim-like median-group order** (6.4: top down, then bottom up, the median group last). It is event-specific but is a FIDE-published Swiss.
- **ETT26 placeholders:** `CUSTOM_SWISS` and `CUSTOM_DOUBLESWISS` (organiser-defined, needing QC authorisation per C.04.2 1.2). Scheveningen and Schiller are "not yet defined", and those are not Swiss formats.
- **Non-FIDE systems in programs:** "Swiss USCF" in Vega, and Keizer in chesspairing. Both are out of scope.

## Implications for the library

1. **Oracle gap.** For Dubov, Burstein, Lim and Double-Swiss there is *no endorsed program and no FIDE test suite*. "Pairings identical to the endorsed reference programs" is only achievable for Dutch (and possibly TPS via Gacrux). For the other systems, conformance has to rest on (a) a handbook-article-traceable implementation, (b) worked examples lifted from the texts (Burstein 4.3 ordering, Lim 4.2–4.3 exchange tables, Double-Swiss 3.5.4 and 3.6.2 identifiers, Dubov 4.3 middle-out order), and (c) differential testing against Vega (Dubov, Burstein, Lim) and chesspairing, treated as *witnesses*, not oracles. Ticket 10 should record this.
2. **Being the first 2026-compliant implementation of these systems** is a real opportunity, because TAPCs restart under the next TEC. The library could also serve as the free "pairing-checker" that C.04.2 1.4 requires before these systems stop being deprecated.
3. **Shared kernel.** The 2026 texts share a lot: TPN (GHR Art. 2), Basic Rules C1/C2, completion C4, and colour preferences (absolute, strong or mild), with a per-system rule for players who have not played: Dutch and Burstein "none", Dubov "Black". Colour allocation is a priority list with first-round, both, stronger, alternate and HRP steps, in per-system order. Criteria are lexicographically compared quality vectors. Dubov, Burstein and Double-Swiss assign the PAB first, which differs from Dutch. Model a `PairingSystem` as its own criteria list, bracket-candidate enumerator and colour-rule chain over a common tournament-state model.
4. **Candidate enumeration differs per system and should mirror each text literally**: Dutch uses S1/S2 transpositions and exchanges; Dubov uses a colour split plus full G2 permutation; Burstein uses an all-matchings "fold" order with BSN-0 floats; Double-Swiss uses lexicographic identifiers; Lim uses a procedural exchange. Brute-force enumeration is acceptable under the readability-first rule, but Dubov's G2 permutation and Burstein's matching enumeration are factorial in the bracket size. Expect the first performance issue here.
5. **Acceleration is a decorator on the score.** A `PairingScore = standings + virtual(round, participant)` provider plugged into any system, with Burstein opting out of it for its Index. Represent acceleration as explicit per-round, per-participant virtual points (TRF26 250), with Baku as a generator of those records. That keeps any future C.04.7 method pluggable and matches how checkers consume it.
6. **The domain model must support:** ratings as pairing input (Dubov ARO, Lim maxi rule), per-round upfloat and downfloat history, a Buchholz/SB calculator that has its own unplayed-game rules (Burstein, reusable from the C.07 module but *not* identical to it), **sub-round games** (Double-Swiss two-game matches, per-game forfeits), and system-specific PAB values (Double-Swiss 1.5).
7. **Needed TRF extensions (ticket 06):** a Lim 192 code, edition-qualified Dubov and Burstein codes, and a Double-Swiss per-game result encoding.
8. **Edition handling.** The handbook keeps both "till 2026-01-31" and "from 2026-02-01" texts, and ETT26 distinguishes the Dutch editions. Decide whether the library implements only the 2026 editions (recommended: prior editions have no endorsed oracle either, apart from Dutch 2017 via JaVaFo) and name systems with their edition.
9. **Lim is the riskiest system.** It has an undefined "Maxi-tournament", confusing numbering terms, and exchanges that do not generalise beyond six players. Treat it last, and resolve its interpretations explicitly as configuration or documented decisions.

## Open questions

1. Does TEC or SPP hold any **test suites or reference outputs** for Dubov, Burstein, Lim or Double-Swiss (e.g. Vega's internal JaVaFo Dubov/Burstein builds, Almog Burstein's work on the Burstein correction)? Worth asking TEC directly.
2. **Which edition** do Vega's Dubov, Burstein and Lim implement, and which Burstein edition does bbpPairings implement ("a previous version")? This determines whether they are useful as witnesses at all.
3. What is a **"Maxi-tournament"** in Lim? Is it a player-count threshold, or organiser-declared?
4. **Double-Swiss + Baku**: are the virtual points a game win (1) or a match win (2)? And how should Double-Swiss results be encoded in TRF26?
5. Under acceleration, are **floats** (Dutch, Dubov and Double-Swiss float criteria) based on pairing score or on real score?
6. **Dubov "upfloated"** is used in MaxT and C8–C10. Is it counted as opponent's score > own score (by pairing score), and does it include PAB rounds?
7. Should the library also cover the **Olympiad Pairing Rules** (D.02) as a named system, given that it is Swiss but event-specific?
8. When TAPCs restart, will the Verification Check Lists (not yet final, per the TEC 2026 report p. 31) define checker requirements for non-Dutch systems? Watch the new TEC Manual.
