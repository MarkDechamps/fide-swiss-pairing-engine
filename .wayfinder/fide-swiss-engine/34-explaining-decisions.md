---
title: Explaining standings, pairings and progress
labels: [wayfinder:grilling]
status: open
assignee:
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
