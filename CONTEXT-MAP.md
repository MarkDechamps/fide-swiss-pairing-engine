# Context map

- **Pairing** (`CONTEXT.md`, the library: core, trf, generator, cli): decides who plays whom under the FIDE Swiss systems, and computes standings and tie-breaks from a tournament as recorded.
- **Tournament Administration** (`webapp/CONTEXT.md`, the arbiter web app): runs a tournament for an arbiter, from preparing it to finishing it: its lifecycle, rounds being published and results being entered, corrections, and the log of what the arbiter did.

## Relationships

- Tournament Administration is **Conformist** to Pairing: it adopts Pairing's language and types (Participant, Rating, Outcome, Round Pairing, Standings, Tournament Settings) as its own and adds only what Pairing lacks. It uses only Pairing's public API. Pairing knows nothing of Tournament Administration.
- Both contexts have a *Tournament*. In Tournament Administration it is the tournament the arbiter runs, and it holds Pairing's Tournament as the record it is replayed into.
