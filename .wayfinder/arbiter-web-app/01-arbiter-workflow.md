---
title: Arbiter workflow and tournament lifecycle
labels: [wayfinder:grilling]
status: closed
assignee: mark
blocked_by: []
---

## Question

What does an arbiter do, step by step, from creating a tournament to closing it, and what can be undone? This covers the lifecycle states (draft, registration, rounds in progress, finished), when settings freeze, pairing a round and publishing it, entering and correcting results (GHR 4.3), withdrawals, byes on request, late entries, manual changes to a proposed pairing (checked with the library's `check`), re-pairing a round before any result is in, and what 'finished' allows.

## Resolution

Grilled 2026-09-30. The user settled Q1 and accepted the recommended answers for the rest ("continue with recommended").

1. **Three lifecycle states.** *Preparing*: settings (name, dates, profile, rounds, tie-breaks) and players are freely editable. *Running*: begins when round 1 is paired. From then on the pairing system, rules edition, scoring and acceleration are frozen, because changing them rewrites history. The number of rounds can still change (not below the rounds already paired), and so can the tie-break list and the name. *Finished*: the arbiter closes a tournament whose last round is complete. It is read-only apart from logged corrections, and can be reopened.
2. **Round cycle.** Pair: the library proposes a pairing, which the arbiter alone sees. Adjust: the arbiter can swap opponents, colours or the bye; every change is run through the library's `check`, and an illegal pairing needs an explicit acknowledgement with a reason, which is logged, because an arbiter may override the rules. Publish: the pairing becomes fixed and visible. Enter results. The round is complete once every board has a result, and only then can the next round be paired. A published round can be unpublished (and re-paired) only while none of its results is entered.
3. **Results.** Every outcome the library knows, per board: 1-0, ½-½, 0-1, the forfeits (+/-, -/+, -/-) and the odd played results (½-0, 0-½, 0-0). Entry must be fast from the keyboard. A result in a completed round can be corrected at any time (GHR 4.3; `withCorrectedOutcome`, `withCorrectedColours`, `withCorrectedRating`). If a later round was already paired on the wrong data, that pairing stands, and the app warns the arbiter.
4. **Changes to the field while Running.** *Withdrawal* from a chosen round onwards (`withdraw`); it can be undone while that round is still unpaired. *Requested bye* per player per future round, with the values the settings allow (`requestBye`). *Late entry* (`enterLate`) from the next unpaired round, with the missed rounds scored per the settings (`MissedRounds`). An absence without notice is entered as a forfeit loss; withdrawing the player afterwards is the arbiter's call, never automatic.
5. **Players.** They carry the TRF player data (name, FIDE id, rating, title, federation, birth date, sex, club). They can be edited freely while Preparing. While Running, rating changes go through `withCorrectedRating` and pairing numbers stay as they are (ADR 0006).
6. **No general undo.** Every arbiter action is recorded with a timestamp in a tournament log. Reversal happens only through the steps above: re-pair before publishing, unpublish before any result, correct results, undo a withdrawal before its round is paired, and reopen a finished tournament.

Library fit: the public `Tournament` API already covers pairing, `check`, `withRound`, `withdraw`, `requestBye`, `enterLate` and the corrections. The "Library API fit" ticket verifies the details.

Candidate terms for the app glossary (decided in the "App domain model" ticket): Preparing / Running / Finished, Proposed Pairing, Published Round, Override (an illegal pairing the arbiter acknowledged), Tournament Log.
