# Does the library API fit the arbiter workflow?

Ticket: `.wayfinder/arbiter-web-app/09-library-api-fit.md`. Workflow checked against: the Resolution of `01-arbiter-workflow.md` (Preparing / Running / Finished, the round cycle, corrections, field changes, no general undo).

Sources are all local and primary: the exported packages (`core/src/main/java/module-info.java` exports `tournament`, `pairing`, `standings`; `trf/src/main/java/module-info.java` exports `trf`), the public types in them, `docs/manual.md`, `docs/adr/0002, 0004, 0006, 0008` and `CONTEXT.md`. Line numbers are on branch `research/library-api-fit` (cut from `build/dutch-2026` at `f5012b7`). `T` = `core/src/main/java/io/github/markdechamps/fideswiss/tournament/Tournament.java`; other files are named in full below the table.

The app rebuilds a `Tournament` from stored facts on every request (ADR 0002 says exactly this is the intended use: "clients map their own storage to the snapshot at the edge"). Every method below is judged in that setting.

## Summary

Verdict letters: **C** covered, **P** partially covered, **G** gap. "Change" numbers refer to section "Proposed library changes"; all are additive, so **minor** under ADR 0008 unless said otherwise.

| # | Workflow step | Verdict | Where (or what is missing) | Change |
|---|---|---|---|---|
| 1 | Pair the next round | C | `T:262` `pairNextRound()` returns a `RoundPairing` (`pairing/RoundPairing.java:23`); refuses beyond the declared rounds (`T:268`) | |
| 2 | Adjust: swap opponents / colours / bye | P | The app edits a `ProposedPairing` (`pairing/ProposedPairing.java:14`, records of `ProposedBoard(white, black)` plus an optional PAB). No helper for swap, colour flip or moving the bye; no board numbers | 2 |
| 3 | Check an adjusted pairing | P | `T:279` `check(ProposedPairing)` returns `PairingCheck` (violations with article; `differences` are free strings). It runs a whole system pairing inside, with no progress and no cancel | 3 |
| 4 | Acknowledge an illegal pairing, record it anyway | C | ADR 0004: `withRound` accepts any structurally valid round (`T:312`). `PairingCheck.isLegal()` (`PairingCheck.java:17`). The Override and its reason are the app's | |
| 5 | Turn a proposed pairing into a recorded round | G | Only `RoundPairing.completedWith(outcomes)` (`RoundPairing.java:41`) exists, and only for the system's own pairing. For a `ProposedPairing` the app must build `Round.of(...)`, number the boards (GHR 3.6) and put the PAB and the unpaired participants in the `byes` map itself | 2 |
| 6 | Publish | C (app state) | Publishing is app state: a published round is not a `Round` yet, because a `Round` cannot hold a board without an `Outcome` (`Board.java:12`, `requireNonNull(outcome)`). Nothing in the library to do | (note A) |
| 7 | Enter results incl. forfeits and odd results | C | `GameOutcome` has all ten values incl. `WHITE_WINS_BY_FORFEIT`, `BLACK_WINS_BY_FORFEIT`, `DOUBLE_FORFEIT`, `WHITE_HALF_BLACK_ZERO`, `WHITE_ZERO_BLACK_HALF`, `BOTH_ZERO` (`GameOutcome.java:6`). No notation parser or symbol for keyboard entry | 8 |
| 8 | Round completion | P | `completedWith` throws `InvalidTournamentException` "No outcome for board n" if a board is missing (`RoundPairing.java:66`); `withRound` checks the round number (`T:314`). "Is the round complete" is an app rule. There is no in-progress round type, so no live standings mid-round | (note A) |
| 9 | Unpublish / re-pair | C | Immutable snapshot: the app just does not record the round. Re-pair is `pairNextRound()` again on the same snapshot (deterministic, ADR 0002) | |
| 10 | GHR 4.3 outcome correction | C | `T:147` `withCorrectedOutcome(round, participant, outcome)`; `Round.withOutcome` (`Round.java:59`) | |
| 11 | GHR 4.3 colour correction | C | `T:152` `withCorrectedColours(round, board)`; mirrors the outcome (`Round.java:69`) | |
| 12 | GHR 4.3 rating correction | C, with a trap | `T:160` `withCorrectedRating`. The pairing numbers freeze once four rounds are recorded (`T:26, 163`), so the result depends on **when** the correction is applied relative to `withRound` | 1 |
| 13 | Effect of a correction on later rounds | P | Later rounds stay as recorded (doc of `T:146`). To warn the arbiter the app must check a recorded round against the snapshot before it (`PairingsChecker.java:57` in the CLI does this with `TrfTournament.tournamentBefore`). The public `Tournament` has no "as before round r" | 4 |
| 14 | Withdraw | C | `T:114` `withdraw(participant, from)`; earliest round wins (`Attendance.java:168`). Refuses a round already recorded (`T:194`) | |
| 15 | Undo a withdrawal | G (by design) | No removal method on `Attendance`/`Tournament`. Fine when the app replays facts and omits the withdrawal, but see the replay order problem | 1 |
| 16 | Requested bye | P | `T:107` `requestBye(participant, round, RequestedBye)` with `full()`, `half()`, `zero()` (`RequestedBye.java:11-19`). The ticket says "the values the settings allow": settings hold no such rule, so the app owns the policy. No range check against the declared rounds. No cancel | 1 |
| 17 | Late entry with missed rounds | C | `T:124, 128` `enterLate(participant, firstRound[, MissedRounds])`; `MissedRounds.zeroPoints()` and `halfPointByes()` only (`MissedRounds.java:12, 16`) | |
| 18 | Change the number of rounds mid-tournament | P | `TournamentSettings.with(NumberOfRounds)` (`TournamentSettings.java:121`) but a `Tournament` cannot take new settings: `Tournament.of` (`T:52`) starts empty. Only a full rebuild works, and nothing enforces "not below the rounds already recorded" | 5 |
| 19 | Change the tie-break list mid-tournament | P | `TournamentSettings.with(TieBreakList)` (`:135`) plus `with(TieBreakEdition)` (`:149`), same rebuild problem as 18. `TieBreakList.parse` checks syntax (`TieBreakList.java:53`); whether the list suits the scoring/edition is only found when `standings()` runs (manual section 6) | 5, 6 |
| 20 | Freeze of system, edition, scoring, acceleration at round 1 | G | No library check; `TournamentSettings.with(...)` is free at any time. The app must remember the frozen list | 5 |
| 21 | Standings | C | `T:250` `standings()`, `T:255` `standingsAfter(round)` returning `Standings` (`Standings.java`) with `ranked()`, `standing(id)` | |
| 22 | Standings explanations | C | `Standings.compare(a, b)` returns `RankComparison(higher, lower, DecidingTieBreak)` (`Standings.java:45`); `TieBreakValue.contributions()` and `explanation()` (`TieBreakValue.java:13`); `Standing.decidedBy()` (`Standing.java:50`) | |
| 23 | Tie-break picker for the settings form | G | `TieBreakList.INDIVIDUAL/TEAM_ONLY` are private sets; only `isTeamOnly` is public. No names, descriptions or edition availability | 6 |
| 24 | Explain a pairing | C | `RoundPairing.about(id)` returns `ParticipantExplanation` (`RoundPairing.java:51`, `ParticipantExplanation.java:15`); `PairingTrace.describe()` (`PairingTrace.java:7`) | |
| 25 | Progress | C | `T:267` `pairNextRound(PairingProgress)`; `PairingProgress.stepStarted/advanced/searching` (`PairingProgress.java:7`) | |
| 26 | Cancel | C, awkward | Interrupt the pairing thread; result is `PairingCancelledException` (`PairingProgress.java` doc, `PairingCancelledException.java:8`). `Future.cancel(true)` works; `check()` offers neither progress nor cancel | 3 |
| 27 | TRF import to continue a tournament | P | `TrfReader.read` then `TrfTournament.tournament()` (`TrfTournament.java:235`). Loses: player data other than name, rating, title (manual section 4, "ignored"); the file's participant ids become the start ranks; a Late Entry is not recognised (`tournamentAfter` only calls `Tournament.of` and `withRound`), so `isLateEntry()` is false and Baku's accelerated group could differ; a withdrawal is a plain `Z` bye | 7, 9 |
| 28 | TRF export | P | `TrfWriter.write(tournament, Options)` (`TrfWriter.java:67`). Header carries only name (012), rounds, colour, scoring, system, tie-breaks; no dates, city, federation, arbiter (022-122); players carry only title, name, rating (no FIDE id, federation, sex, birth date, club). Start ranks are the registration order (`TrfWriter.java:63`) but the reader ranks `asListed` (`TrfTournament.java:111`), so an export of participants not registered in ranking order re-imports with other pairing numbers | 7, 9 |
| 29 | Rebuild a `Tournament` from stored facts each request | P | Works (`Tournament.of` then `withRound`, `withdraw`, `requestBye`, `enterLate`, `withCorrected*`) but only if the app replays the facts in chronological order and knows the ordering rules (see next section) | 1 |

