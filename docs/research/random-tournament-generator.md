# Random tournament generator: result model and existing RTG configs

Collected on 2026-09-23 for the Random tournament generator ticket.

## Milvang's result model (TEC reference for RTG results)

Source: Otto Milvang, *Probability for the outcome of a chess game based on rating*, Oslo, 2015-01-12
(http://www.nordstrandsjakk.no/documents/spp/Probability.pdf). It was fitted on 3,912,831 FIDE-rated
games (tournaments 50000–100000, Aug 2010 – Oct 2014). C.02.03 §7.2.4 asks that RTG results "roughly
follow" this model.

With `Rm = (Wr + Br) / 2`, `Rd = Wr − Br` and `q = (Rm − 1200) / 1200` if `Rm > 1200`, else `q = 0`:

| | White win `Pw` | Black win `Pb` |
|---|---|---|
| centre `CL` | 40 | −80 |
| centre value `CV` | 0.45 − 0.10·q² | 0.46 − 0.13·q² |
| lower limit `LL` | −1492 + 0.391·Rm | −1753 + 0.416·Rm |
| upper limit `UL` | 1691 − 0.428·Rm | 1428 − 0.388·Rm |

- `Pw` = 0 below `WLL`; `WCV·((Rd−WLL)/(WCL−WLL))²` up to `WCL`; `1 − (1−WCV)·((Rd−WUL)/(WCL−WUL))²` up to `WUL`; 1 above.
- `Pb` = 1 below `BLL`; `1 − (1−BCV)·((Rd−BLL)/(BCL−BLL))²` up to `BCL`; `BCV·((Rd−BUL)/(BCL−BUL))²` up to `BUL`; 0 above.
- `Pd = 1 − Pw − Pb`.
- Worked example from the paper (a test fixture): White 2467, Black 2344 gives Pw 0.511, Pb 0.138, Pd 0.351.
- Observed draw rate between equal players: 36.2% overall, 41.6% at 2100–2399, 26.9% at 1500–1799.

## bbpPairings v6 generator (`src/tournament/generator.{h,cpp}`, `src/fileformats/generatorconfiguration.cpp`)

- Config file of `Key=Value` lines, the same keys as JaVaFo's RTG: `PlayersNumber`, `RoundsNumber`,
  `DrawPercentage`, `ForfeitRate`, `RetiredRate`, `HalfPointByeRate` (typo `HalfPointByteRate` is also accepted),
  `HighestRating`, `LowestRating`, `PointsForWin`, `PointsForDraw`, `PointsForLoss`, `PointsForZPB`.
- Random defaults when a key is missing: players 15–215, rounds 5–15, highest rating 2400–2800, lowest
  rating 1400–2300 (ratings drawn uniformly between them), ForfeitRate 6–30 (1 in N games; each side is
  absent independently with probability 1 − √(1 − 1/N)), RetiredRate and HalfPointByeRate 15–3225,
  DrawPercentage 10–50.
- Its results depend on rating order and `DrawPercentage`, not on Milvang's model.
- There are no half-point byes in the last round. It gives zero-point byes to "retired" players. `model -g` reuses
  a TRF's players and matches configuration, and reports the model's own rates.
- If no legal pairing exists, generation fails with "No valid pairing exists for round r of the generated tournament".
