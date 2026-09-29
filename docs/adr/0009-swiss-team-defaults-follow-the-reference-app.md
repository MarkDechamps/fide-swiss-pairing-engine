# The engine's defaults follow the reference app where it knowingly differs from the text

Author decision, 2026-09-29: "default = reference app; the FIDE-literal reading stays available via `--interpretation`; the bye value must be configurable". ADR 0003 already made each open reading of C.04.6 default to Gacrux's. Four differences were not readings but knowing departures from the text, so they were Known Divergences that the library kept literal: KD-1 (Gacrux seats the members of a bracket by score, then TPN, against C.04.6 3.6.1), KD-2 (Gacrux's PAB wins every board when TRF `162` has no `P`, against 1.4), KD-3 (Gacrux adds the Virtual Points of Baku, in match points, to the secondary score, against C.04.7 1.5) and the Board Count order of EDEBT (Gacrux ranks the higher sum first, against C.07 12.1). Each is now an Interpretation whose default is Gacrux's reading and whose alternative is the text: `bracket-seating=score-then-tpn|tpn`, `pab-value=win|draw|loss`, `baku-secondary-score=virtual-match-points|real` and `edebt-board-count=higher|lower`. KD-4 (Dutch 2017, JaVaFo) is not covered: bbpPairings v5 is the 2017 reference, and the engine already equals it.

The PAB value is a setting, not only a reading: a `P` in `162` selects it (a win, a draw or a loss per board; under game points a `P` of any value states the PAB's points), the flag `--interpretation pab-value=...` selects it without a file, and a `320` record still overrides the match points. Without either, the value is a win per board, Gacrux's. The PAB's match points stay those of a drawn match (Gacrux's default too).

The Oracle gate now compares the defaults with plain Gacrux and needs no patch. The literal readings are compared with Gacrux patched with `tpn-order` (`-Dfideswiss.oracle.gacrux.literal=true`), from which only KD-3 remains to register.

## Considered Options

- Keep the text as the default and register the differences: the default profile would then differ from the app users compare against, and the gates would need a patch and exclusions to pass.
- One switch, `--reference-app`, for all four: the four differences are independent, and the arbiter may agree with Gacrux on one and not another.

## Consequences

The defaults change pairings, standings and the PAB's value for existing Swiss Team users (ADR 0008): a major-version change, listed in the CHANGELOG. The Known Divergences KD-1 to KD-3 now exist only under the literal readings.
