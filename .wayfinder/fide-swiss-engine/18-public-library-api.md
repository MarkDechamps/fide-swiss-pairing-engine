---
title: Public library API
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

What are the exact public types and operations of the library, given the immutable `Tournament` snapshot from Shared domain model across all systems? Covers how a client builds a snapshot (participants, settings, completed rounds), the pre-round operations (`withdraw`, `requestBye`, `enterLate`) and result corrections (GHR 4.3), the error model (which exceptions, and what they report), and the exact shape of `RoundPairing` (including the pairing trace) and `Standings`. The aim is the smallest intention-revealing surface that still makes everything FIDE defines possible.

## Context

Graduated from the "Public library API" fog once the domain model was settled.

## Resolution

Decided on 2026-09-23. The author handed the ticket over to the recommended answers ("continue with recommended and only stop on real blockers"). The API builds on what is already settled: the immutable snapshot ([ADR 0002](../../docs/adr/0002-immutable-tournament-snapshot.md)), `TournamentSettings`, and the profile factories. Evidence: `docs/research/basic-handling-rules.md` (GHR 2.3–2.5, 3.1, 4.3, 4.4; ambiguities 4–5) and Verification strategy (the `-check` direction and the invariant checker). Every type below lives in an exported package (`tournament`, `pairing`, `standings`). Everything else stays internal.

**Building a snapshot.**

- `Profiles.individualSwiss(NumberOfRounds.of(9))` returns a complete `TournamentSettings`. Each profile factory takes the number of rounds, so a settings value without one cannot exist, and no `Profile` type is needed. Overrides are typed `with(...)` calls on `TournamentSettings` (`with(PairingSystems.dubov(edition))`, `with(Acceleration.baku())`, `with(TieBreakList.parse("BH/C1, SB"))`, `with(UpfloaterLookAhead.graded())`, `with(InitialColour.black())`, `with(RankingKey.declared(order))`). Each one returns a new value.
- `Tournament.of(settings, participants)`. The simplest overload is `Tournament.of(participants, NumberOfRounds.of(n))`, which uses `individualSwiss`.
- `Participant.of(ParticipantId.of("…"), Name.of("…"), Rating.of(2145))`, plus `withTitle(Title.IM)`. `Rating.unrated()` ranks below every rating unless the client supplies an estimate (GHR 2.1 leaves the estimate to the Chief Arbiter). A team is also a `Participant`, and its name and strength are whatever the competition rules use.
- **Initial order key.** `RankingKey` is part of the settings: strength → title → name (GHR 2.2, the individual default), `declared(List<ParticipantId>)` for a replacement key that has been announced (a random order, for example), and `asListed()` (the team default, because TPS 1.1 hands the order to the competition rules).
- **Completed rounds.** The common loop is `var pairing = tournament.pairNextRound();`, then `tournament = tournament.withRound(pairing.completedWith(outcomes));`, where `outcomes` maps `BoardNumber` to `Outcome`. The resulting `Round` keeps the `PairingNumbers` it was paired with. A client can also build a round directly (`Round.of(boards, entries)`, for imported history) and the library then derives the numbers.
- **Outcomes** (sealed): `GameOutcome.whiteWins() / draw() / blackWins() / whiteWinsByForfeit() / blackWinsByForfeit() / doubleForfeit() / adjourned()` (adjourned counts as a draw for pairing, GHR 3.1), and `MatchOutcome.ofGames(List<GameOutcome>)`, which derives the game points and so keeps per-board results for team tie-breaks.
- **Pre-round operations**, each returning a new snapshot: `withdraw(id, RoundNumber from)`, `requestBye(id, RoundNumber, RequestedBye.half() / full() / zero())`, and `enterLate(participant, RoundNumber firstRound)`, whose missed rounds count as zero-point unplayed rounds (GHR 2.4). An overload takes `MissedRounds.halfPointByes()` for tournament rules that award points. There is no "rejoin": a return after absence is a requested bye.
- **Corrections (GHR 4.3)**: `withCorrectedOutcome(RoundNumber, ParticipantId, Outcome)`, `withCorrectedColours(RoundNumber, BoardNumber)` (swaps them), and `withCorrectedRating(ParticipantId, Rating)`. The library does not check the deadline, because that is arbiter workflow: the snapshot *is* the recorded history as of pairing time. Whether a rating correction renumbers is decided by the edition's numbering rule (GHR 2.3 freeze after the round-4 pairing, Dubov 1.2.2 "must recalculate", TPS 1.1 fixed numbers). Late entries renumber at any time, following ADutch's reading of ambiguities 4 and 5, and that reading is documented. Past rounds are never re-paired.

