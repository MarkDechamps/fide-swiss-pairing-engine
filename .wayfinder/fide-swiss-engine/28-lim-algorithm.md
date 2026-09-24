---
title: Readable Lim pairing algorithm
labels: [wayfinder:prototype]
status: closed
assignee: markdechamps
blocked_by: [25-dubov-algorithm, 26-burstein-algorithm, 27-double-swiss-algorithm]
---

## Question

Can the Lim System (C.04.4.3, 2026) be written literally and readably? Its procedure is a step-by-step exchange with no global criteria order, so decide first whether the pattern (a target from one matching, exact reachability) applies at all, or whether Lim is written as its own procedural object. Prototype the median-first group order, the up- and downfloaters, the exchange tables of 4.2–4.3 beyond six players, colour limits as compatibility (relaxed in the last round) and the "Maxi-tournament" rating rule, and record each ambiguity as a fixed reading or an Interpretation.

## Context

Graduated from the "Algorithm design for each remaining system" fog; last on purpose ("Lim riskiest, do it last", Dubov, Burstein, Lim, Double-Swiss and acceleration). Evidence: `docs/research/other-swiss-systems.md` §5 (numerous ambiguities; no `192` code in TRF26), Witnesses Vega and chesspairing (Maxi as a boolean).

From Readable Dubov pairing algorithm: chesspairing's CLI (`pair`, legacy `-p`) drops the last recorded round of every TRF, because its reader sets `CurrentRound` to the rounds played. Its random generator (`generate`) therefore writes tournaments full of rematches, and the Witness must be run through a driver like `prototypes/dubov-algorithm/witness/main.go` (branch `prototype/dubov-algorithm`). It also renumbers TPNs by score every round, and it lets a forfeit win leave the PAB open ([C2] 2026), so expect differences in order and in byes. A Literal Enumerator (plain enumeration in the text's orders) checks the search where no Oracle exists.

## Resolution

Prototyped on 2026-09-24 on branch `prototype/lim-algorithm` (folder `prototypes/lim-algorithm/`; the README maps each class to its article). The author told this session to take the recommended options and stop only at real blockers. There were none, so the decisions below are the recommended ones, for the author to overturn.

**Answer: yes, readably, but not on the pattern.** Lim has no quality criteria and no candidate order to optimise, so there is no target to compute and nothing to weigh. Every choice in the text is "the first in this order" (a floater, an opponent, an exchange). A matching answers only one kind of question: can the players left still all be paired, and how many pairings can this scoregroup keep (4.4, 3.10.2). So Lim is its own procedural object that shares only the matching mechanism with the other systems. The text needs fourteen readings, all fixed and none switchable. With them the procedure is complete: it never blocks a round that has a legal pairing.

- **Played events** (6–40 players, 5–11 rounds planned, forfeits, half- and zero-point byes, withdrawals; 5,000 events plain, 5,000 as Maxi-tournaments). 38,403 and 38,542 rounds were paired, and every one is legal: no rematch, the colour limits held outside the last round, no second PAB. 1,939 / 1,989 rounds cracked a pairing (2.6), and 9,291 / 9,359 floated a floater again (3.5).
  - 534 / 518 events stopped at a round with **no legal pairing at all**, whatever the procedure. Lim's colour limits are absolute outside the last round. Only small fields hit this: 654 of 1,000 events of 6–10 players with up to 11 rounds, 34 of 1,000 at 11–20 players, none above 20.
  - **None** stopped at a round that had a legal pairing. With the literal PAB (1.1 without the completeness reading, decision 3) 160 rounds did.
