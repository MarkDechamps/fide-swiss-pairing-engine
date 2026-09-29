# Burstein 2026: what existing programs do, against our seven readings

Purpose: for each ambiguous point in the README section "Readings of the Burstein 2026 text", record what prior art does, so our readings can be aligned or knowingly differ. Read on 2026-09-29.

## Sources and how far they can be trusted

- **FIDE text.** C.04.4.2 (2026), as fetched 2026-09-23: `/home/mark/swiss-burstein/prototypes/burstein-algorithm/C040402202602.txt`. Cited by line.
- **bbpPairings** commit 8f9e3c5, `src/swisssystems/burstein.cpp` and `common.cpp`. Its README calls the Burstein part "a flawed implementation of a previous version" (README.txt:15-18), not endorsed. It is the **old text**: Index = Sonneborn-Berger, then Buchholz, then Median; unplayed games count as draws for opponents. So it can only speak on points where the old and new text overlap. It has no source for the 2026 edition.
- **chesspairing** (Go) commit ba9d4f6, `pairing/burstein/*.go`. AI-assisted; it covers Burstein loosely. Its own docs say there is no look-ahead and no float criteria (`docs/content/en/docs/pairing-systems/burstein.md:10,125-126`). It does not implement [C6] or [C7] at all, so it is a weak witness.
- **Not checked** (closed source or not reachable offline): Vega, Swiss-Manager, and any Systems of Pairings and Programs Commission (SPP) commentary. No local copy of an SPP note on Burstein exists in the repo or the prototype. Treat "no commentary found" as "not looked at online", not as proof none exists.
- Nothing here is copied; only behaviours are described.

## 1. 1.7.2 exception: which rounds are "zero-point-byes" for the Index

| Source | What it does |
|---|---|
| FIDE text | 1.7.2 (line 39): "a series of consecutive zero-point-byes up to the current round, each of the ones gathered in previous rounds ... is considered as a draw" for the opponents' benefit. Silent on requested vs. forced, and on late entry. |
| bbpPairings | Old text: every unplayed game is an adjusted draw, no series rule (`burstein.cpp:96-108`, `getAdjustedPoints`). Its "virtual opponent" score for an unplayed round is a separate rule (`:113-132`). No help here. |
| chesspairing | Skips forfeits; treats byes as 1.0 point in its score map (`opposition.go:241-357`). Does not implement the exception. |
| Others | Not checked. |

Recommendation: **keep**. No prior art implements this sentence, so there is nothing to align with. Our reading (requested zero-point byes plus rounds after withdrawal, not rounds before late entry) follows "up to the current round" literally.

## 2. [C7]: which criteria the following bracket must satisfy

| Source | What it does |
|---|---|
| FIDE text | 2.3.3 (line 69): "every criterion from [C1] to [C6]". C1-C4 are absolute/completion, so only C5 and C6 are quality criteria in that range. [C8] is excluded by the text. |
| bbpPairings | No look-ahead beyond "one floater per scoregroup, next group must stay pairable" (`burstein.cpp:556-558, 895, 938`). No [C6]/[C7]. |
| chesspairing | Explicitly none ("no C8 look-ahead", `burstein.md:125`). |
| Others | Not checked. |

Recommendation: **keep**. The literal text already says C1 to C6, which is what our reading (C4 kept, then best C5, then C6, not C8) implements. This is not really ambiguous.

## 3. [C6]: floaters' scores compared as a descending sequence

| Source | What it does |
|---|---|
| FIDE text | 2.3.2 (line 68): "Minimise the scores (taken in descending order) of the outgoing floaters". |
| bbpPairings | Does not compare floater scores. It keeps at most one floater per scoregroup and picks by rank order (`burstein.cpp:1022-1063`). |
| chesspairing | Not implemented. |
| Others | Not checked. |

Recommendation: **keep**. "Taken in descending order" reads as a sequence compared highest first; no prior art contradicts it.

## 4. Acceleration: which score brackets, [C6]/[C7], PAB and Index use