**Recorded rounds are facts; rule checking is separate** ([ADR 0004](../../docs/adr/0004-recorded-rounds-are-facts.md)). `withRound` validates only structure: known ids, the next round number, every active participant exactly once, board outcomes of the competition type. It does not refuse a rematch or a second PAB, because GHR 4.4 lets an arbiter change a published pairing and the history must record what was actually played. Rule checking is a separate query:

- `tournament.check(ProposedPairing)` returns a `PairingCheck`: `violations()` (each one a `Violation` citing its article and the participants involved, against the Basic Rules absolute criteria and the system's own), `isLegal()`, and `isSystemPairing()` with `differences()` against what `pairNextRound()` would give. This backs the TRF `-check`, the invariant checker and GHR 4.4 manual pairings. A `ProposedPairing` is boards (white, black) plus an optional PAB, built by the client or read from TRF.

**`RoundPairing`** (the result of `pairNextRound()`):

- `roundNumber()`
- `boards()`: `PairedBoard(BoardNumber, white, black)`, sorted per GHR 3.6 by default.
- `pairingAllocatedBye()`: an `Optional<ParticipantId>`.
- `unpaired()`: every other participant not on a board, with its reason (requested bye, withdrawn, not yet entered). The pairing is then a complete account of the round.
- `pairingNumbers()`
- `settings()`: the effective system, editions, acceleration and Interpretations (GHR 1.3). No profile name.
- `trace()`
- `completedWith(outcomes)`, which returns a `Round`.
- `samePairingAs(other)`, which compares boards, colours and PAB only, ignoring the trace. This is the equality that Oracle comparisons use.

**`PairingTrace`** (BR 9, GHR 1.3) is a returned domain object, always computed, and never logged by `core`. It is an ordered list of sealed `TraceStep`s:

- `ByeDecision(participant, article)`
- `BracketStep(label, residents, movedDown, chosen pairs, downfloaters, CriteriaVector)`, where the vector is the per-article `Failure`s of the chosen candidate
- `ColourDecision(board, article)`, the 5.2.x or 4.3.x rule that decided the colours

Candidate counts are diagnostics on the step and never take part in equality. `describe()` renders the trace as text for the CLI. The shared shape is only this outer envelope: whether criteria and `Failure` become shared across systems is still left to the second prototype.

**`Standings`**: `tournament.standings()` (after the last recorded round) and `standingsAfter(RoundNumber)`.

- `ranked()` returns `Standing(Rank, Participant, Score, TieBreakValues)`, with the values in `TieBreakList` order.
- `standing(ParticipantId)` returns one `Standing`.
- `tieBreakList()` and `tieBreakEdition()`.
- When a tie remains after the whole list, the participants share a rank. C.07 leaves the final decision (lot, play-off) to the organiser, so the library never draws lots.
- Tie-break values are exact decimals.

**Error model.** All exceptions are unchecked and extend a sealed `SwissPairingException`. Each one carries a list of `Problem`s: the article (where there is one), the participants involved, and a message. Validation collects every problem instead of stopping at the first.

- `InvalidSettingsException`: an invalid combination, raised in `Tournament.of`.
- `InvalidTournamentException`: a structural snapshot problem (a duplicate or unknown id, a round out of order, a participant missing from or repeated in a round, an operation on an unknown participant or a past round, pairing after the last round).
- `NoLegalPairingException`: the absolute criteria cannot be met. It carries the trace up to that point, because the arbiter has to decide (GHR 4.4).

There are no `null`s and no error codes, and `Optional` appears only where a value may truly be absent (the PAB, a title).

**Value types** used on the surface: `ParticipantId`, `Name`, `Rating`, `Title`, `NumberOfRounds`, `RoundNumber`, `BoardNumber`, `PairingNumber`, `Points`, `Score`, `Rank`.

**Glossary** gained Late Entry, Requested Bye, Withdrawal, Correction, Proposed Pairing and Pairing Trace. ADR 0004 records the facts-vs-checking split.

**Graduated:** TRF CLI surface, now that the library operations the CLI wraps (`pairNextRound`, `check`, settings precedence) are fixed.