- **Against plain enumeration.** Every reachability question (the pairings a scoregroup can keep, the floater choice, whether the rest can still be paired, the completeness of the median, the PAB) was answered a second time by trying every pairing, with no matching theory (`LiteralEnumeration`). The results were identical in all **44,482 rounds** (3,000 events of 6–22 players, plain and Maxi). So the blossom mechanism adds speed, not behaviour.
- **The text's examples are fixtures, and all of them pass.** They cover every column of the 4.2 and 4.3 tables, 4.4 with 4.4.2 (1-6 stays, #2 and #5 float) and 4.4.1 (#2 swaps with the floater #7), 7.2 (the forty-player lines for both lots), 7.1, 8.3, and one fixture each for 2.6.1, 3.2.2 (both directions), 3.2.3, 3.9, 3.10, 5.4 and Article 6.
- **Every rule matters.** Dropping one rule changes the pairs in this many of 9,000 played rounds (plain / Maxi):
  - the colour exchange of 5.2/3.8: 7,065 / 5,772;
  - 3.8's opponent order for floaters: 6,776 / 7,237;
  - 3.2.2's colour choice of floater: 3,597 / 1,294;
  - 3.10: 2,215 / 2,286;
  - the 3.9 types: 1,427 / 1,355.
- **Speed** (one core, Java 25, slowest round): 31 ms at 64 players, **98 ms at 250**, 269 ms at 500.
- **Witness** (chesspairing `gnutterts/chesspairing` ba9d4f6, its Lim pairer run through `witness/main.go`). 300 events of 10–60 players, 7–11 rounds, 2,681 rounds (plain; Maxi was similar).
  - **Agreement:** 333 identical and 14 with the same pairs but other colours.
  - **Illegal:** 37 (31 give a second PAB, 6 break a colour limit), and 5 leave players unpaired.
  - **Other pairs:** 2,292. chesspairing does not follow the text's structure. It pairs leftover floaters with each other after all groups, skipping the adjacent-group transfer of 3.2.1. It has no cracking and no 3.5, and its floater opponent is not 3.8's. So it floats fewer players in 532 rounds and more in 640, and floats the same players otherwise in 820.
  - Witnesses never gate, so nothing here is a Known Divergence.

