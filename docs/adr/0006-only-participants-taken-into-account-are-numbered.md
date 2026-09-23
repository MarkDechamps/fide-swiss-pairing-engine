# Only participants taken into account for pairing hold a Pairing Number

A registered participant who has not yet been taken into account for any pairing has no Pairing Number: a Late Entry, or someone absent or on a Requested Bye in every round so far. Everyone else is numbered by the GHR 2.2 ranking. So a round-1 absentee does not shift the TPN parity of the participants below them for the initial-colour rules. We follow GHR 2.4's definition of a Late Entry ("only taken into account for the pairing of rounds after the first") and the behaviour of both Oracles (bbp v6, Gacrux). This is a fixed reading, not a switchable Interpretation.

## Considered Options

- Number everyone registered (GHR 2.1's initial list): no Oracle does this, so it could not be verified, and it treats 2.5's provisional TPNs as final.
- A switchable Interpretation (the pattern of ADR 0003): its only effect would be round-1 colours in rare tournaments that no Oracle checks, so the extra setting is not worth having.

## Consequences

The Pairing Numbers emitted for a round can differ from the TRF starting ranks. The CLI therefore always uses file ids, never TPNs. If a TEC ruling went the other way, it would need a new Swiss Rules Edition.
