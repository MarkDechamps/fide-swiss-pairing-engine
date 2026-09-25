---
title: Explaining standings, pairings and progress
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

What does the library tell a client (and the CLI a user) about *why* it decided what it did, and how far along it is? Three parts:

1. **Standings.** When two participants have the same Score, how does a client learn why one ranks above the other: which tie-break of the Tie-break List separated them, with what values, and how each value was built (per-opponent contributions, the Dummy Opponent for Unplayed Rounds, the modifiers that cut or capped a term)? Is that part of `Standing`, a separate `explain(a, b)` query, or both, and how does the CLI's `standings` show it?
2. **Pairings.** `PairingTrace` already records bye, bracket and colour steps with the article that decided them. Is that enough guidance for an arbiter or player asking "why was I paired with X / given White / floated", or does it need a per-participant view, plain-language article summaries, and the rejected alternatives that a higher criterion ruled out?
3. **Progress.** A round can take seconds (budgets up to 10 s at 250 players). How does `core` report progress without logging or threads of its own (a listener or callback on `pairNextRound`, cancellation?), what is a meaningful unit of progress for a search whose size is unknown in advance (brackets done, scoregroups done), and how does the CLI render it (a progress bar on stderr, suppressed when not a terminal)?

## Context

Raised by the author while grilling Acceleration across the systems (2026-09-25): "When 2 players have the same amount of points, we should be able to get info on why one was higher than the other. Also when the system is thinking, some sort of progress bar would be nice. It is a complex system, guidance on what rules decided what would be nice."

Evidence: Public library API (`PairingTrace` with sealed `TraceStep`s, `describe()`; `Standings` with `Standing(Rank, Participant, Score, TieBreakValues)` and shared ranks), TRF CLI surface (`-l` trace file, text only for 1.0; `standings` command; stderr for the trace on exit 1), Tie-break regulations and Tie-break interpretation rulings (modifiers, Dummy Opponent, Art. 16), the per-system algorithm tickets (performance budgets), Shared domain model across all systems (`core` never logs).

## Resolution

Decided on 2026-09-25. The author handed the ticket over to the recommended answers ("continue with recommended and only stop on real blockers"). There were none, so each decision below is the recommended one, for the author to overturn. Evidence: Public library API (`Standing`, `TieBreakValues`, `PairingTrace`, the sealed exception family), TRF CLI surface (`standings`, `-l`, stderr for diagnostics), Tie-break regulations and Tie-break interpretation rulings (modifiers, Adjusted Score, Dummy Opponent), Shared domain model across all systems (`core` never logs), and the budgets in the per-system algorithm tickets. One rule runs through all three parts: **an explanation is derived from the computation that made the decision, never recomputed beside it**, so it cannot drift from what the library actually did.

**1. Standings: why one participant ranks above another.**

- **A tie-break value carries its own breakdown.** Each tie-break object computes a `TieBreakValue` as the sum of its `TieBreakContribution`s, and the total is read off them. A contribution is one round's share: the round, the counterpart (an opponent, or the Dummy Opponent for an own Unplayed Round), the raw value, the value used (Adjusted Score under C.07 16.3, the 16.4 cap), and the modifiers that touched it (`/C1` cut, `/M1` median, `/P` opponent adjustment, Art. 16 substitution), each with its article. A value that is not a per-round sum (DE, ARO, TPR, the rating ones) carries its inputs and the article of its formula instead. So `Standing(Rank, Participant, Score, TieBreakValues)` stays as decided and already holds everything.
- **`standings.compare(a, b)`** returns a `RankComparison`: who ranks higher, and the **Deciding Tie-break**: `Score` when the scores differ, otherwise the first tie-break in the Tie-break List whose values differ, with both values and every earlier (equal) value. When the whole list is equal it says **Shared Rank** (the library never draws lots; C.07 leaves it to the organiser).
- **`standing.decidedBy()`**: for each participant with the same Score as the one ranked directly above, the Deciding Tie-break against that neighbour. This is the common question ("why am I 5th and not 4th?") without a second call.
- **CLI.** `fide-swiss standings` prints rank, id, name, Score and one column per tie-break (list order), plus a last column `decided by` for rows tied on Score with the row above (`BH/C1 31.5 > 30.0`, or `shared`). `standings --why <id> <id>` prints the `RankComparison` and the full breakdown (one line per contribution, modifiers named with articles) of the Deciding Tie-break for both participants.

**2. Pairings: why was I paired with X, given White, floated.**

