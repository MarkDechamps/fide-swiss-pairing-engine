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
| C.04.5 | Double-Swiss System | 2026 | experimental; no Oracle exists, checked against a plain enumeration of the text |
| C.04.6 | Swiss Team Pairing System | 2026 | experimental; matches a patched Gacrux on a 40-tournament regression corpus |
| C.04.7 | Acceleration methods (Baku, explicit virtual points) | 2026 | implemented for Dutch, Dubov, Burstein, Lim and Double-Swiss |
| D.02 | Olympiad Pairing Rules | 2022 (the only edition) | experimental; no Oracle exists, checked against a plain enumeration of the text |
| C.07 | Tie-Breaks | 2026-03 and 2024-08 editions | implemented for individual and team tournaments |

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
  C.04.4.2 Burstein System 2026: experimental
  C.04.4.3 Lim System 2026: experimental
  C.04.5 Double-Swiss System 2026: experimental
  C.04.6 Swiss Team Pairing System 2026: experimental
  D.02 Olympiad Pairing Rules 2022: experimental
```

Running with no arguments, or with `help`, `--help` or `-h`, prints the usage text.

## 3. Commands

There are two grammars. A subcommand name first (`pair`, `check`, `standings`, `generate`, `version`) gives the canonical grammar. Anything else is parsed as the JaVaFo/bbpPairings short form (`--dutch in.trf -p reply`), so a tournament-handling program that already drives JaVaFo or bbpPairings can call `fide-swiss` unchanged (ADR 0005). Both grammars produce the same results.

```
fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]] [--explain <id>]... [--quiet] [settings]
fide-swiss [--dutch|--dubov|--burstein|--lim|--double-swiss|--swiss-team|--olympiad] <in.trf> -p [<reply>] [-l [<trace>]] [settings]
fide-swiss check <in.trf> [--round <r>] [settings]   |   <in.trf> -c [<r>]   |   -check <in.trf>
fide-swiss standings <in.trf> [--after <r>] [--why <id> <id>] [settings]
fide-swiss generate -o <out%d.trf> [--seed <n>] [--count <k>] [--profile <name>] [--config <cfg>] [--model <in.trf>] [ranges] [tournament flags]
fide-swiss -g [<cfg>|<seed>] -o <out.trf> [-s <seed>] [--dutch|--dubov|--burstein|--lim|--double-swiss]
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
| `--system <name>` | `dutch`, `dubov`, `burstein`, `lim`, `double-swiss`, `swiss-team`, `olympiad` | record `192` | The pairing system. Also written `--dutch`, `--dubov`, `--burstein`, `--lim`, `--double-swiss`, `--swiss-team`, `--olympiad`. The choice also decides how the file's round columns are read: `double-swiss` reads two TRF rounds per match, every other system one (4.2). |
| `--maxi-tournament` | none | (no TRF record exists) | Declare a Lim tournament a Maxi-tournament. Only meaningful for Lim. |
| `--edition` | `2026`, `pre-2026` | record `192` edition | The Swiss Rules Edition. `pre-2026` is the Dutch System 2017. Dubov, Burstein, Lim and Double-Swiss reject `pre-2026`. |
| `--rounds <n>` | integer | `142` / `XXR` | The number of rounds (for Double-Swiss: of matches). |
| `--initial-colour` | `white`, `black` | `152` / `XXC` | The initial colour. |
| `--tiebreaks "<list>"` | a C.07 list, see section 6 | `202` / `212` | The tie-break list. |
| `--tiebreak-edition` | `2026-03`, `2024-08` | profile | The C.07 edition that defines the tie-break values. |
| `--interpretation <name>=<value>` | see 5.5 and 5.6 | profile | A Swiss Team or Double-Swiss reading (ADR 0003), or the tie-break reading `edebt-board-count` for any team file. Repeatable. |

A team file (one with `310` records) is paired by the Swiss Team System, or by the Olympiad Pairing Rules when its `192` is `FIDE_OLYMPIAD`, and only accepts the team systems `--swiss-team` and `--olympiad` (or `--system swiss-team|olympiad`). `--swiss-team` on a Swiss Team file keeps the file's colour preferences; naming the other team system switches to it (the Swiss Team System then has Type A preferences). Asking for an individual system on a team file is an error, and so is asking for a team system on a file with no `310` records (see section 9).

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

In the short form the system flags `--dutch`, `--dubov`, `--burstein`, `--lim`, `--double-swiss`, `--swiss-team`, `--olympiad` are settings flags like any other and may appear anywhere. If `-p` is missing (and no `-c`), the command stops with `nothing to do: give -p to pair the next round or -c to check`.

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

The first line is the number of lines that follow. Each further line is `white black`, one per board in board order (General Handling Rules 3.6), using the ids of the file. The pairing-allocated bye, if any, comes last as `<id> 0`. For Double-Swiss a line is one match and `white` is the player with White in game 1 (game 2 is played with the colours reversed). Ids are the file's own, never the internal pairing numbers (ADR 0006 explains why the two can differ).

**Absent players in the last round column.** If the last round column of the file holds only `0000 - H`, `0000 - F` or `0000 - Z` marks, those participants are treated as absent from the round to be paired (half-point bye request, full-point bye request, zero-point bye request). For Double-Swiss the marks fill the two columns of the next match (a mark in the first of them is enough).

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
| `142` | number of rounds | First word of the value. Without it, `XXR`, else the number of round columns in the file (at least 1). For Double-Swiss it counts TRF rounds, two per match, so it must be even (`Record 142 declares 5 rounds: a Double-Swiss file has two per match`); `142 18` is a 9-round event. |
| `XXR` | number of rounds | JaVaFo's equivalent of `142`; used when `142` is absent. |
| `152` | initial colour | Value starting with `B` is black, anything else white. Without `152` or `XXC`, the initial colour is inferred from the top participant paired in round 1 (as TRF26 prescribes and bbpPairings does); it defaults to white. |
| `XXC` | initial colour | JaVaFo: the words `white1` or `black1`; the last one wins. |
| `162` | scoring scheme | Symbol and points pairs: `W` win, `D` draw, `L` loss, `P` pairing-allocated bye. Missing values keep the standard 1 / 0.5 / 0. In a team file it gives the game points (and `P` per board). For Double-Swiss `W`, `D`, `L` score one game, and `P` is the value of a PAB for the whole match (default 1.5); `F` and `H`, which the writer adds, are read as two games won (full-point bye) and two drawn (half-point bye), whatever value they carry. |
| `192` | pairing system and acceleration | See 4.3. A blank value is like no record. |
| `092` | pairing system | bbpPairings writes its code here. Read as `192` only when there is no `192` and the value starts with `FIDE_DUTCH`. |
| `202` | tie-break list | The tie-breaks among equal points, comma separated (section 6). |
| `212` | tie-break list | The full standings order starting with `PTS`; `PTS` is dropped. `212` is preferred over `202` when both exist. Without either, the profile's `BH/C1, BH, SB, DE`. |
| `250` | explicit acceleration | Virtual points (TRF26): game points in columns 10-13, first and last round in 15-17 and 19-21, first and last id in 23-26 and 28-31. Blank ranges mean all rounds or a single id (the first). For Double-Swiss the rounds are TRF rounds and must cover whole matches (start odd, end even); `250 ... 1 4 ...` is matches 1 and 2. |
| `XXA` | explicit acceleration | JaVaFo: id in columns 5-8, then the virtual points of round r at column 10 + 5(r-1). Refused in a Double-Swiss file (JaVaFo has no Double-Swiss); use `250`. |
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

