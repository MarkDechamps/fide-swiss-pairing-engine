# Context map

- **Pairing** (`CONTEXT.md`, the library: core, trf, generator, cli): decides who plays whom under the FIDE Swiss systems, and computes standings and tie-breaks from a tournament as recorded.
- **Tournament Administration** (`webapp/CONTEXT.md`, the arbiter web app): runs a tournament for an arbiter, from preparing it to finishing it: its lifecycle, rounds being published and results being entered, corrections, and the log of what the arbiter did.

## Relationships

- Tournament Administration is a downstream client of Pairing and uses only its public API. One anti-corruption adapter in the app translates between the two languages; Pairing knows nothing of Tournament Administration.
