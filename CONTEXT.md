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

**Dubov System**:
The FIDE Pairing System for individual players that aims to equalise, within a scoregroup, the average rating of the opponents (C.04.4.1).

**Burstein System**:
The FIDE Pairing System for individual players that aims to give players with the same score equally strong opposition, measured by the Opposition Index (C.04.4.2). Its first rounds are Seeding Rounds.

**Lim System**:
The FIDE Pairing System for individual players that pairs the scoregroups from the top down to the Median Scoregroup, then from the bottom up, and the Median Scoregroup last, by scrutiny and exchange rather than by criteria (C.04.4.3). Its colour limits are part of compatibility.

**Double-Swiss System**:
The FIDE Pairing System for individual players in which every pairing is a Match of two games with colours reversed, scored per game (C.04.5, new in 2026). Its pairing procedure is the Swiss Team System's Top-Scoregroup Procedure, with no colour criteria.

**Match**:
One pairing that consists of several games: the boards of two teams (Swiss Team, Olympiad) or two games between the same two players (Double-Swiss). A Double-Swiss match is forfeited only when a player forfeits both games; otherwise it counts as played, and the player scheduled White in game 1 had White (1.6) if at least one game was played.
_Avoid_: Mini-match, game (for the whole match)

**Seeding Round**:
One of the first min(⌊rounds/2⌋, 4) rounds of a Burstein tournament, which are paired by the Dutch System (Burstein 1.6).

**Olympiad Pairing Rules**:
The FIDE Pairing System for the Chess Olympiad (D.02), a team Swiss defined outside C.04. It pairs the groups of equal matchpoints from the top down to the Median Group, from the bottom up to it, and the Median Group last, by the order of 9.3 rather than by criteria. Only board 1 gets a colour.

**Participant**:
The entity that gets paired: a player in an individual tournament, a team in a team tournament.
_Avoid_: Player (when a team is meant), competitor, entrant

**Pairing Number**:
The number (TPN) a Numbered Participant carries for pairing in a given round, from ranking by strength, title and a declared key. It can change between rounds, so it is not the participant's identity. A participant not yet taken into account for pairing has none.
_Avoid_: Start number, seed, ID

**Numbered Participant**:
A participant taken into account for the pairing of the current round or an earlier one. Only these hold a Pairing Number, and a participant who is numbered once stays numbered, even after a Withdrawal.
_Avoid_: Valid player, active participant

**Basic Rules**:
The FIDE rules that apply to every Swiss Pairing System (C.04.1), refined by each system.

### Pairing a bracket

**Bracket**:
The group of participants paired together at one step: the residents of a scoregroup plus any moved-down participants left over from the bracket above (Dutch 1.3.2), or plus the upfloaters chosen for it (Swiss Team 1.3.2, Double-Swiss 1.3.2, Dubov 1.3.2), or plus the incoming floaters left unpaired by the bracket above (Burstein 1.2.2).
_Avoid_: Group, pool

**Upfloater**:
A participant from a lower scoregroup taken into a bracket (Swiss Team 3.5, Double-Swiss 3.5, Dubov 3.2.2), chosen as one set of upfloaters. A participant is **upfloated** in a round when it is paired with an opponent who had a higher score.
_Avoid_: Floater (without direction)

**Maximum Upfloater**:
A Dubov player already upfloated MaxT = 2 + ⌊rounds/5⌋ times (Dubov 1.8); [C8]–[C9] avoid upfloating them again.

**ARO**:
Average Rating of Opponents: the mean rating of the opponents a player met over the board, rounded half up, and 0 before the first game (Dubov 1.7).
_Avoid_: Average opponent rating, AvgOpp (the C.07 tie-break)

**Shifter**:
A Dubov player moved from the subgroup of its colour preference (G1 wants White, G2 the rest) into the other one, so that the subgroups can be paired against each other (Dubov 3.2.4, 4.3).

**Transposition**:
An ordering of a Dubov bracket's G2 players; the first one in lexicographic TPN order that pairs legally against S1 gives the bracket's pairs (Dubov 3.2.6, 4.4).
_Avoid_: Permutation (in the model)

**Following scoregroup**:
The scoregroup right below the bracket's score in the round's standings (Swiss Team and Double-Swiss [C6]). Once all its teams are paired, it is empty and [C6] holds; it is never the next non-empty level further down.

**Top-Scoregroup Procedure**:
The pairing procedure shared, word for word, by the Swiss Team and Double-Swiss Systems (3.3–3.6): the PAB first, then repeatedly the top-scoregroup of the participants still unpaired, plus the first set of upfloaters in 3.5.4 order, paired by the first Pairing Identifier. The two systems differ only in their criteria lists and colour rules.