**Double-Swiss matches (ADR 0007).** TRF26 has one result per round, so a Double-Swiss match *k* takes two columns: TRF round 2k-1 is game 1 and TRF round 2k is game 2. The reader does this when `192` is `FIDE_DOUBLESWISS` or when `--double-swiss` (`--system double-swiss`) is given; without either a Double-Swiss file reads as twice the rounds. Each column holds that game's own result codes, so a 1½-½ match is `1` then `=`, a game ending ½-0 is `=` for one player and `0` for the other, 0-0 is `0` for both, and a single forfeited game is `+`/`-` in its column. The two columns of a match must agree, otherwise the file is invalid (exit 3):

- the same opponent in both, with reversed colours (`w` then `b`, or `b` then `w`); the same colour twice is `... has the same colour in both games`;
- a bye fills both columns with opponent `0000` and the same code (`U`, `H`, `F`, `Z`); a bye in one game only is `... has a bye in one game only: byes apply only to matches`;
- a match with game 1 recorded and game 2 empty is `Double-Swiss match <k> is half recorded`.

A player forfeits the match only by forfeiting both games (`-` in both columns); such a match is not a meeting, so the two may be paired again. The player scheduled White in game 1 had White in the match if at least one game was played. Everywhere else (the reply, `--rounds`, `check --round`, the trace and every message) a round is a match.

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
| `FIDE_DOUBLESWISS`, `FIDE_DOUBLESWISS_2026` | Double-Swiss System 2026; also switches on two TRF rounds per match (4.2) |
| `FIDE_TEAM...` | Swiss Team System (team files only, see below) |
| `FIDE_OLYMPIAD` | Olympiad Pairing Rules 2022 (team files only; provisional: TRF26 has no Olympiad code) |
| blank or no `192` | the profile's system: Dutch 2026 |

Any other code is rejected with `Unsupported pairing system in record 192: <CODE>` (exit 3), for example `CUSTOM_DOUBLESWISS` or `CUSTOM_SWISS`. `_BAKU` on any code is provisional.

For a **team file** (any `310` record, or a `192` containing `TEAM` or equal to `FIDE_OLYMPIAD`), the code must be `FIDE_OLYMPIAD` (5.7) or start with `FIDE_TEAM`. `FIDE_OLYMPIAD` takes the match points of `362` (primary), the game points of `162` and the bye of D.02 4.3 (1 matchpoint; `320` is not read); `FIDE_OLYMPIAD_BAKU` is refused, since D.02 has no acceleration (`[C.04.7] Acceleration is defined for the C.04 systems, not the Olympiad Pairing Rules`). The Swiss Team form is `FIDE_TEAM[_TYPEA|_TYPEB][_MP|_GP][_GP|_MP][_BAKU]`:

- `_TYPEA` or `_TYPEB` sets the colour preference type (5.5); a bare `FIDE_TEAM` is Type A; a code with neither suffix, for example `FIDE_TEAM_MP`, uses no colour preferences.
- The first of `MP` (match points) and `GP` (game points) is the primary score; a second one is the secondary score used for colours. A code naming one has no secondary score; a code naming neither keeps the C.04.6 default (match points primary, game points secondary).

Any other `192` code for a team file gives `Unsupported 192 code <code> for a team file`.

### 4.4 What is written

The `generate` command writes TRF26 files with: `012` (the name `RTG <version> seed <n>`), `142`, `152`, `162` (only when different from the standard scoring), `192` (with `_BAKU` when Baku is on), `212` (`PTS` followed by the tie-break list, when the list is not empty) and one `001` record per participant (a team tournament is written as a team file, below). The writer reads back to the same rounds and settings. The writer can also emit `XXR` and `XXC` for JaVaFo. A Double-Swiss tournament is written with two columns per match (4.2), `142` as twice the number of matches, `192 FIDE_DOUBLESWISS`, and a `162` that always carries `P`, `F` and `H`, the per-match bye values, so another reader never has to guess them.

