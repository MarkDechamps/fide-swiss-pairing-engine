# A Double-Swiss match takes two TRF rounds

TRF26 has one opponent, colour and result code per player per round, so it cannot hold a two-game match. We write match *k* as TRF rounds 2k−1 and 2k, one per game, switched on by `192 FIDE_DOUBLESWISS` (or `--system double-swiss`). The two columns must agree (same opponent, reversed colours, a bye in both or neither). Inside the file every round number counts columns; the CLI and the library count matches.

## Considered Options

- One column per match plus an extension record with the game points: every other TRF reader would lose the scores, and FIDE rating needs each game anyway.
- A match result in the one column: it cannot hold 1½-0, a single forfeited game or the match-level forfeit.

## Consequences

The file is plain TRF26 and lossless, but its round numbers are games, not pairing rounds, so `142`/`XXR`, `240` and `250` must be read with that in mind. `162`'s bye symbols become values per match under Double-Swiss. Without `192` or the flag, a reader sees twice the rounds. If FIDE later publishes its own encoding, the reader must accept it as a second dialect.
