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
| `--interpretation <name>=<value>` | see 5.6 | profile | A Swiss Team reading (ADR 0003). Repeatable. |

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
