# Apache-2.0 licence

The library aims to become the reference implementation, so it must be embeddable in any pairing program, including closed commercial arbiter software; a copyleft licence (GPL, or LGPL, which is awkward for Java jars) would shut those programs out. We chose Apache-2.0 over MIT for its explicit patent grant, and because it keeps any port of logic from the Apache-2.0 oracle bbpPairings clean with a NOTICE attribution.

## Consequences

- Oracles (bbpPairings, Gacrux, JaVaFo) run only as external processes in tests, so their licences impose nothing on our code.
- Output produced by JaVaFo, whose licence is unclear, is never committed.
- Javadoc cites FIDE Handbook articles by number with short quotes and never reproduces the Handbook, whose text FIDE holds the copyright on.
