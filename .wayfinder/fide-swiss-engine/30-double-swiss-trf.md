---
title: Double-Swiss matches in TRF
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

How do the TRF reader, the CLI's `pair`/`check`/`generate` and the Pairing Reply carry a Double-Swiss match? TRF26 player records have one opponent, one colour and one result character per round (`1 = 0 + - W D L H F U Z`), so they cannot hold a 1½-½ match, a ½-0 game, a single forfeited game or the match-level forfeit of the Preface (a player forfeits both games). Decide the encoding the library reads and writes (for example two TRF rounds per match, an extension record per match, or result pairs in the round field), what `192` `FIDE_DOUBLESWISS` implies, how the colour of 1.6 (the game-1 colour, if at least one game was played) is recovered, and whether the random generator's TRF26 output for Double-Swiss uses the same encoding.

## Context

Graduated from Readable Double-Swiss pairing algorithm (decision 6). Evidence: `docs/research/other-swiss-systems.md` §6 and §8 (TRF26 cannot encode a match; ETT26's `FIDE_DOUBLESWISS` description is stale), `docs/research/trf-format.md`. Already fixed elsewhere: the domain model holds per-game results as a `MatchOutcome` (Shared domain model, Public library API); the CLI surface and the Pairing Reply shape (TRF CLI surface); the generator plays two games per match with colours reversed, each drawn independently (Random tournament generator). What a pairing reads from a match is listed in the resolution of Readable Double-Swiss pairing algorithm, decision 3.

## Resolution

Resolved on 2026-09-25 from the research on `main` (`docs/research/trf-format.md` §4, §3.1, §5; `docs/research/other-swiss-systems.md` §6, §8) and the C.04.5 text. No published convention exists: TRF26, ETT26, bbpPairings, JaVaFo, Gacrux and chesspairing (one game per round, 1-½-0) carry nothing for two-game matches, and a web search found none. The author told this session to take the recommended options and stop only at real blockers. There were none, so the decisions below are the recommended ones, for the author to overturn.

**Answer: a Double-Swiss match takes two TRF rounds, one per game, in plain TRF26.** No extension record is needed. ADR 0007 records the trade-off.

1. **Two columns per match.** Match *k* is TRF round 2k−1 (game 1) and 2k (game 2) in every `001` record. TRF result codes are per player, so every game in the Preface is native: `1`/`0`, `=`/`=`, the odd ½-0 (`=`/`0`), 0-0 played (`0`/`0`), a single forfeited game (`+`/`-`), and a double forfeit (`-`/`-`). A file of a rated Double-Swiss already has to list each game, and a double round robin already writes each game as a round.
   - Rejected: one column per match plus an extension record for game points (other readers lose the scores); a match result in one column (it cannot hold 1½-0 or a per-game forfeit).
2. **The pair of columns is read as one match, and must agree** (otherwise invalid input, exit 3):
   - The same opponent in both, with reversed scheduled colours (`w` then `b`, or `b` then `w`).
   - A bye fills both columns with opponent `0000`, colour `-` and the same code (`U`, `H`, `F` or `Z`). A bye in one column and a game in the other is invalid ("Byes apply only to matches").
   - The **match colour** (1.6) is the game-1 column's scheduled colour, and it counts only if at least one of the two games was played. `W`/`D`/`L` (less than one move) count as played, as they do for pairing everywhere else.
   - A **forfeit match** is one where a player has `-` in both columns. When each player forfeits one game, the match counts as played and as a meeting, but it gives no colour (as in Readable Double-Swiss pairing algorithm).
   - Each column maps to one `GameOutcome` (game 2's colours reversed), and the two form one `MatchOutcome` from the view of the game-1 White player.
3. **Scores.** `162` `W`/`D`/`L`/`A` score games, and a match scores the sum of its games. A bye pair counts once. Under Double-Swiss, `162`'s bye symbols (`P`, `F`, `H`) are **values per match**, and an absent one takes the value of the `doubleSwiss` profile (PAB = win + draw, 1.4), not TRF26's per-game default. The writer always emits `P`, `F` and `H` for Double-Swiss, so other readers never have to guess. The points column (81–84) holds the standings score.
4. **Round numbers: in the file a round is a column; everywhere else it is a match.** `142`/`XXR` count columns and must be even. `240` names TRF rounds: the writer lists both of a match, and the reader accepts either one with a warning and expands it to both. `250` ranges must cover whole matches (start odd, end even). `132` dates each column. The CLI and the library count matches: `--rounds`, `check --round r`, the trace and every message.
5. **CLI.**
   - `pair` pairs the first match whose two columns are both empty. A match with game 1 recorded and game 2 empty is invalid input (exit 3, "match k is half recorded").
   - `check` reads each column pair as one `ProposedPairing` and reports per match.
   - The **Pairing Reply is unchanged** (TRF CLI surface): one `white black` line per match, where "white" is the game-1 White, and `id 0` for the PAB, in GHR 3.6 order.
6. **`192`.** `FIDE_DOUBLESWISS` means C.04.5 2026, the only edition, and switches on this encoding. `FIDE_DOUBLESWISS_BAKU` adds Baku acceleration (its virtual-point value stays in the acceleration fog). `CUSTOM_DOUBLESWISS` is rejected as unsupported (exit 3), as bbp rejects codes it does not know. ETT26's stale "possible evolution" wording is ignored. Without `192`, `--system double-swiss` switches the encoding on, and flags still override records with a warning. A Double-Swiss file read as another system is read as twice the rounds, which only `192` or the flag can prevent.
7. **The random generator writes the same encoding**, with `192 FIDE_DOUBLESWISS` and an explicit `162`, so the free TRF26 generator of technical acceptance and our reader agree by construction.
8. **Consequence for the domain model.** `GameOutcome` needs the Preface's odd played results (½-0, 0-½, 0-0), for example `GameOutcome.played(whitePoints, blackPoints)` with `whiteWins()`, `draw()` and `blackWins()` as shorthands. Public library API had only the usual three. The TRF reader maps any per-player code pair onto it.

No tickets surfaced from this answer. With the frontier empty, three fog patches were graduated: Readable Dutch 2017 pairing procedure, Acceleration across the systems, and Publishing to Maven Central and packaging the CLI. The acceptance route stays in the fog (on hold until the author contacts FIDE). The README's TRF row now says how a Double-Swiss match is written.