| Source | What it does |
|---|---|
| FIDE text | 1.7.2 (line 41-42): virtual points "shall be excluded from the computation of any method", so the Index reads standings only. Brackets are scoregroups: silent on whether they read the accelerated score. |
| bbpPairings | **Scoregroups and sorting use the accelerated score** (`scoreWithAcceleration`, `burstein.cpp:430, 762-765, 874-876`). This matches our "brackets read the Pairing Score". The **Index does include acceleration** (`:723` starts each player's adjusted score from `player.acceleration`, and the Buchholz/SB code adds virtual points at `:144-147, 288-289`), the opposite of the 2026 text, because the old text differed. Its default acceleration applies when no XXA line is present (`:26-65`; README.txt:41-48). |
| chesspairing | Baku only, applied to `PairingScore`; score groups are built from it (`burstein.go:85-98`). Index (`opposition.go`) sorts by real `Score` and real results, so no virtual points in the Index. Matches our reading. |
| Others | Not checked. |

Recommendation: **keep**. Both programs group by accelerated score; chesspairing keeps the Index clean like our reading; bbp's Index difference is explained by the old text, and the 2026 sentence is explicit.

## 5. Board order

| Source | What it does |
|---|---|
| FIDE text | Not defined in C.04.4.2. (1.8 note, line 48: scores are not used in the *pairing* ranking; that is a different thing.) |
| bbpPairings | `sortResults` (`common.cpp:172-201`): byes last, then the higher player's score (descending), then the lower player's score, then the higher player's rank. Same three keys as our reading, **but scores are the unaccelerated scores** (`scoreWithoutAcceleration`, `unacceleratedScoreRankCompare`). It is the shared Dutch/JaVaFo publishing order, applied to Burstein. |
| chesspairing | Board = order the matching returns pairs (`burstein.go:129-137`, `Board: i+1`); no defined ordering. |
| Others | Not checked. |

Recommendation: **keep the three keys, change one detail**: use the real (standings) score, not the Pairing Score, when ordering boards under acceleration, as bbp does. This departs from our current README ("higher Pairing Score"). The basic rules for board order (C.04.1) should be checked before deciding; bbp's choice is the only prior art.

## 6. Seeding rounds: Dutch decides everything

| Source | What it does |
|---|---|
| FIDE text | 1.6.1 (line 26): seeding rounds "are paired following the rules of the FIDE (Dutch) System". Silent on PAB, colours, board order. |
| bbpPairings | Old text has no Dutch seeding: one engine for all rounds; round 1 falls out of rank order (`burstein.cpp:757-767`). Not comparable. |
| chesspairing | Only ranks by TPN in seeding rounds and prints a note (`burstein.go:60-68`); pairing, colours and PAB are the same non-Dutch global matching as later rounds. Does not delegate to Dutch. Seeding count is `min(floor(rounds/2), 4)` (`options.go:98-113`), same as 1.6.2. |
| Others | Not checked. |

Recommendation: **keep**. Nobody delegates whole-round pairing to Dutch, but the text says "paired following the rules of the Dutch System", and PAB, colours and board order are part of a Dutch round-pairing.

## 7. 4.3 note lists "1-3, 2-0, 4-5, 6-0" twice

| Source | What it does |
|---|---|
| FIDE text | Lines 113 and 114 of the fetched text are identical (`1-3, 2-0, 4-5, 6-0`). The order in 4.3 itself generates each pairing once. |
| bbpPairings / chesspairing | Neither enumerates the note; no witness. |
| Others | Not checked. |

Recommendation: **keep** (one entry, 45 candidates): the rule in 4.3 cannot produce a duplicate, so the repeated line is a typo in the note. If SPP publishes an erratum, prefer it.

## Summary

- Keep readings 1, 2, 3, 4, 6, 7. There is no prior art that contradicts them, and where the FIDE text is explicit (2, 3, 4-Index) our reading is the literal one.
- Change 5 (board order under acceleration) to unaccelerated scores, following bbpPairings.
- Prior art is thin: bbpPairings is the old text, and chesspairing skips [C6]/[C7] entirely. Only Points 4 and 5 have a real data point.
