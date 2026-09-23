# TRF (Tournament Report File) format

Research for the FIDE Swiss reference library (Java) and its TRF CLI.
Researched 2026-09-23. Column positions are 1-based and inclusive, as in the FIDE documents.
Sources are listed at the end as [S1]..[S12]. [E] marks behaviour I checked by running the real
programs (see "Empirical checks").

## TL;DR

- **The current format is TRF26** ("Tournament Report File Format Version 2026"). It is Annexure A of
  Handbook C.02.03 (*Chess Equipment with Electronic Components*, in force 1 March 2026). It was
  approved by FIDE Council on 12/05/2025 and applied from 01/09/2025 [S1][S2]. THPs must import and
  export "the latest version" and stay backward compatible with TRF16 and TRF06. The older versions
  now sit in the TEC Manual [S1 §7.1.3b, §7.2.1–7.2.2].
- TRF26 is a strict superset of TRF16. The `001` player record and the `012`–`132` header records are
  unchanged. It adds records for engine configuration and In-Tournament Data eXchange (ITDX): `142`
  number of rounds, `152` initial colour, `162` scoring system, `192` encoded tournament type (the
  pairing system), `250` acceleration, `260` prohibited pairings, `240` byes. It also adds team
  records (`310`, `320`, `330`, `300`, `299`, `352`, `362`, `801`/`802`), tie-break records
  (`202`/`212`) and national ratings (`172` plus NRS records) [S2].
- **TRF26 standardises most of what the JaVaFo `XX?` extensions did**: `142` ≈ `XXR`, `152` ≈
  `XXC white1/black1`, `162` ≈ `XXS`, `250` ≈ `XXA`, `260` ≈ `XXP`, and `240` ≈ `XXZ` (the last only
  partly) [S2][S8].
- **The request/response protocol is not standardised by FIDE.** The de facto protocol is JaVaFo's:
  `engine input.trf -p [out]`. The output is a count line, then one `white black` pair per line,
  using starting-rank numbers, with `0` as the opponent for the pairing-allocated bye.
  bbpPairings copies it [S8][S9][E].
- **The two reference engines disagree on which dialect they read** [E]:
  - JaVaFo 2.2 (build 3222, the latest; the jar is dated 2017) reads only TRF16+`XX?`. It ignores
    `142` and `240` (I did not verify `152`).
  - bbpPairings (v6.0.0, 2026-02) reads TRF26 records plus `XXR`/`XXC`/`XXA`/`XXP` and its own
    `BB?` lines. It silently ignores `XXZ` and `XXS`.
  - The library should read both dialects and be explicit about which one it writes.

---

## 1. Versions and where they live

| Version | Status | Primary source |
|---|---|---|
| TRF06 | legacy; described in the TEC Manual (TEC calls it "Annexure B – TRF06 – Version 2006") | [S1 §7.2.1], [S6] |
| TRF16 | legacy but still accepted; last updated Tromsø 13/08/2014, approved Elista 10/08/2015 (PDF titled "TRF2016") | [S5], [S1 §7.2.2] |
| TRF26 | **current**; approved FIDE Council 12/05/2025, applied 01/09/2025 | [S2], [S1 §7.2.1] |

- Handbook C.02.03 §7.2.2 says a TRF is "a data file encoded in one of the standard formats, TRF26,
  TRF16, or TRF06" [S1].
- §7.1.3b makes import/export of the latest format mandatory in "FIDE mode", with backward
  compatibility [S1].
- The TEC restructuring moved C.04.A (software endorsement) into C.02.03, with the note "TRF16 became
  TRF26; … Older TRF annexes moved to the TEC Manual" [S11].
- TEC's own page publishes an earlier copy of TRF-2026 (dated 17/04/2025, "Approved by ???"). It also
  publishes a type-code table that uses `FIDE_DUTCH_2025` and a 1 July 2025 cut-over. The Handbook
  copy (PDF built 2026-03-02) uses `FIDE_DUTCH_2026` and a 1 February 2026 cut-over [S6][S4].
  **Use the Handbook copies [S2][S3][S4] as normative.** Apart from the approval header, the TRF26
  text of the TEC and Handbook copies matched when I diffed their extracted text.
- The TRF25 drafts (2024/2025) on tec.fide.com are superseded [S7]. Gacrux still carries dead
  parsing code for draft-era codes (`ACC`, `TSE`, `PAB`, `FPB`, `HPB`, `ZPB`, `MFO`, `DFM`) inside a
  string literal, so it never runs [S10]. **Do not implement the draft codes.**

## 2. General line rules (TRF26)

- Every line ends with CR (Remark 1). The record type is in columns 1–3. For header records the value
  is free text "from position 5" [S2].
- Comment lines start with `###` (new in TRF26). The examples in the spec use `###` lines as column
  rulers [S2].
