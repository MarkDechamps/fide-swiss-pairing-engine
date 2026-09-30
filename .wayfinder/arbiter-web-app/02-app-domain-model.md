---
title: App domain model and its bounded context
labels: [wayfinder:grilling]
status: open
assignee: mark
blocked_by: [01]
---

## Question

How does the app's domain relate to the library's? Is the app its own bounded context with its own glossary (CONTEXT-MAP.md), and what are its aggregates and value objects (event, registration, round, result sheet)? Does the app store the recorded facts and rebuild the library's immutable `Tournament` from them each time (event-sourced style), or store a snapshot? Where is the anti-corruption layer between app terms and library terms?

## Resolution

Grilled 2026-09-30 with the user.

1. **Its own bounded context**, *Tournament Administration* (`webapp/CONTEXT.md`), beside the library's *Pairing* context (`CONTEXT-MAP.md`).
2. **Conformist, not an anti-corruption layer.** The user asked whether an anti-corruption layer would duplicate concepts; it would. The library is DDD, pure Java, immutable and in the same FIDE language, so the app domain uses its types directly (Participant, Name, Rating, Title, Outcome, GameOutcome, RoundPairing, Standings, TournamentSettings/Profiles). The app adds only what the library lacks: Lifecycle, Proposed/Published/Complete Round, Override, Tournament Log, and the TRF player data the library does not carry (FIDE id, federation, club, birth date). There is no pairing port. Ports remain only for IO (repository, clock, TRF files). A breaking library change ripples into the app, which is accepted: the app is the library's acceptance test.
3. **Store the facts.** The Tournament Log (Arbiter Actions) is the single source of truth, and the rest is derived by replay. The library's `Tournament` is rebuilt from the log whenever it is needed, with a cache later only if needed. This makes library change 1 (a replayable change log) a must-have.
4. **One aggregate, `Tournament`**, holding `Players`, `Rounds` (each Proposed, Published or Complete, with `Boards` and results), `TournamentLog` and `Lifecycle`, plus value objects such as `TournamentName`, `FideId` and `Override`. It encapsulates the library: the library snapshot is a private field `recorded`, imported by its full name in that one class, and callers see behaviour only (`proposeNextRound()`, `adjust(...)`, `publish()`, `enterResult(...)`, `standings()`).
5. **The Arbiter Actions** are recorded in the past tense with a timestamp: TournamentCreated, SettingsChanged, PlayerRegistered/Edited/Removed, RoundProposed, PairingAdjusted, OverrideAcknowledged, RoundPublished/Unpublished, ResultEntered, PlayerWithdrawn, WithdrawalUndone, ByeRequested/Cancelled, LateEntryAdded, Outcome/Colours/RatingCorrected, TournamentFinished/Reopened. The proposed pairing is stored as it came out and never recomputed on replay, so a new library version cannot change a published round. Derived state is not stored. An action is checked against the aggregate's rules before it is appended, and a refused action leaves no trace.