**Pairing Identifier**:
The TPNs of a bracket's top members (the smaller TPN of each pair) in ascending order, followed by the bottom member of each; pairings are taken in the lexicographic order of their identifiers (Swiss Team and Double-Swiss 3.6.2).
_Avoid_: Pairing key, signature

**Downfloater**:
A participant left unpaired in a bracket and moved to the next one, where it is a **Moved-Down Participant** (MDP). Burstein calls it an outgoing floater of its bracket and an incoming floater of the next (1.2.2).
_Avoid_: Floater (without direction), leftover

**Limbo**:
The MDPs of a bracket who are not paired in it and are bound to float again (Dutch 3.2.4).

**Median Scoregroup**:
The Lim scoregroup whose score is half of what the rounds played can give (Lim 2.2). It is paired last and downward, and it receives the floaters from both sides. It exists even when no player has that score, and then holds only floaters.
_Avoid_: Middle group, centre group

**Median Group**:
The Olympiad group (teams with the same matchpoints) that holds the median team: of the teams to be paired after the bye, in the ranking of 3.2, the lower ranked of the two middle ones (D.02 6.4). Unlike the Lim Median Scoregroup it is found by position, not by score, so it always has residents. It is paired last and downward and receives the floaters from both sides; no floater passes it.
_Avoid_: Middle group (the text's word, kept apart from Lim's median)

**Kept Pairings**:
The most pairings an Olympiad group can make among its own teams while every team not yet paired can still be paired (D.02 9.5, 8.4). One heaviest perfect matching over the rest of the round answers it. The group floats exactly the teams it cannot keep, and every step of its pairing must still reach this number.
_Avoid_: Local maximum (it is asked of the whole round; the prototype calls it `GroupOptimum`)