## Where the API leaks or is awkward

1. **Replay order is hidden knowledge.** `withdraw`, `requestBye`, `enterLate` all refuse a round that is already recorded (`T:194-199`, `T:132`), and `withCorrectedRating` behaves differently before and after four rounds are recorded (`T:163`). A state snapshot ("who is withdrawn from round 3, current ratings") cannot be replayed in any order; the app must store a timestamped event log and interleave it with `withRound`. That is workable (the Tournament Log exists anyway) but the rule lives in library internals and the app finds out by exception. A retroactive rating correction stored as "current rating" would silently produce different pairing numbers.
2. **The PAB and the unpaired live in a raw `Map<ParticipantId, Bye>`.** To record a manual pairing the app must know that the PAB is `Bye.PAIRING_ALLOCATED` inside `Round.byes()`, and that everyone in `absencesInNextRound()` (`T:99`) must be copied into the same map (`RoundPairing.completedWith` does exactly this, `RoundPairing.java:45-46`; `ProposedPairing.of(Round)` does the reverse by scanning the map, `ProposedPairing.java:41`). That is library internals in the app.
3. **Raw collections in the public API.** `Tournament.participants()`, `rounds()`, `Round.boards()`, `Round.byes()`, `Tournament.absencesInNextRound()`, `RoundPairing.boards()/unpaired()`, `Standings.ranked()`, `PairingCheck.violations()/differences()`. All are immutable copies (`List.copyOf`, `Map.copyOf`), so **there is no mutability leak**, but the project values first-class collections: `Participants`, `Rounds`, `Boards`, `Byes` (or `Absences`) would carry behaviour such as `boardOf`, `find`, `withReplaced`, and shrink the app's own wrappers. `PairingNumbers` (`PairingNumbers.java`) and `TieBreakValues` (`TieBreakValues.java`) show the pattern is already used. Wrapping is a source-compatible-looking but **major** change if it replaces the `List` return types; adding parallel accessors is minor (change 10).
4. **`PairingCheck.differences()` is `List<String>`** built with string concatenation (`T:289-309`). The UI cannot highlight the board that differs. Same for `Violation`: it has structured `article` and `participants` (good), but the board is not identified.
5. **Public canonical constructors** on `Round`, `Board`, `RoundPairing` (6 components), `PairingCheck` let the app build states the library never produces. They validate what they can (`Board` refuses a self-meeting), so this is a design smell, not a bug.
6. **`Bye` mixes system, requested and derived reasons** in one enum (`Bye.java:4`), with `isRequested()`; the app gets `WITHDRAWN` and `NOT_YET_ENTERED` back from `absenceIn`, which it must not pass to `requestBye` (only `RequestedBye` exists for that, which is a good guard).
7. **Cancellation by thread interrupt** is a JVM idiom; it works with `Future.cancel(true)` on a virtual or platform thread, but `SearchHeartbeat.checkInterrupted` clears the flag (`search/SearchHeartbeat.java:26`), so a caller that reuses the thread must re-check. Acceptable; documented on `PairingProgress`.
8. **Structural `Tournament.check` cost.** `check` always also pairs the round with the system (`T:282`), even when the caller only wants violations. For an interactive "swap and re-check" loop that is the expensive path on every click.
9. **`Problem.message` is English text** (`Problem.java`); the map lists internationalisation as fog, so no change now.