- The `PairingTrace` stays the single record. Three things are added on top of it:
  - **Per-participant view.** `trace.about(ParticipantId)` returns a `ParticipantExplanation`: the bracket the participant was a resident of or moved down into, whether and why it floated (the step and the criterion or article that made it), its opponent and board, the `ColourDecision` article, or its PAB / unpaired reason. It is a filter over the existing steps, not a new computation. `describe(ParticipantId)` renders it as text.
  - **Plain-language article summaries.** Every article a trace step can cite is an `Article` value with its number, its edition and a one-line summary written by us in plain English (a paraphrase, never the Handbook text). The summaries live next to the per-article rule objects of each edition, so a new edition cannot cite an article without one. `describe()` prints the number and the summary. English only; translation is out of scope.
  - **Rejected alternatives, on demand.** `pairing.whyNot(a, b)` answers "why was a not paired with b?" by re-running the round's decision with the pair a–b forced (a forced edge in the round-wide matching the optimising systems already use). The answer is sealed: `Impossible` (an absolute criterion, with its article and the evidence, e.g. "played in round 3"), `Worse` (the first bracket where the forced pairing loses, and the two Criteria Vectors, naming the first criterion where it is worse), or `SameQuality` (equally good; the chosen one wins on the system's order, e.g. Transposition or Pairing Identifier order, cited). Lim and the Olympiad Pairing Rules optimise nothing, so there `whyNot` returns `Impossible` or the procedure step that went the other way (the floater choice or Exchange Order step, cited). It is never computed during `pairNextRound`, so the round's cost does not change. The trace never records every rejected candidate: the search space is too large, and candidate counts stay diagnostics.
- **CLI.** `pair … --explain <id>` writes the participant's explanation to stderr (or into the `-l` file when given). `pair … --why-not <id> <id>` prints the `whyNot` answer to stderr. Neither changes the Pairing Reply.
- **Delivery order.** The per-participant view and the article summaries are part of each system's definition of done (the trace is already required). `whyNot` is an opt-in addition (a minor version under ADR 0008) and does not gate 1.0.

**3. Progress while pairing.**

- **Listener, no threads.** `pairNextRound(PairingProgress listener)` is an overload; the plain `pairNextRound()` passes a no-op listener. `core` calls it synchronously on the caller's thread, starts no threads and logs nothing. `PairingProgress` is a small interface with default no-op methods, so a client implements only what it needs:
  - `stepStarted(ProgressStep step)`: a bracket, scoregroup or Lim/Olympiad group begins (its label, as in the trace).
  - `advanced(Progress progress)`: after each settled step.
  - `searching(ProgressStep step, long candidatesTried)`: a heartbeat at most every 100 ms of search time inside a long step, so a display never looks frozen.
- **Unit of progress: participants settled.** `Progress(settled, toPair)`: participants that are paired or given the PAB in a step that is final, out of the participants to be paired this round. The total is known before the search starts and the count never decreases. Brackets or scoregroups are not the unit: their number changes as downfloaters move and as Dutch 2017 collapses the last brackets. When a later step re-pairs an earlier one (the Dutch 2017 Penultimate Pairing Bracket), the count holds until the re-pair is final.
- **Cancellation: the Java idiom.** A client cancels by interrupting the pairing thread. `core` checks `Thread.interrupted()` at every step boundary and at every heartbeat, and throws `PairingCancelledException`, a new member of the sealed `SwissPairingException` family carrying the trace so far. No cancellation token type is added.
- **The same listener shape for the other long operations**: `check` reports rounds checked out of rounds, and the generator reports tournaments played out of tournaments, each through its own `…Progress` interface with the same `Progress(done, total)` value.
- **CLI.** A one-line progress indicator on stderr, rewritten in place: `round 7: 143/250 settled · bracket 5.5 · 12,400 candidates`. It appears only when stderr is a terminal (`System.console()` with `isTerminal()`), only after the operation has run for 500 ms (fast rounds print nothing), and never with `--quiet`. It is erased before the reply, a diagnostic or an error is printed, so a harness reading stdout or a captured stderr never sees it. `generate` and `check` show the same line with their own counts. Ctrl-C interrupts the pairing thread, and the CLI exits with code 130 (the shell convention for SIGINT) without writing a reply.

**Glossary** gained Deciding Tie-break, Tie-break Contribution and Shared Rank. No ADR: every choice here is cheap to reverse.

**Definition of done.** The README's "Command line" section documents `standings --why`, `pair --explain`, `--why-not`, `--quiet`, exit code 130 and the progress line, and the library section documents `compare`, `about`, `whyNot` and `PairingProgress`, kept current as systems land.

**No new tickets.** Nothing new surfaced that the build cannot take as specified.