A **team tournament** is written as a team file that reads back to the same teams, settings and matches. Its header has `012`, `062` (players), `072` (rated players), `082` (teams), `142`, `152`, `162` (`W`, `D`, `L` and `P`, always all four: `P` is the PAB's game points per board, stated so that no reader has to guess it: a win's by default, ADR 0009), `192` (`FIDE_TEAM_TYPEA|TYPEB` or none, then `_MP_GP`, `_MP`, `_GP_MP` or `_GP`, and `_BAKU`; or the provisional `FIDE_OLYMPIAD`), `212` (`PTS` and the team tie-breaks, when not empty), `362` (match points), `320` (only when a Swiss Team scoring states the PAB's match points) and `352` (`WBWB...`, one letter per board). Then one `310` per team in team-number order (number, name, strength, match points, game points, rank from the standings, and the start ranks of its members) and one `001` per member: as many members as boards, member *b* of team *t* being start rank (t-1) x boards + b. Each `001` line has one block per round: the opponent team's member on the same board, the colour (board 1 has the team's colour, C.04.6 1.6.1, the others alternate), and the result of that board, `+`/`-` for a forfeit; a team without a match (PAB `U`, requested bye `F`, `H` or `Z`) has the same block on every member's line. The model has no players, so the line-up is the `310` order in every round and no `300` is written. A TRF does not number the boards of a round: they come back in team order, so the reader does not restore the boards' order (GHR 3.6), only who met whom and how it ended. What is not written: the Interpretations of ADR 0003 and ADR 0009 (they are settings, not TRF records), which read back at their defaults; only the PAB's value is, through `P`.

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
- A team file has an empty tie-break list until it declares one (`202`/`212`) or `--tiebreaks` gives one: the team tie-breaks are in section 6.4.
- The 2026 edition only.

**Interpretations.** Where the C.04.6 text literally allows two readings, the library follows the reading of its Oracle, or of the reference app where that knowingly differs from the text, by default and lets you choose the other with `--interpretation <name>=<value>` (repeatable). Changing a default is a major-version change (ADR 0003, ADR 0008).

| Name | Values (default first) | Ruling | What it decides |
|---|---|---|---|
| `upfloater-look-ahead` | `parity-minimum`, `graded` | A6 | [C6]: whether the following scoregroup must reach its parity minimum of upfloaters (pass or fail), or needs as few as it can (graded). |
| `last-round-zero-cd-type-b` | `strong`, `none` | A1 | Type B: whether a team with colour difference 0 that had the same colour in its last two matches has a strong preference when the last round is paired, or none. |
| `float-score` | `pairing`, `real` | A8 | Under acceleration: whether a floater of the previous round is judged on the Pairing Scores of that round or on the real scores. |
| `bracket-seating` | `score-then-tpn`, `tpn` | KD-1 | 3.6.1: whether the top member of a pair is the team with the higher score and, on equal scores, the smaller TPN (Gacrux), or the smaller TPN whatever the score (the text; ruling G1). It also decides which of several equal pairings of a bracket is chosen. |
| `pab-value` | `win`, `draw`, `loss` | KD-2 | 1.4: the game result the PAB scores on every board, so its game points (the primary score under game points, the secondary one under match points). `win` is Gacrux's, `draw` the text ("as many points as are rewarded for a draw"). The PAB's match points are a drawn match's in every case, unless `320` says otherwise. |
| `baku-secondary-score` | `virtual-match-points`, `real` | KD-3 | 4.2.2 under Baku (C.04.7): whether the secondary score that picks the first-team for colours includes the round's Virtual Points, in match points (Gacrux), or is the real game points (the text). |
| `edebt-board-count` | `higher`, `lower` | EDEBT | C.07 13.3.2 with 12.1: whether EDEBT and EDEBB rank the higher Board Count first (Gacrux) or the lower (the text). The stand-alone `BC` always ranks the lower first. It is a standings setting, not a pairing one: it is accepted for every team file (Swiss Team, Olympiad, Double-Swiss), whatever the pairing system, and survives `--system`. |

**Reference app by default (ADR 0009).** The first three rows, and `edebt-board-count`, are the places where Gacrux knowingly differs from the text: the default is Gacrux's reading and the text stays selectable. For the literal text, give `--interpretation bracket-seating=tpn --interpretation pab-value=draw --interpretation baku-secondary-score=real --interpretation edebt-board-count=lower`.

**The value of the PAB.** Three ways to set it, the first that applies wins: a `320` record (match points, and game points under game points as primary score); a `P` in `162` (the game points per board: a `P` equal to `W`, `D` or `L` selects `pab-value=win|draw|loss`, and under game points as primary score any `P` is multiplied by the boards); the flag `--interpretation pab-value=win|draw|loss`, which the file's `P` does not override on the command line: under game points as primary score it also replaces the game points a `P` or `320` stated, so the reading is selectable there too; under match points as primary score it sets the PAB's game points, the secondary score, and leaves a stated `320` match-point value alone. `P` maps to the reading under both primary scores; only under game points does it also scale per board into the PAB's primary points. Without them the PAB is worth a win per board. The library's writer always states `P`, so a file it wrote reads back to the same value.

An unknown name or value is `error: unknown --interpretation <text>`. Double-Swiss takes `upfloater-look-ahead` and `float-score` with the same defaults (5.6), and refuses the four Swiss Team readings of ADR 0009. The Olympiad Pairing Rules take none: their team tie-breaks use the defaults. Giving an interpretation to a system that has none (Dutch, Dubov, Burstein, Lim) fails with `error: The pairing system has no Interpretation <VALUE>`, for example `REAL`. Known divergences from the Oracle are registered in `docs/verification/known-divergences.md`.

### 5.6 Double-Swiss System (C.04.5)

Every pairing is a match of two games between the same two players, played in succession with the colours reversed; each game is scored 1 / ½ / 0, so a match ends 2-0, 1½-½, 1-1 and so on, and the Preface's rare ½-0, 0-½ and 0-0 games are allowed. Select it with `192 FIDE_DOUBLESWISS`, `--double-swiss` or `--system double-swiss`; in a TRF a match takes two round columns (4.2).

The pairing is the procedure of the Swiss Team System (5.5), which C.04.5 repeats word for word (3.3–3.6): the PAB first, then the top scoregroup of the players still unpaired with the first set of upfloaters (the fewest, then the highest scores, then [C6] and [C7], then the 3.5.4 order), paired by the first Pairing Identifier that best keeps [C8] (upfloaters' opponents who floated in the previous round). Colours come last, by Article 4 for each match: both new, the initial colour to an odd-TPN higher-ranked player (4.3.1); then White to the one with fewer Whites (4.3.2); then the most recent difference (4.3.3); then alternation of the higher-ranked player (4.3.4) and of the opponent (4.3.5). There is no colour criterion in the pairing. Boards follow GHR 3.6.

- **PAB** (1.4): 3.4.2 the lowest score, 3.4.3 the most matches played, 3.4.4 the largest TPN, among the players whose bye leaves the others pairable and who have not had a PAB, won a match by forfeit or had a full-point bye ([C2]). It is worth a game won plus a game drawn, 1.5, unless `162 P` says otherwise. A requested full-point bye is worth two games won (2), a half-point bye two draws (1).
- **Forfeits** (Preface): a match is forfeited only when a player forfeits both games; it is then not a meeting and gives no colour, and the pair may meet again. Any other match is a meeting and counts as played.
- **Float criteria** [C7] and [C8] lapse only in the last planned round.
- **Interpretations** (`--interpretation`, as in 5.5): `upfloater-look-ahead=parity-minimum|graded` for [C6] and `float-score=pairing|real` for floats under acceleration. Double-Swiss has no colour preferences, so `last-round-zero-cd-type-b` is refused (`[C.04.5] The Double-Swiss System has no colour preferences, so no STRONG`).
- **Library**: `PairingSystems.doubleSwiss()`, `Profiles.doubleSwiss(rounds)` and `ScoringScheme.doubleSwiss()`; record a match as `MatchOutcome.ofGames(List.of(game1, game2))`, each game seen from the player with White in game 1, whatever colour that player has in the game (so `WHITE_WINS` as game 2 is a win for the game-1 White, who plays game 2 with Black).
- Edition: 2026 only (`--edition pre-2026` is rejected, `[GHR 1.3]`). Acceleration: Baku (5.8) and explicit `250`.
- No Oracle exists. It is checked against a plain enumeration of the text, and the text's examples (3.5.4, 3.6.2) pass. Documented readings: README, "Readings of the Double-Swiss 2026 text"; prior art: `docs/research/double-swiss-2026-prior-art.md`.

### 5.7 Olympiad Pairing Rules (D.02)

The FIDE rules for the Chess Olympiad (D.02, effective from 1 January 2022, the only edition): a team Swiss outside C.04. Select it with `192 FIDE_OLYMPIAD` in a team file, `--olympiad` or `--system olympiad`. Teams are ranked by matchpoints, then their initial pairing number (3.2); a group is the teams with the same matchpoints (6.3). The round is paired as follows:

1. **Bye** (Article 4): with an odd number of teams, the lowest ranked team that may have it and whose bye leaves the others pairable. A team may not have it if it had the bye, won a match because the opponents did not arrive (every board forfeited), or joined after round 1 (a Late Entry). It is worth 1 matchpoint and 2 game points (a drawn match on four boards).
2. **Groups** (6.4): from the top group down to the Median Group, then from the bottom group up to it, and the Median Group last. The Median Group is the group of the median team: of the teams being paired, the lower of the two middle ones in the ranking. The groups above it and the Median Group are paired downward, the groups below it upward, and every upward rule is the downward rule mirrored.
3. **Floaters** (Article 8, 9.4, 9.5): a group pairs as many of its teams as it can while the rest of the round can still be paired (Kept Pairings), and floats the others one group towards the Median Group. Above the median the lowest ranked team floats down first, below it the highest ranked floats up first, preferring a team that has an opponent in the next group (8.2.3, 8.3.3). A downfloater plays the highest ranked team of its new group, an upfloater the lowest ranked, as long as the group still keeps its pairings; a floater nobody there can take floats on. Floaters stop in the Median Group.
4. **Inside a group** (9.1–9.3): the first team tries the first team of the bottom half, then the next, then the top half upwards (for six teams: 4, 5, 6, 3, 2), taking the first opponent with which the rest of the group can still be paired; the rest is paired the same way. This is the order of the 9.3 table.
5. **Colour limits** (7.3, 7.4): board 1 of a team never gets a colour difference beyond +2 or -2, nor the same colour three times running, counted on played matches; a group that would keep fewer pairings within these limits is paired without them.
6. **Colours of board 1** (7.2, 7.5–7.7), decided for the higher ranked team: a colour that breaks 7.3 for either team is avoided; then the team with the larger colour difference gets Black (equalisation); then each team gets the colour the other had in the latest round in which their colours differed; then the higher ranked team alternates from its last colour; with no colours at all, the colour drawn by lot (`152`, `--initial-colour`) goes to the higher ranked team when its pairing number is odd, the other colour when it is even (in round 1: the odd teams of the top half get the lot's colour).
7. **Board order** (11.1): the matches are numbered by the higher ranked team's matchpoints, then the sum of both teams' matchpoints, then the higher ranked team's rating (the `310` strength, the average of its four highest ratings), highest first.

- **Met** (6.1): a match with at least one board played. A match lost by default on every board is not a meeting, gives no colour, and the teams may meet again.
- **Ranking** (3.1): a team file keeps the `310` order as the initial ranking; in the library `TeamStrength.initialRanking(teams, strengths)` ranks by the average of the four highest ratings, then the fifth player's rating, then the name, and `Profiles.olympiad(rounds)` sets up four boards at 2 / 1 / 0 match points with no tie-breaks (the Olympiad's tie-breaks are in its event regulations, not in D.02; declare them with `212` or `--tiebreaks`).
- **Refused settings**: acceleration of any kind (`_BAKU`, `250`), and a scoring scheme without match points primary (`[D.02 3.2.1] The Olympiad Pairing Rules pair by matchpoints (TRF 362)`). It has no Interpretations.
- **Check** (section 8): besides the Basic Rules, `check` reports `[D.02 7.3]` when board 1's colours break a limit that the other colours would keep. A pair that breaks 7.3 whatever its colours is not reported, because 7.4 may have disregarded the limits.
- **Not in the library**: host teams, who is paired in round 1 and dropping a team short of players (articles 2 and 10) reach it as Late Entries, Withdrawals and absences; an unfinished game (5.1) is recorded as a draw and corrected later, and the published pairing stands (5.2); the arbiter's changes of 11.2 and 11.3.
- No Oracle exists: Gacrux (FIDE's TieBreakServer) pairs only C.04.6, and bbpPairings, JaVaFo and chesspairing have no D.02. It is checked against a plain enumeration of the text on simulated tournaments, and the text's examples (the 9.3 table, the 88-team median of 6.4) pass. Documented readings: README, "Readings of the Olympiad Pairing Rules (2022)".

### 5.8 Acceleration (C.04.7)

Acceleration adds Virtual Points to a participant's score for pairing only. The result is the Pairing Score, which is the score wherever the pairing text says score (floats, PAB, board order included); standings never see it.

- `Baku`: `192` suffix `_BAKU`, or `--acceleration baku` for the generator. The Accelerated Group is the top 2 x ceil(N/4) of the round-1 list, and a Late Entry ranked above its last participant joins it. Members of the group receive a win's points as virtual points in the first ceil(A/2) rounds and half of that in the rest of the first A rounds, where A = ceil(R/2) and R is the number of rounds; after that, nothing. For Double-Swiss a win is a match won 2-0, so the values are 2 and 1. Baku needs a win worth two draws and a loss worth nothing (`[C.04.7 1.1]`).
- Explicit: `250` (TRF26) or `XXA` (JaVaFo) records give virtual points per participant and round. They override `_BAKU`.

### 5.9 Colours and other settings at a glance

| Setting | Source | Values |
|---|---|---|
| Initial colour | `152`, `XXC`, `--initial-colour` | `white`, `black` |
| Scoring scheme | `162` (and `362` for teams) | any points for win, draw, loss, PAB; standard 1 / 0.5 / 0 |
| Number of rounds | `142`, `XXR`, `--rounds` | integer |
| Swiss Rules Edition | `192`, `--edition` | `2026`, `pre-2026` (Dutch only; the Olympiad Pairing Rules have their own single edition) |
| Games per round | `192 FIDE_DOUBLESWISS`, `--double-swiss` | two (a Double-Swiss match, two TRF rounds), else one |
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

Modifiers, after a slash: `/C1`, `/C2` (Cut-1, Cut-2: drop the lowest 1 or 2 values), `/M1`, `/M2` (Median-1, Median-2: drop the highest and lowest 1 or 2), only on `BH`, `FB`, `SB`, `PS`, `ARO`; `/P` (forfeits count as games against the scheduled opponent, on `BH`, `FB`, `SB`, `DE`); `/F` (`AOB` and `SSSC`); `/L+n` or `/L-n` (`KS` only: moves the Koya limit by n half points). Examples: `BH/C1`, `SB/M1/P`, `KS/L+2`, `AOB/F`.

Rejected with exit 3 (all problems in the list are reported together, each as `error: [C.07 5] ...`):

- `unknown tie-break <code>`;
- `<code> needs a team competition (match scoring, TRF 362)` for a team tie-break, or a `:MP`/`:GP` entry, in a file without match scoring (reported when the standings are computed);
- `ESB is a family (C.07 13.2): name EMMSB, EMGSB, EGMSB or EGGSB`, `<code> takes no team score (:MP or :GP, C.07 13)`, `<code> counts matches or games won: only :MP applies (C.07 7.1)`, `<code> takes Cut-1 and Cut-2 only (C.07 14.1.2)`;
- `<code> is self-defined (C.07 4.1) and has no definition here` for `OTHER_...`;
- `<code> takes no Cut or Median modifier (C.07 14)`, `/F applies only to AOB and SSSC (C.07 8.2, 13.4)`, `/L applies only to KS (C.07 14.5)`, `/K applies only to SSSC (C.07 13.4.2)`.

### 6.2 Ranks and shared ranks

Participants are ranked by score, then by the list in order. Those still equal after the whole list **share a rank** (in the table above, 8, 8, 8 and then 11): the program never draws lots. Rating-based tie-breaks (`ARO`, `TPR`, `PTP`, `APRO`, `APPO`, `RTNG`) are dropped, and so separate nobody, when any participant is unrated (art. 10). Virtual points from acceleration never enter a tie-break.

Documented readings of the C.07 text (Art. 16 adjusted scores and the Dummy Opponent, `/P`, Fore Buchholz, TPR and PTP scales, DE 6.3, REP, double forfeits and so on) are listed in `README.md`, section "Readings of the C.07 text". The default list is our choice: C.07 2.1 leaves it to the organiser.

### 6.3 Reading the `standings` output

Columns: `rank`, `id`, `name`, `score`, one column per tie-break in list order, `decided by`. `decided by` is only filled when a row has the same score as the row above; it names the tie-break that put the row below (with both values, for example `BH/C1 4 > 3.5`), or `shared` for a tie the list cannot break. With `--after <r>` the round r standings are shown; `r` must be a recorded round (`Round <r> is not recorded yet`).

`--why <a> <b>` gives the full evidence for the pair: the deciding tie-break, then for each participant the value with its C.07 article and every per-round contribution, for example `round 1 9 2 cut (C.07 16.3; /C1 (C.07 14) low cut)`, meaning that in round 1 the opponent was 9 with value 2 and it was cut by the Cut-1 modifier.

### 6.4 Team tie-breaks and team standings

In a team competition (a file with `310` records, or any file with match scoring) the *score* is the primary score of `192` (match points, MP, or game points, GP), and the *secondary score* is the other one (C.07 11.1). `fide-swiss standings team.trf` ranks the teams by the primary score and then by the list of `202`/`212` or `--tiebreaks`, exactly as for players; the table's `id` and `name` are the team's. Example:

```
$ fide-swiss standings team.trf --tiebreaks "MPvGP, EDET"
rank  id  name    score  MPvGP  EDET  decided by
1     2   Team 2  3      6.5    1
2     3   Team 3  3      6      2     MPvGP 6.5 > 6
3     1   Team 1  3      6      1     EDET 2 > 1
4     4   Team 4  3      5.5    1     MPvGP 6 > 5.5
```

Team score on the individual tie-breaks. `:MP` or `:GP` after `WIN`, `WON`, `PS`, `BH`, `AOB`, `FB`, `KS` computes it in that score whatever the primary score is (`BH:GP/C1`, `PS:MP`, `FB:GP/P`). Without it the primary score is used. `WIN` and `WON` count won matches and take `:MP` only.

Tie-breaks used only for teams (C.07 art. 12 and 13):

| Code | Article | Meaning | Higher is better |
|---|---|---|---|
| `MPvGP` | 13.1 | the team's total in the score that does not decide the competition: GP when MP decide, MP when GP decide | yes |
| `EMMSB` | 13.2.1 | Extended Sonneborn-Berger: sum over opponents of opponent's total MP × MP scored against it | yes |
| `EMGSB` | 13.2.2 | opponent's total MP × GP scored | yes |
| `EGMSB` | 13.2.3 | opponent's total GP × MP scored | yes |
| `EGGSB` | 13.2.4 | opponent's total GP × GP scored | yes |
| `EDE` | 13.3 | Extended Direct Encounter: Direct Encounter on the primary score, then, if it separates nobody, on the secondary score; restarts on every new subset of tied teams | yes |
| `EDEBT` | 13.3.2 | EDE, then, for exactly two teams still tied in MP and GP, `BC`, then `TBR` | yes |
| `EDEBB` | 13.3.2 | EDE, then `BC`, then `BBE` | yes |
| `EDET` | 13.3.2 | EDE, then `TBR` | yes |
| `EDEB` | 13.3.2 | EDE, then `BBE` | yes |
| `SSSC` | 13.4 | secondary score + Schedule Strength (the Buchholz on the primary score divided by the normalising factor) | yes |
| `BC` | 12.1 | Board Count: sum of board number × game points on that board, over all matches; usable only among teams with equal game points | no: the lower the better |
| `TBR` | 12.2 | Top Board Results: game points on board 1, then board 2, and so on | yes |
| `BBE` | 12.3 | Bottom Board Elimination: game points on all boards but the bottom one, then without the two bottom boards, and so on | yes |

Modifiers: `/C1`, `/C2` on the four ESB codes (the product of the opponent with the lowest total in the score of the opponent's factor is left out, 14.1.2); `/P` on ESB, EDE\*, SSSC and `BH`/`FB` (forfeits as played); `/Kx` on `SSSC` redefines the normalising factor (`SSSC/K4`); `/F` on `SSSC` uses Fore Buchholz. A bare `ESB` is refused. `SSSC`'s default factor is (rounds × the primary score of a win) divided by (the secondary score of a win in one match), truncated: with 9 rounds and 4 boards it is 18 / 4 = 4 in match points and 36 / 2 = 18 in game points.

Forfeits, byes and boards. For `BC`, `TBR` and `BBE` a forfeited game is a standard win or loss, and a pairing-allocated bye gives every board a win's points (art. 12 preamble). Art. 16 governs the ESB codes like `SB`. A team file's `212` or `202` carries the codes in TEC syntax, for example `212 PTS,MPvGP,EDET,EMGSB/C1/P,SSSC`.

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
- The Dutch, Dubov, Burstein, Lim and Double-Swiss systems generate individual tournaments; `--swiss-team` and `--olympiad` (or `--system swiss-team|olympiad`, or the profiles below) generate **team tournaments** (7.7): teams play matches over boards, and the file is a team file with `310` records (4.4). A team system is applied before the other flags whatever their order, since it changes the defaults of the ranges; an individual system after a team profile brings back the individual defaults.

### 7.2 Precedence

1. the **profile** (`--profile individual-swiss`, the default, `accelerated-open` which is Baku, `double-swiss`, two-game matches with the PAB worth 1.5, `team-swiss`, the Swiss Team System of C.04.6, or `olympiad`, the Olympiad Pairing Rules of D.02 on four boards); the profile's number of rounds is a placeholder because the generator draws the rounds;
2. a **configuration file** (`--config`, or the argument of `-g`), then a **model TRF** (`--model`): the settings and rates observed in an existing tournament (its field size, forfeit rate, bye rate, withdrawals) are copied;
3. the **flags**.

Another profile name gives `unknown profile <name>`.

### 7.3 Ranges

A range flag takes a number, which fixes the parameter, or `A..B`, drawn per tournament. Anything not given is drawn from the defaults, which follow bbpPairings v6's generator:

| Flag | Default | Meaning |
|---|---|---|
| `--players` | 15..215 | number of participants |
| `--rounds` | 5..15 (at most players - 1) | number of rounds |
| `--highest-rating` | 2400..2800 | rating of the top player |
| `--lowest-rating` | 1400..2300 | rating of the bottom player; ratings are uniform between the two |
| `--unrated <%>` | 0..10 | percentage of unrated players (with a hidden strength) |
| `--forfeit-rate` | 1 game in 6..30 | forfeit frequency; each side absent independently, so double forfeits occur; 0 never. In a Double-Swiss match a player absent for the match forfeits both games, and a player present may still forfeit one game at the same rate. |
| `--hpb-rate`, `--zpb-rate` | 1 in 15..3225 player-rounds | half-point and zero-point byes (never in the last round, at most two per participant) |
| `--fpb-rate` | none | full-point byes (1 in N; 0 never) |
| `--withdrawals <%>` | 0..5 | withdrawals after a random round |
| `--late-entries <%>` | 0..5 | late entries in round 2 to ceil(rounds/2) |
| `--draw-percentage <P>` | Milvang's model (C.02.03 7.2.4) with white advantage | a flat draw share instead |

A range that is not a number or `A..B` gives `<flag> takes a number or a range A..B, not <text>`. The initial colour is drawn and always written to `152`.

### 7.4 Tournament flags

| Flag | Values | Meaning |
|---|---|---|
| `--system`, `--dutch`, `--dubov`, `--burstein`, `--lim`, `--double-swiss`, `--swiss-team`, `--olympiad` | dutch, dubov, burstein, lim, double-swiss, swiss-team, olympiad | the pairing system; `double-swiss` also takes the Double-Swiss scoring (byes worth a match); the team systems generate team tournaments (7.7) |
| `--maxi-tournament` | | declare Lim a Maxi-tournament |
| `--edition` | `2026`, `pre-2026` | Swiss Rules Edition |
| `--tiebreak-edition` | `2026-03`, `2024-08` | C.07 edition |
| `--acceleration` | `none`, `baku`, `random` | `random` applies Baku to 20 % of the tournaments; Baku is only applied where the scoring allows it |
| `--tiebreaks` | a list or `random` | `random` draws 3 to 5 entries per tournament; for a team tournament from the team tie-breaks of C.07 art. 12–13 (`MPvGP`, the four `E?MSB`/`E?GSB` with Cut-1, the `EDE` family, `SSSC`, `BC`, `TBR`, `BBE`) and the individual ones over a team score (`WIN:MP`, `BH:GP/C1`, `PS:MP`, ...) |
| `--random-scoring` | | scores 10 % of the tournaments 3/1/0 or 2/1/0 (never a Double-Swiss or a team one) |
| `--random-team-format` | | Swiss Team only: every tournament draws its match points (2/1/0 or 3/1/0), its primary score (match or game points), whether the secondary score is used for colours, and Type A, Type B or no colour preferences (C.04.6 1.2, 1.7); the Olympiad has one format (D.02 3.2.1) |
| `--boards` | a number or `A..B` | boards per team match (7.7) |

### 7.5 Configuration files

`--config` (and `-g <file>`) read JaVaFo/bbpPairings `Key=Value` files. Recognised keys: `PlayersNumber`, `RoundsNumber`, `DrawPercentage`, `ForfeitRate`, `RetiredRate`, `HalfPointByeRate`, `HighestRating`, `LowestRating`, `PointsForWin`, `PointsForDraw`, `PointsForLoss`. A missing key keeps its random default.

### 7.6 Skipped tournaments and the manifest

If a round of a random tournament has no legal pairing, the tournament is *skipped*, never bent; no file is written for it. `skipped <n> of <k> seeds` is printed on standard error, and the exit code is 7 when more than 0.1 % of the seeds are skipped.

With `--count` above 1, `<pattern>.manifest.tsv` is written (for `-o 't%d.trf'` the file is named `t%d.trf.manifest.tsv`). It is tab separated with the columns: `index`, `seed`, `status` (`ok` or `skipped in round <r>`), `players`, `rounds`, `highest rating`, `lowest rating`, `unrated`, `forfeit rate`, `hpb rate`, `zpb rate`, `fpb rate`, `withdrawals`, `late entries`, `scoring` (`1/0.5/0`), `acceleration` (`none`, `baku`, `explicit`), `tiebreaks`, `boards` (the boards of a team match, 0 for an individual tournament). Example row:

```
0	7191089600892374487	ok	12	4	2596	1692	0	14	919	336	0	0	0	1/0.5/0	none	BH/C1, BH, SB, DE	0
```

A typical use is a corpus for another program:

```sh
fide-swiss generate -o 'corpus/t%d.trf' --seed 2026 --count 100 --players 20..80 --acceleration random --tiebreaks random
```

### 7.7 Team tournaments

With `--swiss-team` or `--olympiad` the generator plays **teams** instead of players, through the same engine, and writes a team file (4.4). What changes:

- `--players` is the number of teams (default 8..40 for a team system); `--rounds` defaults to 5..11 (at most teams - 2, so that every round can be paired); `--boards` is the number of boards of a match (default 2..6 for the Swiss Team System; the Olympiad always has 4, D.02 3.2.1). Teams are all rated (`--unrated` defaults to 0) with distinct strengths, strongest first: the strength is the team's rating in `310`, and TPN 1 is the strongest.
- A match is decided over its boards: board *b* is played between players whose strength is the team's, shifted by 15 per board from board 1 down, with Milvang's model; the team with White on board 1 has White on the odd boards (the `352` pattern is `WBWB...`, C.04.6 1.6.1). `--forfeit-rate` (default 1 in 30..300, rarer than for players) is per board, and a team may also not show up at all with the chance of a forfeit on every board, so that forfeited matches (`+`/`-` on every board) occur; `--hpb-rate` and `--zpb-rate` (default 1 in 100..3225 team-rounds) give requested half- and zero-point byes. Withdrawals and late entries default to 0.
- `--random-team-format` and `--tiebreaks random` draw the format and the team tie-breaks (7.4).
- Baku (`--acceleration baku|random`) applies where the format allows it: with match points primary, and only for the Swiss Team System (C.04.7 1.4.4; the Olympiad has no acceleration).

```sh
fide-swiss generate -o 'teams/t%d.trf' --swiss-team --seed 2026 --count 50 --random-team-format --tiebreaks random
fide-swiss generate -o olympiad.trf --olympiad --players 40 --rounds 9 --seed 7
```

The invariant checker (test scope) runs on 200 tournaments of each team system in every build: no rematch of a match with a board played (a match lost by default on every board is not a meeting), at most one PAB per round and none for a team that had one or won a match by forfeit (a full-point bye also bars it under the Swiss Team System; the Olympiad's bar is D.02 4.3's), nobody unpaired, and the standings rank every team under the drawn tie-breaks. There is no colour invariant: C.04.6 has no colour criterion, and the Olympiad's limits (7.3) yield to keeping the pairings (7.4).

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
| `error: unsupported pairing system <x>` | `--system` takes `dutch`, `dubov`, `burstein`, `lim`, `double-swiss`, `swiss-team`, `olympiad`. |
| `error: --edition takes 2026 or pre-2026, not <x>` | Bad edition value. Likewise `--tiebreak-edition takes 2026-03 or 2024-08`, `--initial-colour takes white or black`. |
| `error: unknown --interpretation <x>` | Use one of the pairs in 5.5. |
| `error: SwissTeamSystem pairs teams, and the file has no 310 records` | `--swiss-team` used on an individual file. |
| `error: D.02 Olympiad Pairing Rules 2022 pairs teams, and the file has no 310 records` | `--olympiad` used on an individual file. |
| `error: <system> cannot pair a team file (310 records)` | Another system was asked for on a team file. |
| `error: Unsupported pairing system in record 192: <CODE>` | The `192` value is not in the table of 4.3 (for example `CUSTOM_DOUBLESWISS`, or a typo). Correct or remove the record: the record is parsed before any flag applies, so `--system` cannot work around it. |
| `error: Unknown result code '<c>'` | A result character in a round block that is not in 4.2. |
| `error: Player <n> in Double-Swiss match <k> (TRF rounds <a> and <b>) has ...` | The two columns of a Double-Swiss match disagree: another opponent, a bye in one game only, or the same colour twice (4.2). Fix the file, or leave out `--double-swiss` if it is not a Double-Swiss file. |
| `error: Double-Swiss match <k> is half recorded: TRF round <b> has no games` | Game 1 is recorded and game 2 is not. Record game 2, or remove game 1, before pairing. |
| `error: Record 142 declares <n> rounds: a Double-Swiss file has two per match` | `142` counts TRF rounds, two per match. |
| `error: Record 250 covers TRF rounds <a>-<b>, not whole Double-Swiss matches`, `XXA has no Double-Swiss reading` | Explicit acceleration in a Double-Swiss file: give `250` ranges from an odd to an even TRF round. |
| `error: Round <r> refers to unknown player <id>` | An opponent id with no `001` record. |
| `error: Points must be a number like 11.5, not '...'` / `A rank must be a whole number, not '...'` | Malformed columns 81-84 or 86-89 of a `001` record. |
| `error: The tournament has only <n> rounds` | Pairing after the last round. Raise `--rounds` if the event is longer. |
| `error: Participant appears more than once in round <r> (<id>)`, `Expected round <r> but got round <n>`, `Unknown participant in round <r>`, `Two participants share an id` | The file's rounds are structurally inconsistent (checked while the file is read). |
| `error: [GHR 1.3] The library has only the 2026 text of the <system>, not edition PRE_2026` | `--edition pre-2026` with Dubov, Burstein, Lim or Double-Swiss; only Dutch has the older edition. |
| `error: [GHR 1.3] The Dutch System of edition ... cannot pair a tournament of edition ...` | A conflict between the system and the edition. |
| `error: [C.07 5] unknown tie-break <x>` (and the other C.07 messages of 6.1) | Fix the list of `202`/`212`/`--tiebreaks`. |
| `error: [C.04.7 1.1] Baku acceleration needs a win worth two draws and a loss worth nothing` | Baku with a scoring such as 3/1/0. Use explicit virtual points or another scoring. |
| `error: [C.04.4.3 3.2.3] only the Lim System has a Maxi-tournament setting, not <system>` | `--maxi-tournament` without `--lim`. |
| `error: [C.04.3 1.9.3] ...` / `[C.04.6 3.3.3]` or `[C.04.5 3.3.3] The round-pairing cannot be completed` (exit 1) | No legal pairing exists for this round. The pairing trace follows the message on standard error and shows the last bracket the engine reached. Check requested byes and withdrawals, and run `check` on the earlier rounds. |
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

## 10. Oracle gates

The `oracle-it` module compares the library's pairings with external programs (the Oracles of the verification strategy). It runs them as separate processes; nothing of them is linked, copied or committed. The gates are Failsafe tests, so `./mvnw verify -pl oracle-it -am` runs them and the ordinary unit-test build never does.

For every gate, the Random Tournament Generator plays a corpus of Dutch tournaments. The Oracle then pairs each round from the rounds before it, and its pairing must equal ours, board for board and bye for bye. The first differing round of a tournament is reported. The exact input the Oracle was given is saved under `oracle-it/target/oracle-failures/` for each difference, so the nightly job can keep it.

| Gate | Program | Scope |
|---|---|---|
| `bbpPairingsV6PairsDutch2026AsWeDo` | bbpPairings v6.0.0 | Dutch 2026 |
| `bbpPairingsV5PairsDutch2017AsWeDo` | bbpPairings v5.0.1 | Dutch 2017 (pre-2026 edition) |
| `jaVaFoPairsDutch2017AsWeDo` | JaVaFo 2.2 | Dutch 2017; rounds that differ only through KD-4 (half-point byes) are registered, not failed |
| `SwissTeamOracleIT.gacruxPairsSwissTeam2026AsWeDo` | Gacrux (TieBreakServer) @ 6419149, plain (with the `tpn-order` patch and the literal readings under `-Dfideswiss.oracle.gacrux.literal=true`) | Swiss Team 2026 under the defaults, team tournaments of every colour preference type and score format, with forfeits and requested byes (10.1) |

Point each gate to its program with a system property or an environment variable. A gate whose program is not configured is skipped, and the skip message names both.

| Program | System property | Environment variable |
|---|---|---|
| bbpPairings v6.0.0 | `fideswiss.oracle.bbp6` | `FIDESWISS_ORACLE_BBP6` |
| bbpPairings v5.0.1 | `fideswiss.oracle.bbp5` | `FIDESWISS_ORACLE_BBP5` |
| JaVaFo 2.2 (a `.jar`, run with `java -jar`) | `fideswiss.oracle.javafo` | `FIDESWISS_ORACLE_JAVAFO` |
| Gacrux (the path of a clone of its repository) | `fideswiss.oracle.gacrux` | `FIDESWISS_ORACLE_GACRUX` |
| the Python for Gacrux (default `python3`; it needs `networkx`) | `fideswiss.oracle.gacrux.python` | `FIDESWISS_ORACLE_GACRUX_PYTHON` |

Corpus settings, all system properties: `fideswiss.oracle.tournaments` (default 100), `fideswiss.oracle.seed` (default 20260929; the nightly job rotates it) and `fideswiss.oracle.events`, a comma list of what besides plain results is played (`forfeits`, `byes`, `withdrawals`, `late-entries`). The printed summary reads, for example, `bbpPairings v6.0.0: 822 of 822 rounds agree in 100 tournaments, 0 with a difference`.

**Nightly job** (`.github/workflows/nightly.yml`, 03:00 UTC): 1,000 tournaments per Oracle gate, 500 fully Baku-accelerated Dutch tournaments, 200 team tournaments with withdrawals and late entries and 100 under the FIDE-literal Swiss Team readings, plus 5,000 invariant-checked tournaments per system. Each job stops after 4 hours. Mutation testing runs on Sundays only (and on a manual run). The full 50,000-tournament run belongs to the release gate below.

```sh
FIDESWISS_ORACLE_BBP6=$HOME/oracles/bbpPairings.exe \
  ./mvnw verify -pl oracle-it -am -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false \
  -Dfideswiss.oracle.tournaments=500 -Dfideswiss.oracle.seed=42
```

**Known Divergences.** The gates read the register's machine-readable companion, `oracle-it/src/main/resources/known-divergences.tsv` (tab-separated: id, the start of the Oracle's name, a regular expression its input matches). A differing round the register recognises is counted apart and printed as `registered rounds {KD-4=29}`; it does not fail the gate, and the rounds after it are still compared (each round is paired from our history, so they are independent). Any other difference fails the gate and ends its tournament, as before. Currently KD-4 (JaVaFo, an `H` bye in the input) and KD-3 (only the literal Gacrux program, `Gacrux @ 6419149 (tpn-order)`, `250` records; the literal Gacrux gate also leaves those tournaments out: plain Gacrux agrees with the defaults, so the default gate registers nothing). Add a line to the file with each new register entry.

**Acceleration.** The Dutch gates accelerate `fideswiss.oracle.baku` percent of their tournaments by Baku (default 20; the nightly job also runs 100). JaVaFo and bbpPairings accelerate only from fictitious points, so the dialect writes each player's Virtual Points as `XXA` records (JaVaFo manual, "Accelerated rounds"). The summary line ends with the number of accelerated tournaments. Locally, 50 of 50 Baku tournaments: bbpPairings v6.0.0 and v5.0.1 agree in 396 of 396 rounds each; JaVaFo in 367 of 396, the other 29 registered as KD-4.

**Both directions.** Two more gates run per program that has a checker and a generator (bbpPairings v6.0.0 and v5.0.1, JaVaFo 2.2; Gacrux has neither):

| Gate | What it does |
|---|---|
| `OracleOwnTournamentsIT` | the program's own random tournament generator (`-g <config> -o <file>`, with `-s <seed>` for bbpPairings; JaVaFo takes no seed) plays `fideswiss.oracle.tournaments` tournaments of 14..60 players and 5..11 rounds; our `check` (`Tournament.check`, as `fide-swiss check` runs it, and the declared points) must find every round legal and the system's own pairing. A JaVaFo tournament that fails is kept as generated |
| `OracleCheckerIT` | the tournaments our generator plays, with every event of the corpus and Baku, are written whole in the program's dialect and given to its checker (`<file> -c`); every round must be accepted |
| `OracleControlsIT` | positive controls: a bbpPairings tournament with one round re-paired by hand is refused by both Oracle checkers and by ours |

Both programs' `-c` exit 0 even when a round differs (they print the two pairings), so a file counts as accepted only when the exit code is 0 and the output is nothing but the `name: Round #n` lines. Locally, 50 tournaments each: our check finds 403 of 403 (seed 20260929) and 411 of 411 (seed 777) rounds legal and consistent for each of bbpPairings v6.0.0, v5.0.1 and JaVaFo; each checker accepts 50 of 50 of our tournaments (15 and 8 accelerated). Note that JaVaFo's checker does **not** see KD-4: it accepts our pairing of the round where its own `-p` differs, so the register lists no divergence for the checker direction.

**Release gate (50,000 tournaments).** The size is `-Dfideswiss.oracle.tournaments=50000`; the default stays 100 for local runs. The `Release gate` workflow (`.github/workflows/release-gate.yml`, called by `Release` before anything is built or published, and startable by hand with its `tournaments` and `shards` inputs, default 50,000 and 50) builds the Oracles and runs every gate above in shards of the total, each on its own seed range; the inputs of every difference are kept as artifacts. Do not run it locally.

### 10.1 Gacrux and the Swiss Team System

The Gacrux gate plays team tournaments (7.7) with `--swiss-team`'s defaults narrowed to 6..24 teams, 4..11 rounds and 2..6 boards, a drawn format for each (`--random-team-format`), Baku at random, forfeits and requested half- and zero-point byes (`fideswiss.oracle.events` defaults to `forfeits,byes` here; add `withdrawals` and `late-entries` to play team withdrawals and late entries, as the nightly job does: 939 of 939 rounds agree in 136 tournaments, no new divergence). It runs Gacrux as `python -c <driver> <clone> <file> <round>` in the clone's `gacrux` directory; the driver is ours and only imports Gacrux's own modules (nothing of Gacrux is in this repository). Under the defaults it runs plain Gacrux (ADR 0009). With `-Dfideswiss.oracle.gacrux.literal=true` it applies **Ruling G1** in front of Gacrux's bracket seating, `update_bracket` over the nodes sorted by TPN (C.04.6 3.6.1; Known Divergence KD-1), which is what "patched" means, and compares it with the literal readings (`bracket-seating=tpn`, `pab-value=draw`, `baku-secondary-score=real`). Gacrux must be at commit 6419149 (v1.10.62), and its Python needs `networkx`:

```sh
git clone <TieBreakServer> ~/oracles/TieBreakServer && git -C ~/oracles/TieBreakServer checkout 6419149
python3 -m venv ~/oracles/gacrux-venv && ~/oracles/gacrux-venv/bin/pip install networkx
FIDESWISS_ORACLE_GACRUX=$HOME/oracles/TieBreakServer FIDESWISS_ORACLE_GACRUX_PYTHON=$HOME/oracles/gacrux-venv/bin/python \
  ./mvnw verify -pl oracle-it -am -Dtest=NONE -Dsurefire.failIfNoSpecifiedTests=false \
  -Dit.test=SwissTeamOracleIT -Dfideswiss.oracle.tournaments=50
```

The input is the team file of 4.4 with three additions for Gacrux (the `OracleDialect.GACRUX` writer): the members' points (columns 81-84) are 0.0, because Gacrux refuses a file whose stated points differ from the results it adds up, pending absences included, and does not check a 0.0; an accelerated tournament carries its Virtual Points as explicit `250` records, since Gacrux accelerates only from them; and `162` states `P` (a win's under the defaults). Under the literal readings, tournaments with Baku and the secondary score used for colours are left out (KD-3); under the defaults nothing is left out. Locally with the defaults: 706 of 706 rounds agree in 98 tournaments (seed 20260929) and 1,946 of 1,946 in 278 tournaments (seed 777, with withdrawals and late entries); with the literal readings 691 of 691 in 96 tournaments. Earlier, with the patch: Locally, 359 of 359 rounds agree in 50 tournaments and 2,653 of 2,653 in 385 tournaments (seed 77; 15 of 400 were Baku with the secondary score and left out): no new Known Divergence. A reply that leaves a team unpaired, or gives two PABs, counts as a refusal, and both refusing is agreement.

## References

- `README.md`: overview, status table, readings of each Handbook text, the library API.
- `CHANGELOG.md`: added features and pairing changes (every change that can alter an output is listed under "Pairing changes").
- `docs/adr/`: decisions: 0003 Swiss Team interpretations, 0009 the defaults follow the reference app, 0004 recorded rounds are facts, 0005 the JaVaFo/bbpPairings protocol, 0006 pairing numbers, 0007 Double-Swiss encoding, 0008 a pairing change is a versioned change.
- `docs/verification/`: the article-to-test map for the Dutch System 2026 and the Known Divergence register.
- `docs/research/`: notes on the Handbook texts, TRF and the oracles.