**Decisions (recommended options, taken under the author's go-ahead):**

1. **Lim is written as a procedure, not an optimisation.** The build has `LimSystem` (2.2 order and the median), `ScoregroupPairing` (2.3–2.5), `FloaterSelection` (Article 3), `Scrutiny` (3.6–3.8, Article 4, 5.2), `ExchangeOrder` (4.2–4.3), `ColourAllocation` (Article 5) and `RoundOne` (Article 7).
   - It shares only the matching mechanism, through a cardinality face (`maximumPairs`, `canPairAll`) over the same `core` matching. No weights, no `Criterion`, no known optimal candidate.
   - Upward pairing is written as the mirror of downward (`Direction`). Every upward rule of the text mirrors a downward one: 3.2.1/3.2.3/3.2.4, 3.3/3.4, 3.6/3.7, 3.8, 4.1.1/4.1.2 and 5.4.
2. **One rule for every floater: the scoregroup keeps as many pairings as it can have** (3.10.2, 4.4).
   - Exactly as many players float as a maximum matching leaves over. They are chosen one at a time, each the first in the order below whose transfer still leaves that many pairings.
   - This makes 2.3.1–2.3.3 (no suitable opponent), 2.3.4 (make the number even), 3.1 and 4.4–4.4.2 a single rule, and the text's 4.4 examples come out of it.
   - The order:
     1. 3.9.2: type d, then c, then b, then a.
     2. 3.10: within a type, a player who did not float in the previous round.
     3. 3.2.2: a player due the colour that more players are due. In a Maxi-tournament this applies only to a player rated within 100 points of the lowest numbered one (3.2.3).
     4. 3.2.4/3.3: the lowest numbered player.
   - An incoming floater is a member like any other, so it can float on (3.5, types a/b).
3. **Fixed readings:**
   - **Numbering.** "Highest numbered" is #1 (4.2), everywhere, "highest pairing number" (3.6, 3.6.2, 3.7.2) included.
   - **Median Scoregroup.** Its score is half of what the rounds played can give: rounds played × the points for a win ÷ 2. It always exists, even without residents, in which case it holds only the floaters from both sides. Under acceleration the median belongs to the acceleration fog.
   - **Compatibility (2.1).** Two players are compatible when they have not met and some colour allocation keeps both inside 5.1.1/5.1.2, counted on played games (GHR 3.4). In the last round only the meeting counts (Article 6). There is no other exception: a round with no compatible pairing is a `Failure`.
   - **Adjacent scoregroup.** The next scoregroup in the pairing order on the same side; for the last group of a side, the median. A floater "has a compatible opponent" there among its residents, after excluding the opponents of floaters already chosen that rank ahead of it (3.3/3.4, as a matching).
   - **"Floated the round before" (3.10).** The player played the previous round, not forfeited and not a bye, against an opponent whose score before that round was different. This is Double-Swiss's reading of 1.5.
   - **Incoming floaters are paired first** (3.6–3.8).
     - Order: floaters from ahead first (downfloaters above and in the median), those from behind next (the median's upfloaters, 3.6.3). Downfloaters go highest score first, then highest numbered (3.6); upfloaters lowest score first, then lowest numbered (3.7).
     - Each takes the first *available* player, in the direction's order, who is due the other colour; if there is none, the first available player. Other floaters count as candidates.
     - *Available* means compatible, with the rest still able to be paired.
   - **Article 4, generalised.**
     - The first player of the arranged list tries the bottom half in order, then the top half from the bottom up. Once it has an opponent, the rest is proposed afresh.
     - The first scrutiny (compatibility) and the second (5.2 colours) are one walk. Each scrutinised player takes the first available opponent in that order who is due the other colour, otherwise the first available one.
     - "Each player, if possible" is read in scrutiny order: an earlier player keeps its colour.
   - **Maxi-tournament.** It is configuration, declared by the organiser, off by default, and never inferred from the field. In a Maxi-tournament, an exchange for colour (3.8, 5.7) is allowed only when the opponent due the other colour is rated within 100 points of the first available one, and 3.2.3 limits the colour choice of floater as above.
   - **Cracking (2.6).**
     - "The next pairing" of a side is the last pairing made on it: pairings are taken back in reverse order, from the neighbouring scoregroup outward. The lower side is cracked when more floaters came from above than from below, otherwise the higher side; when the chosen side has none left, the other side.
     - Both players join the median as floaters from that side, and the median is paired again from the start.
     - Cracking can reach every pairing, so with the PAB below a legal round always pairs.
   - **PAB.** 1.1 gives it to the lowest-ranked eligible player (Basic Rule 4) of the lowest scoregroup. We read "eligible" to include that the bye must leave the others pairable, which is Swiss Team's and Double-Swiss's 3.4.1. Round 1 follows 7.1 as written: the lowest rated player, where unrated players count as the lowest rated, and ties go to the lowest rank.
   - **Colours (Article 5).**
     - A player is due the equalising colour when their colours are unequal, otherwise the alternating one. Round parity (5.5/5.6) is read on the player's own played games.
     - The rules apply in this order: the limits (5.3); the due colours when they differ; the history going back (5.4), where at the latest round in which the two had different colours each gets the colour the other had then; with identical histories, the higher ranked player gets the colour due to them in the median scoregroup or above, and the lower ranked player below it.
     - A pairing made in the median, cracked pairings included, counts as "median or above". Players with no games take the lot colour of 7.2.
4. **A Literal Enumerator** (`LiteralEnumeration`) goes into test scope, following Readable Dubov pairing algorithm decision 5. It runs as a property test on fields of ≤ 22 players and must agree with the matching everywhere. The text's examples above are fixtures.
5. **Configuration and surface.** The Lim settings get a typed `maxiTournament` (default off), and the CLI gets `--maxi-tournament`. No TRF record exists for it, so, like the `--interpretation` flags, it comes only from settings or the flag. The provisional `192` code stays `FIDE_LIM` (TRF CLI surface).
6. **Performance budget:** one Lim round with ≤ 250 players in ≤ 1 s (98 ms measured).
7. **For the Olympiad ticket:** the parts Lim built that D.02 6.4 may reuse are the median-group order (top down, bottom up, the median last), the `Direction` mirror and the 2.6 cracking. Nothing new graduates from this ticket. The Lim median under acceleration joins the acceleration fog.