**Pairing Direction**:
Whether a Lim scoregroup or an Olympiad group is paired downward (above the median, and the median itself) or upwards (below it). Every upward rule is the downward rule mirrored: the "first" player is the highest numbered (#1) downward and the lowest numbered upwards; for Olympiad teams, the highest and the lowest ranked (D.02 8.2/8.3, 9.2).
_Avoid_: Side, half

**Floater Type**:
One of the four Lim floater kinds, a to d, in descending order of disadvantage (3.9.1): whether the player already floated into this scoregroup, and whether it has a compatible opponent in the adjacent one. A Lim scoregroup keeps as many pairings as it can have, and its floaters are chosen avoiding type a, then b, then c.
_Avoid_: Floater class, category

**Exchange Order**:
The opponents the first player of a Lim scoregroup tries, in turn: the bottom half in order, then the top half from the bottom up (Lim 4.2–4.3, generalised). Once it has an opponent, the rest is proposed top half against bottom half again. The Olympiad's 9.3 is the same order, stated for any group size.
_Avoid_: Permutation, transposition (Dubov's term)

**Cracking**:
Taking back the last pairing made on one side of a blocked Lim Median Scoregroup, so that its two players join the median as floaters from that side (Lim 2.6).
_Avoid_: Unpairing, breaking

**Bracket Sequence Number**:
A participant's position, from 1, in its bracket's ranking (BSN). In Burstein a virtual player with BSN 0 is added for each outgoing floater: whoever it is paired with floats (4.1–4.2).
_Avoid_: Bracket rank, index (the Opposition Index is something else)

**Opposition Index**:
A Burstein player's Buchholz, then Sonneborn-Berger, over the tournament so far. It uses standings points only and Burstein's own rules for unplayed rounds (1.7.2): a round not played counts as a game against oneself at one's own current score, and a run of zero-point byes up to the last round counts as draws for one's opponents. It ranks the players of a bracket, ahead of TPN and without their scores (1.8).
_Avoid_: Tie-break (it is not one of the Standings' tie-breaks), Burstein score

**Candidate**:
One complete proposal for pairing a bracket: its pairs and its downfloaters, produced in the order the system's text defines and judged by the criteria in priority order.
_Avoid_: Solution, option

**Criteria Vector**:
How badly a candidate fails each criterion, in priority order; the lower vector wins and ties go to the candidate generated first.
_Avoid_: Score (a Score is points), weight

**Optimum**:
The best criteria vector any candidate of a bracket can reach, computed exactly before the search; the first candidate generated that reaches it is the one chosen (Dutch 3.4 "perfect"). A candidate found to reach it is a **known optimal candidate**.
_Avoid_: Perfect bound, target (in the model), witness (a Witness is a verification program)

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

**Unplayed Round**:
A round in which a participant, paired or not, played no game (or, in a team event, no match): a bye, a forfeit, or a round after a Withdrawal (C.07 15.1).
_Avoid_: Missing game, non-game

**Voluntary Unplayed Round**:
An Unplayed Round the participant chose: a half-point or zero-point Requested Bye, a round after a Withdrawal, or a forfeit loss (VUR, C.07 16.1.2).
_Avoid_: Voluntary bye, absence

**Adjusted Score**:
A participant's Score as its opponents' tie-breaks see it: its Requested Byes after which it played no more games (including every round after a Withdrawal) count as draws (C.07 16.3).
_Avoid_: Virtual score, corrected score

**Dummy Opponent**:
The imagined opponent that each of a participant's own Unplayed Rounds is scored against in its own tie-breaks. Its score is the participant's Score, capped as the Tie-break Edition prescribes (C.07 16.4).
_Avoid_: Virtual opponent (the abolished pre-2023 concept)

### Tournament history

**Late Entry**:
A participant who is only taken into account for the pairing of rounds after the first (GHR 2.4).
_Avoid_: Latecomer, added player

**Requested Bye**:
A round a participant asked in advance not to be paired in, scored as the tournament rules allow (full, half or zero point).
_Avoid_: Absence, voluntary bye

**Withdrawal**:
A participant leaving the tournament, so it is no longer paired from a given round onwards.
_Avoid_: Dropout, retirement

**Correction**:
A change to a recorded result, colour or rating (GHR 4.3); it affects future pairings only, never re-pairs a past round.
_Avoid_: Edit, amendment

**Proposed Pairing**:
A pairing for the next round that did not come from the library (an arbiter's manual pairing, another program's output), to be checked against the rules and the system.
_Avoid_: Manual pairing (when not made by hand), external pairing

**Pairing Trace**:
The record of how a Pairing System reached a round's pairing: bye decisions, each bracket's chosen candidate with its criteria vector, and the article behind each colour (BR 9, GHR 1.3).
_Avoid_: Log, debug output

### Configuration

**Swiss Rules Edition**:
A dated release of the C.04 rules package: the Basic Rules, the General Handling Rules and every Pairing System's text, taken together (for example 2026 or pre-2026).
_Avoid_: Version, ruleset, rules year

**Tie-break Edition**:
A dated release of the FIDE Tie-break Regulations (C.07), chosen independently of the Swiss Rules Edition (for example 2026-03 or 2024-08).
_Avoid_: Tie-break version

**Profile**:
A named, ready-made set of every tournament setting (Pairing System, editions, scoring, byes, acceleration, colour preferences, tie-breaks) except the number of rounds, for a common kind of event, such as an individual Swiss or a team Swiss. Any one setting can be overridden.
_Avoid_: Preset, template, mode

**Maxi-tournament**:
A Lim setting, declared by the organiser and off by default, under which floater choices and exchanges for colour are allowed only between players rated within 100 points (Lim 3.2.3, 3.8, 5.7). It is never inferred from the size of the field.
_Avoid_: Large tournament, open

**Interpretation**:
A named, switchable choice between two readings of an ambiguous article, used only where the text literally allows both; it has a documented default and is set in the tournament's settings or a Profile.
_Avoid_: Option, flag, variant, mode

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

**Literal Enumerator**:
A test-only pairing of a system that generates every candidate in the orders the text defines and assesses each by exhaustion, with no matching. On small fields it must agree exactly with the library's search. It is how a system without an Oracle checks that its search is exact.
_Avoid_: Brute force, reference implementation, oracle

**Random Tournament Generator**:
A tool that produces complete, valid tournaments by having the library pair every round of a simulated field, with results drawn from a Result Model. It is FIDE's RTG.
_Avoid_: Fuzzer, simulator, test data generator

**Result Model**:
The probability rule that decides a simulated game's outcome from the two players' ratings and colours (by default Milvang's model).
_Avoid_: Outcome distribution, score model

**Pairing Reply**:
The answer a pairing engine gives to a request to pair the next round: the number of pairs, then one `white black` line per pair using the file's own ids, with the Pairing-Allocated Bye as `id 0`. It is the de facto protocol that JaVaFo set and bbpPairings copies.
_Avoid_: Output file, pairing list

**Pairings Checker**:
A tool that rebuilds a recorded tournament round by round, pairs each round itself, and reports every round and set of standings the file does not match (FIDE's PTC).
_Avoid_: Validator, verifier
