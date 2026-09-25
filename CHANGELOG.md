# Changelog

All notable changes to this project are documented here, in the [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format. The project follows Semantic Versioning over its API, its CLI protocol and its output under unchanged settings ([ADR 0008](docs/adr/0008-a-pairing-change-is-a-versioned-change.md)).

Every change that can alter a pairing, a standing or a check verdict is listed under **Pairing changes**, with the article, the systems and editions affected, and any Known Divergence added or closed.

## [Unreleased]

### Added

- The immutable `Tournament` snapshot: participants, recorded rounds, requested byes, withdrawals, late entries (GHR 2.4) and corrections (GHR 4.3), with the `individualSwiss` and `acceleratedOpen` profiles.
- The Dutch System (C.04.3, 2026) with the Basic and General Handling Rules (C.04.1/C.04.2, 2026): the literal bracket procedure of Articles 3–4 with an exact optimum finder, colour allocation (Article 5), board order (GHR 3.6) and a pairing trace.
- Baku and explicit acceleration (C.04.7, 2026) on the Pairing Score.
- `Tournament.check(ProposedPairing)`: the Basic Rules' violations and the differences from the system's pairing.
- Pairing progress (`PairingProgress`), cancellation by interrupt and `RoundPairing.about(id)`.
- The `trf` reader for TRF26 and TRF16 (with `XXR`, `XXC`, `XXA`, `250` and `_BAKU`).
- The `fide-swiss` command line: `pair` and `-p`, `check`, `-c` and `-check`, `version`, `--explain`, `--quiet`, and bbp's exit codes plus 6; a portable distribution with launchers.

### Pairing changes

- First pairings: Dutch 2026, identical to bbpPairings v6.0.0 in every round of the committed regression corpus and of 647 generated tournaments. Documented readings: 4.4.1, [C5], [C8], [C9], [C18]/[C20] and ADR 0006.
