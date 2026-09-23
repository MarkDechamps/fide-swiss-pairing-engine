# FIDE Swiss Pairing Engine

A Java reference library for the FIDE Swiss pairing systems: it pairs the next round of a Swiss tournament, individual or team, exactly as the FIDE Handbook prescribes, and computes the FIDE tie-breaks. It ships as a library plus a command-line tool that reads and writes FIDE Tournament Report Files (TRF).

> **Not affiliated with or endorsed by FIDE.** "FIDE" names the rules this library implements; it is not a claim of approval.

**Status: pre-alpha.** The project foundation is in place; no pairing system is implemented yet. The specification is being charted in [`.wayfinder/fide-swiss-engine/map.md`](.wayfinder/fide-swiss-engine/map.md).

## FIDE rules targeted

| Handbook | Rules | Edition | Status |
|---|---|---|---|
| C.04.1 | Basic Rules for Swiss Systems | 2026 | planned |
| C.04.2 | General Handling Rules for Swiss Tournaments | 2026 | planned |
| C.04.3 | Dutch System | 2026 | planned |
| C.04.4.1 | Dubov System | 2026 | planned |
| C.04.4.2 | Burstein System | 2026 | planned |
| C.04.4.3 | Lim System | 2026 | planned |
| C.04.5 | Double-Swiss System | 2026 | planned |
| C.04.6 | Swiss Team Pairing System | 2026 | planned |
| C.04.7 | Acceleration methods | 2026 | planned |
| D.02 | Olympiad Pairing Rules | current | planned |
| C.07 | Tie-break Regulations | 2026 | planned |
| — | TRF26 (reads TRF16 and the JaVaFo dialect) | 2026 | planned |

## Modules

| Artifact | Contents |
|---|---|
| `fide-swiss-pairing-engine-core` | Domain model, handling rules, every pairing system, acceleration, tie-breaks. No runtime dependencies. |
| `fide-swiss-pairing-engine-trf` | TRF reader and writer. |
| `fide-swiss-pairing-engine-cli` | Command-line pairing, checking and tournament generation. |

Group id: `io.github.markdechamps`. Not yet published to Maven Central.

## Building

Requires Java 25 (`sdk env` picks up the version pinned in `.sdkmanrc`).

```sh
./mvnw verify                 # tests, formatting check, coverage report
./mvnw spotless:apply         # format the code
git config core.hooksPath .githooks   # run the tests before every commit
```

## Licence

[Apache-2.0](LICENSE).
