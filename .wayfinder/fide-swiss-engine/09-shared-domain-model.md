---
title: Shared domain model across all systems
labels: [wayfinder:grilling]
status: closed
assignee: mark
blocked_by: [01-basic-handling-rules, 02-dutch-system, 03-swiss-team-system, 04-other-swiss-systems, 06-trf-format, 07-tie-breaks, 12-historic-rule-editions, 16-olympiad-rules-scope]
---

## Question

What is the shared domain model all pairing systems and tie-breaks build on (tournament state, participant as player vs team, round, game vs match, result, colour history, float history, bye, score and its variants, pairing number), and where do the systems genuinely diverge so that a clean OO model keeps them separate?

## Context

- From Historic rule editions: model the Swiss Rules Edition and Tie-break Edition as first-class settings, and build each edition by composing per-article rule objects (no inheritance between editions).

## Resolution

Grilled with the author on 2026-09-23. The author accepted the remaining recommendations without going through them one by one. Design constraints from the map Notes: DDD, clean public API, composition over inheritance, `Optional` instead of `null`, value objects instead of primitives.

- **Aggregate: an immutable `Tournament` snapshot** ([ADR 0002](../../docs/adr/0002-immutable-tournament-snapshot.md)). `Tournament.of(settings, participants)`, then `withRound(...)`, `withdraw(...)`, `requestBye(...)` and `enterLate(...)` each return a new snapshot, validated on construction. The queries are `pairNextRound()` (returns a `RoundPairing`) and `standings()` (returns `Standings`). Same snapshot, same pairing.
- **`TournamentSettings`** (one value object): competition type (`Individual` or `Team`), `PairingSystem`, Swiss Rules Edition, Tie-break Edition, `ScoringScheme`, `Acceleration` (a "none" object, not `Optional`), `TieBreakList`, ranking key, `NumberOfRounds` (last-round rules depend on it). An invalid combination (a system of the wrong competition type, a system with no text in the chosen edition) fails in `Tournament.of` with a specific exception. The system is part of the settings and is never passed per call, because GHR 1.3 fixes it for the whole event.
- **`Participant`** (one type, no Player/Team subtypes, since lineups are out of scope): `ParticipantId` (stable, opaque, supplied by the client), name, `Rating`, optional title.
- **Identity vs Pairing Number**: history refers to `ParticipantId` only. The `PairingNumber` (TPN) is assigned for each round by one explicit ranking operation (strength, title, then name or a declared key, per GHR 2.2), and the resulting `PairingNumbers` map is part of each `RoundPairing` so checkers can reproduce it.
- **Rounds hold the facts, round-centric**: a `Round` has `Board`s (white, black, `Outcome`; forfeits keep their assigned colours) and per-participant entries for those not on a board: `PairingAllocatedBye`, `FullPointBye`, `HalfPointBye`, `ZeroPointBye` (requested or withdrawn), `NotYetEntered`. `Outcome` is sealed: `GameOutcome` (one game: win/draw/loss, forfeits) or `MatchOutcome` (game points per side over several games: Swiss Team, Olympiad, Double-Swiss).
- **`ParticipantHistory` is derived**, computed once and shared by every system and the tie-breaks: played-colour sequence (GHR 3.4), opponents met (BR 2 + GHR 3.5), PAB eligibility (BR 4), unplayed-game count, Score. Floats are **not** shared: each system has its own float rule object over the history.
- **Scores**: `Points` is an exact decimal (normalised `BigDecimal`) with only `plus` and comparison. `ScoringScheme` maps outcomes to `Points`; team competitions add match-point rules and a `PrimaryScore`. There are separate types for `Score` (standings) and `PairingScore` (Score plus acceleration virtual points), so systems pair on `PairingScore` and tie-breaks (and the Burstein Index, 1.7.2) can only see `Score`. Tie-break adjusted scores stay private to the tie-break module.
- **Acceleration** is a virtual-points layer declared in the settings. It only feeds `PairingScore`.
- **Pairing System seam**: a public `PairingSystem` interface (`RoundPairing pairNextRound(Tournament)`), which `Tournament` delegates to, obtained from factories (`PairingSystems.dutch(edition)`). It is not sealed, so an arbiter's own authorised system (GHR 1.1) can be plugged in. `RoundPairing` = boards with colours, byes, `PairingNumbers`, the editions used, and a pairing trace (GHR 1.3, BR 9).
  - Shared internal building blocks: `ParticipantHistory`, the participants to be paired this round, the Basic Rules absolute criteria (no rematch, at most one PAB, colour limits where the edition applies them).
  - Where the systems diverge (each composed from rule objects inside its own package): the bracket procedure, criteria sets, float rule, colour allocation, PAB strategy (after pairing in Dutch; before pairing in Dubov, Burstein, Double-Swiss and Swiss Team; lowest-ranked in Lim).
  - Whether criteria get a shared abstraction across systems is decided after Readable Dutch pairing algorithm, once there is a second adapter.
- **Standings and tie-breaks**: `tournament.standings()` uses the `TieBreakList` (TEC code syntax) and the Tie-break Edition. Each tie-break is a small object behind a `TieBreak` interface, and modifiers (Cut-1/2, Median-1/2, Limit) are decorators. How unplayed rounds count is a separate `UnplayedRoundPolicy` object (C.07 2026-03, C.07 2024-08, Burstein), so the Burstein Index is `Buchholz` + `SonnebornBerger` composed with the Burstein policy, reusing the same calculators.
- **Logging**: none in `core` (it stays dependency-free). The pairing trace is a returned domain object, and only the CLI may log. This overrides the clean-java SLF4J rule for `core`.
- **Packages in `core`** (under `io.github.markdechamps.fideswiss`), by domain concept. Exported: `tournament` (aggregate, settings, participants, rounds, outcomes, scoring), `pairing` (`PairingSystem`, `PairingSystems`, `RoundPairing`), `standings` (`Standings`, `TieBreakList`). Internal: `history`, `rules` (Basic Rules, editions as compositions), and one package per system (`dutch`, `dubov`, `burstein`, `lim`, `doubleswiss`, `team`, `olympiad`), plus `tiebreak` internals.
- **Glossary** gained Participant, Score, Pairing Score, Pairing-Allocated Bye, Pairing Number, Standings, Tie-break List.