- The R/P columns in the spec mark how important each field is for Rating and for Pairing (including
  tie-breaks). The markers are: ■ mandatory, ◙ mandatory for title events, □ warning if wrong [S2].
- ITDX (Remark 3) is the use of a TRF *during* a tournament, both between THPs and between a THP and
  a pairing engine. Several records are "mandatory only for ITDX" [S2].
- **Encoding is not specified** by TRF16 or TRF26 [S2][S5]. JaVaFo recommends UTF-8 [S8]. bbpPairings
  rejects input that is not legal UTF-8 [S9 trf.cpp]. Gacrux's default for TRF is "ascii" [S10 doc].
- Line endings in practice: bbpPairings splits on both `\r` and `\n` [S9]. Gacrux turns `\r` into
  `\n` [S10]. JaVaFo accepted CRLF files in my tests [E].

## 3. Tournament header records (`xx2`)

TRF16 has `012`–`132` [S5]. TRF26 keeps them and adds `142`–`362` [S2].

| Code | Meaning | Notes |
|---|---|---|
| 012 | Tournament name | ■R ■P |
| 022 | City | ■R |
| 032 | Federation | ■R |
| 042 / 052 | Start / end date | TRF26 adds the format `YYYY/MM/DD` and marks them ■R |
| 062 / 072 / 082 | No. of players / rated players / teams | informative |
| 092 | Type of tournament | free text |
| 102 / 112 | Chief Arbiter / Deputy (one line per arbiter) | |
| 122 | Allotted times per moves/game | free text |
| 132 | Round dates, format `YY/MM/DD`, round *r* at columns `92+10(r-1)` to `99+10(r-1)` | aligned with the round blocks in `001` |
| **142** | Number of rounds | mandatory only for ITDX. The engine needs it to know whether this is the last round |
| **152** | Initial colour, `W` or `B` | mandatory only if it differs from the colour of the highest-ranked participant paired in round 1, "or, for ITDX, before the first round is paired" |
| **162** | Scoring system for individual games (and game points in team events) | see §3.1 |
| **172** | Encoded Starting Rank Method (only if there are NRS records) | columns 5–7 = federation; columns 9–13 = `FIDE`, `NRO`, `FIDON`, `NIDOF`, `HBFN`, `LBFN` or `OTHER` |
| **182** | Pairing Controller Identifier (program or user) | ◙ |
| **192** | Encoded Type of Tournament, taking a code from ETT26 | ◙R ■P. This is the pairing-system selector; see §3.2 |
| **202** | Tie-breaks used among participants on equal points | comma-separated MTB26 codes, see §3.3 |
| **212** | Tie-breaks defining the standings | the same codes plus `PTS`. `212 PTS,…` ≡ `202 …`. 202 and 212 are alternatives |
| **222** | Encoded time control | `d[:d]` or `Wd[:d]-Bd[:d]`, with `d` ∈ `M/S`, `M/S+I`, `S`, `S+I`. Examples: `5400+30`, `40/6000+30:900+30`, `W300-B240` |
| **352** | Board colour sequence for team matches, e.g. `WBWB` | its length gives the number of boards |
| **362** | Scoring system for team match points | see §3.1 |

### 3.1 Scoring systems (`162`, `362`)

- **`162` layout:**
  - Symbol at column 6, points in columns 7–10 (format `11.5`).
  - Optional further pairs start 9 columns later: symbol at 15 with points at 16–19, then 24/25–28,
    and so on (the spec mentions 33, 42 and 51).
  - Symbols and defaults: `W` 1.0 (win OTB, forfeit win or FPB), `D` 0.5 (draw or HPB), `L` 0.0,
    `A` 0.0 (absence: ZPB or forfeit loss), `P` same as W (PAB), `X` same as D (unknown result,
    e.g. an adjourned game).
  - The record is needed only when some value differs from its default [S2].
- **`362` layout:** `TW` 2.0, `TD` 1.0, `TL` 0.0. Symbol at columns 5–6 with points at 7–10, then
  14–15 with 16–19, then 23–24 with 25–28 [S2].
- **Ambiguity: `162` column numbers.** The prose says "different by the one specified in 5" and "in
  5 and 13", but the table puts the symbols at 6, 15 and 24. bbpPairings reads the symbol at column 6
  with a stride of 9 and writes `162  W 3.0` (two spaces after `162`) [S9]. Follow the table (6/15/24).
- **Ambiguity: `162` cannot express everything `XXS` can.** It has no separate white/black values and
  no FPB/HPB distinct from W/D. It also merges ZPB and forfeit loss into `A`. JaVaFo's `XXS` can set
  all of these separately (§7.2).

### 3.2 Pairing-system selection (`192`, ETT26)

ETT26 [S4] lists these codes:

- **Individuals:**
  - Dutch: `FIDE_DUTCH_2017`, `FIDE_DUTCH_2026` and `FIDE_DUTCH`. The bare `FIDE_DUTCH` means 2017
    rules before 1 February 2026 and 2026 rules after.
  - With the Baku acceleration method: `FIDE_DUTCH_2017_BAKU`, `FIDE_DUTCH_2026_BAKU`,
    `FIDE_DUTCH_BAKU`, `FIDE_DUBOV_BAKU`, `FIDE_BURSTEIN_BAKU`.
  - Other systems: `FIDE_DUBOV`, `FIDE_BURSTEIN`, `CUSTOM_SWISS`, `FIDE_DOUBLESWISS[_BAKU]`,
    `CUSTOM_DOUBLESWISS`.
  - Round robins: `BERGER_ROUNDROBIN[_Gn]`, `FIDE_ROUNDROBIN`, `FIDE_DOUBLEROUNDROBIN`.
  - Others: Schiller and Scheveningen variants (rules not yet defined), `CUSTOM_KNOCKOUT`.
- **Teams** (the word `TEAM` always appears in the code):
  - Pattern: `FIDE_TEAM[_TYPEA|_TYPEB][_MP|_GP][_GP|_MP][_BAKU]`.
  - TYPEA/TYPEB/none selects the colour-preference type. The first of MP/GP is the primary score. The
    optional second one is the secondary score, used in colour allocation.
  - `FIDE_TEAM` defaults to `FIDE_TEAM_TYPEA_MP_GP`, and `FIDE_TEAM_BAKU` to
    `FIDE_TEAM_TYPEA_MP_GP_BAKU`.
  - Also: `CUSTOM_TEAM_SWISS[_MP|_GP]` and team round robins.

**Gaps in ETT26 (flag):**

- There is **no code for the Lim system** (C.04.4.3), even though the Handbook has Lim rules in force
  from 1 February 2026.
- There are **no year-versioned codes for Dubov or Burstein**, although both have new texts in force
  from 1 February 2026.
- `FIDE_DOUBLESWISS` is still described as a "possible evolution", although C.04.5 now exists.
- The only acceleration named is Baku, which matches C.04.7 (2026), where Baku is the only method
  described [S4][S12].

bbpPairings accepts `FIDE_DUTCH`, `FIDE_DUTCH_2025`, `FIDE_DUTCH_2026` (and their `_BAKU` forms),
`FIDE_BURSTEIN` and `FIDE_BURSTEIN_BAKU`. It rejects every other `192` value with "unsupported
tournament type". Accepting `_2025` follows the older TEC table. On `-p`, the `--dutch`/`--burstein`
command-line flag decided the system even when `192` named the other one [S9][E].

### 3.3 Tie-break codes (`202`/`212`, MTB26)

- A Rank Order Descriptor has the form `ACRONYM[:MP|:GP][/modifier…]` [S3]:
  - Modifiers: `/Cn`, `/Mn`, `/L±n` (Koya), `/Kx` (SSSC).
  - Options: `/P` (forfeits count as played), `/F` (Fore Buchholz), `/R` (reverse order).
  - Everything is case-insensitive.
- Non-FIDE tie-breaks must use the prefix `OTHER_` [S3].
- Acronyms (individuals): `DE`, `BPG`, `BWG`, `REP`, `STD`, `SB`, `ARO`, `TPR`, `PTP`, `APRO`,
  `APPO`, `RTNG`.
- Acronyms (both individuals and teams): `WIN`, `WON`, `PS`, `TPN`, `BH`, `AOB`, `FB`, `KS`.
- Acronyms (teams only): `BC`, `TBR`, `BBE`, `MPvGP`, `EMMSB`, `EMGSB`, `EGMSB`, `EGGSB`, `EDE*`,
  `SSSC`.

Tie-breaks are out of scope for a pairing library, except that `TPN` and rank matter for ordering.

## 4. Player record `001` (identical in TRF16 and TRF26)

| Cols | Field | Notes |
|---|---|---|
| 1–3 | `001` | |
| 5–8 | Starting-rank number (pairing number), 1–9999 | ■R ■P |
| 10 | Sex `m`/`w` | |
| 11–13 | Title: GM, IM, WGM, FM, WIM, CM, WFM, WCM | |
| 15–47 | Name, "Lastname, Firstname" | |
| 49–52 | FIDE rating | |
| 54–56 | Federation | |
| 58–68 | FIDE ID (includes 3 reserve digits) | ■R |
| 70–79 | Birth date `YYYY/MM/DD` | |
| 81–84 | Points, format `11.5` | ■P. The standings score under the tournament's scoring system, e.g. `17.0` for 5W 2D 2L at 3/1/0. In team events it is informative only (the player's OTB + forfeit-win score) |
| 86–89 | Rank | TRF26: "Exact definition – ties allowed", ■R ■P (TRF16 marked it R only) |
| 92–95 | Round 1 opponent's starting rank; `0000` or blank = no opponent | TRF26: for teams it may mean a forfeit win against an undefined opponent |
| 97 | Round 1 colour: `w`, `b`, or `-`/blank for no colour | |
| 99 | Round 1 result, see below | |
| 102–105, 107, 109 | Round 2, then +10 columns per round | round *r*: opponent `92+10(r-1)`…`95+10(r-1)`, colour `97+10(r-1)`, result `99+10(r-1)` |

