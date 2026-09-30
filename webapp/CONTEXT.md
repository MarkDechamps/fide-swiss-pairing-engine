# Tournament Administration

Runs a Swiss tournament for an arbiter, from preparing it to finishing it. It is conformist to the Pairing context (the library's `CONTEXT.md`): Participant, Rating, Outcome, Round Pairing, Standings and Tournament Settings mean here what they mean there. This glossary holds only the terms Pairing does not have.

## Language

**Tournament**:
The tournament an arbiter runs: its players, rounds, lifecycle and log. It holds Pairing's Tournament as the record its log is replayed into.
_Avoid_: Event, competition

**Arbiter**:
The person who runs a Tournament and whose actions are recorded.

**Lifecycle**:
The stage a Tournament is in: Preparing, Running or Finished.

**Preparing**:
The stage before round 1 is paired, in which the settings and the players can be changed freely.

**Running**:
The stage from the pairing of round 1 until the arbiter finishes the Tournament. The pairing system, edition, scoring and acceleration can no longer change.

**Finished**:
The stage after the arbiter closes a Tournament whose last round is complete. Only corrections are possible, and the arbiter can reopen it.

**Proposed Round**:
A round's pairing that the arbiter has not yet published. The arbiter can adjust it, and only the arbiter sees it.
_Avoid_: Draft pairing

**Published Round**:
A round whose pairing is fixed and visible, and whose results are being entered.

**Complete Round**:
A Published Round with a result on every board.

**Override**:
A pairing the arbiter published although Pairing found it illegal, recorded with the arbiter's reason.

**Tournament Log**:
The ordered record of every Arbiter Action in a Tournament. Everything else about the Tournament is derived from it.
_Avoid_: History, audit trail

**Arbiter Action**:
One entry in the Tournament Log: something the arbiter did, such as registering a player, publishing a round or correcting a result, with the moment it happened.
_Avoid_: Event (it clashes with a chess event)
