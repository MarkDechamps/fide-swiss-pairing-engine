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

**Participant**:
The entity that gets paired: a player in an individual tournament, a team in a team tournament.
_Avoid_: Player (when a team is meant), competitor, entrant

**Pairing Number**:
The number (TPN) a participant carries for pairing in a given round, from ranking by strength, title and a declared key. It can change between rounds, so it is not the participant's identity.
_Avoid_: Start number, seed, ID

**Basic Rules**:
The FIDE rules that apply to every Swiss Pairing System (C.04.1), refined by each system.

### Pairing a bracket

**Bracket**:
The group of participants paired together at one step: the residents of a scoregroup plus any moved-down participants left over from the bracket above (Dutch 1.3.2).
_Avoid_: Group, pool

**Downfloater**:
A participant left unpaired in a bracket and moved to the next one, where it is a **Moved-Down Participant** (MDP).
_Avoid_: Floater (without direction), leftover

**Limbo**:
The MDPs of a bracket who are not paired in it and are bound to float again (Dutch 3.2.4).

**Candidate**:
One complete proposal for pairing a bracket: its pairs and its downfloaters, produced in the order the system's text defines and judged by the criteria in priority order.
_Avoid_: Solution, option

**Criteria Vector**:
How badly a candidate fails each criterion, in priority order; the lower vector wins and ties go to the candidate generated first.
_Avoid_: Score (a Score is points), weight

### Scores

**Score**:
The points a participant has earned from the outcomes of its rounds, as used for standings.
_Avoid_: Points total, result

**Pairing Score**:
The score a Pairing System pairs on: the Score plus any acceleration virtual points.
_Avoid_: Accelerated score, virtual score

**Pairing-Allocated Bye**:
The bye given to the one participant left unpaired when the number to be paired is odd (PAB).
_Avoid_: Free bye, odd bye

**Standings**:
The ranking of participants by Score, with ties broken by the tournament's Tie-break List.
_Avoid_: Ranking list, leaderboard

**Tie-break List**:
The ordered tie-breaks a tournament declares for its Standings, written in the Technical Commission's code syntax (for example `BH/C1, SB, DE`).
_Avoid_: Tie-break order, tiebreakers

### Configuration

**Swiss Rules Edition**:
A dated release of the C.04 rules package: the Basic Rules, the General Handling Rules and every Pairing System's text, taken together (for example 2026 or pre-2026).
_Avoid_: Version, ruleset, rules year

**Tie-break Edition**:
A dated release of the FIDE Tie-break Regulations (C.07), chosen independently of the Swiss Rules Edition (for example 2026-03 or 2024-08).
_Avoid_: Tie-break version

**Profile**:
A named, ready-made set of tournament settings (Pairing System, scoring, byes, acceleration, tie-breaks) for a common kind of event, such as a simple club Swiss.
_Avoid_: Preset, template, mode

### Verification

**Oracle**:
An external pairing or tie-break program whose output for a given scope must match the library's exactly (for example bbpPairings for the Dutch System 2026).
_Avoid_: Reference, golden program

**Witness**:
An external program that is compared with the library for a scope with no Oracle; differences are explained but never block a release.
_Avoid_: Secondary oracle

**Known Divergence**:
A recorded, justified difference between the library and an Oracle or Witness, citing the article and the reading the library follows.
_Avoid_: Known bug, exception, waiver