Result codes (case-insensitive) [S2][S5]:

| Code | Meaning |
|---|---|
| `+` / `-` | forfeit win / forfeit loss (game not played) |
| `W` / `D` / `L` | win / draw / loss where the game lasted less than one move (not rated) |
| `1` / `=` / `0` | regular played win / draw / loss |
| `H` | half-point bye |
| `F` | full-point bye |
| `U` | pairing-allocated bye (PAB), at most one per round |
| `Z` or blank | zero-point bye / known absence. TRF26 adds that it is also a rest round in team events and in odd-sized round robins |

How the round block encodes byes and forfeits:

- **Byes:** opponent `0000`, colour `-`, and the code `H`, `F`, `U` or `Z`.
- **Forfeits:** a real opponent and a scheduled colour (`w`/`b`), with `+` for one player and `-` for
  the other.
- **Double forfeit:** each player gets `-` against the other.
- **Team forfeits:** players of the present team are written `XXXX C +`, where `XXXX` is `0000` or a
  player of the absent team, and `C` is `w`, `b` or `-` (`-` only with `0000`) [S2 "Forfeited
  matches"].

**Ambiguity:** TRF16 and TRF26 do not define how to tell "the round to be paired" apart from
"rounds already played" inside `001`. The engines infer it (§7).

## 5. Acceleration, prohibited pairings and byes (individual and team)

### `250` Accelerated rounds (TRF26) [S2]

| Cols | Field |
|---|---|
| 1–3 | `250` |
| 5–8 | fictitious match points (teams only; empty ≡ 0.0; empty for individuals) |
| 10–13 | fictitious (game) points. Individuals: must be non-zero. Teams: at least one of MP/GP must be non-zero |
| 15–17 | first round |
| 19–21 | last round (may equal the first) |
| 23–26 | first player/team ID |
| 28–31 | last player/team ID (a *range* of IDs) |

- For ITDX, `250` records override any acceleration implied by the `192` code (e.g. `_BAKU`) [S2].
- **Ambiguity:** the spec's own example is an "11 round Swiss team tournament based on matchpoints",
  yet it puts the virtual points in the **GP** field: `250 00.0 02.0 001 003 0001 0090`. C.04.7 says
  that for MP-primary team events the Baku method assigns virtual *match points* (2 then 1) [S12].
  The example looks wrong. Raise it with TEC.
- **Ambiguity:** `250` addresses a contiguous ID range, so a group that is not contiguous needs
  several lines. The ranges are over *starting-rank numbers*, not ranks.
- bbpPairings enforces the individual rules: MP must be empty or zero, GP must be non-zero [S9].

### `260` Prohibited pairings (TRF26) [S2]

- Layout: `260 RRF RRL PPPP PPPP …`, with the first round at columns 5–7, the last round at 9–11,
  and IDs at 13–16, 18–21, 23–26 and so on.
- No two of the listed players/teams may meet in rounds RRF..RRL. Example: `260 001 002  125  180  184  216`.

### `240` Byes (TRF26) [S2]

- Layout: `240 T RRR IIII IIII …`, with type `T` at column 5 (`F`, `H` or `Z`), the round at 7–9 and
  IDs at 11–14, 16–19 and so on. At most one record per type per round.
- For individuals the record is optional, because it duplicates the `001` data. For teams, F and H
  are mandatory.
- **For ITDX, a `240` for a round not yet paired is mandatory.** This is how TRF26 tells an engine
  who is absent or took a requested bye in the next round (the job of JaVaFo's `XXZ`).
- Example: `240 H 003  026  047`.

### `320` Pairing-allocated bye for teams (TRF26) [S2]

- Layout: `320 MMMM GGGG TTT TTT …`, with PAB match points at columns 5–8, PAB game points at 10–13,
  then the PAB team for rounds 1, 2, 3… at 15–17, 19–21, 23–25 and so on (`000` or empty = none).
- One record per tournament.

## 6. Team records

### `013` Team (TRF16; "to be phased out" in TRF26) [S5][S2]

- Name at columns 5–36.
- Member starting-rank numbers at 37–40, 42–45, 47–50 and so on: 4 digits in 5-column steps
  (member 8 at 72–75, member 14 at 102–105).
- The order of the members is the registration order and, when board order is fixed, the playing
  order.
- There is **no team pairing number field**. The TPN is implicit, which is an ambiguity.

### `310` Team (TRF26, replaces 013) [S2]

| Cols | Field |
|---|---|
| 1–3 | `310` |
| 5–7 | Team pairing number, 1–999 |
| 9–40 | Team name |
| 42–46 | Team nickname (used in 801/802) |
| 48–53 | Strength factor `111111` |
| 55–60 | Match points `1111.5` (end of tournament) |
| 62–67 | Game points `1111.5` |
| 69–71 | Team rank (ties allowed) |
| 74–77, 79–82, … | Members 1, 2, …, as 4-digit starting-rank numbers in 5-column steps (member 8 at 109–112, member 14 at 139–142) |

Gacrux ignores `013` whenever `310` records exist [S10].

### Other team records (TRF26) [S2]

- **`330` Forfeited match:** `330 TT RRR WWW BBB`.
  - Type at columns 5–6: `+-` means White won by forfeit, `-+` means Black won, `--` is a double
    forfeit.
  - Round at 8–10, the White team at 12–14, the Black team at 16–18.
  - A team counts as present if at least one of its players is present.
- **`300` Out-of-default-order line-up:** `300 RRR TTT OOO PPPP PPPP …`.
  - Round at 5–7, the team at 9–11, the opponent at 13–15, then the player ID on board 1, 2, … at
    17–20, 22–25 and so on (`0000` = empty board).
  - Required when a team does not play in its `310` order, gaps allowed (e.g. 1 5 3 4), or plays
    with an empty board.
- **`299` Abnormal points assignment:** `299 T MMMM GGGG RRR IIII …`.
  - Type at column 5: `W`, `D`, `L`, `F`, `H`, `Z`, `+`, `-` or blank (penalty/bonus).
  - MP at 8–11 and GP (or individual points) at 14–17, both `[-]11.5`; round at 20–22 (`000` = all);
    IDs at 24–27, 29–32, …
  - Used when a team's MP/GP do not follow from 162/362 and the board results.
  - Example: `299 +  2.0  2.5` / `299 -  0.0  1.5`.
- **`801` / `802`:** informative, human-readable team crosstables.
  - 801 has variable width, with widths derived from the number of teams, rounds, boards and maximum
    MP/GP, and uses base-36 "registration IDs".
  - 802 is fixed width: TPN 5–7, nickname 9–13, MP 15–20, GP 22–27, then 13 columns per round
    (opponent or `PAB`/`FPB`/`HPB`/`ZPB`, colour, GP, and an `f` forfeit flag).
  - Neither is needed by an engine.
- **`352` / `362`:** see §3.

### National Rating Support (NRS) records (TRF26) [S2]

- Same static layout as `001` (columns 5–79), but columns 1–3 hold a **federation code** (e.g.
  `BEL`) instead of `001`.
- They are linked to the player by the starting rank at 5–8. They carry a national rating at 49–52,
  a national ID at 58–68, and so on.
- `172` says how they affect initial ranking.
- A parser must therefore **not treat an unknown 3-letter alphabetic prefix as an error**. Gacrux
  handles this through a placeholder ("FID" is replaced by the NRS code) [S10].

## 7. Engine extensions and the pairing protocol

### 7.1 Invocation and output (de facto standard)

**JaVaFo 2.2** [S8]:

- Commands:
  - `java -ea -jar javafo.jar input.trfx -p [output]` pairs the next round.
  - `-c [round]` checks the pairings of one round or of all rounds.
  - `-g [config|seed] [-b] -o out.trf` runs the random tournament generator.
  - `-b` applies Baku acceleration to the round being paired.
  - `-l [file]` writes a checklist.
  - `-r` prints the release.
- The engine also has a Java API: `String JaVaFoApi.exec(int operation, Object... params)`, with
  operation codes such as 1000 (pair), 1100/1110/1111 (checklists), 1200 (check tournament) and
  1210 (check one round).
- Output: the first line is the number of pairs P. Each of the next P lines is `white black`, using
  pairing IDs from the `001` records. The PAB is written as `id 0`.
- If an error occurs, no output file is written.

**bbpPairings** [S9]:

- Commands:
  - `bbpPairings.exe [-r] (--dutch|--burstein) input -p [output] [-l [checklist]]`
  - `… input -c` checks a tournament.
  - `… (model -g | -g [config]) -o trf [-s seed]` generates a tournament.
- The system flag is mandatory. There is no `-b`: acceleration comes from the file.
- The output format is the same as JaVaFo's. The pairs are sorted, and the PAB is `id 0` [S9 main.cpp].
- Exit codes: 0 ok, 1 no valid pairing, 2 unexpected error, 3 invalid request or file, 4 size limit,
  5 file access.

**Gacrux** [S10]:

- TEC's MIT-licensed reference platform by Otto Milvang: a pairing checker, tie-break checker and
  tournament generator (Python). Its generator exports TRF-26.
- Its main data format is JSON ("JCH"); TRF is an input and output format.

**TEC PTC/RTG requirements:** Handbook C.02.03 §7.2.3–7.2.4 requires every endorsed THP to ship a
free CLI Pairings and Tie-Breaks Checker (PTC) and Random Tournament Generator (RTG). It defers the
details to the "TEC Manual" [S1]. I could not find that manual: the TEC Manual page on tec.fide.com is
empty, so the CLI syntax for PTC/RTG has no official spec I could check (Open question).

### 7.2 JaVaFo TRF(x) extension lines [S8]

- JaVaFo takes a *partial* TRF16 covering the rounds played so far and pairs the next round.
- Points (columns 81–84) must be correct.
- A player who is absent for the round to be paired can be marked in the next round's columns as
  `0000 - Z`, or `0000 - H` / `0000 - F` with Points updated. `XXZ` does the same job.

| Line | Syntax | Meaning |
|---|---|---|
| `XXR` | `XXR n` | Total rounds. **Needed:** in my tests, without it JaVaFo just output round 1's existing pairings again and did not pair a new round [E] |
| `XXC` | `XXC [rank] [white1\|black1]`, cumulative | `rank` pairs by position in the file (the "positional-id"), while output still uses 001 IDs. `white1`/`black1` forces the initial colour (the default is a hash-based pseudo-random choice) |
| `XXZ` | `XXZ id id …` (multiple lines allowed) | these players are not paired in the current round |
| `XXS` | `XXS CODE=VALUE …` (multiple lines allowed, later values override) | scoring system. Codes and defaults: `WW`/`BW` 1, `WD`/`BD` 0.5, `WL`/`BL` 0, `ZPB` 0, `HPB` 0.5, `FPB` 1, `PAB` 1, `FW` 1, `FL` 0. Shortcuts: `W` (=WW, BW, FW, FPB), `D` (=WD, BD, HPB), `L` (=WL, BL). When XXS is present, Points are strictly checked against the results |
| `XXA` | `XXA NNNN pp.p pp.p …` | fictitious (acceleration) points per player per round. NNNN at columns 5–8; round *r* at column `10+5(r-1)`. The full history must be kept because it affects float history |
| `XXP` | `XXP id id …` | none of the listed players may meet, in any round |

- **JaVaFo 2.2 ignores TRF26 records.** With `142 5` and no `XXR` it produced round 1 again.
- A `240 H 002 0002` did not remove player 2 from the pairing, even with `XXR` present.
- I could not tell from a round-2 test whether `152` is honoured, because colours by then follow from
  history.
- `XXZ 2` did remove player 2 [E].

### 7.3 bbpPairings "TRF(bx)" extensions and TRF26 support [S9]

- It "is designed to support the TRF-2026 file format". It "outputs files using the codes introduced
  in the 2026 version of the TRF, but it can also read files produced using the codes specified in
  the JaVaFo AUM".
- It does not support free points (`299` is rejected) or the `X` adjourned-game score (`162 X`
  rejected). Team tournaments (`013`/`310`) are rejected.
- Records it reads:
  - `001`;
  - `142` or `XXR`;
  - `152`, which must be exactly `152 W` or `152 B` (5 characters);
  - `XXC rank|white1|black1`;
  - `162`, `192`, `240`, `250`, `260`, `XXA`, `XXP`;
  - `BBW`/`BBD`/`BBL`/`BBZ`/`BBF`/`BBU` (`BBx pp.p`, the points for win, draw, played loss, ZPB,
    forfeit loss and PAB).
- Lines it silently ignores: `XXZ` and `XXS` (confirmed by grep and by running it [E]).
- **Byes:** `240` marks a player as unpaired only for the round being paired. The bye type at column
  5 is ignored [S9].
- **Round count:** for `-p`, the total number of rounds is required (via 142 or XXR).
- **Initial colour:** if none is given, it is inferred from the first round with colours. Round 1
  cannot be paired without an explicit initial colour, because there is no random choice.
- **Burstein default acceleration:** with no `XXA`/`250` lines, Burstein applies its default
  acceleration. Adding any acceleration line switches it off.
- **Score check:** Points are always checked against the results and the point system.
- **Writer quirk:** the generator writes the system as `092 FIDE_DUTCH_2025`, i.e. into the
  free-text *type* record, not `192` [S9 trf.cpp writeFile]. This looks like a bug.

### 7.4 Mapping between the two dialects

| Concept | JaVaFo TRF(x) | TRF26 | Notes |
|---|---|---|---|
| total rounds | `XXR n` | `142 n` | both engines' docs agree in meaning |
| initial colour | `XXC white1/black1` | `152 W/B` | TRF26 has no equivalent of the random default |
| pair by file order | `XXC rank` | — | no TRF26 equivalent. TRF26 relies on `001` columns 5–8 as the pairing number |
| absent / requested bye next round | `XXZ ids` or `0000 - Z/H/F` in the next round's columns | `240 Z/H/F rrr ids` for the round to be paired | the in-column form works in both engines [E] |
| scoring | `XXS …` | `162 …` | XXS is richer (colour-specific values, FPB/HPB separate from W/D) |
| acceleration | `XXA id p p p …` per player per round | `250` ranges of IDs × rounds | converting XXA→250 is lossless (one line per player per round if needed) |
| forbidden pairs | `XXP ids`, all rounds | `260 rf rl ids`, a round range | |
| system | CLI (`-b` for Baku); JaVaFo is Dutch-only | `192 FIDE_…` | |

## 8. Empirical checks [E]

- **Setup:**
  - JaVaFo: `javafo.jar` from rrweb.org/javafo/current, `-r` = "Rel. 2.2 (Build 3222)".
  - bbpPairings: built from `master` at commit 8f9e3c5 (2026-07-30).
  - Input: a 5-player TRF16 with round 1 played, pairing round 2.
- **Results:**

| Input variant | JaVaFo 2.2 | bbpPairings |
|---|---|---|
| `XXR 5` + `XXC white1` | 3 1 / 4 5 / 2 0 | same |
| `142 5` + `152 W` (TRF26 only) | 1 4 / 5 2 / 3 0 (just round 1's existing pairings again) | 3 1 / 4 5 / 2 0 |
| no round count at all | same as above (round 1 again) | error, exit 3: "total number of rounds … must be specified" |
| `240 H 002 0002` (with `142`; for JaVaFo also retested with `XXR`) | ignores the 240 | 2 pairs, player 2 not paired |
| TRF16 + `XXZ 2` | 2 pairs, player 2 not paired | ignores XXZ (player 2 gets the PAB) |
| `0000 - H` in player 2's round-2 columns, Points 0.5 | player 2 not paired | player 2 not paired |
| `XXS W=3 D=1` | (not tested) | silently ignored |

## Implications for the library

1. **Model TRF26 as the canonical format, with a TRF16+XX reader.**
   - The parser should accept TRF26 and TRF16, the JaVaFo `XX?` lines (`XXR`, `XXC`, `XXZ`, `XXS`,
     `XXA`, `XXP`) and bbp's `BB?` lines, and normalise them into one tournament model.
   - It should keep `###` comments and unknown lines, NRS federation-code records among them, for
     round-tripping. Unknown lines must never be an error.
2. **Pick the writer dialect explicitly.**
   - Write TRF26 by default (`142`, `152`, `162`, `192`, `240`, `250`, `260`).
   - Offer a `--dialect=javafo` option that also emits `XXR`/`XXC`/`XXZ`/`XXS`/`XXA`/`XXP`. Without
     those lines JaVaFo, still the most-used oracle, pairs incorrectly.
   - Emitting both sets together is safe: each engine ignores the other's lines, with the caveat that
     `XXC rank` changes semantics.
3. **Use the JaVaFo/bbp CLI contract** so existing THP integrations and test harnesses can drop the
   library in:
   - `-p [out]`, `-c [round]`, `-g … -o …`, `-l`, `-r`, and bbp-style `--<system>` flags;
   - output as a count line plus `white black` lines, with `0` for the PAB;
   - bbp's exit codes 0–5.
4. **Choose the pairing system in this order:** CLI flag first, then `192`, then the default
   (`FIDE_DUTCH`, resolved by date).
   - Report a mismatch as an error. bbp silently lets the flag win.
   - Accept `FIDE_DUTCH_2025*` as an alias of `_2026` for bbp/TEC-draft compatibility.
   - Define our own codes for systems missing from ETT26 (Lim; year-versioned Dubov/Burstein),
     treat them as provisional, and document them.
5. **Separate "rounds played" from "round to pair".**
   - The round to pair is `max(played)+1`.
   - Absences and requested byes for that round come from `240`, `XXZ` or next-round `0000 - Z/H/F`
     entries. Merge all three.
   - `142`/`XXR` is required for `-p` because last-round rules depend on it.
6. **Keep scores exact.** Use a decimal or fixed-point type with one decimal place ("11.5" formats).
   Like bbp, check Points (81–84) against the results and the scoring system, and fail loudly on a
   mismatch.
7. **Represent acceleration as per-participant, per-round virtual points.** That covers both `XXA`
   and `250`. Apply `_BAKU` from `192` only when no explicit `250`/`XXA` exists, as TRF26 says for
   ITDX.
8. **Team (Swiss Team System) support needs:**
   - `310` (with `013` as a fallback), `320`, `330`, `300`, `299`, `352` and `362`;
   - `250` MP/GP fields and team IDs in `240`/`260`;
   - 3-digit team pairing numbers in some records (`310`/`320`/`330`/`300`) and 4-digit IDs in
     others (`240`/`250`/`260`/`299`);
   - no engine protocol exists for team pairing output, so define one, e.g. `teamW teamB` lines with
     `id 0` for the PAB.
9. **Test corpus:** use JaVaFo `-g` and bbp `-g` (TRF16/TRF(x)) and Gacrux's generator (TRF-26) as
   independent sources, with JaVaFo and bbp as cross-checking oracles for the Dutch system.

## Open questions

1. **Lim, Dubov and Burstein codes:** ETT26 has no `192` code for Lim and no 2026-versioned Dubov or
   Burstein codes. Will TEC add them? Until then, what should our TRF writer emit?
2. **`250` example:** is it intentional that the TRF26 example (MP-based team Baku) puts the virtual
   points in the GP field? C.04.7 implies match points.
3. **PTC/RTG CLI:** the TEC Manual the Handbook cites for their CLI syntax is not published (the
   tec.fide.com page is empty). Is there an official CLI contract, or is JaVaFo's the expected one?
4. **Encoding and line endings:** TRF26 says only "CR". Should we write CR, CRLF or LF, and must the
   file be UTF-8? This needs a decision; I suggest reading any of them and writing CRLF + UTF-8.
5. **Team pairing output:** there is no standard pairing-engine output for team rounds (board order,
   colours per board from `352`). We need to design one, or find whether Gacrux or a THP has a
   convention.
6. **JaVaFo and TRF26:** will JaVaFo (2.2, build 3222) be updated for the 2026 Dutch rules and TRF26?
   If not, it is only a valid oracle for `FIDE_DUTCH_2017`, with TRF(x) input.
7. **`162` vs `XXS`:** `162` cannot express colour-dependent scores, or FPB/HPB values that differ
   from W/D. When a `XXS` file can't be mapped to `162`, should we emit both, or refuse?
8. **Pairing numbers vs ranks:** should `XXC rank` (pairing by file order) be supported for writing
   at all, given TRF26 has no equivalent?

## Sources

- [S1] FIDE Handbook C.02.03 *Chess Equipment with Electronic Components (effective from 1 March 2026)*, §7 (THPs), §7.2.1–7.2.4, Annexures A–C. https://handbook.fide.com/chapter/ChessEquipmentWithElectronicComponenets032026
- [S2] FIDE Handbook, Annexure/Appendix A – *Tournament Report File Format Version 2026 (TRF26)*, "Approved by FIDE Council on 12/05/2025, Applied from 01/09/2025". https://handbook.fide.com/files/handbook/TRF26.pdf
- [S3] Annexure B – *MTB26 Mandatory Tie-Breaks*. https://handbook.fide.com/files/handbook/MTB26.pdf
- [S4] Annexure C – *ETT26 Encoded Type (of tournament) Table* (codes for record 192). https://handbook.fide.com/files/handbook/ETT26.pdf
- [S5] *Format of TRF (Tournament Report File)* – TRF16 (C. Krause 2006; updated Tromsø 13/08/2014; approved Elista 10/08/2015). https://www.fide.com/FIDE/handbook/C04Annex2_TRF16.pdf (also https://handbook.fide.com/files/handbook/C04Annex2_TRF16.pdf)
- [S6] FIDE TEC, *TRF-2026* page and attachments (TRF-2026.pdf dated 2025-04-17; TournamentTypeCodeTable192-TRF26.pdf with `FIDE_DUTCH_2025`; Annexures B/C TRF06/TRF16). https://tec.fide.com/trf-2026/
- [S7] FIDE TEC drafts: *Draft TRF 2025* (2024-09-04) http://tec.fide.com/2024/09/04/draft-trf-2025-extensions-for-team-pairing-and-tie-breaks/ ; *TRF25 Final Draft* (2025-01-09) https://tec.fide.com/2025/01/09/trf25-final-draft/
- [S8] R. Ricca, *JaVaFo Advanced User Manual* (v2.x). https://www.rrweb.org/javafo/aum/JaVaFo2_AUM.htm ; JaVaFo home http://www.rrweb.org/javafo/JaVaFo.htm (current version 2.2, 2018-09-15); jar http://www.rrweb.org/javafo/current/javafo.jar
- [S9] bbpPairings, `README.txt`, `src/fileformats/trf.cpp`, `src/main.cpp` at commit 8f9e3c5 (2026-07-30); latest release v6.0.0 (2026-02-01). https://github.com/BieremaBoyzProgramming/bbpPairings
- [S10] Gacrux / TieBreakServer (O. Milvang), `gacrux/trf2json.py` at commit 6419149 (2026-09-19) https://github.com/OttoMilvang/TieBreakServer ; *Gacrux software* v1.7 (2026-03-24) http://gacrux.no/spp/doc/GacruxSoftware.pdf
- [S11] FIDE TEC, *2026 FIDE Congress – TEC Commission Meeting and Term Report 2022–2026*. http://tec.fide.com/wp-content/uploads/2026/09/TEC-2026-Congress-Meeting.pdf
- [S12] FIDE Handbook C.04.7 *FIDE-approved Accelerated Systems (effective from 1 February 2026)*. https://handbook.fide.com/chapter/C0407202602
- [E] Local runs of JaVaFo 2.2 (build 3222) and bbpPairings (commit 8f9e3c5, built with g++ 15.2) on hand-made TRF files, 2026-09-23.
