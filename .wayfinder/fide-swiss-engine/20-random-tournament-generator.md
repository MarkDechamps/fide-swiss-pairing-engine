---
title: Random tournament generator
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: [10-verification-strategy]
---

## Question

What does our random tournament generator produce, and how is it configured? It feeds the invariant checker, the regression corpus and the 50k release gate, and FIDE's technical acceptance requires it as a free TRF26 generator. Open points: the distributions of results, draws, forfeits, requested byes, withdrawals and late entries; rating spread; field size and number of rounds; which systems and editions it covers (individual, team, Double-Swiss matches); seeding and determinism; staying compatible with the inputs `bbp -g` and Gacrux's generator produce; and its CLI flags.

## Context

From Verification strategy: the generator must only produce valid input, so every checker violation is a library bug. The tournaments have to be reproducible from a logged seed, and bbp's and Gacrux's generators are run alongside ours so that neither side only sees its own style of input.

## Resolution

Decided on 2026-09-23. The author asked to go with the recommended answers and to stop only on real blockers; there were none. Evidence: `docs/research/random-tournament-generator.md` (Milvang's result model, bbp v6's generator config and defaults), `docs/research/fide-endorsement.md` §3.2–3.3 (the RTG requirements, JaVaFo/Gacrux RTG CLIs), Verification strategy, Tournament profiles, Public library API.

**What it is.** The **Random Tournament Generator** (glossary) is the library itself playing a whole tournament. It builds a field, then for each round applies the round's events (withdrawals, requested byes, late entries), calls `pairNextRound()`, draws the results with a **Result Model**, and records the round with `withRound(...)`. Its product is a completed, immutable `Tournament`. Because the pairings come from our own engine, it is also what drives our engine through the invariant checker. Because every event goes through the public API, it can only produce valid input: an illegal event is a generator bug and fails the run.

- **Where it lives:** a new published module **`generator`** (`fide-swiss-pairing-engine-generator`, package `...fideswiss.generator`). It depends only on `core`. `cli` wraps it, `trf` writes its output. This amends Project foundation's module list. The invariant checker moves from `core`'s test code into `generator`'s test code, because the PR-gate invariant run needs generated tournaments and `core` cannot depend on `generator`. This amends Verification strategy; the checker's content is unchanged.
- **Java API:** `TournamentGenerator.of(GeneratorSettings).generate(TournamentSeed) → GeneratedTournament`. That value holds the `Tournament` plus its seed and the effective settings, or a `Skipped` outcome (see below). `GeneratorSettings` takes a `TournamentSettings`, so a profile or any system/edition/Interpretation choice passes straight through, together with the ranges below, which have typed `with…` overrides in the style of Tournament profiles.

**Coverage.** The generator covers every system, edition and Interpretation the library implements, because it is driven by `TournamentSettings`. It refuses a system that has not landed yet. Each landed system gets its README status only after the generator runs on it (definition of done, item 3).
- *Individual* (Dutch, Dubov, Burstein, Lim, with or without acceleration): one game per pairing.
- *Double-Swiss*: two games per match with colours reversed. Each game is drawn independently.
- *Swiss Team and Olympiad*: teams of `boards` players (default 4, range 2–6) with a fixed line-up and no reserves (line-up validation is out of scope). Each board is drawn with the Result Model from the two board players' ratings, then summed into game and match points. The team's rating is the mean of its players' ratings.

**Default distributions.** Each tournament draws its own parameters from these ranges. The ranges follow bbp v6 where bbp has the knob, so that corpora look like FIDE's style of input.

