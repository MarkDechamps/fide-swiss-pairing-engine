# Double-Swiss 2026: what existing programs do, against our readings

Purpose: for each ambiguous point in the README section "Readings of the Double-Swiss 2026 text", record what prior art does, so our readings can be aligned or knowingly differ. Read on 2026-09-29.

## Sources and how far they can be trusted

- **FIDE text.** C.04.5 (2026), as fetched 2026-09-24: `prototypes/double-swiss-algorithm/C0405202602.txt` on branch `prototype/double-swiss-algorithm`.
- **chesspairing** (Go) commit 79034be, `pairing/doubleswiss/*.go` and the shared `pairing/lexswiss/*.go`, with its page `docs/content/en/docs/pairing-systems/double-swiss.md`. AI-assisted, no golden tests for this system. It models **one game per round** (1-½-0 per round, no two-game match), so every reading about matches, single forfeited games and 1.6 colours has no counterpart there. The prototype ran its Double-Swiss steps over our state (Readable Double-Swiss pairing algorithm, the Witness review at commit ba9d4f6): never better on any criterion, never first in either order.
- **bbpPairings**, **JaVaFo**: no Double-Swiss system. **Gacrux** (commit 6419149): maps the `FIDE_DOUBLESWISS` code in `trf2json.py` but has no engine for it; its C.04.6 module is the same procedure (3.3–3.6) and is our Swiss Team Oracle, which is why identical text keeps the Swiss Team reading.
- **Not checked**: Swiss-Manager (closed source; its August 2026 changelog mentions "double/multi-round pairings for singles Swiss", unconfirmed), Vega, any SPP commentary. "No commentary found" means "not looked at", not that none exists.
- Nothing here is copied; only behaviours are described.

## 1. The procedure (3.3–3.6) and the quality criteria [C4]–[C8]

| Source | What it does |
|---|---|
| FIDE text | The PAB first (3.4), then the top-scoregroup plus the first set of upfloaters in 3.5.4 order that complies with [C4]–[C7], paired by the first Pairing Identifier complying with [C1] and [C8] (3.6.4). |
| chesspairing | Pairs each score group by a depth-first search in identifier order (`lexswiss/bracket.go`); an odd group floats its lowest-ranked player up one group if it has a compatible opponent (`lexswiss/upfloater.go:22`). No [C3] look-ahead, no [C4]–[C7]: a group it cannot complete is paired partially. |
| Gacrux (C.04.6) | The same procedure, with [C6] as pass/fail on the parity minimum. |

Recommendation: **keep** the shared Top-Scoregroup Procedure. Nothing to align with in chesspairing.

## 2. [C8] "upfloaters' opponents who were floaters in the previous round"

| Source | What it does |
|---|---|
| FIDE text | 2.3.5, the same words as C.04.6 [C10]. |
| chesspairing | Its "C8" is a colour check (two players who both need the same colour after two same colours in a row, `doubleswiss/doubleswiss.go:180`). The text has no colour criterion. |
| Swiss Team build | [C10] counts the resident of each resident-upfloater pair. |

Recommendation: **keep** the Swiss Team build's reading (one procedure, one reading of identical words). The prototype counted the higher-scored player of any pair with an upfloater; the two differ only when two upfloaters of different scores meet. With the Literal Enumerator switched to the prototype's reading, the agreement tests still pass on every simulated round and random history (checked 2026-09-29), so the difference is not seen in practice.

## 3. The PAB (3.4, [C2], 1.4)

| Source | What it does |
|---|---|
| FIDE text | Lowest score, then most matches played, then largest TPN, among players leaving a legal pairing (3.4.1) and not barred by [C2] (a PAB, a match won by forfeit, a full-point bye). Worth a game won and a game drawn. |
| chesspairing | Lowest score, then largest TPN; only an earlier bye bars it (`lexswiss/bye.go:21`). No 3.4.1, no 3.4.3, no forfeit-win bar. PAB worth 1.5. |

Recommendation: **keep** the text. The value 1.5 agrees.

## 4. Forfeits (Preface) and colours (1.6, Article 4)

| Source | What it does |
|---|---|
| FIDE text | A match is forfeited only when a player forfeits both games; a player had a colour when at least one game was played, the game-1 colour. 4.3: 4.3.1 initial-colour by the HRP's TPN parity, 4.3.2 fewer Whites, 4.3.3 the most recent difference, 4.3.4/4.3.5 alternation. |
| chesspairing | One game per round, so a forfeit game is simply not a meeting (`lexswiss/state.go:102`). Colours (`doubleswiss/color.go:28`): a three-in-a-row ban first, then more Whites, then alternation, board alternation in round 1, then rank. None of that is the text's order. |

Recommendation: **keep** the text. The Preface's odd case (each player forfeits one game) is a meeting without a colour, since 1.6 asks for a game actually played.

## 5. Baku (C.04.7) under Double-Swiss

| Source | What it does |
|---|---|
| FIDE text | C.04.7 gives "the number of points awarded for a win"; C.04.5 does not say whether a game or a match. |
| chesspairing | Hard-codes 1.0 and 0.5 for every system. |

Recommendation: **keep** 2, then 1 (Acceleration across the systems, decision 3): one round's win, as team Baku gives a match win's points. A Witness difference, never a gate.

## 6. TRF

No program writes a Double-Swiss match in TRF: TRF26 has one result per round, and chesspairing, bbpPairings, JaVaFo and Gacrux read one game per round. ADR 0007 (two TRF rounds per match) stands; a FIDE encoding, if one appears, becomes a second dialect.
