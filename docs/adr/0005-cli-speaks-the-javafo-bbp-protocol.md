# The CLI speaks the JaVaFo/bbpPairings protocol

FIDE defines no request/reply protocol for pairing engines. What exists is JaVaFo's convention, which bbpPairings copies: `engine input.trf -p [out]` answers with a count line and then `white black` lines that use the file's own ids, with `id 0` for the PAB. bbpPairings adds exit codes 0–5. Every THP integration and every Oracle harness already speaks it, and the TEC's acceptance runs call a checker the same way (`-check file`). The `fide-swiss` CLI therefore accepts those short forms and produces that exact Pairing Reply, and it keeps bbp's exit codes 0–5 with their meanings. Its own subcommands (`pair`, `check`, `standings`, `generate`, `version`) are the same operations under readable names. Three extensions are ours: exit codes 6 ("the check found inconsistencies") and 7 ("the generator skipped too many seeds"), which neither program defines, and the team Pairing Reply, the same shape using `310` team numbers, because no team protocol exists.

## Considered Options

- Our own protocol only (JSON or a richer text format): cleaner, but no existing THP or harness could drop the library in, and the Oracle runs would each need an adapter.
- Gacrux's status codes (1 = check false, 2 = no legal pairing): Gacrux is the Swiss Team Oracle, but bbp's table is the one THPs already handle for the Dutch System, and code 1 cannot mean both "no legal pairing" (bbp) and "check false" (Gacrux).

## Consequences

The exit-code table and the reply format are a public contract: changing them is a major version. A richer machine-readable format (JSON trace or report) can be added later as an option without breaking the protocol.