| Parameter | Default range per tournament | Notes |
|---|---|---|
| Field size | 15–215 players; teams: 10–80 teams | Capped so that `rounds ≤ participants − 1` |
| Rounds | 5–15 (Double-Swiss 5–11) | |
| Ratings | highest 2400–2800, lowest 1400–2300, uniform between them | Pairing numbers follow the rating order (ties broken by the draw) |
| Unrated participants | 0–10% of the field | Each gets a hidden playing strength drawn from the same range, used only by the Result Model |
| Results | **Milvang's model** (C.02.03 §7.2.4), with the white advantage | `DrawPercentage` given explicitly switches to a flat draw share for bbp/JaVaFo compatibility |
| Forfeits | 1 in N scheduled games, N = 6–30 | Each side is absent independently, as in bbp, so double forfeits (0F-0F) occur. Only the codes 1F-0F / 0F-1F / 0F-0F (VCL) |
| Requested byes | HPB and ZPB rate 1 in N player-rounds, N = 15–3225; FPB off by default | Never in the last round, at most 2 per participant. The value comes from the settings' configuration |
| Withdrawals | 0–5% of the field, each after a uniformly chosen round | Unpaired for every later round |
| Late entries | 0–5% of the field, entering in round 2 to ⌈rounds/2⌉ | Through `enterLate`, so the missed rounds follow the settings |
| Scoring | the settings' scoring; with `--random-scoring`, 10% of tournaments get a non-standard one (e.g. 3/1/0) | bbp's `PointsFor…` keys override it |
| Acceleration | the settings' choice; `random` applies Baku to 20% of tournaments of a system that supports it | |
| Tie-break List | the settings' list; `random` draws 3–5 entries from the edition's catalogue, modifiers included | Standings are written with the tournament |
| Initial colour (152) | drawn per tournament and always written explicitly | as Verification strategy requires |

**No legal pairing.** When a generated round has no legal pairing, the tournament is not retried or bent. `generate` returns `Skipped(round, reason)` and the seed is logged. A corpus run reports skips separately and fails if more than 0.1% of its seeds are skipped (that would mean the ranges are wrong).

**Seeding and determinism.**
- The PRNG is the JDK's `L64X128MixRandom`, a named algorithm with a specified output, so seeds reproduce across JDKs. No `Math.random` or unseeded source is used anywhere.
- A corpus has a `CorpusSeed`. Tournament *k*'s `TournamentSeed` is derived as SplitMix64(corpusSeed, k), so any single tournament can be regenerated alone from the logged pair.
- Inside a tournament every draw uses a substream keyed by *(seed, round, purpose)*, and a game result by *(seed, round, white id, black id, board)*. A library change that alters one pairing then changes only what depends on it, so the diffs stay local.
- The seed and the generator version are written into the TRF (012 name `RTG <version> seed <n>`, as JaVaFo echoes its seed). Reproduction is guaranteed per generator version. The committed regression corpus stores TRF files, not seeds, so generator changes never break it.

**Compatibility with bbp and Gacrux.**
- *Input:* the generator also reads bbp/JaVaFo `Key=Value` config files (the keys in the research note, with the same meaning and the same random defaults when a key is missing). A `--model file.trf` mode reuses a TRF's field and configuration, as `model -g` does.
- *Output:* canonical TRF26 with one file per tournament, written by `trf`. The dialect writers for oracle runs (JaVaFo TRF16 + XX?, bbp TRF26 + XXR/XXC/XXA/XXP) stay in `oracle-it`, as Verification strategy set out. The reverse direction (reading bbp's and Gacrux's generated files) is `trf`'s job and is covered by the "their pairings, our checker" runs.

**CLI** (`generate` subcommand of `cli`; the rest of the CLI and the shared exit-code table belong to TRF CLI surface):

```
fide-swiss generate (--profile <name> | --system <system>) [--edition …] [--interpretation …]
    [--seed <corpusSeed>] [--count <k>] -o <pattern-with-%d>.trf
    [--players A[..B]] [--teams A[..B] --boards A[..B]] [--rounds A[..B]]
    [--highest-rating A[..B]] [--lowest-rating A[..B]] [--unrated A[..B]%]
    [--draw-percentage P] [--forfeit-rate N] [--hpb-rate N] [--zpb-rate N] [--fpb-rate N]
    [--withdrawals A[..B]%] [--late-entries A[..B]%]
    [--acceleration none|baku|random] [--tiebreaks <list>|random] [--random-scoring]
    [--config <javafo/bbp cfg>] [--model <file.trf>]
fide-swiss -g [<cfg>|<seed>] -o out.trf [-s <seed>]      # JaVaFo/bbp-compatible short form
```

- The executable name `fide-swiss` is a placeholder until TRF CLI surface fixes it.
- Precedence is the same as Tournament profiles: profile < config file / model < flags.
- A single value fixes a parameter and a range `A..B` draws it per tournament.
- A manifest (`<pattern>.manifest.tsv`) records one line per seed: index, TournamentSeed, status (ok/skipped with round), and the effective parameters.
