---
title: TRF CLI surface
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

What exactly does the `cli` module accept and print? Covers the JaVaFo/bbpPairings-compatible commands (`-p` pair the next round, `-c`/`-check` check the last round, output file layout, exit codes), how the trace is shown, flags for system/profile/edition/Interpretations (precedence is settled in Tournament profiles), our reply format for team pairings (no standard exists), and how to handle what TRF26 cannot express (Lim has no `192` code; what `XXS` can say that `162` cannot).

## Context

Graduated from the "TRF CLI surface" fog once Public library API fixed the operations the CLI wraps: `pairNextRound()` → `RoundPairing` (with `PairingTrace`), `check(ProposedPairing)` → `PairingCheck`, and the error model. Evidence: `docs/research/trf-format.md` (TRF26, JaVaFo `-p`, dialect differences between JaVaFo 2.2 and bbp). The generator's `generate` subcommand and JaVaFo-style `-g` form are fixed in Random tournament generator; this ticket fixes the executable name and the shared exit-code table they use.

From Readable Swiss Team pairing algorithm: C.04.6 does not fix the board order of a round. Gacrux orders matches by the pair's higher score, then the sum of scores, then the lower TPN, with the bye last (`pairingfideteam.update_board`), the way its Dutch engine does. The reply format must say which order it prints.

## Resolution

Decided on 2026-09-23. The author asked this session to take the recommended answers and to stop only on real blockers. There were none, so each decision below is the recommended one, for the author to overturn. Evidence: `docs/research/trf-format.md` (§3.1–3.2 `162`/`192`/ETT26 gaps, §7 JaVaFo/bbp protocol, §8 empirical runs, open questions 1, 4, 5, 7, 8), `docs/research/fide-endorsement.md` §3 (the PTC/RTG requirements, the de facto CLIs, Gacrux's status codes), Tournament profiles (precedence), Public library API (the operations wrapped), Random tournament generator (`generate`), Readable Swiss Team pairing algorithm (Gacrux's board order). The protocol choice is recorded in [ADR 0005](../../docs/adr/0005-cli-speaks-the-javafo-bbp-protocol.md).

**Executable and commands.** The executable is **`fide-swiss`**: a launcher script over the `cli` jar, whose main class is `...fideswiss.cli.Main`. How it is packaged (fat jar, jlink image) belongs to the Publishing fog. The CLI has two grammars that parse into the same sealed `Command` values (`Pair`, `Check`, `Standings`, `Generate`, `Version`, `Help`), so each operation has one implementation:

| Operation | Subcommand (canonical) | Compatible short form |
|---|---|---|
| Pair the next round | `fide-swiss pair <in.trf> [-o <reply>] [-l [<trace>]]` | `fide-swiss [--<system>] <in.trf> -p [<reply>] [-l [<trace>]]` (JaVaFo/bbp) |
| Check a tournament | `fide-swiss check <in.trf> [--round <r>]` | `<in.trf> -c [<r>]` (JaVaFo/bbp), `-check <in.trf>` (the TEC Manual's example) |
| Print standings | `fide-swiss standings <in.trf> [--after <r>]` | — |
| Generate tournaments | `fide-swiss generate …` (as fixed in Random tournament generator) | `-g [<cfg>\|<seed>] -o <out> [-s <seed>]` |
| Version | `fide-swiss version` | `-r` |

- The first argument chooses the grammar: a subcommand name gives the canonical one, anything else the compatible one (a file literally named `pair` is written `./pair`).
- The argument parser is hand-written in `cli` (no picocli): the grammar is small, it has a positional-first compatible form, and the zero-dependency spirit of `core` carries over.
- `-` as the input reads the TRF from stdin.
- `version` prints the library version and every implemented system and edition with its README status (GHR 1.3).

**The Pairing Reply (`pair` / `-p`).** It is exactly JaVaFo's format, so existing harnesses and THPs can drop the library in:

```
<P>                  number of lines that follow, the PAB line included
<white> <black>      one line per board, in RoundPairing.boards() order
<id> 0               the PAB, if any, always last
```

- **Ids are the file's ids:** the `001` starting rank for individuals, and the `310` team number for teams. They are never the per-round Pairing Numbers. So the reply does not depend on *Pairing numbers of participants not yet paired*, and it stays comparable with bbp and JaVaFo whatever that ticket decides.
- **Board order** is GHR 3.6 (the library's default `boards()` order). Oracle comparisons use `samePairingAs`, so order never matters for verification.
- **Team reply:** the same shape, one `white black` line per match, where "white" is the team with White on board 1. The board colours follow from `352` and are not printed. Match order is GHR 3.6 applied to the pair's primary scores (higher score, then the sum, then the lower TPN, the bye last), which is Gacrux's `update_board` order. This settles research open question 5.
- Participants left out of the round (requested byes, withdrawals, not yet entered) are not printed, as in JaVaFo. The trace lists them.
- Without an output file the reply goes to stdout. Diagnostics always go to stderr. On any error no reply file is written (JaVaFo).

**Trace (`-l`).** `-l <file>` writes `PairingTrace.describe()` as plain text. A bare `-l` writes it to `<input>.trace.txt` beside the input. Text only for 1.0. A JSON form of the trace and of the check report is a later option under ADR 0005, not a 1.0 requirement.

**Checker (`check` / `-c` / `-check`).** This is the Pairings Checker (PTC) that C.02.03 §7.2.3 and the TEC Manual §3.9.4.2.c ask for.

- For each recorded round (or only `--round r`), it rebuilds the snapshot from the earlier rounds, reads the recorded round as a `ProposedPairing` and calls `check`. The round is **ILLEGAL** when it has violations, **DIFFERENT** when it is legal but not the system's pairing, and consistent otherwise.
- **Standings** are checked after the last recorded round (and after `--round r` when given), using the file's `202`/`212` list, or the profile's list when the file has none. The file's Points (`001` 81–84, and team MP/GP) must equal ours. Where the file gives a rank (`001` 86–89, `310` 69–71), it must fall inside our shared rank range, because the library never draws lots.
- Report, one block per inconsistent item, then a summary line:

```
round 3: ILLEGAL
  [C.04.1 BR 2] 12 and 7 have already played each other (round 1)
round 5: DIFFERENT
  file:   3 1 | 4 5 | 2 0
  system: 1 3 | 4 5 | 2 0
standings after round 9: DIFFERENT
  17: points 5.5 in file, 6.0 computed
checked 9 rounds and 1 set of standings: 8 consistent, 2 not
```

- A **points mismatch** is an input error for `pair` (exit 3, as bbp does) but a reported inconsistency for `check`, because reporting it is the checker's job.

**Exit codes** (shared by every command, `generate` included):

| Code | Meaning | Source |
|---|---|---|
| 0 | success: a reply was produced, or the check found everything consistent | bbp |
| 1 | no legal pairing (`NoLegalPairingException`; its trace goes to stderr) | bbp |
| 2 | unexpected internal error | bbp |
| 3 | invalid request or input: usage error, a malformed TRF, an unknown or unsupported `192`, `InvalidSettingsException`, `InvalidTournamentException`, pairing after the last round, no round count | bbp |
| 4 | size limit: reserved for bbp compatibility, never emitted (the library has no limit) | bbp |
| 5 | file access error | bbp |
| 6 | the check found inconsistencies (ILLEGAL or DIFFERENT rounds, or standings) | ours |
| 7 | `generate` skipped more than 0.1% of its seeds (Random tournament generator) | ours |

Code 6 is new because code 1 already means "no legal pairing" in bbp's table (Gacrux uses 1 for "check false", which clashes). Errors print each `Problem` on stderr as `error: [<article>] <message> (<participants>)`.

**Settings flags.** They follow Tournament profiles' precedence (profile < file records < flags). They are kebab-case and repeat nothing that a file record cannot also say:

- `--profile individual-swiss|accelerated-open|double-swiss|team-swiss|olympiad`
- `--system dutch|dubov|burstein|lim|double-swiss|swiss-team|olympiad`, with the bbp-style aliases `--dutch`, `--burstein`, `--dubov`, `--lim`, `--double-swiss`, `--swiss-team`, `--olympiad`
- `--edition 2026|pre-2026` (Swiss Rules Edition), `--tiebreak-edition 2026-03|2024-08`
- `--interpretation <name>=<value>`, repeatable: `upfloater-look-ahead=parity-minimum|graded`, `last-round-zero-cd-type-b=strong|none`, `float-score=pairing|real`
- `--acceleration none|baku` and JaVaFo's `-b` (Baku for the round being paired), `--rounds <n>` (over `142`/`XXR`), `--initial-colour white|black` (over `152`/`XXC`), `--tiebreaks "<list>"` (over `202`/`212`)

A flag that overrides a record in the file prints a **warning** on stderr naming the record (`warning: --dubov overrides 192 FIDE_DUTCH_2026`). bbp lets the flag win silently. Research implication 4 wanted an error, but the precedence was settled in Tournament profiles, so the flag still wins and the warning keeps it visible.

**What TRF26 cannot express.** This settles research open questions 1, 7 and 8.

- **Missing `192` codes.** Lim and Olympiad have no ETT26 code, and Dubov and Burstein have no year-versioned ones. The reader and writer use the documented, provisional codes `FIDE_LIM`, `FIDE_LIM_BAKU` and `FIDE_OLYMPIAD`, and they also read `FIDE_DUBOV_2026`, `FIDE_BURSTEIN_2026` and `FIDE_LIM_2026`. The writer emits only the bare codes, plus a `###` comment saying the code is provisional when it is not in ETT26. The README lists them. If TEC publishes codes, the official codes replace them, and the provisional ones stay readable.
- **Bare codes and editions.** A bare code (`FIDE_DUTCH`, `FIDE_DUBOV`) takes the profile's Swiss Rules Edition (2026) and is never resolved by the tournament date, as Historic rule editions decided. `FIDE_DUTCH_2017` gives pre-2026, and `FIDE_DUTCH_2025*` is an alias of `_2026` (bbp and the TEC draft).
- **`162` vs `XXS`.** Both are read into one `ScoringScheme`. If both are present and disagree, the input is invalid (exit 3), because we never choose silently. Colour-dependent values (`WW` ≠ `BW`) are rejected: FIDE defines no such scoring system. FPB and HPB values that differ from W/D map onto the requested-bye values the scheme already has. The TRF26 writer emits `162`, and also an `XXS` line when `162` cannot carry the scheme (HPB ≠ D, FPB ≠ W). This is harmless to other readers.
- **`XXC rank`** (pair by position in the file) is read as `RankingKey.declared(file order)`. The writer never emits it.

**Files.** The CLI reads UTF-8, falling back to ISO-8859-1 on malformed input, and accepts CR, LF or CRLF. It writes TRF files as UTF-8 with CRLF (research open question 4). The reply, the trace and the report use LF.

**Definition of done.** The functional README gets a "Command line" section: every command, the reply format, the exit-code table, the provisional `192` codes and every flag. It is kept current as systems land.

**Nothing graduated.** The executable's packaging joins the Publishing fog.
