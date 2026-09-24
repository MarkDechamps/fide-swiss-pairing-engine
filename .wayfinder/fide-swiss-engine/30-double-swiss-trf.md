---
title: Double-Swiss matches in TRF
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: []
---

## Question

How do the TRF reader, the CLI's `pair`/`check`/`generate` and the Pairing Reply carry a Double-Swiss match? TRF26 player records have one opponent, one colour and one result character per round (`1 = 0 + - W D L H F U Z`), so they cannot hold a 1½-½ match, a ½-0 game, a single forfeited game or the match-level forfeit of the Preface (a player forfeits both games). Decide the encoding the library reads and writes (for example two TRF rounds per match, an extension record per match, or result pairs in the round field), what `192` `FIDE_DOUBLESWISS` implies, how the colour of 1.6 (the game-1 colour, if at least one game was played) is recovered, and whether the random generator's TRF26 output for Double-Swiss uses the same encoding.

## Context

Graduated from Readable Double-Swiss pairing algorithm (decision 6). Evidence: `docs/research/other-swiss-systems.md` §6 and §8 (TRF26 cannot encode a match; ETT26's `FIDE_DOUBLESWISS` description is stale), `docs/research/trf-format.md`. Already fixed elsewhere: the domain model holds per-game results as a `MatchOutcome` (Shared domain model, Public library API); the CLI surface and the Pairing Reply shape (TRF CLI surface); the generator plays two games per match with colours reversed, each drawn independently (Random tournament generator). What a pairing reads from a match is listed in the resolution of Readable Double-Swiss pairing algorithm, decision 3.