Not a leak: `Tournament` is immutable and every `with...` returns a new value (`T:24` doc, ADR 0002); the app cannot corrupt a snapshot.

## Proposed library changes

Each one is additive; none alters a pairing, standing or check verdict, so by ADR 0008 they are **minor** (a new method or type the client opts into) unless stated.

**1. A replayable change log.** Problem: leak 1 and workflow rows 12, 15, 16, 29.

```java
public sealed interface TournamentChange {
    record RoundRecorded(Round round) implements TournamentChange {}
    record Withdrawal(ParticipantId participant, RoundNumber from) implements TournamentChange {}
    record ByeRequested(ParticipantId participant, RoundNumber round, RequestedBye bye) implements TournamentChange {}
    record LateEntry(Participant participant, RoundNumber firstRound, MissedRounds missed) implements TournamentChange {}
    record OutcomeCorrected(RoundNumber round, ParticipantId participant, Outcome outcome) implements TournamentChange {}
    record ColoursCorrected(RoundNumber round, BoardNumber board) implements TournamentChange {}
    record RatingCorrected(ParticipantId participant, Rating rating) implements TournamentChange {}
}
Tournament apply(TournamentChange change);
static Tournament replay(TournamentSettings settings, List<Participant> participants, List<TournamentChange> changes);
```

