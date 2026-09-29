# fide-swiss user manual

This manual describes what the program does today, for tournament arbiters and for developers. Anything not built yet is marked **planned**. Where the manual and the code disagree, the code wins; report the difference.

Contents

1. [What it is](#1-what-it-is)
2. [Installing, building and running](#2-installing-building-and-running)
3. [Commands](#3-commands)
4. [TRF input and output](#4-trf-input-and-output)
5. [Pairing systems](#5-pairing-systems)
6. [Tie-breaks and standings](#6-tie-breaks-and-standings)
7. [The tournament generator](#7-the-tournament-generator)
8. [Checking pairings](#8-checking-pairings)
9. [Troubleshooting](#9-troubleshooting)

## 1. What it is

`fide-swiss` is a Java library and a command-line tool. It pairs the next round of a Swiss tournament, individual or team, as the FIDE Handbook prescribes, computes the FIDE tie-breaks (C.07), checks pairings that someone else made, and generates random tournaments for testing. It reads and writes FIDE Tournament Report Files (TRF16 and TRF26). It is not affiliated with or endorsed by FIDE.

The project is **pre-alpha**. Every system below is `experimental`: it pairs rounds, but no release has passed the full verification gate yet. Statuses follow the definition of done in the project's verification strategy: *experimental* (in progress), *Handbook-verified* (with witness review) or *Oracle-verified* (the 50,000-tournament release gate passed). Do not rely on it as the only pairing authority for a rated event yet.

### Rules and editions

| Handbook | Rules | Edition | Status today |
|---|---|---|---|
| C.04.1, C.04.2 | Basic Rules, General Handling Rules | 2026 | experimental; pairing numbers, requested byes, withdrawals, colour history, forfeits and board order are implemented |
| C.04.3 | Dutch System | 2026 | experimental; matches the Oracle bbpPairings v6.0.0 in every round of 488 generated tournaments |
| C.04.1 to C.04.3 | Dutch System, historic edition ("pre-2026", the 2017 text) | 2017 / pre-2026 | experimental; matches bbpPairings v5.0.1 in 354 generated tournaments |
| C.04.4.1 | Dubov System | 2026 | experimental; no Oracle exists, checked against a plain enumeration of the text |
| C.04.4.2 | Burstein System | 2026 | experimental; seeding rounds are Dutch rounds; no Oracle |
| C.04.4.3 | Lim System | 2026 | experimental; no Oracle |
| C.04.5 | Double-Swiss System | 2026 | **planned**; not built |
| C.04.6 | Swiss Team Pairing System | 2026 | experimental; matches a patched Gacrux on a 40-tournament regression corpus |
| C.04.7 | Acceleration methods (Baku, explicit virtual points) | 2026 | implemented for Dutch, Dubov, Burstein and Lim |
| C.07 | Tie-Breaks | 2026-03 and 2024-08 editions | implemented for individual tournaments; team tie-breaks **planned** |

The Oracle results are the ones stated in `README.md` at the time of writing; the README table is the maintained record.

Two things that surprise people:

- A bare pairing system code in a TRF file always means the 2026 rules, never the date of the tournament.
- The library never draws lots. Participants that tie on every tie-break share a rank.

## 2. Installing, building and running

### Requirements

Java 25. The repository pins `java=25.0.4-tem` in `.sdkmanrc`; with SDKMAN run `sdk env` in the repository directory. With an older Java the launcher fails with `Unsupported major.minor version 69.0`.

### Building from source

```sh
source ~/.sdkman/bin/sdkman-init.sh && sdk env   # selects Java 25
./mvnw verify                                    # tests, formatting check, coverage report
./mvnw -q -o install -DskipTests                 # build only; -o works offline once dependencies are cached
./mvnw spotless:apply                            # format the code
git config core.hooksPath .githooks              # optional: run the tests before every commit
```

The build produces, under `cli/target/`, a portable distribution `fide-swiss-<version>.zip` and `.tar.gz`. It contains `bin/fide-swiss` (and `bin/fide-swiss.bat` for Windows), `lib/` with the four modular jars, and `LICENSE`, `NOTICE`, `README.md`. Unpack it anywhere and run `bin/fide-swiss`. It needs Java 25 on the path (or `JAVA_HOME`).

Release builds are also planned as jlink images (linux-x64, linux-aarch64, macos-aarch64, windows-x64) that need no Java. On macOS remove the quarantine attribute and run the image from a terminal. The library is not yet published to Maven Central.

### Modules

| Artifact | Contents |
|---|---|
| `fide-swiss-pairing-engine-core` | Domain model, handling rules, every pairing system, acceleration, tie-breaks. No runtime dependencies. |
| `fide-swiss-pairing-engine-trf` | TRF reader and writer. |
| `fide-swiss-pairing-engine-generator` | Random Tournament Generator. Depends only on `core`. |
| `fide-swiss-pairing-engine-cli` | The `fide-swiss` command. |

Group id `io.github.markdechamps`, one version for every module. The design follows a pure domain core with ports and adapters: the core knows nothing about files, the `trf` module and the CLI are adapters.

### Running

```
$ fide-swiss version
fide-swiss 0.1.0-SNAPSHOT
  C.04.3 Dutch System 2026 (with C.04.1/C.04.2 2026): experimental
  C.04.4.1 Dubov System 2026: experimental
  C.04.4.3 Lim System 2026: experimental
  C.04.6 Swiss Team Pairing System 2026: experimental
```

Note: the `version` listing does not yet mention the Burstein System although it is built; the list in section 1 is the accurate one.

Running with no arguments, or with `help`, `--help` or `-h`, prints the usage text.

## 3. Commands

There are two grammars. A subcommand name first (`pair`, `check`, `standings`, `generate`, `version`) gives the canonical grammar. Anything else is parsed as the JaVaFo/bbpPairings short form (`--dutch in.trf -p reply`), so a tournament-handling program that already drives JaVaFo or bbpPairings can call `fide-swiss` unchanged (ADR 0005). Both grammars produce the same results.

```
fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [--explain <id>]... [--quiet] [settings]
fide-swiss [--dutch|--dubov|--burstein|--lim|--swiss-team] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
fide-swiss check <in.trf> [--round <r>] [settings]   |   <in.trf> -c [<r>]   |   -check <in.trf>
fide-swiss standings <in.trf> [--after <r>] [--why <id> <id>] [settings]
fide-swiss generate -o <out%d.trf> [--seed <n>] [--count <k>] [--profile <name>] [--config <cfg>] [--model <in.trf>] [ranges] [tournament flags]
fide-swiss -g [<cfg>|<seed>] -o <out.trf> [-s <seed>] [--dutch|--dubov|--burstein|--lim]
fide-swiss version | -r
```

`-` as the input file reads the TRF from standard input.

### 3.1 Settings and precedence

Settings come from three layers. A later layer overrides an earlier one:

1. the **profile**'s defaults (for pairing: the defaults of the individual Swiss profile, i.e. Dutch 2026, 1 / 1/2 / 0 scoring, tie-break list `BH/C1, BH, SB, DE` under C.07 2026-03);
2. the **file**'s records (`142`, `152`, `192`, `202`/`212`, `XXR`, `XXC`, `XXA`, ...; see section 4);
3. the **command-line flags**.

The flags below work on `pair`, `check` and `standings`, and after `-p`/`-c` in the short form.

| Flag | Values | Overrides | Meaning |
|---|---|---|---|
| `--system <name>` | `dutch`, `dubov`, `burstein`, `lim`, `swiss-team` | record `192` | The pairing system. Also written `--dutch`, `--dubov`, `--burstein`, `--lim`, `--swiss-team`. |
| `--maxi-tournament` | none | (no TRF record exists) | Declare a Lim tournament a Maxi-tournament. Only meaningful for Lim. |
| `--edition` | `2026`, `pre-2026` | record `192` edition | The Swiss Rules Edition. `pre-2026` is the Dutch System 2017. Dubov, Burstein and Lim reject `pre-2026`. |
| `--rounds <n>` | integer | `142` / `XXR` | The number of rounds. |
| `--initial-colour` | `white`, `black` | `152` / `XXC` | The initial colour. |
| `--tiebreaks "<list>"` | a C.07 list, see section 6 | `202` / `212` | The tie-break list. |
| `--tiebreak-edition` | `2026-03`, `2024-08` | profile | The C.07 edition that defines the tie-break values. |
| `--interpretation <name>=<value>` | see 5.5 | profile | A Swiss Team reading (ADR 0003). Repeatable. |

A team file (one with `310` records) is paired by the Swiss Team System and only accepts `--swiss-team` (or `--system swiss-team`); asking for another system on it is an error, and so is asking for `--swiss-team` on a file with no `310` records (see section 9).

### 3.2 `pair`

Pairs the next round: the first round after the last one the file records.

```
fide-swiss pair tournament.trf                       # reply on standard output
fide-swiss pair tournament.trf -o reply.txt          # reply into a file
fide-swiss pair tournament.trf -o reply.txt -l       # also write the trace to tournament.trf.trace.txt
fide-swiss pair tournament.trf -l why.txt --explain 12 --explain 5
fide-swiss --dutch tournament.trf -p reply.txt       # JaVaFo / bbpPairings form
fide-swiss --dubov tournament.trf -p                 # -p without a file: reply on standard output
```

| Flag | Meaning |
|---|---|
| `-o <file>` (canonical) or `-p [<file>]` (short form) | Where the reply goes. Without a file the reply is written to standard output. |
| `-l [<file>]` | Write the trace: for every bracket its residents, moved-down players, pairs, downfloaters and the failed criteria, then for every board the colour rule used. A bare `-l` writes `<input>.trace.txt` beside the input. Without `-l` no trace is written. |
| `--explain <id>` | Explain what happened to that participant: the bracket, floats, opponent, and colour rule. Repeatable. The explanation goes to standard error, or, when `-l` is given, is appended to the trace file. The id is the file's own id (the starting rank in the `001` record). |
| `--quiet` | Suppress the progress line. Without it, a terminal shows a one-line progress indicator on standard error after 500 ms of pairing, erased when the pairing ends. It is never shown when standard error is not a terminal. |
| settings | see 3.1 |

In the short form the system flags `--dutch`, `--dubov`, `--burstein`, `--lim`, `--swiss-team` are settings flags like any other and may appear anywhere. If `-p` is missing (and no `-c`), the command stops with `nothing to do: give -p to pair the next round or -c to check`.

**The reply** is JaVaFo's Pairing Reply, ended with LF:

```
6
1 3
7 2
9 6
5 4
8 12
10 11
```

The first line is the number of lines that follow. Each further line is `white black`, one per board in board order (General Handling Rules 3.6), using the ids of the file. The pairing-allocated bye, if any, comes last as `<id> 0`. Ids are the file's own, never the internal pairing numbers (ADR 0006 explains why the two can differ).

**Absent players in the last round column.** If the last round column of the file holds only `0000 - H`, `0000 - F` or `0000 - Z` marks, those participants are treated as absent from the round to be paired (half-point bye request, full-point bye request, zero-point bye request).

**Ctrl-C** cancels a running pairing without writing a reply; the README states exit code 130 for this (that value comes from the shell/JVM, not from program code).

A trace looks like this (Dutch, from a generated tournament):

```
bracket 3: residents [1], moved down []; pairs [], downfloaters [1]; fails C6=[1] C7=[3]
bracket 2.5: residents [3], moved down [1]; pairs [1-3], downfloaters []; fails C12=[1] C13=[1]
...
board 1: colours by [C.04.3 5.2.4]
```

`fails C6=[1]` lists the quality criteria that the chosen pairing did not fully satisfy and by how much; it is not an error. The bracket header is the score of the bracket.

An `--explain 1` gives lines such as `1 floated down from bracket 3`, `1 is paired with 3 in bracket 2.5 on board 1`, `1 has white [C.04.3 5.2.4]`.

### 3.3 `check`

Re-pairs every round recorded in the file, and compares the recorded pairings with the rules and with the system's own pairing. See section 8 for what is verified and how to read the output.

```
fide-swiss check tournament.trf
fide-swiss check tournament.trf --round 3
fide-swiss tournament.trf -c        # short form, all rounds
fide-swiss tournament.trf -c 3      # short form, one round
fide-swiss -check tournament.trf    # JaVaFo-style alias
```

`--round <r>` (or `-c <r>`) restricts the check to one recorded round, and then the standings are not checked. Settings flags apply (for example to check a file against a different system).

### 3.4 `standings`

```
fide-swiss standings tournament.trf
fide-swiss standings tournament.trf --after 3
fide-swiss standings tournament.trf --why 3 4
```

- Without `--after`, the standings after the last recorded round; with `--after <r>`, after round r.
- Output is a table with one row per participant: `rank`, `id`, `name`, `score`, one column per tie-break in list order, and `decided by`. The list comes from record `212` (without its `PTS`) or `202`; without either, the profile's list; `--tiebreaks` overrides both.
- `decided by` is filled for a row that has the same score as the row above. It names the first tie-break that separates them (for example `BH 5 > 4`) or `shared` when every tie-break is equal (the two share a rank).
- `--why <id> <id>` prints why the first participant ranks above the second, with the value of the deciding tie-break for both and every per-round contribution (the opponent or the Dummy Opponent, the value used, what cut or adjusted it, with the C.07 article). When the two are fully tied it prints `<a> and <b> share a rank: every tie-break is equal`.

```
rank  id  name         score  BH/C1  BH   SB    DE  decided by
1     1   Player 0001  3      4      5.5  5.5   1
2     3   Player 0003  2.5    4      5.5  4.75  1
...
8     5   Player 0005  1      4      4    0     1
8     8   Player 0008  1      4      4    0     1   shared
```

See section 6 for the tie-breaks themselves.

### 3.5 `generate` and `-g`

Generates random, valid tournaments as TRF26 files. The complete description of ranges, profiles and the manifest is section 7.

```
fide-swiss generate -o 'tournament%d.trf' --seed 42 --count 10 --players 20..60 --rounds 7
fide-swiss -g 42 -o one.trf                 # JaVaFo/bbpPairings form: one tournament from seed 42
fide-swiss -g rtg.cfg -o one.trf -s 42      # from a configuration file and a seed
```

### 3.6 `version` and `-r`

Prints the version and the implemented systems (see section 2).

### 3.7 Exit codes

| Code | Meaning |
|---|---|
| 0 | Success. |
| 1 | No legal pairing for the round. The message lists the problems and the pairing trace is printed on standard error. |
| 2 | Unexpected internal error (`error: internal: ...`). This is a bug; please report it with the file. |
| 3 | Invalid request or invalid input: bad flag or value, invalid TRF, invalid settings, or an invalid tournament. |
| 4 | Size limit. Reserved for bbpPairings compatibility; never emitted. |
| 5 | File access error (missing or unreadable file, unwritable output). |
| 6 | `check` found inconsistencies. |
| 7 | `generate` skipped more than 0.1 % of its seeds. |

The table is a public contract (ADR 0005); changing it is a major-version change (ADR 0008). It is bbpPairings' table plus code 6.

### 3.8 Output streams and encoding

- The reply, the standings table, the check report and the version text go to standard output. Errors, the printed generator seed, the progress line and `--explain` text go to standard error.
- Errors have the form `error: <message>`. Rule problems add the article in brackets and the participants involved: `error: [<article>] <message> (<participant>, ...)`.
- Input is read as UTF-8, falling back to ISO-8859-1 when it is not valid UTF-8. Line endings CR, LF and CRLF are all accepted. Output files are written as UTF-8 with LF.

## 4. TRF input and output

The reader takes TRF26 and TRF16 files, plus the JaVaFo `XX?` lines. Records are recognised by their first three characters. Lines shorter than three characters and lines starting with `###` are skipped. A record the reader does not know is set aside and never an error. Fixed-column fields use the FIDE layout with 1-based columns.

### 4.1 Records read

| Record | Read for | Notes |
|---|---|---|
| `001` | one participant | Start rank (columns 5-8), title (11-13), name (15-47), rating (49-52; blank or 0 is unrated), points (81-84), rank (86-89), then one 10-column block per round from column 92. The start rank becomes the participant id used in every reply, table and message. The declared points and rank are only used by `check`. Other 001 fields (sex, federation, FIDE id, birth date) are ignored. |
| `142` | number of rounds | First word of the value. Without it, `XXR`, else the number of round columns in the file (at least 1). |
| `XXR` | number of rounds | JaVaFo's equivalent of `142`; used when `142` is absent. |
| `152` | initial colour | Value starting with `B` is black, anything else white. Without `152` or `XXC`, the initial colour is inferred from the top participant paired in round 1 (as TRF26 prescribes and bbpPairings does); it defaults to white. |
| `XXC` | initial colour | JaVaFo: the words `white1` or `black1`; the last one wins. |
| `162` | scoring scheme | Symbol and points pairs: `W` win, `D` draw, `L` loss, `P` pairing-allocated bye. Missing values keep the standard 1 / 0.5 / 0. In a team file it gives the game points (and `P` per board). |
| `192` | pairing system and acceleration | See 4.3. A blank value is like no record. |
| `092` | pairing system | bbpPairings writes its code here. Read as `192` only when there is no `192` and the value starts with `FIDE_DUTCH`. |
| `202` | tie-break list | The tie-breaks among equal points, comma separated (section 6). |
| `212` | tie-break list | The full standings order starting with `PTS`; `PTS` is dropped. `212` is preferred over `202` when both exist. Without either, the profile's `BH/C1, BH, SB, DE`. |
| `250` | explicit acceleration | Virtual points (TRF26): game points in columns 10-13, first and last round in 15-17 and 19-21, first and last id in 23-26 and 28-31. Blank ranges mean all rounds or a single id (the first). |
| `XXA` | explicit acceleration | JaVaFo: id in columns 5-8, then the virtual points of round r at column 10 + 5(r-1). |
| `310` | a team | Team number (5-7), name (9-40), strength factor (48-53, used as rating), match points (55-60), game points (62-67), rank (69-71), member start ranks from column 74 in steps of 5. Its presence makes the file a **team file**. |
| `300` | team line-up for a round | Overrides the `310` line-up for that round. |
| `320` | team pairing-allocated bye value | Match points at columns 5-8 and game points at 10-13. |
| `330` | matches nobody played | Read for team files. |
| `352` | board colour pattern | The pattern's length is the number of boards; it also gives a team its colour when board 1 has none. Without it the number of boards is the longest line-up. |
| `362` | team match scoring | Symbols `TW`, `TD`, `TL` with points; defaults 2 / 1 / 0. |

Everything else (`012`, `022`, `032`, `042`, `052`, `062`, `072`, `082`, `092` with another prefix, `102` ... `132`, `172`, `182`, `240`, etc.) is accepted and ignored. In particular `062`, the number of players, is not needed: the reader counts the `001` records itself. The header value of `012` is not used.

### 4.2 The round blocks of `001`

Each block covers 10 columns: opponent id (first 4 columns of the block), colour (column 6 of the block, `w`, `b` or `-`/blank for none), result (column 8).

| Result | Meaning |
|---|---|
| `1` or `W` | win (`W` is a win in under one move: a game played) |
| `=` or `D` | draw (`D`: a draw in under one move) |
| `0` or `L` | loss |
| `+` (with an opponent) | forfeit win. The opponent's cell holds `-`. |
| `-` (with an opponent) | forfeit loss; `-` in both cells is a double forfeit |
| `U` (opponent `0000`) | pairing-allocated bye |
| `F` or `+` (opponent `0000`) | full-point bye (requested) |
| `H` | half-point bye (requested) |
| `Z`, `-` or blank (opponent `0000`) | zero-point bye (requested, or absence) |

A game with no colour (a forfeit) puts the lower start rank on White. An unknown result code is an error: `Unknown result code 'x'`. A round that refers to an unknown player is `Round <r> refers to unknown player <id>`.

Recorded rounds are read as facts (ADR 0004): the reader does not reject a rematch or a second PAB. `check` reports those.

**The round to pair.** The rounds the file records are the rounds already played; `pair` pairs the round after them. If the last round column holds only byes/absences (no opponents and no PAB), it is not a played round but the list of participants who are absent, or who requested a bye, in the round to be paired: `0000 - H`, `0000 - F`, `0000 - Z`.

### 4.3 Record `192` and system selection

The value is upper-cased. An optional `_BAKU` suffix means Baku acceleration and does not change the system. Explicit virtual points in `250` or `XXA` override `_BAKU`.

| `192` code | System |
|---|---|
| `FIDE_DUTCH`, `FIDE_DUTCH_2026`, `FIDE_DUTCH_2025` | Dutch System, 2026 |
| `FIDE_DUTCH_2017` | Dutch System, pre-2026 edition (the 2017 text) |
| `FIDE_DUBOV`, `FIDE_DUBOV_2026` | Dubov System 2026 |
| `FIDE_BURSTEIN`, `FIDE_BURSTEIN_2026` | Burstein System 2026 |
| `FIDE_LIM`, `FIDE_LIM_2026` | Lim System 2026 (provisional: TRF26 has no Lim code) |
| `FIDE_TEAM...` | Swiss Team System (team files only, see below) |
| blank or no `192` | the profile's system: Dutch 2026 |

Any other code is rejected with `Unsupported pairing system in record 192: <CODE>` (exit 3). This includes `FIDE_DOUBLESWISS` (Double-Swiss is **planned**, ADR 0007 describes the intended TRF encoding, two TRF rounds per match, but the reader does not accept it yet).

For a **team file** (any `310` record, or a `192` containing `TEAM`), the code must start with `FIDE_TEAM`; the form is `FIDE_TEAM[_TYPEA|_TYPEB][_MP|_GP][_GP|_MP][_BAKU]`:

- `_TYPEA` or `_TYPEB` sets the colour preference type (5.4); a bare `FIDE_TEAM` is Type A; a code with neither suffix, for example `FIDE_TEAM_MP`, uses no colour preferences.
- The first of `MP` (match points) and `GP` (game points) is the primary score; a second one is the secondary score used for colours. A code naming one has no secondary score; a code naming neither keeps the C.04.6 default (match points primary, game points secondary).

Any other `192` code for a team file gives `Unsupported 192 code <code> for a team file`.

### 4.4 What is written

The `generate` command writes TRF26 files with: `012` (the name `RTG <version> seed <n>`), `142`, `152`, `162` (only when different from the standard scoring), `192` (with `_BAKU` when Baku is on), `212` (`PTS` followed by the tie-break list, when the list is not empty) and one `001` record per participant. The writer reads back to the same rounds and settings. The writer can also emit `XXR` and `XXC` for JaVaFo. It does not write team files.

## 5. Pairing systems

Every system pairs one round from the tournament as recorded, and returns the boards in General Handling Rules 3.6 order, the pairing-allocated bye (PAB) if the number of players is odd, and a trace. All systems share the Basic Rules (C.04.1) and General Handling Rules (C.04.2): no two players meet twice, at most one PAB per player (and none for a player who already had a PAB or won by forfeit), absolute colour limits, requested byes and withdrawals, and the rule that only participants already taken into account for a pairing hold a pairing number (ADR 0006).

Select a system with the `192` record or the `--system` flag (section 3.1). The default is the Dutch System 2026.

### 5.1 Dutch System (C.04.3)

The classic system. Players are grouped in scoregroups; each scoregroup is a bracket that is split into two halves (highest against the middle of the group) and paired by the order of the rules, while the quality criteria (C1 onwards: pairing completion, PAB, absolute colour, minimum downfloaters and their scores, colour preferences, repeated floats) decide which of the candidate pairings is best. The library finds the exact optimum. Colours are then allocated by Article 5.

- Editions: `2026` (default) and `pre-2026` (the 2017 text with its own last-bracket procedure, A.9), chosen with `--edition` or `FIDE_DUTCH_2017`.
- Acceleration: Baku or explicit virtual points.
- Verified against bbpPairings (v6.0.0 for 2026, v5.0.1 for 2017). Documented readings: README, "Readings of the Dutch 2026 text" and "Readings of the Dutch 2017 text"; ADR 0006.

### 5.2 Dubov System (C.04.4.1)

Aims to give each player the opponents best balanced by their opponents' average rating (ARO). Within a bracket some players upfloat, players are split into two subgroups by colour seeking, and pairs are chosen by a cheapest perfect matching with the criteria [C8]-[C10] (upfloaters, their scores) applied in the text's order. Shifters and transpositions follow 3.2.4.

- Edition: 2026 only. `--edition pre-2026` is rejected (`[GHR 1.3]`).
- Acceleration: supported (uses the Pairing Score).
- No Oracle exists. It is checked against a plain enumeration of the text. Documented readings: README, "Readings of the Dubov 2026 text" (upfloated, [C9], 4.4.1 typo, shifters, ARO rounding, 3.1.4, MaxT, acceleration).

### 5.3 Burstein System (C.04.4.2)

The first rounds ("seeding rounds", 1.6) are paired exactly as the Dutch System 2026 would. Afterwards, players are ranked by the Opposition Evaluation Index (Buchholz, then Sonneborn-Berger, with the unplayed-round rules of 1.7.2) and each bracket is paired by one cheapest perfect matching holding [C5]-[C8] above the order of Article 4.

- Edition: 2026 only.
- Acceleration: supported.
- No Oracle (bbpPairings pairs the older text). Documented readings: README, "Readings of the Burstein 2026 text" (seeding rounds, [C6]/[C7], [C8], unplayed rounds, board order, the duplicated line of the 4.3 note).
- Note: `fide-swiss version` does not list this system yet; the code pairs it.

### 5.4 Lim System (C.04.4.3)

A procedure rather than an optimisation: every choice is "the first in this order". Scoregroups are handled from the top, the Median Scoregroup last; floaters are picked by the rules of 3.9, incoming floaters are paired first, and when a bracket cannot be completed earlier pairings are taken back ("cracking", 2.6). Round 1 follows Article 7.

- Edition: 2026 only.
- **Maxi-tournament**: an organiser declaration, never inferred from the size of the field. Declared with `--maxi-tournament` (there is no TRF record for it; the flag also applies with `check` and `standings`). In a Maxi-tournament, floater choices by colour (3.2.3) and colour exchanges (3.8, 5.7) are only allowed between players rated within 100 points. `--maxi-tournament` on any other system fails with `[C.04.4.3 3.2.3] only the Lim System has a Maxi-tournament setting`.
- Acceleration: supported; the Median Scoregroup keeps its value while other reads use the Pairing Score.
- No Oracle. Documented readings: README, "Readings of the Lim 2026 text".

### 5.5 Swiss Team Pairing System (C.04.6)

Pairs teams for a team event. The PAB is decided first, then the top scoregroup with its upfloaters (the Top-Scoregroup Procedure), bracket after bracket; finally the colour of board 1 of every match is allocated (Article 4). A file is a team file when it has `310` records, and the `192` value then has the form described in 4.3.

- Primary score: match points or game points; a second score for colours. Set by the `192` suffixes `_MP`/`_GP`, the match scoring by `362`, the game scoring by `162`, the boards by `352`, and the PAB by `320`.
- Colour preference type (C.04.6 1.7): `TYPE_A` (simple preferences only; the default), `TYPE_B` (strong and mild preferences) or none. It is chosen by the `_TYPEA` / `_TYPEB` suffix of `192`.
- Baku acceleration is refused when game points are the primary score (`[C.04.7 1.4.4]`), and the system needs match scoring (`[C.04.6 1.2] The Swiss Team System needs match scoring (TRF 362)`).
- Team tie-breaks are **planned**; a team file has an empty tie-break list today.
- The 2026 edition only.

**Interpretations.** Where the C.04.6 text literally allows two readings, the library follows the reading of its Oracle by default and lets you choose the other with `--interpretation <name>=<value>` (repeatable). Changing a default is a major-version change (ADR 0003, ADR 0008).

| Name | Values (default first) | Ruling | What it decides |
|---|---|---|---|
| `upfloater-look-ahead` | `parity-minimum`, `graded` | A6 | [C6]: whether the following scoregroup must reach its parity minimum of upfloaters (pass or fail), or needs as few as it can (graded). |
| `last-round-zero-cd-type-b` | `strong`, `none` | A1 | Type B: whether a team with colour difference 0 that had the same colour in its last two matches has a strong preference when the last round is paired, or none. |
| `float-score` | `pairing`, `real` | A8 | Under acceleration: whether a floater of the previous round is judged on the Pairing Scores of that round or on the real scores. |

An unknown name or value is `error: unknown --interpretation <text>`. Giving an interpretation to a system that has none (any system but Swiss Team) fails with `error: The pairing system has no Interpretation <VALUE>`, for example `REAL`. Known divergences from the Oracle are registered in `docs/verification/known-divergences.md`.

### 5.6 Double-Swiss (C.04.5): planned

Not built. ADR 0007 records the intended TRF encoding (a match is two TRF rounds); the profiles `double-swiss`, `team-swiss` and `olympiad` of the generator are refused with `the <name> profile's system is not implemented yet`.

### 5.7 Acceleration (C.04.7)

Acceleration adds Virtual Points to a participant's score for pairing only. The result is the Pairing Score, which is the score wherever the pairing text says score (floats, PAB, board order included); standings never see it.

- `Baku`: `192` suffix `_BAKU`, or `--acceleration baku` for the generator. The Accelerated Group is the top 2 x ceil(N/4) of the round-1 list, and a Late Entry ranked above its last participant joins it. Members of the group receive a win's points as virtual points in the first ceil(A/2) rounds and half of that in the rest of the first A rounds, where A = ceil(R/2) and R is the number of rounds; after that, nothing. Baku needs a win worth two draws and a loss worth nothing (`[C.04.7 1.1]`).
- Explicit: `250` (TRF26) or `XXA` (JaVaFo) records give virtual points per participant and round. They override `_BAKU`.

### 5.8 Colours and other settings at a glance

| Setting | Source | Values |
|---|---|---|
| Initial colour | `152`, `XXC`, `--initial-colour` | `white`, `black` |
| Scoring scheme | `162` (and `362` for teams) | any points for win, draw, loss, PAB; standard 1 / 0.5 / 0 |
| Number of rounds | `142`, `XXR`, `--rounds` | integer |
| Swiss Rules Edition | `192`, `--edition` | `2026`, `pre-2026` (Dutch only) |
| Ranking of participants | file order | listed order of the `001` records |
| Tie-break list and edition | `202`/`212`, `--tiebreaks`, `--tiebreak-edition` | section 6 |

## 6. Tie-breaks and standings

Standings are computed from the recorded rounds by the FIDE Tie-Break Regulations (C.07). The definition of every value is fixed by the **Tie-break Edition**, chosen with `--tiebreak-edition` and independent of the Swiss Rules Edition:

| Edition | Meaning |
|---|---|
| `2026-03` (default) | applied from 1 March 2026: the Dummy Opponent is capped (16.4); `STD`, `TPN`, `RTNG` and `AOB/F` exist |
| `2024-08` | applied 1 August 2024 to 28 February 2026: the Dummy Opponent scores the participant's own score; `STD`, `TPN`, `RTNG` and the `/F` modifier are rejected (`[C.07 5] <code> does not exist in Tie-break Edition 2024-08`, reported when the standings are computed, for example by `standings`; `pair` does not need the tie-breaks) |

### 6.1 The tie-break list

The list comes from `212` (without `PTS`), else `202`, else the default `BH/C1, BH, SB, DE`; `--tiebreaks "<list>"` overrides all of them. Entries are comma separated, case insensitive, and `PTS` and blanks are ignored. The score always comes first; the list only separates participants with equal scores. The Handbook hyphen form `BH-C1` is read as `BH/C1`.

Implemented individual tie-breaks (C.07 art. 5):

| Code | Tie-break |
|---|---|
| `DE` | Direct Encounter (art. 6) |
| `WIN`, `WON` | number of wins, number of games won (over the board) |
| `BPG`, `BWG` | games played with Black, wins with Black |
| `PS` | Progressive Scores |
| `REP` | rounds elected to play |
| `STD` | standard points (2026-03 only) |
| `TPN` | tournament pairing number (2026-03 only) |
| `BH` | Buchholz |
| `AOB` | average of opponents' Buchholz (`AOB/F` uses Fore Buchholz, 2026-03 only) |
| `FB` | Fore Buchholz |
| `SB` | Sonneborn-Berger |
| `KS` | Koya System |
| `ARO` | average rating of opponents |
| `TPR` | tournament performance rating |
| `PTP` | perfect tournament performance |
| `APRO`, `APPO` | average performance rating of opponents, average perfect performance of opponents |
| `RTNG` | the participant's own rating (2026-03 only) |

Modifiers, after a slash: `/C1`, `/C2` (Cut-1, Cut-2: drop the lowest 1 or 2 values), `/M1`, `/M2` (Median-1, Median-2: drop the highest and lowest 1 or 2), only on `BH`, `FB`, `SB`, `PS`, `ARO`; `/P` (forfeits count as games against the scheduled opponent, on `BH`, `FB`, `SB`, `DE`); `/F` (`AOB` only); `/L+n` or `/L-n` (`KS` only: moves the Koya limit by n half points). Examples: `BH/C1`, `SB/M1/P`, `KS/L+2`, `AOB/F`.

Rejected with exit 3 (all problems in the list are reported together, each as `error: [C.07 5] ...`):

- `unknown tie-break <code>`;
- `<code> is a team tie-break, which is not implemented yet` (the team tie-breaks `MPVGP`, `ESB`, `EMMSB`, `EGMSB`, `EDE`, `SSSC`, `BC`, `TBR`, `BBE` and the rest are **planned**), or a team-score entry such as `MP:BH`;
- `<code> is self-defined (C.07 4.1) and has no definition here` for `OTHER_...`;
- `<code> takes no Cut or Median modifier (C.07 14)`, `/F applies only to AOB (C.07 8.2)`, `/L applies only to KS (C.07 14.5)`.

### 6.2 Ranks and shared ranks

Participants are ranked by score, then by the list in order. Those still equal after the whole list **share a rank** (in the table above, 8, 8, 8 and then 11): the program never draws lots. Rating-based tie-breaks (`ARO`, `TPR`, `PTP`, `APRO`, `APPO`, `RTNG`) are dropped, and so separate nobody, when any participant is unrated (art. 10). Virtual points from acceleration never enter a tie-break.

Documented readings of the C.07 text (Art. 16 adjusted scores and the Dummy Opponent, `/P`, Fore Buchholz, TPR and PTP scales, DE 6.3, REP, double forfeits and so on) are listed in `README.md`, section "Readings of the C.07 text". The default list is our choice: C.07 2.1 leaves it to the organiser.

### 6.3 Reading the `standings` output

Columns: `rank`, `id`, `name`, `score`, one column per tie-break in list order, `decided by`. `decided by` is only filled when a row has the same score as the row above; it names the tie-break that put the row below (with both values, for example `BH/C1 4 > 3.5`), or `shared` for a tie the list cannot break. With `--after <r>` the round r standings are shown; `r` must be a recorded round (`Round <r> is not recorded yet`).

`--why <a> <b>` gives the full evidence for the pair: the deciding tie-break, then for each participant the value with its C.07 article and every per-round contribution, for example `round 1 9 2 cut (C.07 16.3; /C1 (C.07 14) low cut)`, meaning that in round 1 the opponent was 9 with value 2 and it was cut by the Cut-1 modifier.

## 7. The tournament generator

The generator plays whole random tournaments through the same pairing engine, so it only ever produces valid tournaments and is used to test the systems (and to give arbiters practice files). Each tournament is written as a TRF26 file (`012 RTG <version> seed <n>`; see 4.4). Every tournament is reproducible from its seed and the generator version.

### 7.1 Commands

```
fide-swiss generate -o 'out%d.trf' [--seed <n>] [--count <k>] [--profile <name>] [--config <cfg>] [--model <in.trf>] [ranges] [tournament flags]
fide-swiss -g [<cfg>|<seed>] -o <out.trf> [-s <seed>] [--dutch|--dubov|--burstein|--lim]
```

- `-o` is required. With `--count` above 1 it must contain `%d`, replaced by the index 0, 1, 2, ... (`-o needs %d in it to name <k> files`).
- `--seed <n>` is the corpus seed (unsigned 64 bit). Without it a fresh seed is drawn and printed on standard error as `seed <n>`. Tournament k of a corpus has its own seed derived from the corpus seed and k, and it is the one written in `012` and the manifest, so a single tournament can be reproduced alone.
- `--count <k>`: the number of tournaments (default 1).
- `-g` is the JaVaFo/bbpPairings form: one tournament, with the argument after `-g` being a configuration file or, when it is all digits, a seed; `-s <seed>` also gives the seed; a system flag may follow.
- Only the Dutch, Dubov, Burstein and Lim systems generate individual tournaments; the generator does not write team files.

### 7.2 Precedence

1. the **profile** (`--profile individual-swiss`, the default, or `accelerated-open` which is Baku); the profile's number of rounds is a placeholder because the generator draws the rounds;
2. a **configuration file** (`--config`, or the argument of `-g`), then a **model TRF** (`--model`): the settings and rates observed in an existing tournament (its field size, forfeit rate, bye rate, withdrawals) are copied;
3. the **flags**.

The profiles `double-swiss`, `team-swiss` and `olympiad` are **planned** and refused (`the <name> profile's system is not implemented yet`); another name gives `unknown profile <name>`.

### 7.3 Ranges

A range flag takes a number, which fixes the parameter, or `A..B`, drawn per tournament. Anything not given is drawn from the defaults, which follow bbpPairings v6's generator:

| Flag | Default | Meaning |
|---|---|---|
| `--players` | 15..215 | number of participants |
| `--rounds` | 5..15 (at most players - 1) | number of rounds |
| `--highest-rating` | 2400..2800 | rating of the top player |
| `--lowest-rating` | 1400..2300 | rating of the bottom player; ratings are uniform between the two |
| `--unrated <%>` | 0..10 | percentage of unrated players (with a hidden strength) |
| `--forfeit-rate` | 1 game in 6..30 | forfeit frequency; each side absent independently, so double forfeits occur; 0 never |
| `--hpb-rate`, `--zpb-rate` | 1 in 15..3225 player-rounds | half-point and zero-point byes (never in the last round, at most two per participant) |
| `--fpb-rate` | none | full-point byes (1 in N; 0 never) |
| `--withdrawals <%>` | 0..5 | withdrawals after a random round |
| `--late-entries <%>` | 0..5 | late entries in round 2 to ceil(rounds/2) |
| `--draw-percentage <P>` | Milvang's model (C.02.03 7.2.4) with white advantage | a flat draw share instead |

A range that is not a number or `A..B` gives `<flag> takes a number or a range A..B, not <text>`. The initial colour is drawn and always written to `152`.

### 7.4 Tournament flags

| Flag | Values | Meaning |
|---|---|---|
| `--system`, `--dutch`, `--dubov`, `--burstein`, `--lim` | dutch, dubov, burstein, lim | the pairing system |
| `--maxi-tournament` | | declare Lim a Maxi-tournament |
| `--edition` | `2026`, `pre-2026` | Swiss Rules Edition |
| `--tiebreak-edition` | `2026-03`, `2024-08` | C.07 edition |
| `--acceleration` | `none`, `baku`, `random` | `random` applies Baku to 20 % of the tournaments; Baku is only applied where the scoring allows it |
| `--tiebreaks` | a list or `random` | `random` draws 3 to 5 entries per tournament |
| `--random-scoring` | | scores 10 % of the tournaments 3/1/0 or 2/1/0 |

### 7.5 Configuration files

`--config` (and `-g <file>`) read JaVaFo/bbpPairings `Key=Value` files. Recognised keys: `PlayersNumber`, `RoundsNumber`, `DrawPercentage`, `ForfeitRate`, `RetiredRate`, `HalfPointByeRate`, `HighestRating`, `LowestRating`, `PointsForWin`, `PointsForDraw`, `PointsForLoss`. A missing key keeps its random default.

### 7.6 Skipped tournaments and the manifest

If a round of a random tournament has no legal pairing, the tournament is *skipped*, never bent; no file is written for it. `skipped <n> of <k> seeds` is printed on standard error, and the exit code is 7 when more than 0.1 % of the seeds are skipped.

With `--count` above 1, `<pattern>.manifest.tsv` is written (for `-o 't%d.trf'` the file is named `t%d.trf.manifest.tsv`). It is tab separated with the columns: `index`, `seed`, `status` (`ok` or `skipped in round <r>`), `players`, `rounds`, `highest rating`, `lowest rating`, `unrated`, `forfeit rate`, `hpb rate`, `zpb rate`, `fpb rate`, `withdrawals`, `late entries`, `scoring` (`1/0.5/0`), `acceleration` (`none`, `baku`, `explicit`), `tiebreaks`. Example row:

```
0	7191089600892374487	ok	12	4	2596	1692	0	14	919	336	0	0	0	1/0.5/0	none	BH/C1, BH, SB, DE
```

A typical use is a corpus for another program:

```sh
fide-swiss generate -o 'corpus/t%d.trf' --seed 2026 --count 100 --players 20..80 --acceleration random --tiebreaks random
```

## 8. Checking pairings

`check` is the Pairings Checker (C.02.03 7.2.3). It rebuilds the tournament round by round from the file, and for every recorded round asks the engine of the file's system (or of `--system`) two questions: is the recorded pairing legal, and is it the pairing the system would make? It does not modify anything.

**What it verifies, per round**

- *Legal*: the Basic Rules (C.04.1) violations, each with its article (for example a player paired twice or left unpaired, `Art. 3`, or a rematch, `Art. 2`; the PAB eligibility of Art. 4 and the absolute colour criteria are checked too), and the system's own absolute criteria: [C3] for the Dutch System 2026 (C.3 for the 2017 edition) and Dubov, and 2.1/5.1 for Lim when a pair cannot meet within the colour limits.
- *System pairing*: whether the recorded pairing is the one the system produces from the previous rounds. Because the systems can have several acceptable pairings under the rules, but pick exactly one by their criteria, DIFFERENT does not by itself mean the arbiter's pairing broke a rule; check for ILLEGAL first.

**What it verifies once, over the whole file** (only when no `--round` is given): the standings after the last recorded round. The Points stated in the `001` records (columns 81-84) must equal the computed scores, and a stated rank (columns 86-89) must fall inside the computed shared-rank range: of the file's own tie-break list when it names one, otherwise of the participant's score group.

**Output**

```
round 2: ILLEGAL
  [C.04.1 Art. 3] 7 is paired more than once
  [C.04.1 Art. 2] 1 and 7 have already played each other
round 3: DIFFERENT
  file:   2 1 | 4 3 | 6 8 | 7 5 | 9 10 | 11 12
  system: 2 1 | 4 3 | 6 5 | 7 8 | 9 10 | 11 12
standings after round 4: DIFFERENT
  <id>: points 3.5 in file, 3.0 computed
  <id>: rank 4 in file, 5-6 computed
checked 4 rounds and 1 set of standings: 2 consistent, 3 not
```

- Boards are shown as `white black`, separated by `|`, the bye as `<id> 0`; `system: no legal pairing` appears if the system cannot pair the round from the recorded history.
- Only rounds with problems are listed; the last line is always printed: `checked <n> rounds[ and 1 set of standings]: <c> consistent, <i> not`.
- Exit code 0 when everything is consistent, 6 otherwise. `--round <r>` for a round the file does not record is `error: The file records no round <r>`, exit 3.
- Settings flags change what the file is checked against, for example `check t.trf --dubov` checks a Dutch tournament against Dubov, which typically reports DIFFERENT on most rounds.

Limits: the recorded pairings are facts (ADR 0004), so `check` is the only place rule violations are reported; `pair` will happily pair on top of an illegal history. Check before you pair if the history came from another program.

## 9. Troubleshooting

Errors go to standard error as `error: <message>`; the exit code tells the class (section 3.7). Real messages, by cause:

| Message | Cause and fix |
|---|---|
| `InvalidModuleDescriptorException: Unsupported major.minor version 69.0` | The launcher runs on a Java older than 25. Select Java 25 (`sdk env`, or set `JAVA_HOME`). |
| `error: nofile.trf` (exit 5) | The input cannot be opened; the message is only the file name. Check the path and permissions. Output files that cannot be written also exit 5. |
| `error: no input file given` | A command was given without a TRF. |
| `error: nothing to do: give -p to pair the next round or -c to check` | Short form without `-p` or `-c`. |
| `error: unexpected argument <x>` | Unknown flag, or a second input file. |
| `error: <flag> needs a value` | A flag at the end of the line. |
| `error: not a number: <x>` | A numeric flag (`--rounds`, `--after`, `--round`, `--count`) with text. |
| `error: unsupported pairing system <x>` | `--system` takes `dutch`, `dubov`, `burstein`, `lim`, `swiss-team`. |
| `error: --edition takes 2026 or pre-2026, not <x>` | Bad edition value. Likewise `--tiebreak-edition takes 2026-03 or 2024-08`, `--initial-colour takes white or black`. |
| `error: unknown --interpretation <x>` | Use one of the pairs in 5.5. |
| `error: SwissTeamSystem pairs teams, and the file has no 310 records` | `--swiss-team` used on an individual file. |
| `error: <system> cannot pair a team file (310 records)` | Another system was asked for on a team file. |
| `error: Unsupported pairing system in record 192: <CODE>` | The `192` value is not in the table of 4.3 (for example `FIDE_DOUBLESWISS`, or a typo). Correct or remove the record: the record is parsed before any flag applies, so `--system` cannot work around it. |
| `error: Unknown result code '<c>'` | A result character in a round block that is not in 4.2. |
| `error: Round <r> refers to unknown player <id>` | An opponent id with no `001` record. |
| `error: Points must be a number like 11.5, not '...'` / `A rank must be a whole number, not '...'` | Malformed columns 81-84 or 86-89 of a `001` record. |
| `error: The tournament has only <n> rounds` | Pairing after the last round. Raise `--rounds` if the event is longer. |
| `error: Participant appears more than once in round <r> (<id>)`, `Expected round <r> but got round <n>`, `Unknown participant in round <r>`, `Two participants share an id` | The file's rounds are structurally inconsistent (checked while the file is read). |
| `error: [GHR 1.3] The library has only the 2026 text of the <system>, not edition PRE_2026` | `--edition pre-2026` with Dubov, Burstein or Lim; only Dutch has the older edition. |
| `error: [GHR 1.3] The Dutch System of edition ... cannot pair a tournament of edition ...` | A conflict between the system and the edition. |
| `error: [C.07 5] unknown tie-break <x>` (and the other C.07 messages of 6.1) | Fix the list of `202`/`212`/`--tiebreaks`. |
| `error: [C.04.7 1.1] Baku acceleration needs a win worth two draws and a loss worth nothing` | Baku with a scoring such as 3/1/0. Use explicit virtual points or another scoring. |
| `error: [C.04.4.3 3.2.3] only the Lim System has a Maxi-tournament setting, not <system>` | `--maxi-tournament` without `--lim`. |
| `error: [C.04.3 1.9.3] ...` / `[C.04.6 3.3.3] The round-pairing cannot be completed` (exit 1) | No legal pairing exists for this round. The pairing trace follows the message on standard error and shows the last bracket the engine reached. Check requested byes and withdrawals, and run `check` on the earlier rounds. |
| `error: The pairing was cancelled` | Interrupted by Ctrl-C. No reply is written. |
| `error: A team lists player <id>, who has no 001 record`, `Round <r>: player <id> belongs to no 310 team` | A team file whose `310` lines and `001` records disagree. |
| `error: -o needs %d in it to name <k> files`, `error: generate needs -o <file or pattern with %d>`, `error: -g needs -o <file>` | Generator arguments (section 7.1). |
| `error: not a seed: <x>` | `--seed` and `-s` take an unsigned integer. |
| `error: internal: ...` (exit 2) | A bug. Keep the input file and report it. |

Other things to know:

- A tournament paired with `pair` and the same one paired with `-p` give identical replies.
- The reply ids are the file's start ranks, not the internal pairing numbers; they can differ when late entries or round-1 absentees exist (ADR 0006).
- A file whose last round column contains only `0000 - H|F|Z` marks is read as absences for the round to be paired, not as a recorded round; a file whose last column has real pairings is a recorded round, and `pair` then pairs the next one.
- Progress lines on a terminal are written to standard error; redirect or use `--quiet` if they disturb a script.
- `check` reports DIFFERENT for a pairing that is legal but not the system's own; see section 8.

## References

- `README.md`: overview, status table, readings of each Handbook text, the library API.
- `CHANGELOG.md`: added features and pairing changes (every change that can alter an output is listed under "Pairing changes").
- `docs/adr/`: decisions: 0003 Swiss Team interpretations, 0004 recorded rounds are facts, 0005 the JaVaFo/bbpPairings protocol, 0006 pairing numbers, 0007 Double-Swiss encoding (planned), 0008 a pairing change is a versioned change.
- `docs/verification/`: the article-to-test map for the Dutch System 2026 and the Known Divergence register.
- `docs/research/`: notes on the Handbook texts, TRF and the oracles.
