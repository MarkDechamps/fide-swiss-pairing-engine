# Swiss Pairing

Generates pairings for Swiss-system chess tournaments according to the FIDE Handbook (C.04), for both individual and team competitions.

## Language

### Pairing systems

**Pairing System**:
A named FIDE ruleset that decides who plays whom (and with which colour) in the next round of a Swiss tournament.
_Avoid_: Algorithm, engine mode

**Dutch System**:
The FIDE Pairing System for individual players (C.04.3).
_Avoid_: Individual Swiss, FIDE Swiss

**Swiss Team System**:
The FIDE Pairing System for teams (C.04.6, in force from 1 February 2026).
_Avoid_: Team Dutch, team Swiss

**Olympiad Pairing Rules**:
The FIDE Pairing System for the Chess Olympiad (D.02), a team Swiss defined outside C.04.

**Basic Rules**:
The FIDE rules that apply to every Swiss Pairing System (C.04.1), refined by each system.

### Configuration

**Profile**:
A named, ready-made set of tournament settings (Pairing System, scoring, byes, acceleration, tie-breaks) for a common kind of event, such as a simple club Swiss.
_Avoid_: Preset, template, mode
