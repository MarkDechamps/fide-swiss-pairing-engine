# The tournament is an immutable snapshot

The library does not run rounds, enter results or persist anything, so `Tournament` is an immutable snapshot rather than a stateful entity with a round lifecycle: clients build it from settings, participants and completed rounds (`withRound`, `withdraw`, `requestBye`, `enterLate` each return a new snapshot, validated on construction) and ask it to `pairNextRound()` or give its `standings()`. The same snapshot always gives the same pairing, which makes the checker, the oracle runs and the tests plain replays. Clients map their own storage to the snapshot at the edge, and TRF is one such mapping.

## Considered Options

- A mutable `Tournament` entity with `startRound`/`recordResult`: closer to how an arbiter works, but it pulls round lifecycle, which is out of scope, into the domain and makes replay depend on call order.
- Passing the Pairing System to `pairNextRound(system)`: rejected because GHR 1.3 fixes the system for the whole event. The system is part of the tournament's settings.
