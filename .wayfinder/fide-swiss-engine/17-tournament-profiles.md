---
title: Tournament profiles
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: [09-shared-domain-model]
---

## Question

Which profiles does the library ship, and what does each one fix? A profile is a named preset over all the settings: pairing system, rule edition, scoring, bye value, acceleration, colour-preference type, tie-break list. Candidates: simple club Swiss, FIDE-rated open, large accelerated open, club team Swiss, Olympiad. What is the baseline default when no profile is chosen, how does a client override a single setting, and does a TRF file's own configuration records (142/152/162/192/250/202) override the profile?

## Context

Follows from the principle recorded in Olympiad Pairing Rules scope: everything possible, good defaults as profiles.

## Resolution

Decided on 2026-09-23. The author delegated the ticket to the recommended answers ("continue with recommended"). Evidence: `docs/research/basic-handling-rules.md` (BR 3 PAB defaults, GHR 3.3), `docs/research/swiss-team-system.md` (1.2.2, 1.4, 1.7), `docs/research/other-swiss-systems.md` (C.04.7 Baku, D.02), `docs/research/trf-format.md` (records 142–250, MTB26 codes), `docs/research/tie-breaks.md` (C.07 2.1).

**What a Profile is.** A `Profile` is a named, ready-made `TournamentSettings` minus the one value no preset can know: the **number of rounds**, which the client must always give (BR 1; last-round rules depend on it). It is a value, not a type hierarchy. Each profile is a static factory on `Profiles` that returns a complete settings object, and there is no inheritance between profiles. The settings do not remember which profile they came from. `RoundPairing` reports the effective settings and editions (GHR 1.3), not a profile name.

**Profiles shipped.** A profile exists only where its settings really differ. "Simple club Swiss" and "FIDE-rated open" would be identical, so they merge into one.

| Profile | System and editions | Scoring | PAB | Acceleration | Colour | Tie-break List |
|---|---|---|---|---|---|---|
| `individualSwiss()` (**baseline**) | Dutch, Swiss Rules 2026, Tie-break 2026-03 | 1 / ½ / 0 | as the system defines (win) | none | — | `BH/C1, BH, SB, DE` |
| `acceleratedOpen()` | as `individualSwiss()` | 1 / ½ / 0 | as the system defines | Baku (C.04.7) | — | as `individualSwiss()` |
| `doubleSwiss()` | Double-Swiss, 2026, 2026-03 | 1 / ½ / 0 per game | as the system defines (win + draw, DSS 1.4) | none | — | `BH/C1, BH, SB` |
| `teamSwiss()` | Swiss Team, 2026, 2026-03 | MP 2 / 1 / 0 primary, GP 1 / ½ / 0 per board, GP for colour (1.2.2) | as the system defines (a drawn match's MP and GP, 1.4) | none | Type A (1.7) | `MPvGP, EDE, EMGSB/C1` |
| `olympiad()` | Olympiad Pairing Rules (D.02) | MP 2 / 1 / 0 primary, GP per board | 1 MP + 2 GP (D.02 4.3) | none | D.02's own | `EMGSB/C1, MPvGP` (to be checked against the Olympiad regulations when that system is built) |

- Dubov, Burstein and Lim get **no profile of their own**. A client picks them by overriding the system on `individualSwiss()`, because nothing else changes.
- The Swiss Team Interpretations take their ADR 0003 defaults in `teamSwiss()`. No profile sets a non-default Interpretation.
- Initial colour (TRF 152) defaults to White for the top participant in every profile and can be overridden.
- The tie-break lists are **our own choice**, not FIDE's. C.07 2.1 only says to default to off-the-board tie-breaks, completed by the Chief Arbiter. The functional README says so. They can change before 1.0 without breaking anything, since they are profile data.

**PAB value follows the system.** The PAB value in the `ScoringScheme` defaults to "as the system defines", which each system resolves (BR 3 win; DSS 1.4 win + draw; C.04.6 1.4 a draw; D.02 4.3 1 MP + 2 GP). An explicit value overrides it. So overriding the system on a profile never leaves a wrong PAB behind.

**Baseline default.** There is no settings object without a profile. `TournamentSettings` always starts from a profile, and `individualSwiss()` is the baseline when the client names none (for example `Tournament.of(participants, numberOfRounds)` in the simplest overload). So the common case needs no configuration apart from the participants and the number of rounds.

**Overriding one setting.** Typed, intention-revealing `with…` methods on `TournamentSettings`, each returning a new value (`Profiles.individualSwiss().with(Acceleration.baku())`, `.with(PairingSystems.dubov(...))`, `.with(TieBreakList.parse("BH/C1, SB"))`). No string keys. A combination that becomes invalid (for example a team system over individual scoring, or a system with no text in the chosen edition) fails in `Tournament.of`, as decided in Shared domain model across all systems. To switch competition type the client starts from another profile. The exact signatures belong to Public library API.

**TRF configuration records.** `core` knows nothing of TRF. The `trf` module turns a file into settings with this precedence, lowest first:

1. **The profile.** The CLI's `--profile` flag, or, without it, the profile implied by record 192 (a `FIDE_TEAM_*` code gives `teamSwiss()`, `FIDE_DOUBLESWISS*` gives `doubleSwiss()`, anything else gives `individualSwiss()`).
2. **The file's own records** override the profile, because they are the tournament's declared configuration (GHR 1.3): 142 rounds, 152 initial colour, 162/362 scoring, 192 system and edition (and `_BAKU`), 202/212 Tie-break List, 250 acceleration (which, per TRF26, overrides a `_BAKU` code), and `TYPEA`/`TYPEB` and `MP`/`GP` from the team code.
3. **Explicit CLI flags** override the file, as in the JaVaFo/bbpPairings convention (bbp's `--dutch` beats 192).

A missing record falls back to the profile. An unknown or unsupported 192 code is an error, never silently replaced by a default. What TRF26 cannot express (Lim has no 192 code, and some of what `XXS` can say 162 cannot) stays with the TRF CLI surface fog.

**README.** The functional README lists each profile and the settings it fixes. This is part of the definition of done for the profiles.

**Glossary.** Profile sharpened: it covers every setting except the number of rounds. No ADR: profiles are data and easy to change.