Why: the ordering rules become the library's contract, documented and tested once, and the app stores changes in the same vocabulary as its Tournament Log. An "undo withdrawal" is then the app dropping a log entry. Optional companions: `Tournament.withoutWithdrawal(participant)` and `withoutRequestedBye(participant, round)` for callers that do not keep a log (cheap, `Attendance` already has the maps). Minor.

**2. Manual pairing helpers and recording.**

```java
// ProposedPairing
ProposedPairing withSwappedOpponents(ParticipantId a, ParticipantId b);
ProposedPairing withColoursSwapped(ParticipantId onBoardOf);
ProposedPairing withPairingAllocatedBye(ParticipantId participant); // the one it replaces returns to a board is the caller's call
Round completedWith(Tournament tournament, Map<BoardNumber, ? extends Outcome> outcomes); // numbers boards in GHR 3.6 order, adds PAB and absences
```

or, at the tournament: `Round Tournament.roundFrom(ProposedPairing, Map<BoardNumber, Outcome>)`. Why: removes leak 2; the app no longer knows the `byes` map convention or GHR 3.6 board order for a manual pairing (the boards of a `ProposedPairing` have no numbers, only the system's `PairedBoard` does). Note that after a swap the board order may no longer be GHR 3.6 order, so the library must define whether it re-sorts. Minor.

**3. Structured, cancellable, cheaper `check`.**

```java
PairingCheck check(ProposedPairing proposed, PairingProgress progress);   // cancel by interrupt as for pairing
PairingCheck checkLegality(ProposedPairing proposed);                     // violations only, no system pairing
// PairingCheck.differences(): List<PairingDifference> (sealed: BoardNotInSystem, BoardMissingFromProposal, ByeDiffers) with the participants involved; keep the String view as describe()
```

Why: leak 4 and 8; fast feedback while swapping, and highlighting. Keeping `List<String> differences()` and adding `differenceDetails()` keeps it minor; replacing the type is major.

**4. Prefix snapshot and recorded-round check.**

```java
Tournament Tournament.asBefore(RoundNumber round);              // rounds 1..round-1, attendance kept (round's byes stay known)
List<Violation> Tournament.violationsOfRecorded(RoundNumber round);   // = asBefore(round).check(ProposedPairing.of(rounds().get(round-1)))
```

Why: after a GHR 4.3 correction the app must warn "round r was paired on the old data". `TrfTournament.tournamentBefore` already does this inside the trf module (`TrfTournament.java:247`) by rebuilding from the reader's data; a public `Tournament` cannot. Because attendance is kept in the snapshot, `asBefore` is a two-line constructor call. Minor.

**5. Settings changes on a running tournament, guarded.**

```java
Tournament withNumberOfRounds(NumberOfRounds rounds);        // refuses fewer than the rounds recorded
Tournament withTieBreaks(TieBreakList list, TieBreakEdition edition);   // validates against scoring/edition now
Tournament withSettings(TournamentSettings changed);         // refuses a change to system, edition, scoring, ranking key, initial colour or acceleration once a round is recorded
```

Why: rows 18-20. Today the only way is `Tournament.of(newSettings, participants)` plus a full replay, with no guard; the "frozen after round 1" rule of the workflow (ticket 01, decision 1) would live only in the app. Putting it next to the data it protects also keeps `numberOfRounds` consistent with `pairNextRound` (`T:268`). Minor. The refusals throw `InvalidSettingsException` with `Problem`s (`InvalidSettingsException.java`).

**6. Validate tie-breaks against the settings, and publish a catalogue.**

```java
List<Problem> TournamentSettings.problems();      // includes tie-break list vs scoring vs edition (today found when standings() runs)
record TieBreakInfo(String acronym, String name, boolean forTeams, Set<TieBreakEdition> editions, ...) 
static List<TieBreakInfo> TieBreakCode.catalogue();
```

Why: row 19 and 23. The settings form should show only valid codes and report `STD` under edition 2024-08 (manual section 6) when the arbiter picks it, not on the next standings page. `Tournament.of` currently validates acceleration and pairing system only (`T:53-54`). Minor.

**7. TRF: round-trip the player data and the header.**

```java
record PlayerDetails(Optional<String> fideId, Optional<String> federation, Optional<Sex> sex, Optional<LocalDate> birth, Optional<String> club) {}
TrfWriter.Options withPlayers(Map<ParticipantId, PlayerDetails>);
TrfWriter.Options withEvent(EventDetails)     // city, federation, start and end date, chief arbiter, deputy, time control (022-122)
Map<ParticipantId, PlayerDetails> TrfTournament.playerDetails();
```

Why: rows 27-28; the workflow (decision 5) says players carry the TRF player data, but `Participant` (`Participant.java:7`) has only id, name, rating, title and the reader ignores the rest (manual section 4, record 001). Keeping the extras out of core `Participant` respects "entity that gets paired" and keeps the pairing core small; the trf adapter carries them. Minor.

**8. Result notation for keyboard entry.**

```java
String GameOutcome.notation();                 // "1-0", "1/2-1/2", "0-1", "+/-", "-/+", "-/-", "1/2-0", "0-1/2", "0-0", "adj"
static Optional<GameOutcome> GameOutcome.parse(String notation);
```

Why: row 7; fast keyboard entry needs one mapping, and the library already owns the TRF codes (`TrfWriter.playedCode`, package-private). Minor.

**9. Export in ranking order, and stable ids on import.**

```java
TrfWriter.Options inRankingOrder();   // start ranks follow tournament.pairingNumbers() / the ranking key, not registration order
TrfReader.read(String text, IdScheme ids)  // or TrfTournament.withIds(Function<Integer, ParticipantId>)
```

Why: rows 27-28. The writer uses registration order as start rank (`TrfWriter.java:63`) but the reader ranks `asListed` (`TrfTournament.java:111`), so a tournament whose registration order differs from `strengthTitleName` (a late entrant, a corrected rating) does not re-import to the same pairing numbers. Making it opt-in keeps outputs unchanged under existing settings (ADR 0008: minor); making it the default later is a **major**. Also: a withdrawal or late entry cannot be expressed in TRF, so the reader should expose what it inferred (`TrfTournament.inferredLateEntries()`) and use `enterLate` for participants whose first rounds are all `NOT_YET_ENTERED`/`Z`-like byes with no pairing, at least behind a flag; today `isLateEntry()` is false after import.

**10. First-class collections (leak 3).** Add `Participants participants()` style accessors next to the `List` ones (`Rounds`, `Boards`, `Byes`) in a minor release, deprecate the raw ones, remove in the next major. Names in the ubiquitous language of CONTEXT.md (Late Entry, Requested Bye, Withdrawal); `Byes.pairingAllocated()` removes the `Bye.PAIRING_ALLOCATED` scan.

### Priority for the web app

Must have before the app can be built without a workaround: **1** (log replay, and undo), **2** (record a manual pairing), **5** (settings guards). Should have: **4** (correction warning), **3** (fast check), **6**, **7**, **9**. Nice: **8**, **10**.

### Notes

- **Note A, publish and in-progress rounds.** A `Round` needs an outcome per board, so a published, unfinished round is an app concept (`PublishedRound`: a `RoundPairing` or `ProposedPairing` plus results so far). This is correct for the library (ADR 0002 rejects a round lifecycle in the domain). The price is that standings never include a half-played round; if the arbiter wants live standings, the app would record a draft round with provisional outcomes and discard it, which is allowed since snapshots are throw-away. No change proposed.
- **Requested byes "the settings allow"** (ticket 01): the library allows any of full/half/zero for any future round; the policy (for example no requests in the last rounds, a maximum per player) is not modelled and should stay the app's tournament-rules concern unless the FIDE text gains one.
- **Absence without notice** is a forfeit loss on a paired board (`GameOutcome.WHITE_WINS_BY_FORFEIT` etc., a forfeit keeps its colours), never a bye; matches the workflow. Withdrawing afterwards is `withdraw(participant, nextRound)`.
- What is fully sufficient today: one-shot `pairNextRound`, `check`, `withRound`, the three corrections, `withdraw`, `requestBye`, `enterLate`, `standings`/`standingsAfter`, explanations and progress/cancel, and TRF export of the moment. The gaps are all about **rebuilding, guarding and adjusting**, not about pairing or scoring.
